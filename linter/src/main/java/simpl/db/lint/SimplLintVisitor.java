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

import com.android.tools.lint.detector.api.JavaContext;

import lombok.ast.Annotation;
import lombok.ast.Expression;
import lombok.ast.ForwardingAstVisitor;
import lombok.ast.Modifiers;
import lombok.ast.Node;
import lombok.ast.StrictListAccessor;
import lombok.ast.StringLiteral;
import lombok.ast.TypeDeclaration;
import lombok.ast.VariableDefinition;
import lombok.ast.VariableDefinitionEntry;
import simpl.db.internal.SimplName;

import static simpl.db.lint.SimplLintDetector.COLUMN_NAME;
import static simpl.db.lint.SimplLintDetector.SQL_KEYWORD;

public class SimplLintVisitor extends ForwardingAstVisitor {
    private final JavaContext mContext;

    public SimplLintVisitor(JavaContext context) {
        mContext = context;
    }

    @Override
    public boolean visitAnnotation(Annotation node) {
        Node parent = node.getParent();
        switch (node.astAnnotationTypeReference().getTypeName()) {
            case "Column":
                if (parent instanceof Modifiers && !verifyColumn((Modifiers) parent))
                    mContext.report(COLUMN_NAME, node, mContext.getLocation(node), "must be a string constant");
                return true;
        }
        return false;
    }

    private boolean verifyColumn(Modifiers node) {
        if (!isPublicStaticFinalField(node) && !isInterface(node))
            return false;

        Node parent = node.getParent();
        if (!(parent instanceof VariableDefinition))
            return false;

        VariableDefinition field = (VariableDefinition) parent;
        if (!field.astTypeReference().getTypeName().equals("String"))
            return false;

        StrictListAccessor<VariableDefinitionEntry, VariableDefinition> vars = field.astVariables();
        if (vars.size() != 1)
            return false;

        VariableDefinitionEntry var = vars.first();
        Expression initializer = var.astInitializer();
        if (!(initializer instanceof StringLiteral))
            return false;

        String name = SimplName.from(var.astName().astValue());
        String value = ((StringLiteral) initializer).astValue();

        if (!name.equals(value))
            mContext.report(COLUMN_NAME, node, mContext.getLocation(parent), "must match " + SimplName.quote(name));

        if (SqlKeywords.contain(name))
            mContext.report(SQL_KEYWORD, mContext.getLocation(var.astName()), "use quoted with ContentValues");

        return true;
    }

    private static boolean isInterface(Node node) {
        while (!(node instanceof TypeDeclaration)) {
            if (node == null)
                return false;
            node = node.getParent();
        }
        return ((TypeDeclaration) node).isInterface();
    }

    private static boolean isPublicStaticFinalField(Modifiers node) {
        return node.isPublic() && node.isStatic() && node.isFinal();
    }
}
