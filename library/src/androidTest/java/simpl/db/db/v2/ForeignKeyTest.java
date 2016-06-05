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

import simpl.db.api.Column;
import simpl.db.api.Constraint;
import simpl.db.api.ForeignKey;
import simpl.db.api.Table;
import simpl.db.api.TableDef;

import static simpl.db.api.ColumnType.INTEGER;
import static simpl.db.api.ForeignKeyAction.CASCADE;

@Table
@ForeignKey(columns = ForeignKeyTest.FOREIGN_KEY2, foreignTable = TypeTest.class, foreignColumns = TypeTest._ID, onDelete = CASCADE)
public interface ForeignKeyTest extends TableDef {
    @Column(type = INTEGER)
    @ForeignKey(foreignTable = TypeTest.class, foreignColumns = TypeTest._ID, onDelete = CASCADE)
    String FOREIGN_KEY1 = "foreign_key1";

    @Column(type = INTEGER)
    String FOREIGN_KEY2 = "foreign_key2";

    @Column(type = INTEGER)
    String FOREIGN_KEY3 = "foreign_key3";

    @Constraint
    @ForeignKey(columns = FOREIGN_KEY3, foreignTable = TypeTest.class, foreignColumns = TypeTest._ID, onDelete = CASCADE)
    String FOREIGN_KEY3_CONSTRAINT = "foreign_key3_constraint";
}
