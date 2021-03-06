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


import simpl.db.api.Check;
import simpl.db.api.Column;
import simpl.db.api.Constraint;
import simpl.db.api.Default;
import simpl.db.api.Table;
import simpl.db.api.TableDef;
import simpl.db.api.Unique;
import simpl.db.api.WithoutRowid;

import static simpl.db.api.ColumnType.INTEGER;
import static simpl.db.api.ColumnType.TEXT;

@Table
@WithoutRowid
@Check(expression = "")
public interface TestTable extends TableDef {
    @Column(type = TEXT)
    String KEY1 = "key1";

    @Column(type = TEXT)
    @Unique
    String KEY2 = "key2";

    @Column(type = INTEGER)
    @Default("4")
    String KEY3 = "key3";

    @Constraint
    @Unique(columns = {KEY1, KEY2})
    String CONST1 = "const1";
}
