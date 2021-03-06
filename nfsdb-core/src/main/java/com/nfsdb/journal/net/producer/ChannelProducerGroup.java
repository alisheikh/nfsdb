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

package com.nfsdb.journal.net.producer;

import com.nfsdb.journal.exceptions.JournalNetworkException;
import com.nfsdb.journal.net.ChannelProducer;

import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;

public class ChannelProducerGroup<T extends ChannelProducer> implements ChannelProducer {

    private final List<T> producers = new ArrayList<>();
    private boolean hasContent = false;

    @Override
    public boolean hasContent() {
        return hasContent;
    }

    @Override
    public void write(WritableByteChannel channel) throws JournalNetworkException {
        if (hasContent) {
            for (T p : producers) {
                p.write(channel);
            }
            hasContent = false;
        }
    }

    @Override
    public String toString() {
        return "ChannelProducerGroup{" +
                "size=" + producers.size() +
                '}';
    }

    protected List<T> getProducers() {
        return producers;
    }

    void computeHasContent() {
        for (ChannelProducer p : producers) {
            this.hasContent = p.hasContent();
            if (this.hasContent) {
                break;
            }
        }
    }

    void addProducer(T producer) {
        this.producers.add(producer);
    }
}

