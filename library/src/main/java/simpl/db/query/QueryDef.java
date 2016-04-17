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

import simpl.db.SimplDb;
import simpl.db.SimplDef;
import simpl.db.table.TableDef;

/**
 * {@code QueryDef} is the base class for all simplDb queries.
 * <p>
 * Implementations must also be annotated with {@link Query} or implement {@link TableDef}.
 * If the {@code Query} annotation is not used, the query returns all columns and rows of the table.
 * </p>
 *
 * @see SimplQuery#get(Class)
 */
public interface QueryDef extends SimplDef {
    /**
     * {@code Filter} is used to configure and apply a filter to a query.
     *
     * @see SQLiteDatabase#query(String, String[], String, String[], String, String, String, String)
     */
    class Filter {
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

    /**
     * {@code Callback} for notification of a finished asynchronous query.
     */
    interface Callback {
        /**
         * @param cursor   returned for {@code queryDef} with applied {@code filter}
         * @param queryDef executed
         * @param filter   applied
         * @param db       queried
         */
        void onQueryFinished(Cursor cursor, Class<? extends QueryDef> queryDef, Filter filter, SimplDb db);
    }
}
