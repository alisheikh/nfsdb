package com.nfsdb.thrift;

import com.nfsdb.journal.Journal;
import com.nfsdb.journal.JournalWriter;
import com.nfsdb.journal.exceptions.JournalException;
import com.nfsdb.journal.test.model.Quote;
import com.nfsdb.journal.test.model.Trade2;
import com.nfsdb.journal.test.tools.JournalTestFactory;
import com.nfsdb.journal.utils.Dates;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

public class NullsAdaptorTest {

    @Rule
    public JournalTestFactory factory = new JournalTestFactory(new ThriftNullsAdaptorFactory());

    @Test
    public void testFirstSymbolNull() throws JournalException {

        JournalWriter<Quote> w = factory.writer(Quote.class, "quote", 1000);
        long timestamp = Dates.toMillis("2013-10-05T10:00:00.000Z");
        Quote q = new Quote();
        for (int i = 0; i < 3; i++) {
            q.clear();
            if (i == 0) {
                q.setAsk(123);
                Assert.assertTrue(q.isSetAsk());
            } else {
                Assert.assertFalse(q.isSetAsk());
            }


            q.setTimestamp(timestamp);
            w.append(q);
        }

        w.commit();
        w.close();

        Journal<Quote> r = factory.reader(Quote.class, "quote");
        q = r.read(0);
        Quote q1 = r.read(1);

        Assert.assertNull(q.getSym());
        Assert.assertTrue(q.isSetAsk());

        Assert.assertFalse(q1.isSetAsk());
    }

    @Test
    public void testAppendReadBitSet() throws Exception {
        JournalWriter<Quote> w = factory.writer(Quote.class);
        Quote q = new Quote().setSym("A").setAsk(10);
        Assert.assertFalse(q.isSetBid());
        Assert.assertTrue(q.isSetAsk());
        Assert.assertTrue(q.isSetSym());
        Assert.assertFalse(q.isSetEx());
        w.append(q);
        Quote q2 = w.query().all().asResultSet().readFirst();
        Assert.assertFalse(q2.isSetBid());
        Assert.assertTrue(q2.isSetAsk());
        Assert.assertTrue(q.isSetSym());
        Assert.assertFalse(q.isSetEx());
    }

    @Test
    public void testLargeClass() throws Exception {
        try (JournalWriter<Trade2> writer = factory.writer(Trade2.class)) {
            Trade2 trade = new Trade2();
            trade.setStop87(10);
            Assert.assertTrue(trade.isSetStop87());
            writer.append(trade);
            writer.commit();

            Trade2 readTrade = writer.query().all().asResultSet().readFirst();
            Assert.assertTrue(readTrade.isSetStop87());
        }
    }

}
