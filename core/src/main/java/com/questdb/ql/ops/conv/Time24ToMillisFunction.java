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

package com.questdb.ql.ops.conv;

import com.questdb.ex.NumericException;
import com.questdb.misc.Dates;
import com.questdb.ql.Record;
import com.questdb.ql.ops.AbstractUnaryOperator;
import com.questdb.ql.ops.Function;
import com.questdb.ql.ops.VirtualColumnFactory;
import com.questdb.store.ColumnType;

public class Time24ToMillisFunction extends AbstractUnaryOperator {

    public final static VirtualColumnFactory<Function> FACTORY = (position, configuration) -> new Time24ToMillisFunction(position);

    private Time24ToMillisFunction(int position) {
        super(ColumnType.LONG, position);
    }

    @Override
    public long getDate(Record rec) {
        return getLong(rec);
    }

    @Override
    public double getDouble(Record rec) {
        return getLong(rec);
    }

    @Override
    public long getLong(Record rec) {
        try {
            CharSequence s = value.getFlyweightStr(rec);
            return s == null ? Long.MIN_VALUE : Dates.parseTime24(s);
        } catch (NumericException ignore) {
            return 0;
        }
    }
}
