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

import android.content.Context;
import android.database.Cursor;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.functions.Action0;
import rx.schedulers.Schedulers;
import rx.subjects.AsyncSubject;
import rx.subscriptions.Subscriptions;
import simpl.db.SimplDb;
import simpl.db.SimplQuery;
import simpl.db.api.QueryDef;

/**
 * A RxJava extension to SimplDb using a background Scheduler to perform the database operations on.
 */
public class SimplDbRx extends SimplDb {
    private final Scheduler mScheduler;

    /**
     * Using the {@link Schedulers#io()} scheduler.
     *
     * @param context passed to {@link SimplDb#SimplDb(Context)}
     */
    protected SimplDbRx(Context context) {
        this(context, Schedulers.io());
    }

    /**
     * Using a given scheduler to perform database operations on.
     * This should not be the main thread scheduler.
     *
     * @param context   passed to {@link SimplDb#SimplDb(Context)}
     * @param scheduler to subscribe on
     */
    protected SimplDbRx(Context context, Scheduler scheduler) {
        super(context);
        mScheduler = scheduler;
    }

    /**
     * @param insert to use for insertion
     * @return a hot Observable emitting the rowId
     * @see SimplDb.Insert.Callback#onInsertFinished(long, Insert, SimplDb)
     */
    public Observable<Long> insert(final Insert insert) {
        AsyncSubject<Long> subject = AsyncSubject.create();
        Observable.create(new Observable.OnSubscribe<Long>() {
            @Override
            public void call(final Subscriber<? super Long> subscriber) {
                insert(insert, new SimplDb.Insert.Callback() {
                    @Override
                    public void onInsertFinished(long rowId, Insert insert, SimplDb db) {
                        subscriber.onNext(rowId);
                        subscriber.onCompleted();
                    }
                });
            }
        }).subscribeOn(mScheduler).subscribe(subject);
        return subject;
    }

    /**
     * @param update to use for update
     * @return a hot Observable emitting the count of changed rows
     * @see SimplDb.Update.Callback#onUpdateFinished(int, Update, SimplDb)
     */
    public Observable<Integer> update(final Update update) {
        AsyncSubject<Integer> subject = AsyncSubject.create();
        Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(final Subscriber<? super Integer> subscriber) {
                update(update, new Update.Callback() {
                    @Override
                    public void onUpdateFinished(int rowCount, Update update, SimplDb db) {
                        subscriber.onNext(rowCount);
                        subscriber.onCompleted();
                    }
                });
            }
        }).subscribeOn(mScheduler).subscribe(subject);
        return subject;
    }

    /**
     * @param delete to use for deletion
     * @return a hot Observable emitting the count of deleted rows
     * @see SimplDb.Delete.Callback#onDeleteFinished(int, Delete, SimplDb)
     */
    public Observable<Integer> delete(final Delete delete) {
        AsyncSubject<Integer> subject = AsyncSubject.create();
        Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(final Subscriber<? super Integer> subscriber) {
                delete(delete, new Delete.Callback() {
                    @Override
                    public void onDeleteFinished(int rowCount, Delete delete, SimplDb db) {
                        subscriber.onNext(rowCount);
                        subscriber.onCompleted();
                    }
                });
            }
        }).subscribeOn(mScheduler).subscribe(subject);
        return subject;
    }

    /**
     * @param queryDef to query
     * @return a cold Observable emitting the result of the query
     */
    public Observable<Cursor> query(Class<? extends QueryDef> queryDef) {
        return query(queryDef, null);
    }

    /**
     * @param queryDef to query
     * @param filter   to apply
     * @return a cold Observable emitting the result of the query
     */
    public Observable<Cursor> query(final Class<? extends QueryDef> queryDef, final SimplQuery.Filter filter) {
        return Observable.create(new Observable.OnSubscribe<Cursor>() {
            @Override
            public void call(final Subscriber<? super Cursor> subscriber) {
                query(queryDef, filter, new SimplQuery.Callback() {
                    @Override
                    public void onQueryFinished(Cursor cursor, Class<? extends QueryDef> queryDef, SimplQuery.Filter filter, SimplDb db) {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(cursor);
                            subscriber.onCompleted();
                        }
                    }
                });
            }
        }).subscribeOn(mScheduler);
    }

    /**
     * Observe changes of a table with a given query.
     * <p>
     * This registers a {@link SimplDb.Observer} for every Subscriber.
     * Unregistering happens together with the unsubscription.
     * </p>
     *
     * @param queryDef to observe
     * @return a cold Observable emitting every observed Cursor
     */
    public Observable<Cursor> observe(Class<? extends QueryDef> queryDef) {
        return observe(queryDef, null);
    }

    /**
     * Observe changes of a table with a given query.
     * <p>
     * This registers a {@link SimplDb.Observer} for every Subscriber.
     * Unregistering happens together with the unsubscription.
     * </p>
     *
     * @param queryDef to observe
     * @param filter   to apply
     * @return a cold Observable emitting every observed Cursor
     */
    public Observable<Cursor> observe(final Class<? extends QueryDef> queryDef, final SimplQuery.Filter filter) {
        return Observable.create(new Observable.OnSubscribe<Cursor>() {
            @Override
            public void call(final Subscriber<? super Cursor> subscriber) {
                final SimplQuery.Callback callback = new SimplQuery.Callback() {
                    @Override
                    public void onQueryFinished(Cursor cursor, Class<? extends QueryDef> queryDef, SimplQuery.Filter filter, SimplDb db) {
                        if (!subscriber.isUnsubscribed())
                            subscriber.onNext(cursor);
                    }
                };
                query(queryDef, filter, callback);

                final Observer observer = new Observer() {
                    @Override
                    public void onTableChanged(Class<? extends QueryDef> queryDef, SimplDb db) {
                        if (!subscriber.isUnsubscribed())
                            query(queryDef, filter, callback);
                    }
                };
                registerTableObserver(observer, queryDef);

                subscriber.add(Subscriptions.create(new Action0() {
                    @Override
                    public void call() {
                        unregisterTableObserver(observer);
                    }
                }));
            }
        }).subscribeOn(mScheduler);
    }
}
