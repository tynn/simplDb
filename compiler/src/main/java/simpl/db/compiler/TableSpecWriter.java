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

import java.lang.annotation.Annotation;
import java.util.HashSet;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import simpl.db.api.Column;
import simpl.db.api.Constraint;
import simpl.db.api.Table;
import simpl.db.api.TableDef;
import simpl.db.internal.SimplName;
import simpl.db.spec.TableSpec;

class TableSpecWriter extends SimplSpecWriter {
    TableSpecWriter(ProcessingEnvironment processingEnv) {
        super(processingEnv, TableDef.class.getName(), Table.class, TableSpec.class);
    }

    @Override
    void writeSpecs(TypeElement type, AnnotationMirror annotation, String indent) {
        writeConstraints(type, annotation, indent);
        Fields fields = new Fields();
        collectFields(type, fields);
        writeConstraints2(fields, indent);
        writeColumnSpecs(fields, indent);
    }

    private void writeConstraint(String value, AnnotationMirror annotation, String indent) {
        String name = annotation.getAnnotationType().asElement().getSimpleName().toString();
        if (value != null)
            name += '_' + value;
        name = SimplName.from(name);

        writer.print(indent);
        writer.print("constraints.put(\"");
        writer.print(name);
        writer.print("\", new ");
        writer.print(annotation.getAnnotationType());
        writer.println("() {");
        writeAnnotation(annotation, indent + TAB);
        writer.print(indent);
        writer.println("});");
    }

    private void writeConstraints(TypeElement type, AnnotationMirror table, String indent) {
        for (AnnotationMirror annotation : type.getAnnotationMirrors())
            if (!table.equals(annotation))
                writeConstraint(null, annotation, indent);
    }

    private void writeConstraints2(Fields fields, String indent) {
        for (VariableElement field : fields.constraints)
            for (AnnotationMirror annotation : field.getAnnotationMirrors())
                if (!isAnnotationType(annotation, Column.class, Constraint.class))
                    writeConstraint(field.getSimpleName().toString(), annotation, indent);
    }

    @SafeVarargs
    private static boolean isAnnotationType(AnnotationMirror annotation, Class<? extends Annotation>... anns) {
        for (Class<? extends Annotation> ann : anns)
            if (ann.getName().equals(annotation.getAnnotationType().toString()))
                return true;
        return false;
    }

    private void writeColumnSpecs(Fields fields, String indent) {
        writer.print(indent);
        writer.print(HashSet.class.getName());
        writer.print("<");
        writer.print(Annotation.class.getName());
        writer.println("> annotations;");

        HashSet<String> columnNames = new HashSet<>();
        for (VariableElement field : fields.columns) {
            String name = SimplName.from(field.getSimpleName().toString());
            if (!columnNames.add(name))
                error(field + " provides a duplicated column " + name, field, null);

            writer.print(indent);
            writer.print("columnSpecs.put(\"");
            writer.print(name);
            writer.print("\", annotations = new ");
            writer.print(HashSet.class.getName());
            writer.println("<>());");

            for (AnnotationMirror annotation : field.getAnnotationMirrors()) {
                writer.print(indent);
                writer.print("annotations.add(new ");
                writer.print(annotation.getAnnotationType());
                writer.println("() {");
                writeAnnotation(annotation, indent + TAB);
                writer.print(indent);
                writer.println("});");
            }
        }
    }

    private boolean verifyColumn(VariableElement field) {
        AnnotationMirror column = getAnnotation(field, Column.class);
        if (column == null)
            return false;
        String name = SimplName.from(field.getSimpleName().toString());
        if (name.equals(field.getConstantValue().toString()))
            return true;
        error(field + " must have a constant value of \"" + name + "\"", field, column);
        return false;
    }

    private boolean verifyConstraint(VariableElement field) {
        AnnotationMirror constraint = getAnnotation(field, Constraint.class);
        if (constraint == null)
            return false;
        String name = SimplName.from(field.getSimpleName().toString());
        if (name.equals(field.getConstantValue().toString()))
            return true;
        error(field + " must have a constant value of \"" + name + "\"", field, constraint);
        return false;
    }

    @SuppressWarnings("Convert2streamapi")
    private void collectFields(TypeElement type, Fields fields) {
        TypeMirror s = type.getSuperclass();
        if (s.getKind() != TypeKind.NONE)
            collectFields((TypeElement) types.asElement(s), fields);
        for (TypeMirror i : type.getInterfaces())
            collectFields((TypeElement) types.asElement(i), fields);
        for (Element e : type.getEnclosedElements())
            if (e.getKind() == ElementKind.FIELD) {
                if (verifyColumn((VariableElement) e))
                    fields.columns.add((VariableElement) e);
                if (verifyConstraint((VariableElement) e))
                    fields.constraints.add((VariableElement) e);
            }
    }

    private static class Fields {
        final HashSet<VariableElement> constraints = new HashSet<>();
        final HashSet<VariableElement> columns = new HashSet<>();

        Fields() {
        }
    }
}
