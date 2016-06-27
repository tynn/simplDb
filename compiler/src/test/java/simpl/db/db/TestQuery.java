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

import simpl.db.api.Join;
import simpl.db.api.Query;
import simpl.db.api.QueryDef;

import static simpl.db.db.TestTable.KEY1;
import static simpl.db.db.TestTable.KEY2;

@Query(table = TestTable.class, columns = {KEY1, KEY2}, limit = 2)
@Join(table = TestTable.class, columns = {KEY1, KEY2}, on = KEY1 + "=" + KEY2)
public interface TestQuery extends QueryDef {
}
