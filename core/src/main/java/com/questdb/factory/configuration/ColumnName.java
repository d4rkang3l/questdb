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

package com.questdb.factory.configuration;

import com.questdb.misc.Chars;
import com.questdb.std.ThreadLocal;
import com.questdb.std.str.FlyweightCharSequence;
import org.jetbrains.annotations.NotNull;

public class ColumnName implements CharSequence {
    private static final ThreadLocal<ColumnName> SINGLETON = new ThreadLocal<>(ColumnName::new);
    private final FlyweightCharSequence alias = new FlyweightCharSequence();
    private final FlyweightCharSequence name = new FlyweightCharSequence();
    private CharSequence underlying;

    public static ColumnName singleton(CharSequence that) {
        ColumnName cn = SINGLETON.get();
        cn.of(that);
        return cn;
    }

    public CharSequence alias() {
        return alias;
    }

    @Override
    public int hashCode() {
        return Chars.hashCode(underlying);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ColumnName)) {
            return false;
        }
        ColumnName that = (ColumnName) obj;
        return Chars.equals(alias, that.alias()) && Chars.equals(name, that.name());
    }

    @Override
    public
    @NotNull
    String toString() {
        return underlying == null ? "null" : underlying.toString();
    }

    public boolean isNull() {
        return alias.length() == 0 && name.length() == 0;
    }

    @Override
    public int length() {
        return underlying.length();
    }

    @Override
    public char charAt(int index) {
        return underlying.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        throw new UnsupportedOperationException();
    }

    public CharSequence name() {
        return name;
    }

    private void of(CharSequence that) {
        this.underlying = that;
        int dot = Chars.indexOf(that, '.');
        if (dot == -1) {
            alias.of(null, 0, 0);
            name.of(that, 0, that.length());
        } else {
            alias.of(that, 0, dot);
            name.of(that, dot + 1, that.length() - dot - 1);
        }
    }
}
