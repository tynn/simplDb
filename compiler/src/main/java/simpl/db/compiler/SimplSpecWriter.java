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
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

abstract class SimplSpecWriter {
    static final String TAB = "    ";

    final Messager messager;
    final Elements elements;
    final Types types;

    private final Filer filer;
    PrintWriter writer;

    final TypeElement simplDef;
    private final Class<?> simplSpec;
    private final Class<? extends Annotation> ann;

    SimplSpecWriter(ProcessingEnvironment e, String d, Class<? extends Annotation> a, Class<?> s) {
        messager = e.getMessager();
        elements = e.getElementUtils();
        types = e.getTypeUtils();
        filer = e.getFiler();

        simplDef = elements.getTypeElement(d);
        simplSpec = s;
        ann = a;
    }

    void error(CharSequence msg, Element element, AnnotationMirror annotation) {
        messager.printMessage(Diagnostic.Kind.ERROR, msg, element, annotation);
    }

    boolean verify(TypeElement type, TypeElement superType, AnnotationMirror annotation) {
        if (types.isSubtype(type.asType(), superType.asType()))
            return true;
        String extend = type.getKind() == superType.getKind() ? "extend " : "implement ";
        error(type + " must " + extend + superType, type, annotation);
        return false;
    }

    boolean verify(TypeMirror cls, Class<? extends Annotation> ann) {
        Element type = types.asElement(cls);
        if (type.getAnnotation(ann) != null)
            return true;
        error(cls + " must be annotated with " + ann.getName(), type, null);
        return false;
    }

    void process(TypeElement type) throws IOException {
        AnnotationMirror annotation = getAnnotation(type, ann);
        if (!verify(type, simplDef, annotation))
            return;

        String spec = "$$" + simplSpec.getSimpleName();
        String name = type.getSimpleName().toString();
        JavaFileObject file = filer.createSourceFile(type + spec, type);
        writer = new PrintWriter(file.openWriter());

        writePackage(type);
        writeDefineClass(name + spec, simplSpec);
        writeSpec(name + spec, name, annotation, type);
        writeCloseClass();
        writer.println();
        writer.close();
    }

    private void writePackage(TypeElement type) {
        PackageElement pkg = elements.getPackageOf(type);
        if (pkg.isUnnamed())
            return;
        writer.print("package ");
        writer.print(pkg);
        writer.println(";");
        writer.println();
    }

    private void writeDefineClass(String clsName, Class<?> cls) {
        writer.print("public class ");
        writer.print(clsName);
        writer.print(" extends ");
        writer.print(cls.getName());
        writer.println(" {");
    }

    private void writeCloseClass() {
        writer.println("}");
    }

    void writeAnnotation(AnnotationMirror annotation, String indent) {
        writer.print(indent);
        writer.print("public Class<? extends ");
        writer.print(Annotation.class.getName());
        writer.println("> annotationType() {");
        writer.print(indent);
        writer.print(TAB);
        writer.print("return ");
        writer.print(annotation.getAnnotationType());
        writer.println(".class;");
        writer.print(indent);
        writer.println("}");

        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
                elements.getElementValuesWithDefaults(annotation).entrySet()) {
            ExecutableElement exe = entry.getKey();
            TypeMirror returnType = exe.getReturnType();
            AnnotationValue value = entry.getValue();

            writer.print(indent);
            writer.print("public ");
            writer.print(returnType);
            writer.print(" ");
            writer.print(exe.getSimpleName());
            writer.println("() {");
            writer.print(indent);
            writer.print(TAB);
            writer.print("return ");
            if (returnType.getKind() == TypeKind.ARRAY) {
                writer.print("new ");
                writer.print(types.erasure(returnType));
                writer.print(" ");
            }
            writer.print(value);
            writer.println(";");
            writer.print(indent);
            writer.println("}");
        }
    }

    private void writeDefineConstructor(String clsName) {
        writer.print(TAB);
        writer.print("public ");
        writer.print(clsName);
        writer.println("() {");
    }

    private void writeCloseConstructor() {
        writer.print(TAB);
        writer.println("}");
    }


    void writeSpec(String clsName, String name, AnnotationMirror annotation, TypeElement type) {
        writeDefineConstructor(clsName);

        writer.print(TAB);
        writer.print(TAB);
        writer.print("super(\"");
        writer.print(name);
        writer.print("\", new ");
        writer.print(annotation.getAnnotationType());
        writer.println("() {");

        writeAnnotation(annotation, TAB + TAB + TAB);

        writer.print(TAB);
        writer.print(TAB);
        writer.print("}, ");
        writer.print(type);
        writer.println(".class);");

        writeSpecs(type, annotation, TAB + TAB);

        writeCloseConstructor();
    }

    abstract void writeSpecs(TypeElement type, AnnotationMirror annotation, String indent);

    static AnnotationMirror getAnnotation(Element element, Class<? extends Annotation> ann) {
        String name = ann.getName();
        for (AnnotationMirror annotation : element.getAnnotationMirrors())
            if (name.equals(annotation.getAnnotationType().toString()))
                return annotation;
        return null;
    }

    static AnnotationValue getAnnotationValue(AnnotationMirror annotation, String key) {
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
                annotation.getElementValues().entrySet())
            if (key.equals(entry.getKey().getSimpleName().toString()))
                return entry.getValue();
        throw new IllegalArgumentException(annotation + " has no key " + key);
    }
}
