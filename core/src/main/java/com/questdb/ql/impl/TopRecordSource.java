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

package com.questdb.ql.impl;

import com.questdb.factory.ReaderFactory;
import com.questdb.factory.configuration.RecordMetadata;
import com.questdb.misc.Misc;
import com.questdb.ql.*;
import com.questdb.ql.ops.AbstractCombinedRecordSource;
import com.questdb.ql.ops.VirtualColumn;
import com.questdb.std.str.CharSink;

public class TopRecordSource extends AbstractCombinedRecordSource {

    private final RecordSource delegate;
    private final VirtualColumn lo;
    private final VirtualColumn hi;
    private long _top;
    private long _count;
    private RecordCursor cursor;

    public TopRecordSource(RecordSource delegate, VirtualColumn lo, VirtualColumn hi) {
        this.delegate = delegate;
        this.lo = lo;
        this.hi = hi;
    }

    @Override
    public void close() {
        Misc.free(delegate);
    }

    @Override
    public RecordMetadata getMetadata() {
        return delegate.getMetadata();
    }

    @Override
    public RecordCursor prepareCursor(ReaderFactory factory, CancellationHandler cancellationHandler) {
        this._top = lo.getLong(null);
        this._count = hi.getLong(null) - this._top;
        this.cursor = delegate.prepareCursor(factory, cancellationHandler);
        return this;
    }

    @Override
    public Record getRecord() {
        return delegate.getRecord();
    }

    @Override
    public Record newRecord() {
        return delegate.newRecord();
    }

    @Override
    public StorageFacade getStorageFacade() {
        return cursor.getStorageFacade();
    }

    @Override
    public void releaseCursor() {
        this.cursor.releaseCursor();
    }

    @Override
    public void toTop() {
        this._top = lo.getLong(null);
        this._count = hi.getLong(null) - this._top;
        this.cursor.toTop();
    }

    @Override
    public boolean hasNext() {
        if (_top > 0) {
            return scrollToStart();
        } else {
            return _count > 0 && cursor.hasNext();
        }
    }

    @Override
    public Record next() {
        _count--;
        return cursor.next();
    }

    @Override
    public Record recordAt(long rowId) {
        return cursor.recordAt(rowId);
    }

    @Override
    public void recordAt(Record record, long atRowId) {
        cursor.recordAt(record, atRowId);
    }

    @Override
    public boolean supportsRowIdAccess() {
        return delegate.supportsRowIdAccess();
    }

    @Override
    public void toSink(CharSink sink) {
        sink.put('{');
        sink.putQuoted("op").put(':').putQuoted("TopRecordSource").put(',');
        sink.putQuoted("low").put(':').put(lo.getLong(null)).put(',');
        sink.putQuoted("high").put(':').put(hi.getLong(null));
        sink.put('}');
    }

    private boolean scrollToStart() {
        if (_count > 0) {
            long top = this._top;
            while (top > 0 && cursor.hasNext()) {
                cursor.next();
                top--;
            }
            return (_top = top) == 0 && cursor.hasNext();
        }
        return false;
    }
}
