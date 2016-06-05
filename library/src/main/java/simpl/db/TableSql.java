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

package simpl.db;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import simpl.db.api.Check;
import simpl.db.api.Collate;
import simpl.db.api.Column;
import simpl.db.api.ConflictClause;
import simpl.db.api.Default;
import simpl.db.api.ForeignKey;
import simpl.db.api.ForeignKeyAction;
import simpl.db.api.NotNull;
import simpl.db.api.PrimaryKey;
import simpl.db.api.Sortorder;
import simpl.db.api.Table;
import simpl.db.api.Unique;
import simpl.db.api.WithoutRowid;
import simpl.db.spec.TableSpec;

final class TableSql {
    private final StringBuilder mSql = new StringBuilder();
    private final HashSet<String> mColumns = new HashSet<>();

    private String mName;

    synchronized String build(TableSpec tableSpec, boolean tmp) {
        Table table = tableSpec.annotation;
        mName = SimplDb.getName(tableSpec.name);

        mSql.setLength(0);
        mColumns.clear();

        mSql.append("CREATE ");
        if (table.temporary())
            mSql.append("TEMPORARY ");
        mSql.append("TABLE ");
        if (table.ifNotExists())
            mSql.append("IF NOT EXISTS ");
        if (tmp)
            mSql.append('_');
        mSql.append(mName).append(" (");

        boolean separate = false;
        for (Map.Entry<String, HashSet<? extends Annotation>> e : tableSpec.columnSpecs.entrySet())
            separate |= addColumn(e.getKey(), e.getValue(), separate ? ", " : "");

        boolean withoutRowId = addConstraints(tableSpec.constraints);

        mSql.append(")");
        if (withoutRowId)
            mSql.append(" WITHOUT ROWID");

        return mSql.toString();
    }

    private boolean addColumn(String name, HashSet<? extends Annotation> constraints, String delim) {
        Column column = null;
        PrimaryKey primaryKey = null;
        NotNull notNull = null;
        Unique unique = null;
        Check check = null;
        Default defaultValue = null;
        Collate collate = null;
        ForeignKey foreignKey = null;

        for (Annotation ann : constraints) {
            if (ann instanceof Column)
                column = (Column) ann;
            else if (ann instanceof PrimaryKey)
                primaryKey = (PrimaryKey) ann;
            else if (ann instanceof NotNull)
                notNull = (NotNull) ann;
            else if (ann instanceof Unique)
                unique = (Unique) ann;
            else if (ann instanceof Check)
                check = (Check) ann;
            else if (ann instanceof Default)
                defaultValue = (Default) ann;
            else if (ann instanceof Collate)
                collate = (Collate) ann;
            else if (ann instanceof ForeignKey)
                foreignKey = (ForeignKey) ann;
        }

        mColumns.add(name);
        mSql.append(delim);

        name = SimplDb.quote(name);
        mSql.append(name).append(' ').append(column.type());

        if (primaryKey != null)
            handlePrimaryKey(primaryKey);
        if (notNull != null)
            handleNotNull(notNull);
        if (unique != null)
            handleUnique(unique);
        if (check != null)
            handleCheck(check, name);
        if (defaultValue != null)
            handleDefault(defaultValue);
        if (collate != null)
            handleCollate(collate);
        if (foreignKey != null)
            handleForeignKey(foreignKey);

        return true;
    }

    private boolean addConstraints(HashSet<? extends Annotation> constraints) {
        boolean withoutRowid = false;

        for (Annotation ann : constraints) {
            if (ann instanceof PrimaryKey)
                handlePrimaryKey2((PrimaryKey) ann);
            else if (ann instanceof Unique)
                handleUnique2((Unique) ann);
            else if (ann instanceof Check)
                handleCheck2((Check) ann);
            else if (ann instanceof ForeignKey)
                handleForeignKey2((ForeignKey) ann);
            else if (ann instanceof WithoutRowid)
                withoutRowid = true;
        }

        return withoutRowid;
    }

    private void handlePrimaryKey(PrimaryKey primaryKey) {
        mSql.append(" PRIMARY KEY");

        if (primaryKey.sortorder() != Sortorder.DEFAULT)
            mSql.append(' ').append(primaryKey.sortorder());

        handleConflictClause(primaryKey.conflictClause());

        if (primaryKey.autoincrement())
            mSql.append(" AUTOINCREMENT");
    }

    private void handlePrimaryKey2(PrimaryKey primaryKey) {
        mSql.append(", PRIMARY KEY ");
        if (!handleColumns(primaryKey.columns()))
            throw new SimplError(PrimaryKey.class);
        handleConflictClause(primaryKey.conflictClause());
    }

    private void handleUnique(Unique unique) {
        mSql.append(" UNIQUE");
        handleConflictClause(unique.conflictClause());
    }

    private void handleUnique2(Unique unique) {
        mSql.append(", UNIQUE ");
        if (!handleColumns(unique.columns()))
            throw new SimplError(Unique.class);
        handleConflictClause(unique.conflictClause());
    }

    private void handleNotNull(NotNull notNull) {
        mSql.append(" NOT NULL");
        handleConflictClause(notNull.conflictClause());
    }

    private void handleConflictClause(ConflictClause conflictClause) {
        if (conflictClause != ConflictClause.DEFAULT)
            mSql.append(" ON CONFLICT ").append(conflictClause);
    }

    private void handleDefault(Default defaultValue) {
        if (defaultValue != null) {
            mSql.append(" DEFAULT ");

            if (defaultValue.expression())
                mSql.append('(').append(defaultValue.value()).append(')');
            else
                mSql.append(defaultValue.value());
        }
    }

    private void handleCollate(Collate collation) {
        mSql.append(" COLLATE ").append(collation.collationName());
    }

    private void handleCheck(String expression) {
        mSql.append(" CHECK (").append(expression).append(')');
    }

    private void handleCheck(Check check, String name) {
        handleCheck(String.format(Locale.US, check.expression(), name));
    }

    private void handleCheck2(Check check) {
        mSql.append(',');
        handleCheck(check.expression());
    }

    private void handleForeignKey(ForeignKey foreignKey) {
        mSql.append(" REFERENCES ").append(SimplDb.getName(foreignKey.foreignTable()));
        handleColumns(foreignKey.foreignColumns());
        if (foreignKey.onDelete() != ForeignKeyAction.DEFAULT)
            mSql.append(" ON DELETE ").append(foreignKey.onDelete().toString().replace('_', ' '));
        if (foreignKey.onUpdate() != ForeignKeyAction.DEFAULT)
            mSql.append(" ON UPDATE ").append(foreignKey.onUpdate().toString().replace('_', ' '));
        if (foreignKey.deferrable())
            mSql.append(" DEFERRABLE INITIALLY DEFERRED");
    }

    private void handleForeignKey2(ForeignKey foreignKey) {
        mSql.append(", FOREIGN KEY");
        if (!handleColumns(foreignKey.columns()))
            throw new SimplError(ForeignKey.class);
        handleForeignKey(foreignKey);
    }

    private boolean handleColumns(String[] columns) {
        if (columns.length == 0)
            return false;

        mSql.append(" (").append(columns[0]);
        for (int i = 1; i < columns.length; i++)
            mSql.append(", ").append(columns[i]);
        mSql.append(')');
        return true;
    }

    String getName() {
        return mName;
    }

    Set<String> getColumns() {
        return Collections.unmodifiableSet(mColumns);
    }

    @Override
    public String toString() {
        return mSql.toString();
    }
}
