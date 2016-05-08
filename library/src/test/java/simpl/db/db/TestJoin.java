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


import simpl.db.query.Join;
import simpl.db.query.JoinType;
import simpl.db.query.Query;
import simpl.db.query.QueryDef;

@Query(table = TestTable.class, columns = TestTable.TEST, limit = 3)
@Join(table = TestTable.I.class, columns = TestTable.I.TEST2, type = JoinType.INNER, on = TestJoin.ON)
public interface TestJoin extends QueryDef {
    String ON = "%1$s.test=%2$s.test2";
    String NAME = TestTable.NAME + ' ' + JoinType.INNER + " JOIN i ON (test_table.test=i.test2)";
}
