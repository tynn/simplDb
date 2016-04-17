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

package simpl.db.query;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;

import simpl.db.SimplDb;
import simpl.db.SimplError;
import simpl.db.table.Table;
import simpl.db.table.TableDef;

/**
 * {@code SimplQuery} is the base class for all simplDb queries.
 * Subclasses also implementing {@link QueryDef} must provide and expose a default constructor.
 * <p>
 * Use {@link #get(Class)} to create actual instances of {@code SimplQuery}.
 * </p>
 */
public class SimplQuery {
    private static final HashMap<Class<? extends QueryDef>, SimplQuery> I = new HashMap<>();

    /**
     * Precompiles the configuration of a {@link QueryDef} to execute.
     * <p>
     * {@code queryDef} must be annotated with {@link Query} and optionally {@link Join} or
     * also extend a {@link TableDef} or {@code SimplQuery}.
     * </p>
     *
     * @param queryDef to compiled
     * @return a newly created or cached instance for {@code queryDef}
     */
    public static SimplQuery get(Class<? extends QueryDef> queryDef) {
        SimplQuery simplQuery = I.get(queryDef);
        if (simplQuery == null) {
            Query query = queryDef.getAnnotation(Query.class);
            if (query != null) {
                simplQuery = new SimplQuery(query, queryDef.getAnnotation(Join.class));
            } else if (TableDef.class.isAssignableFrom(queryDef)) {
                simplQuery = new SimplQuery(queryDef.asSubclass(TableDef.class));
            } else try {
                simplQuery = queryDef.asSubclass(SimplQuery.class).newInstance();
            } catch (ClassCastException e) {
                throw new SimplError(queryDef, Query.class);
            } catch (Exception e) {
                throw new SimplError(e);
            }
            I.put(queryDef, simplQuery);
        }
        return simplQuery;
    }

    private final Collection<Class<? extends TableDef>> mTableDefs;
    private final String mTable;
    private final String[] mColumns;
    private final QueryDef.Filter mFilter = new QueryDef.Filter();

    /**
     * @param table   name to compile the query against
     * @param columns to return
     * @param filter  to use with the query
     * @param tables  this query should observe
     * @see SQLiteDatabase#query(String, String[], String, String[], String, String, String, String)
     */
    protected SimplQuery(String table, String[] columns, QueryDef.Filter filter, Class<? extends TableDef>... tables) {
        if (table == null)
            throw new NullPointerException("table must not be null");
        if (tables.length == 0)
            throw new ArrayIndexOutOfBoundsException("at least one TableDef subclass must be passed");

        for (Class<? extends TableDef> tableDef : tables)
            if (!tableDef.isAnnotationPresent(Table.class))
                throw new SimplError(tableDef, Table.class);

        mTableDefs = Collections.unmodifiableCollection(Arrays.asList(tables));
        mTable = table;
        mColumns = columns;
        mFilter.set(filter);
    }

    private SimplQuery(Query query, Join join) {
        Class<? extends TableDef> tableDef = query.table();
        if (!tableDef.isAnnotationPresent(Table.class))
            throw new SimplError(tableDef, Table.class);

        ArrayList<Class<? extends TableDef>> tableDefs = new ArrayList<>(2);
        tableDefs.add(tableDef);
        mTableDefs = Collections.unmodifiableCollection(tableDefs);

        String tableName = SimplDb.getName(tableDef);
        StringBuilder table = new StringBuilder(tableName);
        HashSet<String> columns = new HashSet<>(Arrays.asList(query.columns()));
        if (join != null) {
            tableDef = join.table();
            if (!tableDef.isAnnotationPresent(Table.class))
                throw new SimplError(tableDef, Table.class);
            tableDefs.add(tableDef);

            if (join.natural())
                table.append(" NATURAL");

            if (join.type() != JoinType.DEFAULT)
                table.append(' ').append(join.type());
            table.append(" JOIN ");
            table.append(SimplDb.getName(tableDef));

            String on = String.format(Locale.US, join.on(), tableName, SimplDb.getName(tableDef));
            table.append(" ON (").append(on).append(')');

            columns.addAll(Arrays.asList(join.columns()));
        }

        mTable = table.toString();
        mColumns = columns.toArray(new String[columns.size()]);

        mFilter.setSelection(query.selection(), query.selectionArgs());
        mFilter.setGroupBy(query.groupBy());
        mFilter.setOrderBy(query.orderBy());
        mFilter.setHaving(query.having());
        mFilter.setLimit(query.limit());
    }

    private SimplQuery(Class<? extends TableDef> tableDef) {
        if (!tableDef.isAnnotationPresent(Table.class))
            throw new SimplError(tableDef, Table.class);

        mTable = SimplDb.getName(tableDef);
        mColumns = null;
        mTableDefs = Collections.<Class<? extends TableDef>>singleton(tableDef);
    }

    /**
     * @return all tables queried with this query
     */
    public Collection<Class<? extends TableDef>> getTables() {
        return mTableDefs;
    }

    /**
     * Wraps {@link #exec(SQLiteDatabase, QueryDef.Filter)} for convenience.
     *
     * @param db     to query
     * @param filter to apply
     * @return a cursor positioned before the first entry
     * @see SQLiteDatabase#query(String, String[], String, String[], String, String, String, String)
     */
    public Cursor exec(SimplDb db, QueryDef.Filter filter) {
        return exec(db.getReadableDatabase(), filter);
    }

    /**
     * Executes this query with {@code filter} applied.
     *
     * @param db     to query
     * @param filter to apply
     * @return a cursor positioned before the first entry
     * @see SQLiteDatabase#query(String, String[], String, String[], String, String, String, String)
     */
    public Cursor exec(SQLiteDatabase db, QueryDef.Filter filter) {
        return db.query(mTable, mColumns, mFilter.getSelection(filter), mFilter.getSelectionArgs(filter),
                mFilter.getGroupBy(filter), mFilter.getHaving(filter), mFilter.getOrderBy(filter),
                mFilter.getLimit(filter));
    }
}
