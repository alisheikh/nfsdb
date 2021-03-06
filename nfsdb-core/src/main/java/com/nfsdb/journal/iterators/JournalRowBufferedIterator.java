/*
 * Copyright (c) 2014. Vlad Ilyushchenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nfsdb.journal.iterators;

import com.nfsdb.journal.Journal;
import com.nfsdb.journal.exceptions.JournalException;
import com.nfsdb.journal.exceptions.JournalImmutableIteratorException;
import com.nfsdb.journal.exceptions.JournalRuntimeException;
import com.nfsdb.journal.utils.Rows;

import java.util.Iterator;
import java.util.List;

public class JournalRowBufferedIterator<T> implements Iterable<JournalRow<T>>, Iterator<JournalRow<T>> {
    boolean hasNext = true;
    private final List<JournalIteratorRange> ranges;
    private final Journal<T> journal;
    private final JournalRow<T> row;
    private int currentIndex = 0;
    private long currentRowID;
    private long currentUpperBound;
    private int currentPartitionID;

    public JournalRowBufferedIterator(Journal<T> journal, List<JournalIteratorRange> ranges) {
        this.ranges = ranges;
        this.journal = journal;
        this.row = new JournalRow<>(journal.newObject());
        updateVariables();
    }

    @Override
    public boolean hasNext() {
        return hasNext;
    }

    @Override
    public JournalRow<T> next() {
        try {
            T obj = row.getObject();
            journal.clearObject(obj);
            long rowID = Rows.toRowID(currentPartitionID, currentRowID);
            row.setRowID(rowID);
            journal.read(rowID, obj);
            if (currentRowID < currentUpperBound) {
                currentRowID++;
            } else {
                currentIndex++;
                updateVariables();
            }
            return row;
        } catch (JournalException e) {
            throw new JournalRuntimeException("Error in iterator [" + this + "]", e);
        }
    }

    @Override
    public void remove() {
        throw new JournalImmutableIteratorException();
    }

    @Override
    public Iterator<JournalRow<T>> iterator() {
        return this;
    }

    @Override
    public String toString() {
        return "JournalRowBufferedIterator{" +
                "currentRowID=" + currentRowID +
                ", currentUpperBound=" + currentUpperBound +
                ", currentPartitionID=" + currentPartitionID +
                ", currentIndex=" + currentIndex +
                ", journal=" + journal +
                '}';
    }

    public Journal<T> getJournal() {
        return journal;
    }

    private void updateVariables() {
        if (currentIndex < ranges.size()) {
            JournalIteratorRange w = ranges.get(currentIndex);
            currentRowID = w.lo;
            currentUpperBound = w.hi;
            currentPartitionID = w.partitionID;
        } else {
            hasNext = false;
        }
    }
}
