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

import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import simpl.db.table.Check;
import simpl.db.table.Column;
import simpl.db.table.ConflictClause;
import simpl.db.table.Default;
import simpl.db.table.NotNull;
import simpl.db.table.PrimaryKey;
import simpl.db.table.Sortorder;
import simpl.db.table.Table;
import simpl.db.table.TableDef;
import simpl.db.table.Unique;
import simpl.db.table.WithoutRowid;

final class TableSql {
    private static final String MSG_FORMAT = "Column %1$s in %2$s must match " +
            "'public static final String %1$s = \"%3$s\";'";

    private final StringBuilder mSql = new StringBuilder();
    private final HashSet<String> mColumns = new HashSet<>();

    private String mName;

    synchronized String build(Class<? extends TableDef> tableDef, boolean tmp) {
        Table table = tableDef.getAnnotation(Table.class);
        if (table == null)
            throw new SimplError(tableDef, Table.class);
        mName = SimplDb.getName(tableDef);

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

        boolean delimiter = false;
        for (Field field : tableDef.getFields())
            delimiter |= addColumn(field, delimiter ? ", " : "");

        handleCheck(tableDef);

        mSql.append(")");
        if (WithoutRowid.SUPPORTED && tableDef.getAnnotation(WithoutRowid.class) != null)
            mSql.append(" WITHOUT ROWID");

        return mSql.toString();
    }

    private static boolean isPublicStaticFinalString(Field field) {
        int modifiers = field.getModifiers();
        return Modifier.isPublic(modifiers) && Modifier.isFinal(modifiers) && Modifier.isStatic(modifiers) &&
                field.getType() == String.class;
    }

    private boolean addColumn(Field field, String delimiter) {
        Column column = field.getAnnotation(Column.class);
        if (column == null)
            return false;

        if (isPublicStaticFinalString(field)) {
            String name = SimplDb.getName(field.getName());

            try {
                if (name.equals(field.get(null))) {
                    mColumns.add(name);
                    mSql.append(delimiter);
                    mSql.append('"').append(name).append('"');
                    mSql.append(' ').append(column.type());

                    handlePrimaryKey(field);
                    handleUnique(field);
                    handleNotNull(field);
                    handleDefault(field);
                    handleCheck(field);

                    return true;
                }
            } catch (Exception e) {
                Log.d(SimplDb.TAG, "assert", e);
            }
        }

        throw new SimplError(msg(field));
    }

    private static String msg(Field field) {
        String name = field.getName();
        return String.format(Locale.UK, MSG_FORMAT,
                name, field.getDeclaringClass().getSimpleName(), SimplDb.getName(name));
    }

    private void handlePrimaryKey(Field field) {
        PrimaryKey primaryKey = field.getAnnotation(PrimaryKey.class);
        if (primaryKey != null) {
            mSql.append(" PRIMARY KEY");

            if (primaryKey.sortorder() != Sortorder.DEFAULT)
                mSql.append(' ').append(primaryKey.sortorder());

            if (primaryKey.conflictClause() != ConflictClause.DEFAULT)
                mSql.append(" ON CONFLICT ").append(primaryKey.conflictClause());

            if (primaryKey.autoincrement())
                mSql.append(" AUTOINCREMENT");
        }
    }

    private void handleUnique(Field field) {
        Unique unique = field.getAnnotation(Unique.class);
        if (unique != null) {
            mSql.append(" UNIQUE");

            if (unique.conflictClause() != ConflictClause.DEFAULT)
                mSql.append(" ON CONFLICT ").append(unique.conflictClause());
        }
    }

    private void handleNotNull(Field field) {
        NotNull notNull = field.getAnnotation(NotNull.class);
        if (notNull != null) {
            mSql.append(" NOT NULL");

            if (notNull.conflictClause() != ConflictClause.DEFAULT)
                mSql.append(" ON CONFLICT ").append(notNull.conflictClause());
        }
    }

    private void handleDefault(Field field) {
        Default defaultValue = field.getAnnotation(Default.class);
        if (defaultValue != null) {
            mSql.append(" DEFAULT ");

            if (defaultValue.isExpression())
                mSql.append('(').append(defaultValue.value()).append(')');
            else
                mSql.append(defaultValue.value());
        }
    }

    private void handleCheck(Class<? extends TableDef> tableDef) {
        handleCheck(tableDef.getAnnotation(Check.class));
    }

    private void handleCheck(Field field) {
        handleCheck(field.getAnnotation(Check.class));
    }

    private void handleCheck(Check check) {
        if (check != null)
            mSql.append(" CHECK (").append(check.expression()).append(')');
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
