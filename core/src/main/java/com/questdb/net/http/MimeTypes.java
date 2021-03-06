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

import com.questdb.misc.ByteBuffers;
import com.questdb.misc.Unsafe;
import com.questdb.std.CharSequenceObjHashMap;
import com.questdb.std.str.DirectByteCharSequence;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public final class MimeTypes extends CharSequenceObjHashMap<CharSequence> implements Closeable {
    private ByteBuffer buf;

    public MimeTypes(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            int sz;
            buf = ByteBuffer.allocateDirect(sz = fis.available());

            fis.getChannel().read(buf);

            long p = ByteBuffers.getAddress(buf);
            long hi = p + sz;
            long _lo = p;

            boolean newline = true;
            boolean comment = false;

            CharSequence contentType = null;

            while (p < hi) {
                char b = (char) Unsafe.getUnsafe().getByte(p++);

                switch (b) {
                    case '#':
                        comment = newline;
                        break;
                    case ' ':
                    case '\t':
                        if (!comment) {
                            if (newline || _lo == p - 1) {
                                _lo = p;
                            } else {
                                DirectByteCharSequence s = new DirectByteCharSequence().of(_lo, p - 1);
                                _lo = p;
                                if (contentType == null) {
                                    contentType = s;
                                } else {
                                    this.put(s, contentType);
                                }
                                newline = false;
                            }
                        }
                        break;
                    case '\n':
                    case '\r':
                        newline = true;
                        comment = false;
                        if (_lo < p - 1 && contentType != null) {
                            DirectByteCharSequence s = new DirectByteCharSequence().of(_lo, p - 1);
                            this.put(s, contentType);
                        }
                        contentType = null;
                        _lo = p;
                        break;
                    default:
                        if (newline) {
                            newline = false;
                        }
                        break;
                }

            }
        }
    }

    @Override
    public void close() {
        buf = ByteBuffers.release(buf);
    }
}
