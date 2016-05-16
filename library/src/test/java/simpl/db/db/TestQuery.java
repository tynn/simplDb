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

package simpl.db.db;


import simpl.db.api.Query;
import simpl.db.api.QueryDef;

@Query(table = TestTable.class, columns = TestTable.TEST,
        selection = TestQuery.SELECTION, selectionArgs = TestTable.TEST,
        groupBy = TestTable.TEST, having = TestTable.TEST,
        orderBy = TestTable.TEST, limit = 2)
public interface TestQuery extends QueryDef {
    String SELECTION = TestTable.TEST + "=?";
    String[] SELECTION_ARGS = {TestTable.TEST};

    interface Q1 extends QueryDef {
    }
}
