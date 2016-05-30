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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import simpl.db.api.Check;
import simpl.db.api.Collate;
import simpl.db.api.Column;
import simpl.db.api.Database;
import simpl.db.api.Default;
import simpl.db.api.ForeignKey;
import simpl.db.api.NotNull;
import simpl.db.api.PrimaryKey;
import simpl.db.api.QueryDef;
import simpl.db.api.SimplDef;
import simpl.db.api.Table;
import simpl.db.api.TableDef;
import simpl.db.api.Unique;
import simpl.db.api.WithoutRowid;
import simpl.db.internal.SimplName;
import simpl.db.spec.DatabaseSpec;
import simpl.db.spec.TableSpec;

/**
 * {@code SimplDb} is the base class for all simplDb databases.
 * <p>
 * Implementations must also be annotated with {@link Database}.
 * </p>
 * <p>
 * <b>Note:</b>
 * {@code SimplDb} itself is not synchronized and should better be used as a singleton.
 * </p>
 */
@SuppressWarnings("unused")
public abstract class SimplDb implements SimplDef {
    static final String TAG = "simplDb#" + BuildConfig.VERSION_CODE;
    private static final String MSG_FORMAT = "Column %1$s in %2$s must match " +
            "'public static final String %1$s = \"%3$s\";'";

    private static final String DATABASE_SPEC = "$$DatabaseSpec";
    private static final String TABLE_SPEC = "$$TableSpec";

    private static final HashMap<Class<? extends SimplDb>, DatabaseSpec> D = new HashMap<>();
    private static final HashMap<Class<? extends SimplDef>, String> S = new HashMap<>();
    private static final HashMap<Class<? extends TableDef>, TableSpec> T = new HashMap<>();

    private static final Handler uiHandler = new Handler(Looper.getMainLooper());
    private static Handler sQuitter, sWorker;
    private static final Runnable QUITTER = new Runnable() {
        @Override
        public void run() {
            Looper looper = Looper.myLooper();
            if (looper != null)
                looper.quit();
        }
    };

    private static final WeakHashMap<SimplDb, Boolean> sBlocksQuitter = new WeakHashMap<>();
    private static final HashMap<Class<? extends TableDef>, HashMap<Observer, HashSet<Class<? extends QueryDef>>>> sTableObservers = new HashMap<>();

    private final Context mContext;
    private final SQLiteOpenHelper mSQLiteOpenHelper;

    /**
     * Stores the name of the database.
     */
    public final String name;
    /**
     * Stores the version of the database.
     */
    public final int version;

    /**
     * Lists all tables of the database.
     */
    protected final List<Class<? extends TableDef>> mTableDefs;

    /**
     * @param context used to create the {@link SQLiteOpenHelper} instance
     */
    protected SimplDb(Context context) {
        DatabaseSpec spec = loadDatabaseSpec(getClass());

        name = getName(spec.name);
        version = spec.annotation.version();
        Log.v(TAG, name + ':' + version);

        mTableDefs = Collections.unmodifiableList(Arrays.asList(spec.annotation.tables()));

        mContext = context.getApplicationContext();
        mSQLiteOpenHelper = onCreateSQLiteOpenHelper(mContext);
        if (mSQLiteOpenHelper == null)
            throw new NullPointerException("onCreateSQLiteOpenHelper() must not return null");
    }

    private static synchronized DatabaseSpec loadDatabaseSpec(Class<? extends SimplDb> databaseDef) {
        DatabaseSpec spec = D.get(databaseDef);
        if (spec != null)
            return spec;

        String databaseSpecName = databaseDef.getName() + DATABASE_SPEC;
        try {
            spec = (DatabaseSpec) Class.forName(databaseSpecName).newInstance();
            if (D.put(databaseDef, spec) == null) {
                for (TableSpec tableSpec : spec.tableSpecs)
                    if (T.put(tableSpec.simplDef, tableSpec) == null)
                        S.put(tableSpec.simplDef, tableSpec.name);
                S.put(databaseDef, spec.name);
            }
            return spec;
        } catch (ClassCastException e) {
            throw new SimplError(databaseSpecName + " must extend " + DatabaseSpec.class);
        } catch (ClassNotFoundException e) {
            Log.i(TAG, databaseSpecName + " not found");
        } catch (Exception e) {
            Log.e(TAG, "error", e);
        }

        Database database = databaseDef.getAnnotation(Database.class);
        if (database == null)
            throw new SimplError(databaseDef, Database.class);

        spec = new DatabaseSpec(getSimpleName(databaseDef), database, databaseDef);
        D.put(databaseDef, spec);
        return spec;
    }

    static synchronized TableSpec loadTableSpec(Class<? extends TableDef> tableDef) {
        TableSpec spec = T.get(tableDef);
        if (spec != null)
            return spec;

        String tableSpecName = tableDef.getName() + TABLE_SPEC;
        try {
            spec = (TableSpec) Class.forName(tableSpecName).newInstance();
            if (T.put(tableDef, spec) == null)
                S.put(tableDef, spec.name);
            return spec;
        } catch (ClassCastException e) {
            throw new SimplError(tableSpecName + " must extend " + TableSpec.class);
        } catch (ClassNotFoundException e) {
            Log.i(TAG, tableSpecName + " not found");
        } catch (Exception e) {
            Log.e(TAG, "error", e);
        }

        Table table = tableDef.getAnnotation(Table.class);
        if (table == null)
            throw new SimplError(tableDef, Table.class);

        spec = new TableSpec(getName(tableDef), table, tableDef);
        loadConstraints(spec);
        for (Field field : tableDef.getFields())
            loadColumnSpec(field, spec);
        T.put(tableDef, spec);
        return spec;
    }

    private static void loadConstraints(TableSpec tableSpec) {
        Class<? extends TableDef> tableDef = tableSpec.simplDef;
        HashSet<Annotation> constraints = tableSpec.constraints;
        constraints.add(tableDef.getAnnotation(Check.class));
        constraints.add(tableDef.getAnnotation(ForeignKey.class));
        if (WithoutRowid.SUPPORTED)
            constraints.add(tableDef.getAnnotation(WithoutRowid.class));
        constraints.remove(null);
    }

    private static boolean loadColumnSpec(Field field, TableSpec tableSpec) {
        Column column = field.getAnnotation(Column.class);
        if (column == null)
            return false;

        String fieldName = field.getName();
        String name = getName(fieldName);
        if (isPublicStaticFinalString(field)) {
            try {
                if (name.equals(field.get(null))) {
                    HashSet<Annotation> constraints = new HashSet<>();
                    constraints.add(field.getAnnotation(Column.class));
                    constraints.add(field.getAnnotation(PrimaryKey.class));
                    constraints.add(field.getAnnotation(NotNull.class));
                    constraints.add(field.getAnnotation(Unique.class));
                    constraints.add(field.getAnnotation(Check.class));
                    constraints.add(field.getAnnotation(Default.class));
                    constraints.add(field.getAnnotation(Collate.class));
                    constraints.add(field.getAnnotation(ForeignKey.class));
                    constraints.remove(null);
                    tableSpec.columnSpecs.put(name, constraints);
                    return true;
                }
            } catch (Exception e) {
                Log.d(SimplDb.TAG, "assert", e);
            }
        }

        throw new SimplError(String.format(Locale.UK, MSG_FORMAT, fieldName, tableSpec.name, name));
    }

	/* SQLiteOpenHelper wrapper */

    /**
     * @return the {@link SQLiteOpenHelper} implementation used
     */
    public final SQLiteOpenHelper getSQLiteOpenHelper() {
        return mSQLiteOpenHelper;
    }

    /**
     * @return a database object
     * @see SQLiteOpenHelper#getReadableDatabase()
     */
    public final SQLiteDatabase getReadableDatabase() {
        return mSQLiteOpenHelper.getReadableDatabase();
    }

    /**
     * @return a writable database object
     * @see SQLiteOpenHelper#getWritableDatabase()
     */
    public final SQLiteDatabase getWritableDatabase() {
        return mSQLiteOpenHelper.getWritableDatabase();
    }

    /**
     * Close any open database.
     *
     * @see SQLiteOpenHelper#close()
     */
    public final void close() {
        mSQLiteOpenHelper.close();
    }

    /**
     * Delete this database completely.
     *
     * @see SQLiteOpenHelper#close()
     * @see Context#deleteDatabase(String)
     */
    public final void delete() {
        mSQLiteOpenHelper.close();
        mContext.deleteDatabase(name + "-journal");
        mContext.deleteDatabase(name);
    }

	/* Thread handling */

    private static boolean isUiThread() {
        return Looper.getMainLooper().equals(Looper.myLooper());
    }

    private static boolean runOnWorkerThread(Runnable r) {
        if (sWorker != null && sWorker.post(r))
            return true;

        if (isUiThread() || Looper.myLooper() == null) {
            HandlerThread handlerThread = new HandlerThread(TAG + ":worker");
            handlerThread.start();
            sQuitter = sWorker = new Handler(handlerThread.getLooper());
        } else {
            sWorker = new Handler();
        }

        return sWorker.post(r);
    }

    /**
     * Resumes this instance. Keeps the background worker alive until {@link #onPause()} is called.
     */
    public final void onResume() {
        sBlocksQuitter.put(this, true);
    }

    /**
     * Pauses this instance and closes any open database.
     * Also stops the background worker if {@link #onResume()} wasn't called by another instance before.
     */
    public final void onPause() {
        sBlocksQuitter.remove(this);
        if (sQuitter != null && sBlocksQuitter.isEmpty()) {
            Handler handler = sQuitter;
            if (sQuitter.getLooper() == sWorker.getLooper())
                sQuitter = sWorker = null;
            else
                sQuitter = null;
            handler.post(QUITTER);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        onPause();
        close();
        super.finalize();
    }

	/* Table update handling */

    /**
     * Registers a {@link Observer} to be notified of any changes made to this database through this {@code SimplDb}.
     * The {@code tableObserver} gets registered to any table of {@code queryDef}.
     *
     * @param tableObserver to notify of changes
     * @param queryDef      to be used in {@link Observer#onTableChanged(Class, SimplDb)}
     * @see #unregisterTableObserver(Observer, Class)
     */
    public final void registerTableObserver(Observer tableObserver, Class<? extends QueryDef> queryDef) {
        synchronized (sTableObservers) {
            SimplQuery query = SimplQuery.get(queryDef);

            HashMap<Observer, HashSet<Class<? extends QueryDef>>> observers;
            HashSet<Class<? extends QueryDef>> queries;
            for (Class<? extends TableDef> tableDef : query.getTables()) {
                observers = sTableObservers.get(tableDef);

                if (observers == null) {
                    observers = new HashMap<>();
                    sTableObservers.put(tableDef, observers);
                    queries = null;
                } else {
                    queries = observers.get(tableObserver);
                }

                if (queries == null) {
                    queries = new HashSet<>();
                    observers.put(tableObserver, queries);
                }

                queries.add(queryDef);
            }
        }
    }

    /**
     * Unregisters a {@link Observer} from being notified of changes.
     * The {@code tableObserver} gets unregistered from any table of {@code queryDef}.
     *
     * @param tableObserver to remove
     * @param queryDef      to remove the {@code tableObserver} from
     * @see #registerTableObserver(Observer, Class)
     * @see #unregisterTableObserver(Observer)
     */
    public final void unregisterTableObserver(Observer tableObserver, Class<? extends QueryDef> queryDef) {
        synchronized (sTableObservers) {
            SimplQuery query = SimplQuery.get(queryDef);

            HashMap<Observer, HashSet<Class<? extends QueryDef>>> observers;
            HashSet<Class<? extends QueryDef>> queries;
            for (Class<? extends TableDef> tableDef : query.getTables()) {
                observers = sTableObservers.get(tableDef);

                if (observers != null) {
                    queries = observers.get(tableObserver);
                    queries.remove(queryDef);

                    if (queries.isEmpty())
                        observers.remove(tableObserver);
                }
            }
        }
    }

    /**
     * Unregisters a {@link Observer} from being notified of changes.
     * The {@code tableObserver} gets unregistered from all tables.
     *
     * @param tableObserver to remove
     * @see #registerTableObserver(Observer, Class)
     * @see #unregisterTableObserver(Observer, Class)
     */
    public final void unregisterTableObserver(Observer tableObserver) {
        synchronized (sTableObservers) {
            for (HashMap<Observer, ?> observers : sTableObservers.values())
                observers.remove(tableObserver);
        }
    }

    private void sendTableChanged(Map<Observer, HashSet<Class<? extends QueryDef>>> observers) {
        if (observers != null)
            for (Entry<Observer, HashSet<Class<? extends QueryDef>>> entry : observers.entrySet()) {
                Observer tableObserver = entry.getKey();

                for (Class<? extends QueryDef> queryDef : entry.getValue())
                    tableObserver.onTableChanged(queryDef, this);
            }
    }

    private void sendTableChanged(Class<? extends TableDef> tableDef) {
        synchronized (sTableObservers) {
            sendTableChanged(sTableObservers.get(tableDef));
        }
    }

    private void sendTableChanged(Collection<Class<? extends TableDef>> tableDefs) {
        WeakHashMap<Observer, HashSet<Class<? extends QueryDef>>> observers = new WeakHashMap<>();

        synchronized (sTableObservers) {
            HashMap<Observer, HashSet<Class<? extends QueryDef>>> tableObservers;
            for (Class<? extends TableDef> tableDef : tableDefs) {
                tableObservers = sTableObservers.get(tableDef);

                if (tableObservers != null)
                    for (Entry<Observer, HashSet<Class<? extends QueryDef>>> entry : tableObservers.entrySet()) {
                        Observer tableObserver = entry.getKey();

                        HashSet<Class<? extends QueryDef>> queries = observers.get(tableObserver);
                        if (queries == null) {
                            queries = new HashSet<>();
                            observers.put(tableObserver, queries);
                        }

                        for (Class<? extends QueryDef> queryDef : entry.getValue())
                            queries.add(queryDef);
                    }
            }
        }

        sendTableChanged(observers);
    }

    /**
     * {@code Observer} for changes to a table of the databases.
     */
    public interface Observer {
        /**
         * @param queryDef to execute
         * @param db       which changed
         */
        void onTableChanged(Class<? extends QueryDef> queryDef, SimplDb db);
    }

	/* Database interaction */

    /**
     * @param queryDef to execute
     * @param filter   to use with the query
     * @param callback to notify
     */
    public void query(final Class<? extends QueryDef> queryDef, final SimplQuery.Filter filter, final SimplQuery.Callback callback) {
        if (isUiThread()) {
            runOnWorkerThread(new Runnable() {
                @Override
                public void run() {
                    query(queryDef, filter, callback);
                }
            });
        } else {
            final Cursor cursor = SimplQuery.get(queryDef).exec(mSQLiteOpenHelper.getReadableDatabase(), filter);
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    callback.onQueryFinished(cursor, queryDef, filter, SimplDb.this);
                }
            });
        }
    }

    private long insert(Insert insert) {
        return insert(insert.tableDef, insert.contentValues);
    }

    private synchronized long insert(Class<? extends TableDef> tableDef, ContentValues contentValues) {
        return mSQLiteOpenHelper.getWritableDatabase().insert(getName(tableDef), null, contentValues);
    }

    /**
     * @param tableDef      to operate on
     * @param contentValues to insert
     * @param callback      to notify
     */
    public void insert(Class<? extends TableDef> tableDef, ContentValues contentValues, Insert.Callback callback) {
        insert(new Insert(tableDef, contentValues), callback);
    }

    /**
     * @param insert   to perform
     * @param callback to notify
     */
    public void insert(final Insert insert, final Insert.Callback callback) {
        if (isUiThread()) {
            runOnWorkerThread(new Runnable() {
                @Override
                public void run() {
                    insert(insert, callback);
                }
            });
        } else {
            final long rowId = insert(insert.tableDef, insert.contentValues);

            if (rowId >= 0)
                sendTableChanged(insert.tableDef);

            if (callback != null)
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onInsertFinished(rowId, insert, SimplDb.this);
                    }
                });
        }
    }

    /**
     * @param inserts  to perform
     * @param callback to notify
     */
    public void insert(final Collection<Insert> inserts, final Insert.Callback callback) {
        if (inserts.size() > 0)
            if (isUiThread()) {
                runOnWorkerThread(new Runnable() {
                    @Override
                    public void run() {
                        insert(inserts, callback);
                    }
                });
            } else {
                HashSet<Class<? extends TableDef>> updated = new HashSet<>();

                for (final Insert insert : inserts) {
                    final long rowId = insert(insert);

                    if (rowId >= 0)
                        updated.add(insert.tableDef);

                    if (callback != null)
                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onInsertFinished(rowId, insert, SimplDb.this);
                            }
                        });
                }

                sendTableChanged(updated);
            }
    }

    /**
     * Simple wrapper around values to insert and a table to insert into.
     */
    public static class Insert {
        /**
         * The table to stare the values in.
         */
        public final Class<? extends TableDef> tableDef;
        /**
         * The values to insert into the table.
         */
        public final ContentValues contentValues = new ContentValues();

        /**
         * @param tableDef      to operate on
         * @param contentValues to insert
         */
        public Insert(Class<? extends TableDef> tableDef, ContentValues contentValues) {
            this.tableDef = tableDef;
            if (contentValues != null)
                this.contentValues.putAll(contentValues);
        }

        /**
         * A {@code Callback} to be notified after the insert.
         */
        public interface Callback {
            /**
             * @param rowId  of the inserted row or -1 on error
             * @param insert which was inserted
             * @param db     to insert into
             */
            void onInsertFinished(long rowId, Insert insert, SimplDb db);
        }
    }

    private int update(Update update) {
        return update(update.tableDef, update.contentValues, update.whereClause, update.whereArgs);
    }

    private synchronized int update(Class<? extends TableDef> tableDef, ContentValues contentValues, String whereClause, String[] whereArgs) {
        try {
            return mSQLiteOpenHelper.getWritableDatabase()
                    .updateWithOnConflict(getName(tableDef), contentValues, whereClause, whereArgs, SQLiteDatabase.CONFLICT_ROLLBACK);
        } catch (SQLException e) {
            Log.e("SQLiteDatabase", "Error updating " + contentValues + " where " + whereClause + " " + Arrays.toString(whereArgs), e);
            return 0;
        }
    }

    /**
     * @param tableDef      to operate on
     * @param contentValues to update
     * @param whereClause   of where to update
     * @param whereArgs     to fill the ?s of {@code whereClause}
     * @param callback      to notify
     */
    public void update(Class<? extends TableDef> tableDef, ContentValues contentValues, String whereClause, String[] whereArgs, Update.Callback callback) {
        update(new Update(tableDef, contentValues, whereClause, whereArgs), callback);
    }

    /**
     * @param update   to perform
     * @param callback to notify
     */
    public void update(final Update update, final Update.Callback callback) {
        if (isUiThread()) {
            runOnWorkerThread(new Runnable() {
                @Override
                public void run() {
                    update(update, callback);
                }
            });
        } else {
            final int rowCount = update(update.tableDef, update.contentValues, update.whereClause, update.whereArgs);

            if (rowCount > 0)
                sendTableChanged(update.tableDef);

            if (callback != null)
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onUpdateFinished(rowCount, update, SimplDb.this);
                    }
                });
        }
    }

    /**
     * @param updates  to perform
     * @param callback to notify
     */
    public void update(final Collection<Update> updates, final Update.Callback callback) {
        if (updates.size() > 0)
            if (isUiThread()) {
                runOnWorkerThread(new Runnable() {
                    @Override
                    public void run() {
                        update(updates, callback);
                    }
                });
            } else {
                HashSet<Class<? extends TableDef>> updated = new HashSet<>();

                for (final Update update : updates) {
                    final int rowCount = update(update);

                    if (rowCount >= 0)
                        updated.add(update.tableDef);

                    if (callback != null)
                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onUpdateFinished(rowCount, update, SimplDb.this);
                            }
                        });
                }

                sendTableChanged(updated);
            }
    }

    /**
     * Simple wrapper around values to update and a table to update in.
     */
    public static class Update extends Insert {
        public final String whereClause;
        public final String[] whereArgs;

        /**
         * @param tableDef      to operate on
         * @param contentValues to update
         */
        public Update(Class<? extends TableDef> tableDef, ContentValues contentValues, String whereClause, String... whereArgs) {
            super(tableDef, contentValues);
            this.whereClause = whereClause;
            this.whereArgs = whereArgs;
        }

        /**
         * A {@code Callback} to be notified after the update.
         */
        public interface Callback {
            /**
             * @param rowCount of rows updated
             * @param update   which was applied
             * @param db       to insert into
             */
            void onUpdateFinished(int rowCount, Update update, SimplDb db);
        }
    }

    /**
     * @param tableDef to operate on
     * @param id       of the row
     */
    public void delete(final Class<? extends TableDef> tableDef, final long id) {
        if (isUiThread())
            runOnWorkerThread(new Runnable() {
                @Override
                public void run() {
                    delete(tableDef, id);
                }
            });
        else if (delete(getName(tableDef), "_id=?", Long.toString(id)))
            sendTableChanged(tableDef);
    }

    private boolean delete(String table, String whereClause, String... whereArgs) {
        return mSQLiteOpenHelper.getWritableDatabase().delete(table, whereClause, whereArgs) > 0;
    }

	/* SQLiteOpenHelper handling */

    /**
     * Creates the new {@link SQLiteOpenHelper} instance this database is using.
     * <p>
     * By default this returns a new instance of {@link SQLiteOpenHelperImpl}.
     * </p>
     *
     * @param context of the application
     * @return a new {@code SQLiteOpenHelper} instance
     * @see #getSQLiteOpenHelper()
     */
    protected SQLiteOpenHelper onCreateSQLiteOpenHelper(Context context) {
        return new SQLiteOpenHelperImpl(context);
    }

    /**
     * Creates a new {@link CursorFactory} instance to be used with
     * {@link SQLiteOpenHelper#SQLiteOpenHelper(Context, String, CursorFactory, int)}.
     * <p>
     * By default this returns {@code null}.
     * </p>
     *
     * @param context of the application
     * @return a new {@code CursorFactory} instance or {@code null}
     */
    protected CursorFactory onCreateCursorFactory(Context context) {
        return null;
    }

    /**
     * Gets called at the end of {@link SQLiteOpenHelperImpl#onConfigure(SQLiteDatabase)}.
     *
     * @param db to modify
     */
    protected void onConfigure(SQLiteDatabase db) {
    }

    /**
     * Gets called at the end of {@link SQLiteOpenHelperImpl#onCreate(SQLiteDatabase)}.
     *
     * @param db to modify
     */
    protected void onCreate(SQLiteDatabase db) {
    }

    /**
     * Gets called at the beginning of {@link SQLiteOpenHelperImpl#onUpgrade(SQLiteDatabase, int, int)}.
     *
     * @param db         to modify
     * @param oldVersion of the database
     * @param newVersion of the database
     */
    protected void beforeUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    /**
     * Gets called at the end of {@link SQLiteOpenHelperImpl#onUpgrade(SQLiteDatabase, int, int)}.
     *
     * @param db         to modify
     * @param oldVersion of the database
     * @param newVersion of the database
     */
    protected void afterUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    /**
     * Implementation of {@code SQLiteOpenHelper} handling creation and upgrade of database tables.
     *
     * @see #onCreateSQLiteOpenHelper(Context)
     */
    protected class SQLiteOpenHelperImpl extends SQLiteOpenHelper {
        private static final String COPY_FORMAT = "INSERT INTO _%1$s (%2$s) SELECT %2$s FROM %1$s";

        private final TableSql mTable;

        /**
         * Creates a new {@code SQLiteOpenHelper} with {@link SimplDb#name} and {@link SimplDb#version}.
         *
         * @param context of the application
         * @see #onCreateCursorFactory(Context)
         */
        protected SQLiteOpenHelperImpl(Context context) {
            super(context.getApplicationContext(), name, onCreateCursorFactory(context), version);
            mTable = new TableSql();
        }

        /**
         * Configures this database to use foreign keys.
         *
         * @param db to configure
         */
        @Override
        public void onConfigure(SQLiteDatabase db) {
            if (!db.isReadOnly())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    db.setForeignKeyConstraintsEnabled(true);
                else
                    db.execSQL("PRAGMA foreign_keys=ON;");

            SimplDb.this.onConfigure(db);
        }

        /**
         * Creates all tables defined for this database.
         *
         * @param db to create
         * @see Database#tables()
         * @see SimplDb#onCreate(SQLiteDatabase)
         */
        @Override
        public final void onCreate(SQLiteDatabase db) {
            for (Class<? extends TableDef> tableDef : mTableDefs)
                if (tableDef != null)
                    onCreateTable(db, tableDef);

            SimplDb.this.onCreate(db);
        }

        /**
         * Upgrades the current tables of the database, creates new tables and drops old tables.
         * The "sqlite_sequence" table is ignored.
         *
         * @param db         to upgrade
         * @param oldVersion of database
         * @param newVersion of database
         * @see SimplDb#beforeUpgrade(SQLiteDatabase, int, int)
         * @see SimplDb#afterUpgrade(SQLiteDatabase, int, int)
         * @see #onUpgradeTable(SQLiteDatabase, Class)
         * @see #onCreateTable(SQLiteDatabase, Class)
         * @see #onDropTable(SQLiteDatabase, String)
         */
        @Override
        public final void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            SimplDb.this.beforeUpgrade(db, oldVersion, newVersion);

            HashSet<String> tables = new HashSet<>(getTables(db));

            for (Class<? extends TableDef> tableDef : mTableDefs)
                if (tableDef != null)
                    if (tables.remove(getName(tableDef)))
                        onUpgradeTable(db, tableDef);
                    else
                        onCreateTable(db, tableDef);

            for (String table : tables)
                if (!"sqlite_sequence".equals(table))
                    onDropTable(db, table);

            SimplDb.this.afterUpgrade(db, oldVersion, newVersion);
        }

        /**
         * Creates a new table.
         *
         * @param db       to modify
         * @param tableDef to create
         * @see #onCreate(SQLiteDatabase)
         * @see #onUpgrade(SQLiteDatabase, int, int)
         */
        protected void onCreateTable(SQLiteDatabase db, Class<? extends TableDef> tableDef) {
            db.execSQL(mTable.build(loadTableSpec(tableDef), false));
        }

        /**
         * Upgrades an old table to a new table.
         * <a href="https://www.sqlite.org/lang_altertable.html#otheralter">suggestion</a>
         *
         * @param db       to modify
         * @param tableDef to upgrade
         * @see #onUpgrade(SQLiteDatabase, int, int)
         */
        protected void onUpgradeTable(SQLiteDatabase db, Class<? extends TableDef> tableDef) {
            db.execSQL(mTable.build(loadTableSpec(tableDef), true));

            String table = mTable.getName();
            HashSet<String> columns = new HashSet<>(getColumns(db, table));
            columns.retainAll(mTable.getColumns());
            if (columns.size() > 0) {
                StringBuilder sb = new StringBuilder();
                for (String column : columns)
                    sb.append(',').append('"').append(column).append('"');
                db.execSQL(String.format(Locale.UK, COPY_FORMAT, table, sb.substring(1)));
            }

            db.execSQL("DROP TABLE " + table);
            db.execSQL("ALTER TABLE _" + table + " RENAME TO " + table);
        }

        /**
         * Drops an old tables not used anymore.
         *
         * @param db    to modify
         * @param table name to drop
         * @see #onUpgrade(SQLiteDatabase, int, int)
         */
        protected void onDropTable(SQLiteDatabase db, String table) {
            db.execSQL("DROP TABLE " + table);
        }
    }

	/* Local utilities */

    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    static Collection<String> getTables(SQLiteDatabase db) {
        String[] columns = {"name"};
        Cursor cursor = db.query("sqlite_master", columns, "type='table'", null, null, null, null);
        try {
            ArrayList<String> tables = new ArrayList<>(cursor.getCount());
            while (cursor.moveToNext())
                tables.add(cursor.getString(0));
            return tables;
        } finally {
            cursor.close();
        }
    }

    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    static Collection<String> getColumns(SQLiteDatabase db, String table) {
        Cursor cursor = db.query(table, null, null, null, null, null, null, "0");
        try {
            return Arrays.asList(cursor.getColumnNames());
        } finally {
            cursor.close();
        }
    }

	/* Utilities */

    /**
     * Quotes an SQLite keywords to be used as column name in any of {@link ContentValues#put}.
     *
     * @param value to quote with {@code "}
     * @return the quoted string
     */
    public static String quote(String value) {
        return SimplName.quote(value);
    }

    /**
     * Creates an internal name for any given string.
     * This is a snake case representation of the string itself.
     *
     * @param name string
     * @return the internal name for {@code name}
     */
    public static String getName(String name) {
        return SimplName.from(name);
    }

    /**
     * Creates the internal name for a {@link SimplDef} subclass.
     *
     * @param simplDef implemented simplDb type
     * @return the internal name of the type defined by {@code simplDef}
     * @see #getName(String)
     */
    public static String getName(Class<? extends SimplDef> simplDef) {
        return getName(getSimpleName(simplDef));
    }

    private static String getSimpleName(Class<? extends SimplDef> simplDef) {
        String simpleName = S.get(simplDef);
        if (simpleName == null)
            S.put(simplDef, simpleName = simplDef.getSimpleName());
        return simpleName;
    }

    private static boolean isPublicStaticFinalString(Field field) {
        int modifiers = field.getModifiers();
        return Modifier.isPublic(modifiers) && Modifier.isFinal(modifiers) &&
                Modifier.isStatic(modifiers) && field.getType() == String.class;
    }
}
