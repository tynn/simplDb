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

package simpl.db.api;

/**
 * {@code TableDef} is the base class for all simplDb tables.
 * <p>
 * Implementations must also be annotated with {@link Table}.
 * </p>
 */
public interface TableDef extends SimplDef {
    /**
     * Defines the default _id column for a table.
     */
    interface WithID {
        /**
         * Defines an autoincrement primary key integer column for the table.
         */
        @Column(type = ColumnType.INTEGER)
        @PrimaryKey(sortorder = Sortorder.ASC)
        @NotNull
        String _ID = "_id";
    }

    /**
     * Defines a default current {@code _timestamp} column for a table.
     */
    interface WithCurrentTimestamp {
        @Column(type = ColumnType.TEXT)
        @Default(value = Default.CURRENT_TIMESTAMP)
        String _TIMESTAMP = "_timestamp";

        String _TIMESTAMP_LOCALTIME = "datetime(_timestamp, 'localtime')";
    }

}
