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

package simpl.db.lint;

import com.android.annotations.NonNull;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Speed;

import java.util.Collections;
import java.util.List;

import lombok.ast.AstVisitor;
import lombok.ast.Node;

import static com.android.tools.lint.detector.api.Category.CORRECTNESS;
import static com.android.tools.lint.detector.api.Scope.JAVA_FILE_SCOPE;
import static com.android.tools.lint.detector.api.Severity.ERROR;
import static com.android.tools.lint.detector.api.Severity.INFORMATIONAL;

public class SimplLintDetector extends Detector implements Detector.JavaScanner {
    private static final String NAME = "SimplDb";

    private static final Category CATEGORY = Category.create(CORRECTNESS, NAME, 100);

    private static final Implementation IMPLEMENTATION = new Implementation(SimplLintDetector.class, JAVA_FILE_SCOPE);

    static final Issue COLUMN_NAME = Issue.create(NAME + "ColumnName", "`@Column` annotation",
            "The `@Column` annotation can only be used on a constant `String` value." +
                    "The value itself must conform to `SimplDb.getName()` of the constant name.",
            CATEGORY, 10, ERROR, IMPLEMENTATION);

    static final Issue SQL_KEYWORD = Issue.create(NAME + "SqlKeyword", "SQL keyword column name",
            "The column name is also an SQL keyword. When using this with `ContentValues`, you must use `SimplDb.quote()`.",
            CATEGORY, 2, INFORMATIONAL, IMPLEMENTATION);

    @Override
    public Speed getSpeed() {
        return Speed.FAST;
    }

    @Override
    public List<Class<? extends Node>> getApplicableNodeTypes() {
        return Collections.<Class<? extends Node>>singletonList(lombok.ast.Annotation.class);
    }

    @Override
    public AstVisitor createJavaVisitor(@NonNull JavaContext context) {
        return new SimplLintVisitor(context);
    }
}
