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

package simpl.db.db.v10;

import simpl.db.table.Column;
import simpl.db.table.Default;
import simpl.db.table.NotNull;
import simpl.db.table.Table;
import simpl.db.table.TableDef;
import simpl.db.table.TableDef.WithID;
import simpl.db.table.Unique;

import static simpl.db.table.ColumnType.INTEGER;
import static simpl.db.table.ColumnType.TEXT;

@Table
public interface JoinTest extends TableDef, WithID {
    @Column(type = TEXT)
    @NotNull
    @Unique
    String EXTRA = "extra";

    @Column(type = INTEGER)
    @Default("7")
    String REF = "ref";
}
