# simplDb [![Release][1]][2]
###### Simplify SQLite on Android

SimplDb is an annotation based simple database creator and updater. For this
an implementation of `SQLiteOpenHelper` using [some strategies by SQLite][1]
is provided. Additionally it manages annotation and code defined queries.

Most of the SQLite constraints are already supported, but limited by the fact,
that Java below version 8 only allows to define an annotation a single time on
a type or field.


## Installation

Get the latest release from the [JitPack repository][2]

    compile "com.github.tynn.simpldb:library:$simplDbVersion"
    apt "com.github.tynn.simpldb:compiler:$simplDbVersion"


## Usage

The main aim of this library is to create and update databases easily, while
the data handling itself is done with a `Cursor` directly.

### Creating a database

To define a database, subclass `SimplDb` and define the version and the tables.

    @Database(tables = {MyTable.class}, version = 2)
    public class MyDatabase extends SimplDb {
        public MyDatabase(Context context) {
            super(context);
        }
    }

Tables can be interfaces and need to define the columns as lower case string
constants. The variable name must match the column name. Table constraints can
be defined with the same naming like columns.

    @Table
    public interface MyTable extends TableDef, WithID {
        @Column(type = TEXT)
        @NotNull
        String MY_NAME = "my_name";

        @Column(type = INTEGER)
        @Check(expression = "%1$s>5")
        String SCORE = "score";

        @Constraint
        @Unique(columns = MY_NAME, conflictClause = REPLACE)
        String REPLACE_NAME = "replace_name";
    }

With the placeholder `%1$s` the `Check` supports forward references in its
expression.

To access the database, use the underlying `SQLiteOpenHelper` implementation
directly.

    SQLiteOpenHelper helper = new MyDatabase(context).getSQLiteOpenHelper();
    SQLiteDatabase db = helper.getWritableDatabase();


### Creating a query

To manage and automatically reload queries on database changes, `SimplDb` offers
methods interfacing the `SQLiteOpenHelper` implementation. All operations are
done in a background thread automatically.

Defining queries can achieved with different methods and should be instantiated
as a singleton.

    SimplQuery query = SimplQuery.get(MyQuery.class);

##### Definition as a `@Table`

This creates a query returning all rows and columns of the table.

    @Table
    public interface MyQuery extends TableDef, QueryDef {
        @Column(type = TEXT)
        String KEY = "key";

        @Column(type = BLOB)
        String VALUE = "value";
    }

##### Definition as a `@Query`

This creates a query returning all rows and columns filtered by `Query`.
Optionally other tables can be joined. The format specifiers `%1$s` and `%2$s`
in the join constraint can be used as a placeholder for the query and the join
table.

    @Query(table = MyTable.class, columns = {MyTable.MY_NAME}, limit = 2)
    @Join(table = SomeOtherTable.class, columns = {SomeOtherTable.DATA}, on = JoinTestQuery.ON)
    public interface MyQuery extends QueryDef {
        String ON = "%1$s." + MyTable._ID + "=%2$s." + SomeOtherTable.REF;
    }

##### Definition as a `SimplQuery`

For convenience and more complex queries it is possible to subclass `SimplQuery`
and `QueryDef`.
A default constructor must be provided for this implementation.

### RxJava

The reactive module provides `SimplDbRx`, a `SimplDb` subclass overloading the
`insert()`, `update()`, `delete()` and `query()` methods without callback
parameters returning observables. Additionally an `observe()` method is provided,
wrapping the `registerObserver()` and `unregisterObserver()` methods.

    compile "com.github.tynn.simpldb:reactive:$simplDbVersion"

### Utilities

This is a `QueryLoader` providing a default Android `Loader`. It handles the
asynchronous loading and updates of the data. For the latter a `SimplDb.Observer`
is used.

    compile "com.github.tynn.simpldb:utilities:$simplDbVersion"


## License

    Copyright (C) 2016 Christian Schmitz.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
   
        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


 [1]: https://jitpack.io/v/tynn/simpldb.svg
 [2]: https://jitpack.io/#tynn/simpldb
 [3]: https://www.sqlite.org/lang_altertable.html#otheralter
