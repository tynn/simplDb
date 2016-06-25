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

package simpl.db.compiler;

import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import simpl.db.api.Database;
import simpl.db.api.Table;
import simpl.db.spec.DatabaseSpec;
import simpl.db.spec.TableSpec;

class DatabaseSpecWriter extends SimplSpecWriter {
    private static final String SIMPL_DB = "simpl.db.SimplDb";

    DatabaseSpecWriter(ProcessingEnvironment processingEnv) {
        super(processingEnv, SIMPL_DB, Database.class, DatabaseSpec.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    void writeSpecs(TypeElement type, AnnotationMirror annotation, String indent) {
        AnnotationValue tables = getAnnotationValue(annotation, "tables");
        for (AnnotationValue table : (List<? extends AnnotationValue>) tables.getValue()) {
            TypeMirror value = (TypeMirror) table.getValue();
            writer.print(indent);
            writer.print("tableSpecs.add(new ");
            writer.print(value + "$$" + TableSpec.class.getSimpleName());
            writer.println("());");
            verify(value, Table.class);
        }
    }
}
