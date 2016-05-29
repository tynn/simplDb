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

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import simpl.db.api.Database;
import simpl.db.api.Table;

public final class SimplProcessor extends AbstractProcessor {
    private static final String DATABASE = Database.class.getCanonicalName();
    private static final String TABLE = Table.class.getCanonicalName();

    private SimplSpecWriter databaseSpecWriter;
    private SimplSpecWriter tableSpecWriter;

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        LinkedHashSet<String> types = new LinkedHashSet<>();
        types.add(DATABASE);
        types.add(TABLE);
        return types;
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        databaseSpecWriter = new DatabaseSpecWriter(processingEnv);
        tableSpecWriter = new TableSpecWriter(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement annotation : annotations)
            process(annotation, roundEnv);
        return true;
    }

    private void process(TypeElement annotation, RoundEnvironment roundEnv) {
        SimplSpecWriter writer;
        Class<? extends Annotation> ann;

        String name = annotation.getQualifiedName().toString();
        if (DATABASE.equals(name))
            writer = databaseSpecWriter;
        else if (TABLE.equals(name))
            writer = tableSpecWriter;
        else
            return;

        for (Element element : roundEnv.getElementsAnnotatedWith(annotation))
            try {
                writer.process((TypeElement) element);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
    }
}
