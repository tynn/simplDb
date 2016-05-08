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

package simpl.db.min;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import simpl.db.SimplDb;
import simpl.db.query.QueryLoader;
import simpl.db.SimplQuery;

public class MainActivity extends Activity implements SimplDb.Insert.Callback {
    static final int LIST_VIEW_ID = android.R.id.list;
    private static final int LIST_ITEM_LAYOUT = android.R.layout.simple_list_item_1;

    private SimplDb mDb;
    private ArrayAdapter<String> mAdapter;
    private ListView mList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDb = new ExamplDb(this);
        ContentValues values = new ContentValues();
        values.put(ExamplTable.NAME, mDb.name);
        values.put(ExamplTable.VALUE, mDb.version);
        mDb.insert(ExamplTable.class, values, this);

        mAdapter = new ArrayAdapter<>(this, LIST_ITEM_LAYOUT);
        mAdapter.setNotifyOnChange(false);
        mAdapter.add(mDb.name);
        mAdapter.add(" ↓ " + SimplDb.getName(ExamplTable.class));

        mList = new ListView(this);
        mList.setId(LIST_VIEW_ID);
        mList.setDividerHeight(0);
        mList.setAdapter(mAdapter);
        setContentView(mList);
    }

    @Override
    public void onInsertFinished(long rowId, SimplDb.Insert insert, SimplDb db) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
            queryWithAsyncTask();
        else
            queryWithLoader();
    }

    private void queryWithAsyncTask() {
        new AsyncTask<SimplDb, Void, Cursor>() {
            @Override
            protected Cursor doInBackground(SimplDb... db) {
                return SimplQuery.get(ExamplTable.class).exec(db[0], null);
            }

            @Override
            protected void onPostExecute(Cursor cursor) {
                addColumnsToAdapter(cursor);
                cursor.close();
            }
        }.execute(mDb);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void queryWithLoader() {
        final SimplDb db = mDb;
        getLoaderManager().initLoader(0, null, new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                return new QueryLoader(MainActivity.this, ExamplTable.class, db);
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
                addColumnsToAdapter(cursor);
                getLoaderManager().destroyLoader(0);
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
            }
        });
    }

    void addColumnsToAdapter(Cursor cursor) {
        mAdapter.setNotifyOnChange(false);
        for (String column : cursor.getColumnNames())
            mAdapter.add(" → " + column);
        mAdapter.notifyDataSetChanged();
    }
}
