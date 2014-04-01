/**
 * Copyright (C) 2011-2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.expression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.expression.impl.ExpressionImpl;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Builds a new {@link Expression}. This class provides utility methods for building of different Expression types.
 * Attributes Name and ReturnType must be set.
 * 
 * @author Feng Hui
 * @author Matthieu Chaffotte
 * @author Emmanuel Duchastenier
 */
public class ExpressionBuilder {

    public static final String NOT_EQUALS_COMPARATOR = "!=";

    public static final String NOT_COMPARATOR = "!";

    public static final String LESS_THAN_OR_EQUALS_COMPARATOR = "<=";

    public static final String LESS_THAN_COMPARATOR = "<";

    public static final String GREATER_THAN_OR_EQUALS_COMPARATOR = ">=";

    public static final String GREATER_THAN_COMPARATOR = ">";

    public static final String EQUALS_COMPARATOR = "==";

    private ExpressionImpl expression;

    private static List<String> validOperators;

    private static HashSet<String> numericTypes;

    static {
        validOperators = new ArrayList<String>(7);
        validOperators.add(LESS_THAN_COMPARATOR);
        validOperators.add(GREATER_THAN_COMPARATOR);
        validOperators.add(LESS_THAN_OR_EQUALS_COMPARATOR);
        validOperators.add(GREATER_THAN_OR_EQUALS_COMPARATOR);
        validOperators.add(EQUALS_COMPARATOR);
        validOperators.add(NOT_EQUALS_COMPARATOR);
        validOperators.add(NOT_COMPARATOR);

        numericTypes = new HashSet<String>();
        numericTypes.add(Integer.class.getName());
        numericTypes.add(Long.class.getName());
        numericTypes.add(Double.class.getName());
        numericTypes.add(Float.class.getName());
        numericTypes.add(Short.class.getName());
        numericTypes.add(Byte.class.getName());
        numericTypes.add(int.class.getName());
        numericTypes.add(long.class.getName());
        numericTypes.add(double.class.getName());
        numericTypes.add(float.class.getName());
        numericTypes.add(short.class.getName());
        numericTypes.add(byte.class.getName());
    }

    // public ExpressionBuilder createNewInstance() {
    // expression = new ExpressionImpl();
    // return this;
    // }

    public ExpressionBuilder createNewInstance(final String name) {
        expression = new ExpressionImpl();
        setName(name);
        return this;
    }

    /**
     * The name of the built expression is set to the value of the constant.
     * 
     * @param value
     *            the value of this CONSTANT expression
     * @since 6.0
     */
    public Expression createConstantStringExpression(final String value) throws InvalidExpressionException {
        createNewInstance(value).setContent(value).setExpressionType(ExpressionType.TYPE_CONSTANT.name()).setReturnType(String.class.getName());
        return done();
    }

    public Expression createConstantBooleanExpression(final boolean value) throws InvalidExpressionException {
        createNewInstance(Boolean.toString(value)).setContent(value ? "true" : "false").setExpressionType(ExpressionType.TYPE_CONSTANT.name())
                .setReturnType(Boolean.class.getName());
        return done();
    }

    /**
     * Build a constant expression with date type and value in ISO-8601 format
     * 
     * @param value
     *            a date defined with the ISO-8601 format
     * @return The corresponding expression
     * @throws InvalidExpressionException
     * @since 6.0
     */
    public Expression createConstantDateExpression(final String value) throws InvalidExpressionException {
        createNewInstance(value).setContent(String.valueOf(value)).setExpressionType(ExpressionType.TYPE_CONSTANT.name())
                .setReturnType(Date.class.getName());
        return done();
    }

    public Expression createConstantLongExpression(final long value) throws InvalidExpressionException {
        createNewInstance(Long.toString(value)).setContent(String.valueOf(value)).setExpressionType(ExpressionType.TYPE_CONSTANT.name())
                .setReturnType(Long.class.getName());
        return done();
    }

    public Expression createConstantIntegerExpression(final int value) throws InvalidExpressionException {
        createNewInstance(Integer.toString(value)).setContent(String.valueOf(value)).setExpressionType(ExpressionType.TYPE_CONSTANT.name())
                .setReturnType(Integer.class.getName());
        return done();
    }

    public Expression createDataExpression(final String dataName, final String dataType) throws InvalidExpressionException {
        return createNewInstance(dataName).setContent(dataName).setExpressionType(ExpressionType.TYPE_VARIABLE.name()).setReturnType(dataType).done();
    }

    public Expression createDocumentReferenceExpression(final String documentName) throws InvalidExpressionException {
        return createNewInstance(documentName).setContent(documentName).setExpressionType(ExpressionType.TYPE_DOCUMENT.name())
                .setReturnType(Document.class.getName()).done();
    }

    public Expression createPatternExpression(final String dataContent) throws InvalidExpressionException {
        return createNewInstance(dataContent).setContent(dataContent).setExpressionType(ExpressionType.TYPE_PATTERN.name())
                .setReturnType(String.class.getName()).done();
    }

    public Expression createInputExpression(final String value, final String returnType) throws InvalidExpressionException {
        return createNewInstance(value).setContent(value).setExpressionType(ExpressionType.TYPE_INPUT.name()).setReturnType(returnType).done();
    }

    public Expression createAPIAccessorExpression() throws InvalidExpressionException {
        return createNewInstance(ExpressionConstants.API_ACCESSOR.getEngineConstantName()).setContent(ExpressionConstants.API_ACCESSOR.getEngineConstantName())
                .setExpressionType(ExpressionType.TYPE_ENGINE_CONSTANT.name()).setReturnType(ExpressionConstants.API_ACCESSOR.getReturnType()).done();
    }

    public Expression createEngineConstant(final ExpressionConstants value) throws InvalidExpressionException {
        return createNewInstance(value.getEngineConstantName()).setContent(value.getEngineConstantName())
                .setExpressionType(ExpressionType.TYPE_ENGINE_CONSTANT.name()).setReturnType(value.getReturnType()).done();
    }

    /**
     * Builds a new expression with the specified attributes
     * 
     * @return the newly built {@link Expression}
     * @throws InvalidExpressionException
     *             is name or returnType is not set or set to an empty String.
     * @author Emmanuel Duchastenier
     * @since 6.0
     */
    public Expression done() throws InvalidExpressionException {
        final String returnType = expression.getReturnType();
        final String expName = expression.getName();
        final String content = expression.getContent();
        final String expressionType = expression.getExpressionType();
        final List<Expression> dependencies = expression.getDependencies();
        generalCheck(returnType, expName, content);
        if (ExpressionType.TYPE_CONDITION.name().equals(expressionType)) {
            validateConditionExpression(content, dependencies);
        }

        return expression;
    }

    private void validateConditionExpression(final String content, final List<Expression> dependencies) throws InvalidExpressionException {

        if (!validOperators.contains(content)) {
            throw new InvalidExpressionException("Invalid content for expression of type " + ExpressionType.TYPE_CONDITION + ". The possible values are: "
                    + validOperators);
        }
        if (!NOT_COMPARATOR.equals(content)) {
            if (dependencies.size() != 2) {
                throw new InvalidExpressionException("The comparator " + content
                        + " must have exactly two dependencies. The first one is the left oparand and the second one the right operand.");
            }

            final String r1 = dependencies.get(0).getReturnType();
            final String r2 = dependencies.get(1).getReturnType();
            if (!(numericTypes.contains(r1) && numericTypes.contains(r2) || r1.equals(r2)) && !EQUALS_COMPARATOR.equals(content)) {
                throw new InvalidExpressionException("The dependencies of comparator " + content + " must have the same return type.");
            }
        } else {
            if (dependencies.size() != 1) {
                throw new InvalidExpressionException("The logical complement operator " + content + " must have exactly one dependency.");
            }
            if (!dependencies.get(0).getReturnType().equals(Boolean.class.getName())) {
                throw new InvalidExpressionException("The dependency of logical complement operator " + content + " must have the return type "
                        + Boolean.class.getName());
            }
        }
    }

    private void generalCheck(final String returnType, final String expName, final String content) throws InvalidExpressionException {
        if (returnType == null || returnType.isEmpty()) {
            throw new InvalidExpressionException("Expression return type is not set on " + expression);
        }
        if (expName == null || expName.isEmpty()) {
            throw new InvalidExpressionException("Expression name is not set on " + expression);
        }
        if (content == null) {
            throw new InvalidExpressionException("Expression content is not set on " + expression);
        }
    }

    public ExpressionBuilder setContent(final String content) {
        expression.setContent(content);
        return this;
    }

    public ExpressionBuilder setExpressionType(final String expressionType) {
        expression.setExpressionType(expressionType);
        return this;
    }

    public ExpressionBuilder setExpressionType(final ExpressionType expressionType) {
        expression.setExpressionType(expressionType.name());
        return this;
    }

    public ExpressionBuilder setReturnType(final String returnType) {
        expression.setReturnType(returnType);
        return this;
    }

    public ExpressionBuilder setInterpreter(final String interpreter) {
        expression.setInterpreter(interpreter);
        return this;
    }

    public ExpressionBuilder setDependencies(final List<Expression> dependencies) {
        expression.setDependencies(dependencies);
        return this;
    }

    public ExpressionBuilder setName(final String name) {
        expression.setName(name);
        return this;
    }

    public Expression createExpression(final String name, final String expressionContent, final String returnType, final ExpressionType expressionType)
            throws InvalidExpressionException {
        createNewInstance(name).setContent(expressionContent).setExpressionType(expressionType.name()).setReturnType(returnType);
        return done();
    }

    public Expression createConstantDoubleExpression(final double d) throws InvalidExpressionException {
        createNewInstance(Double.toString(d)).setContent(String.valueOf(d)).setExpressionType(ExpressionType.TYPE_CONSTANT.name())
                .setReturnType(Double.class.getName());
        return done();
    }

    public Expression createConstantFloatExpression(final float f) throws InvalidExpressionException {
        createNewInstance(Float.toString(f)).setContent(String.valueOf(f)).setExpressionType(ExpressionType.TYPE_CONSTANT.name())
                .setReturnType(Float.class.getName());
        return done();
    }

    public Expression createListExpression(final String name, final List<Expression> expressions) throws InvalidExpressionException {
        // name of the list is put in content because content is used to name the List variable in dependencies:
        createNewInstance(name).setContent(name).setExpressionType(ExpressionType.TYPE_LIST.name()).setReturnType(List.class.getName())
                .setDependencies(expressions);
        return done();
    }

    public Expression createListOfListExpression(final String name, final List<List<Expression>> expressions) throws InvalidExpressionException {
        final List<Expression> deps = new ArrayList<Expression>(expressions.size());
        int i = 0;
        for (final List<Expression> list : expressions) {
            final String name2 = name + "_" + i++;
            final Expression exp = createNewInstance(name2).setContent(name2).setExpressionType(ExpressionType.TYPE_LIST.name())
                    .setReturnType(List.class.getName()).setDependencies(list).done();
            deps.add(exp);

        }
        return createNewInstance(name).setContent(name).setExpressionType(ExpressionType.TYPE_LIST.name()).setReturnType(List.class.getName())
                .setDependencies(deps).done();
    }

    public Expression createExpression(final String name, final String content, final String type, final String returnType, final String interpreter,
            final List<Expression> dependencies) throws InvalidExpressionException {
        createNewInstance(name).setContent(content).setExpressionType(type).setReturnType(returnType);
        if (interpreter == null) {
            setInterpreter("NONE");// interpreter,ExpressionInterpreter.GROOVY.toString()
        } else {
            setInterpreter(interpreter);
        }
        if (dependencies == null) {
            setDependencies(new ArrayList<Expression>());
        } else {
            setDependencies(dependencies);
        }
        return done();
    }

    public Expression createGroovyScriptExpression(final String name, final String script, final String returnType) throws InvalidExpressionException {
        createNewInstance(name).setContent(script).setExpressionType(ExpressionType.TYPE_READ_ONLY_SCRIPT.name())
                .setInterpreter(ExpressionInterpreter.GROOVY.name()).setReturnType(returnType);
        return done();
    }

    public Expression createGroovyScriptExpression(final String name, final String script, final String returnType, final List<Expression> dependencies)
            throws InvalidExpressionException {
        createNewInstance(name).setContent(script).setExpressionType(ExpressionType.TYPE_READ_ONLY_SCRIPT.name())
                .setInterpreter(ExpressionInterpreter.GROOVY.name()).setReturnType(returnType).setDependencies(dependencies);
        return done();
    }

    public Expression createGroovyScriptExpression(final String name, final String script, final String returnType, final Expression... dependencies)
            throws InvalidExpressionException {
        createNewInstance(name).setContent(script).setExpressionType(ExpressionType.TYPE_READ_ONLY_SCRIPT.name())
                .setInterpreter(ExpressionInterpreter.GROOVY.name()).setReturnType(returnType).setDependencies(Arrays.asList(dependencies));
        return done();
    }

    public Expression createPatternExpression(final String name, final String messagePattern, final Expression... dependencies)
            throws InvalidExpressionException {
        createNewInstance(name).setName(messagePattern).setContent(messagePattern).setExpressionType(ExpressionType.TYPE_PATTERN.name()).setInterpreter("NONE")
                .setReturnType(String.class.getName()).setDependencies(Arrays.asList(dependencies));
        return done();
    }

    public Expression createParameterExpression(final String expressionName, final String parameterName, final String returnType)
            throws InvalidExpressionException {
        createNewInstance(expressionName).setExpressionType(ExpressionType.TYPE_PARAMETER.name()).setReturnType(returnType).setContent(parameterName);
        return done();
    }

    public Expression createComparisonExpression(final String name, final Expression leftOperand, final ComparisonOperator operator,
            final Expression rightOperand) throws InvalidExpressionException {
        String strOperator = null;
        switch (operator) {
            case EQUALS:
                strOperator = EQUALS_COMPARATOR;
                break;
            case GREATER_THAN:
                strOperator = GREATER_THAN_COMPARATOR;
                break;
            case GREATER_THAN_OR_EQUALS:
                strOperator = GREATER_THAN_OR_EQUALS_COMPARATOR;
                break;
            case LESS_THAN:
                strOperator = LESS_THAN_COMPARATOR;
                break;
            case LESS_THAN_OR_EQUALS:
                strOperator = LESS_THAN_OR_EQUALS_COMPARATOR;
                break;
            case NOT_EQUALS:
                strOperator = NOT_EQUALS_COMPARATOR;
                break;
            default:
                throw new IllegalStateException();
        }
        return createComparisonExpression(name, leftOperand, strOperator, rightOperand);
    }

    public Expression createComparisonExpression(final String name, final Expression leftOperand, final String operator, final Expression rightOperand)
            throws InvalidExpressionException {
        createNewInstance(name).setExpressionType(ExpressionType.TYPE_CONDITION.name()).setReturnType(Boolean.class.getName()).setContent(operator);
        final List<Expression> dependencies = new ArrayList<Expression>(2);
        dependencies.add(leftOperand);
        dependencies.add(rightOperand);
        setDependencies(dependencies);
        return done();
    }

    public Expression createLogicalComplementExpression(final String name, final Expression expression) throws InvalidExpressionException {
        createNewInstance(name).setExpressionType(ExpressionType.TYPE_CONDITION.name()).setReturnType(Boolean.class.getName()).setContent(NOT_COMPARATOR);
        setDependencies(Collections.singletonList(expression));
        return done();
    }

    public Expression createXPathExpressionWithDataAsContent(final String name, final String xPathExpression, final XPathReturnType returnType,
            final String xmlContentAsDataRef) throws InvalidExpressionException {
        final ExpressionBuilder expBuilder = createNewInstance(name).setExpressionType(ExpressionType.TYPE_XPATH_READ).setContent(xPathExpression);
        getXPathReturnType(returnType, expBuilder);
        final Expression dep = new ExpressionBuilder().createNewInstance("xmlContent").setContent(xmlContentAsDataRef)
                .setExpressionType(ExpressionType.TYPE_VARIABLE).setReturnType(String.class.getName()).done();
        expBuilder.setDependencies(Arrays.asList(dep));
        return expBuilder.done();
    }

    public Expression createXPathExpression(final String name, final String xPathExpression, final XPathReturnType returnType, final String xmlContent)
            throws InvalidExpressionException {
        final ExpressionBuilder expBuilder = createNewInstance(name).setExpressionType(ExpressionType.TYPE_XPATH_READ).setContent(xPathExpression);
        getXPathReturnType(returnType, expBuilder);
        final Expression dep = new ExpressionBuilder().createNewInstance("xmlContent").setContent(xmlContent).setExpressionType(ExpressionType.TYPE_CONSTANT)
                .setReturnType(String.class.getName()).done();
        expBuilder.setDependencies(Arrays.asList(dep));
        return expBuilder.done();
    }

    /**
     * @param returnType
     * @param expBuilder
     */
    protected void getXPathReturnType(final XPathReturnType returnType, final ExpressionBuilder expBuilder) {
        switch (returnType) {
            case BOOLEAN:
                expBuilder.setReturnType(Boolean.class.getName());
                break;
            case DOUBLE:
                expBuilder.setReturnType(Double.class.getName());
                break;
            case NODE:
                expBuilder.setReturnType(Node.class.getName());
                break;
            case NODE_LIST:
                expBuilder.setReturnType(NodeList.class.getName());
                break;
            case STRING:
                expBuilder.setReturnType(String.class.getName());
                break;
            case FLOAT:
                expBuilder.setReturnType(Float.class.getName());
                break;
            case INTEGER:
                expBuilder.setReturnType(Integer.class.getName());
                break;
            case LONG:
                expBuilder.setReturnType(Long.class.getName());
                break;
            default:
                break;

        }
    }

    /**
     * Create an expression to call a simple Java method (without parameters)
     * 
     * @param name
     *            the expression name
     * @param methodName
     *            the name of method to call
     * @param returnType
     *            the method return type
     * @param entityExpression
     *            the expression representing the entity (Java Object) where the method will be called
     * @return the created expression
     * @throws InvalidExpressionException
     *             if the created expression is invalid
     * @since 6.0
     */
    public Expression createJavaMethodCallExpression(final String name, final String methodName, final String returnType, final Expression entityExpression)
            throws InvalidExpressionException {
        createNewInstance(name).setExpressionType(ExpressionType.TYPE_JAVA_METHOD_CALL.name()).setReturnType(returnType).setContent(methodName);
        setDependencies(Collections.singletonList(entityExpression));
        return done();
    }

}
