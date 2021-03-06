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

package com.questdb.store;

import com.questdb.std.CharSequenceIntHashMap;
import com.questdb.std.IntIntHashMap;
import com.questdb.std.IntObjHashMap;
import com.questdb.std.ObjIntHashMap;
import com.questdb.txt.sink.StringSink;

import java.nio.ByteBuffer;

public final class ColumnType {
    public static final int BOOLEAN = 0;
    public static final int BYTE = 1;
    public static final int DOUBLE = 2;
    public static final int FLOAT = 3;
    public static final int INT = 4;
    public static final int LONG = 5;
    public static final int SHORT = 6;
    public static final int STRING = 7;
    public static final int SYMBOL = 8;
    public static final int BINARY = 9;
    public static final int DATE = 10;
    public static final int PARAMETER = 11;
    private static final ObjIntHashMap<Class> classMap = new ObjIntHashMap<>();
    private static final IntIntHashMap sizeMap = new IntIntHashMap();
    private static final IntObjHashMap<String> typeNameMap = new IntObjHashMap<>();
    private static final CharSequenceIntHashMap nameTypeMap = new CharSequenceIntHashMap();
    private static final ThreadLocal<StringSink> caseConverterBuffer = ThreadLocal.withInitial(StringSink::new);

    private ColumnType() {
    }

    public static int columnTypeOf(Class clazz) {
        return classMap.get(clazz);
    }

    public static int columnTypeOf(CharSequence name) {
        StringSink b = caseConverterBuffer.get();
        b.clear();
        for (int i = 0, n = name.length(); i < n; i++) {
            b.put(Character.toUpperCase(name.charAt(i)));
        }
        return nameTypeMap.get(b);
    }

    public static int count() {
        return typeNameMap.size();
    }

    public static String nameOf(int columnType) {
        return typeNameMap.get(columnType);
    }

    public static int sizeOf(int columnType) {
        return sizeMap.get(columnType);
    }

    static {
        classMap.put(boolean.class, BOOLEAN);
        classMap.put(byte.class, BYTE);
        classMap.put(double.class, DOUBLE);
        classMap.put(float.class, FLOAT);
        classMap.put(int.class, INT);
        classMap.put(long.class, LONG);
        classMap.put(short.class, SHORT);
        classMap.put(String.class, STRING);
        classMap.put(ByteBuffer.class, BINARY);

        sizeMap.put(BOOLEAN, 1);
        sizeMap.put(BYTE, 1);
        sizeMap.put(DOUBLE, 8);
        sizeMap.put(FLOAT, 4);
        sizeMap.put(INT, 4);
        sizeMap.put(LONG, 8);
        sizeMap.put(SHORT, 2);
        sizeMap.put(STRING, 0);
        sizeMap.put(SYMBOL, 4);
        sizeMap.put(BINARY, 0);
        sizeMap.put(DATE, 8);
        sizeMap.put(PARAMETER, 0);

        typeNameMap.put(BOOLEAN, "BOOLEAN");
        typeNameMap.put(BYTE, "BYTE");
        typeNameMap.put(DOUBLE, "DOUBLE");
        typeNameMap.put(FLOAT, "FLOAT");
        typeNameMap.put(INT, "INT");
        typeNameMap.put(LONG, "LONG");
        typeNameMap.put(SHORT, "SHORT");
        typeNameMap.put(STRING, "STRING");
        typeNameMap.put(SYMBOL, "SYMBOL");
        typeNameMap.put(BINARY, "BINARY");
        typeNameMap.put(DATE, "DATE");
        typeNameMap.put(PARAMETER, "PARAMETER");

        nameTypeMap.put("BOOLEAN", BOOLEAN);
        nameTypeMap.put("BYTE", BYTE);
        nameTypeMap.put("DOUBLE", DOUBLE);
        nameTypeMap.put("FLOAT", FLOAT);
        nameTypeMap.put("INT", INT);
        nameTypeMap.put("LONG", LONG);
        nameTypeMap.put("SHORT", SHORT);
        nameTypeMap.put("STRING", STRING);
        nameTypeMap.put("SYMBOL", SYMBOL);
        nameTypeMap.put("BINARY", BINARY);
        nameTypeMap.put("DATE", DATE);
        nameTypeMap.put("PARAMETER", PARAMETER);
    }
}
