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

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;

import simpl.db.api.Join;
import simpl.db.api.JoinType;
import simpl.db.api.Query;
import simpl.db.api.QueryDef;
import simpl.db.api.Table;
import simpl.db.api.TableDef;
import simpl.db.spec.QuerySpec;

/**
 * {@code SimplQuery} is the base class for all simplDb queries.
 * Subclasses also implementing {@link QueryDef} must provide and expose a default constructor.
 * <p>
 * Use {@link #get(Class)} to create actual instances of {@code SimplQuery}.
 * </p>
 */
public class SimplQuery {
    private static final String QUERY_SPEC = "$$QuerySpec";
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
    public static synchronized SimplQuery get(Class<? extends QueryDef> queryDef) {
        SimplQuery simplQuery = I.get(queryDef);
        if (simplQuery == null) {
            QuerySpec spec = loadQuerySpec(queryDef);
            if (spec != null) {
                simplQuery = new SimplQuery(spec.annotation, spec.join);
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

    private static QuerySpec loadQuerySpec(Class<? extends QueryDef> queryDef) {
        String querySpecName = queryDef.getName() + QUERY_SPEC;
        try {
            return (QuerySpec) Class.forName(querySpecName).newInstance();
        } catch (ClassCastException e) {
            throw new SimplError(querySpecName + " must extend " + QuerySpec.class);
        } catch (ClassNotFoundException e) {
            Log.i(SimplDb.TAG, querySpecName + " not found");
        } catch (Exception e) {
            Log.e(SimplDb.TAG, "error", e);
        }

        Query query = queryDef.getAnnotation(Query.class);
        if (query == null)
            return null;

        QuerySpec spec = new QuerySpec("none", query, queryDef);
        spec.join = queryDef.getAnnotation(Join.class);
        return spec;
    }

    private final Collection<Class<? extends TableDef>> mTableDefs;
    private final String mTable;
    private final String[] mColumns;
    private final Filter mFilter = new Filter();

    /**
     * @param table   name to compile the query against
     * @param columns to return
     * @param filter  to use with the query
     * @param tables  this query should observe
     * @see SQLiteDatabase#query(String, String[], String, String[], String, String, String, String)
     */
    @SuppressWarnings("unchecked")
    protected SimplQuery(String table, String[] columns, Filter filter, Class<? extends TableDef>... tables) {
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
     * Wraps {@link #exec(SQLiteDatabase, Filter)} for convenience.
     *
     * @param db     to query
     * @param filter to apply
     * @return a cursor positioned before the first entry
     * @see SQLiteDatabase#query(String, String[], String, String[], String, String, String, String)
     */
    public Cursor exec(SimplDb db, Filter filter) {
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
    public Cursor exec(SQLiteDatabase db, Filter filter) {
        return db.query(mTable, mColumns, mFilter.getSelection(filter), mFilter.getSelectionArgs(filter),
                mFilter.getGroupBy(filter), mFilter.getHaving(filter), mFilter.getOrderBy(filter),
                mFilter.getLimit(filter));
    }

    /**
     * {@code Callback} for notification of a finished asynchronous query.
     */
    public interface Callback {
        /**
         * @param cursor   returned for {@code queryDef} with applied {@code filter}
         * @param queryDef executed
         * @param filter   applied
         * @param db       queried
         */
        void onQueryFinished(Cursor cursor, Class<? extends QueryDef> queryDef, Filter filter, SimplDb db);
    }

    /**
     * {@code Filter} is used to configure and apply a filter to a query.
     *
     * @see SQLiteDatabase#query(String, String[], String, String[], String, String, String, String)
     */
    public static class Filter {
        /**
         * Identifies this filter instance.
         */
        public final int id;

        private String mSelection, mGroupBy, mHaving, mOrderBy, mLimit;
        private String[] mSelectionArgs;

        /**
         * Creates a new {@code Filter} with id 0.
         */
        public Filter() {
            this(0);
        }

        /**
         * @param id of the filter for identification
         */
        public Filter(int id) {
            this.id = id;
        }

        void set(Filter filter) {
            if (filter != null) {
                mSelection = filter.mSelection;
                mSelectionArgs = filter.mSelectionArgs;
                mGroupBy = filter.mGroupBy;
                mHaving = filter.mHaving;
                mOrderBy = filter.mOrderBy;
                mLimit = filter.mLimit;
            }
        }

        private static String getNullString(String string) {
            return string.length() > 0 ? string : null;
        }

        /**
         * @param selection     or {@code null}
         * @param selectionArgs or {@code null}
         */
        public void setSelection(String selection, String... selectionArgs) {
            mSelection = getNullString(selection);
            mSelectionArgs = selectionArgs.length > 0 ? selectionArgs : null;
        }

        /**
         * @param groupBy or {@code null}
         */
        public void setGroupBy(String groupBy) {
            mGroupBy = getNullString(groupBy);
        }

        /**
         * @param orderBy or {@code null}
         */
        public void setOrderBy(String orderBy) {
            mOrderBy = getNullString(orderBy);
        }

        /**
         * @param having or {@code null}
         */
        public void setHaving(String having) {
            mHaving = getNullString(having);
        }

        /**
         * @param limit or {@code null}
         */
        public void setLimit(int limit) {
            mLimit = limit > 0 ? Integer.toString(limit) : null;
        }

        private static String getNullString(String string1, String string2) {
            return string1 != null ? string1 : string2;
        }

        String getSelection(Filter filter) {
            if (filter == null)
                return mSelection;

            return getNullString(filter.getSelection(null), mSelection);
        }

        String[] getSelectionArgs(Filter filter) {
            if (getSelection(filter) == null)
                return null;

            String[] selectionArgs = null;
            if (filter != null)
                selectionArgs = filter.getSelectionArgs(null);

            if (selectionArgs == null)
                return mSelectionArgs;

            return selectionArgs;
        }

        String getGroupBy(Filter filter) {
            if (filter == null)
                return mGroupBy;

            return getNullString(filter.getGroupBy(null), mGroupBy);
        }

        String getOrderBy(Filter filter) {
            if (filter == null)
                return mOrderBy;

            return getNullString(filter.getOrderBy(null), mOrderBy);
        }

        String getHaving(Filter filter) {
            if (filter == null)
                return mHaving;

            return getNullString(filter.getHaving(null), mHaving);
        }

        String getLimit(Filter filter) {
            if (filter == null)
                return mLimit;

            return getNullString(filter.getLimit(null), mLimit);
        }
    }
}
