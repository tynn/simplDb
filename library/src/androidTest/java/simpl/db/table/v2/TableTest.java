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

package simpl.db.table.v2;

import simpl.db.table.Column;
import simpl.db.table.ColumnType;
import simpl.db.table.Table;
import simpl.db.table.TableDef;

@Table
public interface TableTest extends TableDef {
    @Column(type = ColumnType.BLOB)
    String DATA = "data";

    @Column(type = ColumnType.INTEGER)
    String VALUE = "value";

    @Column(type = ColumnType.TEXT)
    String INFO = "info";

    @Column(type = ColumnType.NUMERIC)
    String KEEP = "keep";
}
