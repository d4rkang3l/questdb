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

package com.questdb.txt.sink;

import com.questdb.std.Mutable;
import com.questdb.std.str.CharSink;
import org.jetbrains.annotations.NotNull;

public class StringSink extends AbstractCharSink implements CharSequence, Mutable {
    private final StringBuilder builder = new StringBuilder();

    public void clear(int pos) {
        builder.setLength(pos);
    }

    public void clear() {
        builder.setLength(0);
    }

    @Override
    public void flush() {
    }

    @Override
    public CharSink put(CharSequence cs) {
        if (cs != null) {
            builder.append(cs);
        }
        return this;
    }

    @Override
    public CharSink put(char c) {
        builder.append(c);
        return this;
    }

    @Override
    public int length() {
        return builder.length();
    }

    @Override
    public char charAt(int index) {
        return builder.charAt(index);
    }

    @Override
    public CharSequence subSequence(int lo, int hi) {
        return builder.subSequence(lo, hi);
    }

    public CharSink put(char c, int n) {
        for (int i = 0; i < n; i++) {
            builder.append(c);
        }
        return this;
    }

    /* Either IDEA or FireBug complain, annotation galore */
    @NotNull
    @Override
    public String toString() {
        return builder.toString();
    }
}
