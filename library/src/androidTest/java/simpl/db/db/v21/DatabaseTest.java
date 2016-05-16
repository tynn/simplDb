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

package simpl.db.db.v21;

import android.annotation.TargetApi;
import android.content.Context;

import simpl.db.Database;
import simpl.db.SimplDb;
import simpl.db.db.v2.TypeTest;

import static android.os.Build.VERSION_CODES.LOLLIPOP;

@TargetApi(LOLLIPOP)
@Database(tables = {TypeTest.class, TableTest.MI.class, TableTest.OI.class}, version = 21)
public class DatabaseTest extends SimplDb {
    public DatabaseTest(Context context) {
        super(context);
    }
}