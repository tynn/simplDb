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

package simpl.db.db.v2;

import simpl.db.table.Column;
import simpl.db.table.ForeignKey;
import simpl.db.table.Table;
import simpl.db.table.TableDef;
import simpl.db.table.TableDef.WithID;

import static simpl.db.table.ColumnType.INTEGER;
import static simpl.db.table.ForeignKeyAction.CASCADE;

@Table
@ForeignKey(columns = ForeignKeyTest.FOREIGN_KEY2, foreignTable = TypeTest.class, foreignColumns = TypeTest._ID, onDelete = CASCADE)
public interface ForeignKeyTest extends TableDef, WithID {
    @Column(type = INTEGER)
    @ForeignKey(foreignTable = TypeTest.class, foreignColumns = TypeTest._ID, onDelete = CASCADE)
    String FOREIGN_KEY1 = "foreign_key1";

    @Column(type = INTEGER)
    String FOREIGN_KEY2 = "foreign_key2";
}
