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

import simpl.db.api.QueryDef;
import simpl.db.api.Column;
import simpl.db.api.ColumnType;
import simpl.db.api.Table;
import simpl.db.api.TableDef;

@Table
public interface TestTable extends TableDefProxy, QueryDef {
    String NAME = "test_table";
    String SQL = "CREATE TABLE %s (\"test\" INTEGER)";

    @Column(type = ColumnType.INTEGER)
    String TEST = "test";

    @Table
    interface I extends TableDef {
        @Column(type = ColumnType.INTEGER)
        String TEST2 = "test2";
    }

    interface I1 extends TableDef {
    }

    @Table
    interface I2 extends TableDef {
        @Column(type = ColumnType.INTEGER)
        String TEST = "Test";
    }
}
