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

package simpl.db.rx;

import android.database.Cursor;
import android.os.Looper;

import org.junit.Rule;
import org.junit.Test;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.subjects.ReplaySubject;
import simpl.db.rx.db.DatabaseTest;
import simpl.db.rx.db.TableTest;
import simpl.db.test.rules.DeleteTestRule;
import simpl.db.test.rules.InsertTestRule;
import simpl.db.test.rules.SimplDbTestRule;
import simpl.db.test.rules.UpdateTestRule;

import static org.junit.Assert.assertEquals;
import static simpl.db.rx.db.TableTest.DATA;
import static simpl.db.rx.db.TableTest._ID;

public class SimplDbRxTest {

    @Rule
    public SimplDbTestRule<SimplDbRx> mSimplDb = new SimplDbTestRule<SimplDbRx>(DatabaseTest.class);

    @Rule
    public InsertTestRule mInsert = new InsertTestRule(TableTest.class);

    @Rule
    public UpdateTestRule mUpdate = new UpdateTestRule(TableTest.class, _ID + "=?");

    @Rule
    public DeleteTestRule mDelete = new DeleteTestRule(TableTest.class, _ID + "=?");

    @Test
    @SuppressWarnings("StatementWithEmptyBody")
    public void observeOnMainThread() throws Exception {
        final LooperHolder looper = new LooperHolder();
        mSimplDb.get().observe(TableTest.class).subscribe(new Action1<Cursor>() {
            @Override
            public void call(Cursor cursor) {
                looper.instance = Looper.myLooper();
                looper.isSet = true;
            }
        });
        while (!looper.isSet) ;
        assertEquals(Looper.getMainLooper(), looper.instance);
    }

    @Test
    public void insert() throws Exception {
        mInsert.contentValues.put(DATA, "Data");
        Observable<Long> insert = mSimplDb.get().insert(mInsert);
        long rowId = insert.toBlocking().single();
        assertEquals(1, rowId);
        rowId = insert.toBlocking().single();
        assertEquals(1, rowId);
    }

    @Test
    public void update() throws Exception {
        mInsert.contentValues.put(DATA, "Data");
        mUpdate.whereArgs[0] = mSimplDb.get().insert(mInsert).toBlocking().single().toString();
        mUpdate.contentValues.put(DATA, "data");
        Observable<Integer> update = mSimplDb.get().update(mUpdate);
        int rowCount = update.toBlocking().first();
        assertEquals(1, rowCount);
        mDelete.whereArgs[0] = mUpdate.whereArgs[0];
        mSimplDb.get().delete(mDelete).toBlocking().single();
        rowCount = update.toBlocking().first();
        assertEquals(1, rowCount);
    }

    @Test
    public void delete() throws Exception {
        mInsert.contentValues.put(DATA, "Data");
        mDelete.whereArgs[0] = mSimplDb.get().insert(mInsert).toBlocking().single().toString();
        Observable<Integer> delete = mSimplDb.get().delete(mDelete);
        int rowCount = delete.toBlocking().first();
        assertEquals(1, rowCount);
        rowCount = delete.toBlocking().first();
        assertEquals(1, rowCount);
    }

    @Test
    public void query() throws Exception {
        mInsert.contentValues.put(DATA, "Data");
        mSimplDb.get().insert(mInsert).toBlocking().single();
        Cursor cursor = mSimplDb.get().query(TableTest.class).toBlocking().single();
        cursor.moveToLast();
        assertData("Data", cursor);
        mInsert.contentValues.put(DATA, "data");
        mSimplDb.get().insert(mInsert).toBlocking().single();
        cursor = mSimplDb.get().query(TableTest.class).toBlocking().single();
        cursor.moveToLast();
        assertData("data", cursor);
    }

    @Test
    public void observe() throws Exception {
        ReplaySubject<Cursor> collector = ReplaySubject.create();
        Subscription subscription = mSimplDb.get().observe(TableTest.class).subscribe(collector);
        mInsert.contentValues.put(DATA, "Data");
        mSimplDb.get().insert(mInsert).toBlocking().single();
        mInsert.contentValues.put(DATA, "data");
        mSimplDb.get().insert(mInsert).toBlocking().single();
        collector.onCompleted();
        subscription.unsubscribe();

        Cursor cursor = collector.toList().toBlocking().single().get(collector.size() - 1);
        assertEquals(2, cursor.getCount());
        cursor.moveToFirst();
        assertData("Data", cursor);
        cursor.moveToLast();
        assertData("data", cursor);
    }

    private static void assertData(String expected, Cursor data) {
        String actual = data.getString(data.getColumnIndex(DATA));
        assertEquals(expected, actual);
    }

    static class LooperHolder {
        boolean isSet = false;
        Looper instance = null;
    }
}
