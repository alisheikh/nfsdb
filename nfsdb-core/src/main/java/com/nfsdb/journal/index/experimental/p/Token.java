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

package com.nfsdb.journal.index.experimental.p;

public class Token {
    final boolean hidden;
    final String text;

    public Token(String text) {
        this.text = text;
        this.hidden = false;
    }

    public Token(boolean hidden, String text) {
        this.hidden = hidden;
        this.text = text;
    }

    @Override
    public String toString() {
        return "Token{" +
                "text='" + text + '\'' +
                ", hidden=" + hidden +
                '}';
    }
}
