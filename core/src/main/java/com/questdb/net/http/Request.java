/*******************************************************************************
 *    ___                  _   ____  ____
 *   / _ \ _   _  ___  ___| |_|  _ \| __ )
 *  | | | | | | |/ _ \/ __| __| | | |  _ \
 *  | |_| | |_| |  __/\__ \ |_| |_| | |_) |
 *   \__\_\\__,_|\___||___/\__|____/|____/
 *
 * Copyright (C) 2014-2017 Appsicle
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 ******************************************************************************/

package com.questdb.net.http;

import com.questdb.ex.DisconnectedChannelException;
import com.questdb.ex.HeadersTooLargeException;
import com.questdb.ex.MalformedHeaderException;
import com.questdb.ex.SlowReadableChannelException;
import com.questdb.log.Log;
import com.questdb.log.LogFactory;
import com.questdb.misc.*;
import com.questdb.net.NetworkChannel;
import com.questdb.std.Mutable;
import com.questdb.std.ObjectPool;
import com.questdb.std.str.DirectByteCharSequence;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;

public class Request implements Closeable, Mutable {
    private final static Log LOG = LogFactory.getLog(Request.class);
    private final ByteBuffer in;
    private final long inAddr;
    private final ObjectPool<DirectByteCharSequence> pool = new ObjectPool<>(DirectByteCharSequence.FACTORY, 64);
    private final RequestHeaderBuffer hb;
    private final MultipartParser multipartParser;
    private final BoundaryAugmenter augmenter = new BoundaryAugmenter();
    private final NetworkChannel channel;
    private final int soRcvSmall;
    private final int soRcvLarge;
    private final int soRetries;
    private final int soConsecutiveBadReadLimit;

    public Request(NetworkChannel channel, ServerConfiguration configuration) {
        this.channel = channel;
        this.hb = new RequestHeaderBuffer(configuration.getHttpBufReqHeader(), pool);
        this.in = ByteBuffer.allocateDirect(Numbers.ceilPow2(configuration.getHttpBufReqContent()));
        this.inAddr = ByteBuffers.getAddress(in);
        this.multipartParser = new MultipartParser(configuration.getHttpBufReqMultipart(), pool);
        this.soRcvSmall = configuration.getHttpSoRcvSmall();
        this.soRcvLarge = configuration.getHttpSoRcvLarge();
        this.soRetries = configuration.getHttpSoRetries();
        this.soConsecutiveBadReadLimit = configuration.getHttpSoConsecutiveBadReadLimit();
    }

    @Override
    public void clear() {
        this.hb.clear();
        this.pool.clear();
        this.in.clear();
        this.multipartParser.clear();
    }

    @Override
    public void close() {
        hb.close();
        multipartParser.close();
        ByteBuffers.release(in);
        augmenter.close();
    }

    public CharSequence getHeader(CharSequence name) {
        return hb.get(name);
    }

    public CharSequence getMethodLine() {
        return hb.getMethodLine();
    }

    public CharSequence getUrl() {
        return hb.getUrl();
    }

    public CharSequence getUrlParam(CharSequence name) {
        return hb.getUrlParam(name);
    }

    public boolean isIncomplete() {
        return hb.isIncomplete();
    }

    public boolean isMultipart() {
        return Chars.equalsNc("multipart/form-data", hb.getContentType());
    }

    public void parseMultipart(IOContext context, MultipartListener handler)
            throws HeadersTooLargeException, IOException, MalformedHeaderException {
        final long fd = channel.getFd();
        if (Net.setRcvBuf(fd, soRcvLarge) != 0) {
            LOG.error().$("Could not set SO_RCVBUF on ").$(fd).$();
        }
        try {
            MultipartParser parser = getMultipartParser().of(getBoundary());
            while (true) {
                int sz = in.remaining();
                if (sz > 0 && parser.parse(context, ByteBuffers.getAddress(in) + in.position(), sz, handler)) {
                    break;
                }
                drainChannel();
            }
        } finally {
            if (Net.setRcvBuf(fd, soRcvSmall) != 0) {
                LOG.error().$("Could not reset SO_RCVBUF on ").$(fd).$();
            }
        }
    }

    public void read() throws HeadersTooLargeException, IOException, MalformedHeaderException {
        drainChannel();
        if (isIncomplete()) {
            readHeaders();
        }
    }

    private void drainChannel() throws IOException {
        try {
            in.clear();
            ByteBuffers.copyNonBlocking(channel, in, soRetries);
            in.flip();
        } catch (SlowReadableChannelException e) {
            if (channel.getConsecutiveBadReadCount() > soConsecutiveBadReadLimit) {
                LOG.error().$("Too many failed read attempts").$();
                throw DisconnectedChannelException.INSTANCE;
            }
            throw e;
        }
    }

    private DirectByteCharSequence getBoundary() {
        return augmenter.of(hb.getBoundary());
    }

    private MultipartParser getMultipartParser() {
        return multipartParser;
    }

    private void readHeaders() throws HeadersTooLargeException, IOException, MalformedHeaderException {
        do {
            in.position((int) (hb.write(inAddr, in.remaining(), true) - inAddr));
            if (hb.isIncomplete()) {
                drainChannel();
            } else {
                break;
            }
        } while (true);
    }

    public static class BoundaryAugmenter implements Closeable {
        private static final String BOUNDARY_PREFIX = "\r\n--";
        private final DirectByteCharSequence export = new DirectByteCharSequence();
        private long lo;
        private long lim;
        private long _wptr;

        public BoundaryAugmenter() {
            this.lim = 64;
            this.lo = this._wptr = Unsafe.malloc(this.lim);
            of0(BOUNDARY_PREFIX);
        }

        public DirectByteCharSequence of(CharSequence value) {
            int len = value.length() + BOUNDARY_PREFIX.length();
            if (len > lim) {
                resize(len);
            }
            _wptr = lo + BOUNDARY_PREFIX.length();
            of0(value);
            return export.of(lo, _wptr);
        }

        private void of0(CharSequence value) {
            int len = value.length();
            Chars.strcpy(value, len, _wptr);
            _wptr += len;
        }

        private void resize(int lim) {
            Unsafe.free(this.lo, this.lim);
            this.lim = Numbers.ceilPow2(lim);
            this.lo = _wptr = Unsafe.malloc(this.lim);
            of0(BOUNDARY_PREFIX);
        }

        @Override
        public void close() {
            if (lo > 0) {
                Unsafe.free(this.lo, this.lim);
                this.lo = 0;
            }
        }
    }
}
