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

package com.questdb.misc;

import com.questdb.txt.sink.StringSink;

public class Rnd {
    private final StringSink sink = new StringSink();
    private long s0;
    private long s1;

    public Rnd(long s0, long s1) {
        this.s0 = s0;
        this.s1 = s1;
    }

    public Rnd() {
        this.s0 = 0xdeadbeef;
        this.s1 = 0xdee4c0ed;
    }

    public boolean nextBoolean() {
        return nextLong(1) != 0;
    }

    public byte nextByte() {
        return (byte) nextLong();
    }

    public byte[] nextBytes(int len) {
        byte bytes[] = new byte[len];
        for (int i = 0; i < len; i++) {
            bytes[i] = (byte) (nextPositiveInt() % 25 + 66);
        }
        return bytes;
    }

    public void nextChars(final long address, int len) {
        long addr = address;
        long limit = addr + len - 2;
        while (addr < limit) {
            Unsafe.getUnsafe().putChar(addr, (char) (nextPositiveInt() % 25 + 66));
            addr += 2;
        }
    }

    public CharSequence nextChars(int len) {
        sink.clear();
        for (int i = 0; i < len; i++) {
            sink.put((char) (nextPositiveInt() % 25 + 66));
        }
        return sink;
    }

    public double nextDouble() {
        return (nextLong(26) << 27 + nextLong(27)) / (double) (1L << 53);
    }

    public float nextFloat() {
        return nextLong(24) / ((float) (1 << 24));
    }

    public int nextInt() {
        return (int) nextLong();
    }

    public long nextLong() {
        long l1 = s0;
        long l0 = s1;
        s0 = l0;
        l1 ^= l1 << 23;
        return (s1 = l1 ^ l0 ^ (l1 >> 17) ^ (l0 >> 26)) + l0;
    }

    public int nextPositiveInt() {
        int n = (int) nextLong();
        return n > 0 ? n : -n;
    }

    public long nextPositiveLong() {
        long l = nextLong();
        return l > 0 ? l : -l;
    }

    public short nextShort() {
        return (short) nextLong();
    }

    public String nextString(int len) {
        char chars[] = new char[len];
        for (int i = 0; i < len; i++) {
            chars[i] = (char) (nextPositiveInt() % 25 + 66);
        }
        return new String(chars);
    }

    private long nextLong(int bits) {
        return nextLong() >>> (64 - bits);
    }
}
