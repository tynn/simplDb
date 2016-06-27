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

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;

import simpl.db.api.Join;
import simpl.db.api.Query;
import simpl.db.api.QueryDef;
import simpl.db.spec.QuerySpec;

class QuerySpecWriter extends SimplSpecWriter {
    QuerySpecWriter(ProcessingEnvironment processingEnv) {
        super(processingEnv, QueryDef.class.getName(), Query.class, QuerySpec.class);
    }

    @Override
    void writeSpecs(TypeElement type, AnnotationMirror annotation, String indent) {
        annotation = getAnnotation(type, Join.class);
        if (annotation != null) {
            writer.print(indent);
            writer.print("join = new ");
            writer.print(annotation.getAnnotationType());
            writer.println("() {");
            writeAnnotation(annotation, indent + TAB);
            writer.print(indent);
            writer.println("};");
        }
    }
}
