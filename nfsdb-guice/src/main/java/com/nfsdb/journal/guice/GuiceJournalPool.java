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

package com.nfsdb.journal.guice;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.nfsdb.journal.factory.JournalConfiguration;
import com.nfsdb.journal.factory.JournalPool;


public class GuiceJournalPool extends JournalPool {

    @Inject
    public GuiceJournalPool(JournalConfiguration configuration, SizeHolder holder) throws InterruptedException {
        super(configuration, holder.maxSize);
    }

    static class SizeHolder {

        @Inject(optional = true)
        @Named("nfsdb.journal.pool.size")
        private final int maxSize = 10;
    }
}
