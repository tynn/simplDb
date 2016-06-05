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

import simpl.db.api.Check;
import simpl.db.api.Collate;
import simpl.db.api.Column;
import simpl.db.api.Default;
import simpl.db.api.NotNull;
import simpl.db.api.Table;
import simpl.db.api.TableDef;
import simpl.db.api.TableDef.WithID;
import simpl.db.api.Unique;

import static simpl.db.SimplDb.quote;
import static simpl.db.api.CollationName.NOCASE;
import static simpl.db.api.ColumnType.INTEGER;
import static simpl.db.api.ColumnType.TEXT;
import static simpl.db.api.ConflictClause.REPLACE;

@Table
public interface ColumnTest extends TableDef, WithID {
    @Column(type = TEXT)
    String COLUMN = "column";

    @Column(type = TEXT)
    @Default(value = "'default'")
    String DEFAULT = "default";

    @Column(type = TEXT)
    @NotNull
    String NOT_NULL = "not_null";

    @Column(type = INTEGER)
    @Check(expression = "%1$s<3")
    String CHECK = "check";
    String CHECK$ = quote(CHECK);

    @Column(type = INTEGER)
    @Unique(conflictClause = REPLACE)
    String UNIQUE = "unique";
    String UNIQUE$ = quote(UNIQUE);

    @Column(type = TEXT)
    @Collate(collationName = NOCASE)
    String COLLATE = "collate";
    String COLLATE$ = quote(COLLATE);
}
