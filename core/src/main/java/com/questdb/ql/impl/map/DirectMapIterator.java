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

package com.questdb.ql.impl.map;

import com.questdb.misc.Unsafe;
import com.questdb.std.AbstractImmutableIterator;

public final class DirectMapIterator extends AbstractImmutableIterator<DirectMapEntry> {
    private final DirectMapEntry entry;
    private int count;
    private long address;

    DirectMapIterator(DirectMapEntry entry) {
        this.entry = entry;
    }

    @Override
    public boolean hasNext() {
        return count > 0;
    }

    @Override
    public DirectMapEntry next() {
        long address = this.address;
        this.address = address + Unsafe.getUnsafe().getInt(address);
        count--;
        return entry.init(address);
    }

    DirectMapIterator init(long address, int count) {
        this.address = address;
        this.count = count;
        return this;
    }
}
