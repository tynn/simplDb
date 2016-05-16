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

package simpl.db.api;

import android.annotation.TargetApi;
import android.os.Build;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The table should be defined without a rowid.
 * This requires the definition of a primary key column.
 */
@Documented
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE})
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public @interface WithoutRowid {
    boolean SUPPORTED = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
}
