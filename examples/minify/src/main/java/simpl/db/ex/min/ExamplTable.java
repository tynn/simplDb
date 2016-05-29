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

package simpl.db.ex.min;

import simpl.db.api.QueryDef;
import simpl.db.api.Column;
import simpl.db.api.Table;
import simpl.db.api.TableDef;
import simpl.db.api.TableDef.WithCurrentTimestamp;
import simpl.db.api.TableDef.WithID;

import static simpl.db.api.ColumnType.INTEGER;
import static simpl.db.api.ColumnType.TEXT;

@Table
interface ExamplTable extends QueryDef, TableDef, WithID, WithCurrentTimestamp {
    @Column(type = TEXT)
    String NAME = "name";

    @Column(type = INTEGER)
    String VALUE = "value";
}
