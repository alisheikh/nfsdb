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

package com.nfsdb.journal.net;

import com.nfsdb.journal.JournalMode;
import com.nfsdb.journal.column.FixedColumn;
import com.nfsdb.journal.column.MappedFileImpl;
import com.nfsdb.journal.exceptions.JournalException;
import com.nfsdb.journal.net.comsumer.FixedColumnDeltaConsumer;
import com.nfsdb.journal.net.producer.FixedColumnDeltaProducer;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.io.File;

public class FixedColumnTest {

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();
    private MappedFileImpl file;
    private MappedFileImpl file2;
    private MockByteChannel channel;

    @After
    public void cleanup() {
        file.delete();
        file2.delete();
    }

    @Before
    public void setUp() throws JournalException {
        file = new MappedFileImpl(new File(temporaryFolder.getRoot(), "col.d"), 22, JournalMode.APPEND);
        // it is important to keep bit hint small, so that file2 has small buffers. This would made test go via both pathways.
        // large number will result in tests not covering all of execution path.
        file2 = new MappedFileImpl(new File(temporaryFolder.getRoot(), "col2.d"), 18, JournalMode.APPEND);
        channel = new MockByteChannel();
    }

    @After
    public void tearDown() throws Exception {
        file.close();
        file2.close();
    }

    @Test
    public void testConsumerSmallerThanProducer() throws Exception {
        FixedColumn col1 = new FixedColumn(file, 4);
        FixedColumn col2 = new FixedColumn(file2, 4);

        FixedColumnDeltaProducer producer = new FixedColumnDeltaProducer(col1);
        ChannelConsumer consumer = new FixedColumnDeltaConsumer(col2);

        int max = 1500000;

        for (int i = 0; i < max; i++) {
            col1.putInt(max - i);
            col1.commit();
        }

        for (int i = 0; i < max - 500000; i++) {
            col2.putInt(max - i);
            col2.commit();
        }

        consumer.reset();
        producer.configure(col2.size(), col1.size());
        Assert.assertTrue(producer.hasContent());
        producer.write(channel);
        consumer.read(channel);
        col2.commit();

        Assert.assertEquals(col1.size(), col2.size());

        for (int i = 0; i < max; i++) {
            Assert.assertEquals(max - i, col2.getInt(i));
        }
    }

    @Test
    public void testConsumerEqualToProducer() throws Exception {
        FixedColumn col1 = new FixedColumn(file, 4);
        FixedColumn col2 = new FixedColumn(file2, 4);

        FixedColumnDeltaProducer producer = new FixedColumnDeltaProducer(col1);
        ChannelConsumer consumer = new FixedColumnDeltaConsumer(col2);

        int max = 1500000;

        for (int i = 0; i < max; i++) {
            col1.putInt(max - i);
            col1.commit();
        }

        for (int i = 0; i < max; i++) {
            col2.putInt(max - i);
            col2.commit();
        }

        consumer.reset();
        producer.configure(col2.size(), col1.size());

        // hasNext() can be true, because of compulsory header
        // however, if column doesn't have data, hasContent() must be false.
        Assert.assertFalse(producer.hasContent());
        Assert.assertEquals(col1.size(), col2.size());

        for (int i = 0; i < max; i++) {
            Assert.assertEquals(max - i, col2.getInt(i));
        }
    }

    @Test
    public void testEmptyConsumerAndProducer() throws Exception {
        FixedColumn col1 = new FixedColumn(file, 4);
        FixedColumn col2 = new FixedColumn(file2, 4);

        FixedColumnDeltaProducer producer = new FixedColumnDeltaProducer(col1);
        ChannelConsumer consumer = new FixedColumnDeltaConsumer(col2);

        consumer.reset();
        producer.configure(col2.size(), col1.size());

        // hasNext() can be true, because of compulsory header
        // however, if column doesn't have data, hasContent() must be false.
        Assert.assertFalse(producer.hasContent());
        Assert.assertEquals(col1.size(), col2.size());
    }

    @Test
    public void testEmptyConsumerAndPopulatedProducer() throws Exception {
        FixedColumn col1 = new FixedColumn(file, 4);
        FixedColumn col2 = new FixedColumn(file2, 4);

        FixedColumnDeltaProducer producer = new FixedColumnDeltaProducer(col1);
        ChannelConsumer consumer = new FixedColumnDeltaConsumer(col2);

        int max = 1500000;

        for (int i = 0; i < max; i++) {
            col1.putInt(max - i);
            col1.commit();
        }

        consumer.reset();
        producer.configure(col2.size(), col1.size());

        // hasNext() can be true, because of compulsory header
        // however, if column doesn't have data, hasContent() must be false.
        Assert.assertTrue(producer.hasContent());
        producer.write(channel);
        consumer.read(channel);
        col2.commit();

        Assert.assertEquals(col1.size(), col2.size());

        for (int i = 0; i < max; i++) {
            Assert.assertEquals(max - i, col2.getInt(i));
        }
    }

    @Test
    public void testConsumerLargerThanProducer() throws Exception {
        FixedColumn col1 = new FixedColumn(file, 4);
        FixedColumn col2 = new FixedColumn(file2, 4);

        FixedColumnDeltaProducer producer = new FixedColumnDeltaProducer(col1);
        ChannelConsumer consumer = new FixedColumnDeltaConsumer(col2);

        int max = 1500000;

        for (int i = 0; i < max - 500000; i++) {
            col1.putInt(max - i);
            col1.commit();
        }

        for (int i = 0; i < max; i++) {
            col2.putInt(max - i);
            col2.commit();
        }

        consumer.reset();
        producer.configure(col2.size(), col1.size());
        Assert.assertFalse(producer.hasContent());
    }

    @Test
    public void testConsumerReset() throws Exception {
        FixedColumn col1 = new FixedColumn(file, 4);
        FixedColumn col2 = new FixedColumn(file2, 4);

        FixedColumnDeltaProducer producer = new FixedColumnDeltaProducer(col1);
        ChannelConsumer consumer = new FixedColumnDeltaConsumer(col2);

        int max = 1500000;

        for (int i = 0; i < max; i++) {
            col1.putInt(max - i);
            col1.commit();
        }

        for (int i = 0; i < max - 500000; i++) {
            col2.putInt(max - i);
            col2.commit();
        }

        consumer.reset();
        producer.configure(col2.size(), col1.size());
        Assert.assertTrue(producer.hasContent());
        producer.write(channel);
        consumer.read(channel);
        col2.commit();

        Assert.assertEquals(col1.size(), col2.size());

        for (int i = 0; i < 10000; i++) {
            col1.putInt(max + 10000 - i);
            col1.commit();
        }

        consumer.reset();
        producer.configure(col2.size(), col1.size());
        Assert.assertTrue(producer.hasContent());
        producer.write(channel);
        consumer.read(channel);
        col2.commit();

        Assert.assertEquals(col1.size(), col2.size());

        for (int i = 0; i < max; i++) {
            Assert.assertEquals(max - i, col2.getInt(i));
        }

        for (int i = max; i < max + 10000; i++) {
            Assert.assertEquals(max + max + 10000 - i, col2.getInt(i));
        }
    }

    @Test
    public void testNonBlockingConsumer() throws Exception {
        FixedColumn col1 = new FixedColumn(file, 4);
        FixedColumn col2 = new FixedColumn(file2, 4);

        FixedColumnDeltaProducer producer = new FixedColumnDeltaProducer(col1);
        ChannelConsumer consumer = new FixedColumnDeltaConsumer(col2);

        int max = 1500000;

        for (int i = 0; i < max; i++) {
            col1.putInt(max - i);
            col1.commit();
        }

        for (int i = 0; i < max - 500000; i++) {
            col2.putInt(max - i);
            col2.commit();
        }

        consumer.reset();
        producer.configure(col2.size(), col1.size());
        Assert.assertTrue(producer.hasContent());
        channel.setBlocking(false);
        producer.write(channel);
        consumer.read(channel);

        Assert.assertFalse(consumer.isComplete());
        consumer.read(channel);
        col2.commit();

        Assert.assertEquals(col1.size(), col2.size());

        for (int i = 0; i < max; i++) {
            Assert.assertEquals(max - i, col2.getInt(i));
        }
    }

}
