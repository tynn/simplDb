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

package simpl.db.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.os.Build;

import simpl.db.SimplDb;
import simpl.db.SimplQuery;
import simpl.db.api.QueryDef;

/**
 * {@code QueryLoader} implements a {@link Loader} for {@link Cursor} returned by a query.
 * <p>
 * This {@code Loader} is aware of changes made to the database through {@link SimplDb}
 * and automatically requeries the data. Loading is always done in a background thread.
 * </p>
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class QueryLoader extends Loader<Cursor> {
    private final Class<? extends QueryDef> mQueryDef;
    private final SimplQuery.Filter mFilter;
    private final SimplDb mDb;
    private Cursor mData;

    private final SimplQuery.Callback mQueryCallback = new SimplQuery.Callback() {
        @Override
        public void onQueryFinished(Cursor cursor, Class<? extends QueryDef> queryDef, SimplQuery.Filter filter, SimplDb db) {
            if (!isReset()) {
                setData(cursor);
                if (isStarted())
                    deliverResult(cursor);
            }
        }
    };

    private final SimplDb.Observer mTableObserver = new SimplDb.Observer() {
        @Override
        public void onTableChanged(Class<? extends QueryDef> queryDef, SimplDb db) {
            onContentChanged();
        }
    };

    /**
     * @param context  of the application
     * @param queryDef to execute
     * @param db       to use
     */
    public QueryLoader(Context context, Class<? extends QueryDef> queryDef, SimplDb db) {
        this(context, queryDef, null, db);
    }

    /**
     * @param context  of the application
     * @param queryDef to execute
     * @param filter   to apply
     * @param db       to use
     */
    public QueryLoader(Context context, Class<? extends QueryDef> queryDef, SimplQuery.Filter filter, SimplDb db) {
        super(context);
        mQueryDef = queryDef;
        mFilter = filter;
        mDb = db;
    }

    void setData(Cursor data) {
        if (mData != data) {
            if (mData != null)
                mData.close();
            mData = data;
        }
    }

    @Override
    protected void onStartLoading() {
        mDb.registerObserver(mTableObserver, mQueryDef);
        if (takeContentChanged() || mData == null || mData.isClosed())
            mDb.query(mQueryDef, mFilter, mQueryCallback);
        else
            deliverResult(mData);
    }

    @Override
    protected void onForceLoad() {
        mDb.query(mQueryDef, mFilter, mQueryCallback);
    }

    @Override
    protected void onStopLoading() {
        mDb.unregisterObserver(mTableObserver);
    }

    @Override
    protected void onAbandon() {
        mDb.unregisterObserver(mTableObserver);
    }

    @Override
    protected void onReset() {
        mDb.unregisterObserver(mTableObserver);
        setData(null);
    }
}
