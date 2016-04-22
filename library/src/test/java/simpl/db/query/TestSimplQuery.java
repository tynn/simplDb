/*
 * Copyright 2016 Christian Schmitz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package simpl.db.query;

import simpl.db.TestTable;

@SuppressWarnings("unchecked")
public class TestSimplQuery extends SimplQuery implements QueryDef {
    public TestSimplQuery() {
        super(TestTable.NAME, null, null, TestTable.class);
    }

    public static class NoName extends SimplQuery implements QueryDef {
        public NoName() {
            super(null, null, null, TestTable.class);
        }
    }

    public static class NoTable extends SimplQuery implements QueryDef {
        public NoTable() {
            super(TestTable.NAME, null, null);
        }
    }
}
