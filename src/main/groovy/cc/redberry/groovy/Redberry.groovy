/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2013:
 *   Stanislav Poslavsky   <stvlpos@mail.ru>
 *   Bolotin Dmitriy       <bolotin.dmitriy@gmail.com>
 *
 * This file is part of Redberry.
 *
 * Redberry is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Redberry is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Redberry. If not, see <http://www.gnu.org/licenses/>.
 */

package cc.redberry.groovy

import cc.redberry.core.indexmapping.IndexMappingBuffer
import cc.redberry.core.indexmapping.IndexMappings
import cc.redberry.core.indexmapping.MappingsPort
import cc.redberry.core.indices.*
import cc.redberry.core.number.Complex
import cc.redberry.core.number.Numeric
import cc.redberry.core.number.Rational
import cc.redberry.core.number.Real
import cc.redberry.core.parser.ParserIndices
import cc.redberry.core.tensor.ApplyIndexMapping
import cc.redberry.core.tensor.Expression
import cc.redberry.core.tensor.Tensor
import cc.redberry.core.tensor.Tensors
import cc.redberry.core.tensor.iterator.FromChildToParentIterator
import cc.redberry.core.tensor.iterator.FromParentToChildIterator
import cc.redberry.core.tensor.iterator.TraverseGuide
import cc.redberry.core.transformations.Transformation
import cc.redberry.core.transformations.TransformationCollection
import cc.redberry.core.transformations.substitutions.SubstitutionIterator
import cc.redberry.core.transformations.substitutions.SubstitutionTransformation
import cc.redberry.core.utils.TensorUtils
import org.codehaus.groovy.runtime.DefaultGroovyMethods

import static cc.redberry.core.tensor.Tensors.*

//import cc.redberry.core.transformations.TransformationCollection

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
class Redberry {

    static Real number2Real(Number num) {
        if (num instanceof Double || num instanceof BigDecimal)
            return new Numeric(num.doubleValue());
        else
            return new Rational(num.toBigInteger());
    }

    private static Complex number2Complex(Number num) {
        return new Complex(number2Real(num));
    }

    /*
    * Math operations
    */

    static Tensor plus(Tensor a, Tensor b) { Tensors.sum(a, b); }

    static Tensor plus(Tensor a, Number b) { Tensors.sum(a, number2Complex(b)); }

    static Tensor minus(Tensor a, Tensor b) { Tensors.subtract(a, b); }

    static Tensor minus(Tensor a, Number b) { Tensors.subtract(a, number2Complex(b)); }

    static Tensor div(Tensor a, Tensor b) { Tensors.divide(a, b); }

    static Tensor div(Tensor a, Number b) { Tensors.divide(a, number2Complex(b)); }

    static Tensor multiply(Tensor a, Tensor b) { multiplyAndRenameConflictingDummies(a, b); }

    static Tensor multiply(Tensor a, Number b) { Tensors.multiply(a, number2Complex(b)); }

    static Tensor negative(Tensor a) { negate(a); }

    static Tensor positive(Tensor a) { a; }

    static Tensor getAt(Tensor a, int position) { a.get(position); }

    static Tensor getAt(Tensor a, int ... position) {
        //todo
        return a.get(position);
    }

    /*
     * Indices
     */

    static int getAt(Indices indices, int position) { indices.get(position) }

    static int getAt(Indices indices, IndexType type, int position) { indices.get(type, position) }

    static Indices asType(int[] indices, Class clazz) {
        if (clazz == SimpleIndices)
            return IndicesFactory.createSimple(null, indices)
        else if (clazz == Indices)
            return IndicesFactory.create(indices)
        else
            return DefaultGroovyMethods.asType(indices, clazz)
    }

    static Iterator<Integer> iterator(final Indices indices) {
        def position = 0;
        new Iterator<Integer>() {

            @Override
            boolean hasNext() {
                return position < indices.size()
            }

            @Override
            Integer next() {
                return indices.get(position++)
            }

            @Override
            void remove() {
                throw new UnsupportedOperationException()
            }
        }
    }

    static SimpleIndices plus(SimpleIndices indices, toAdd) {
        if (toAdd instanceof String)
            toAdd = ParserIndices.parseSimple(toAdd);
        if (toAdd instanceof Indices || toAdd instanceof Integer || toAdd instanceof int[])
            return new SimpleIndicesBuilder().append(indices).append(toAdd).indices;
        if (toAdd instanceof Collection)
            return new SimpleIndicesBuilder().append(indices).append(toAdd as int[]).indices;
        else
            throw new IllegalArgumentException()
    }

    static Indices plus(Indices indices, toAdd) {
        if (toAdd instanceof String)
            toAdd = ParserIndices.parseSimple(toAdd);
        if (toAdd instanceof Indices || toAdd instanceof Integer || toAdd instanceof int[])
            return new SimpleIndicesBuilder().append(indices).append(toAdd).indices;
        if (toAdd instanceof Collection)
            return new IndicesBuilder().append(indices).append(toAdd as int[]).indices;
        else
            throw new IllegalArgumentException()
    }

    /*
    * Tensor traversing
    */

    static Tensor eachInTree(Tensor t, Object guide, Closure<Tensor> closure) {
        FromChildToParentIterator iterator = new FromChildToParentIterator(t, guide);
        Tensor c;
        while ((c = iterator.next()) != null)
            closure.call(c);
        return iterator.result();
    }

    static Tensor eachInTreeReverse(Tensor t, TraverseGuide guide, Closure<Tensor> closure) {
        FromParentToChildIterator iterator = new FromParentToChildIterator(t, guide);
        Tensor c;
        while ((c = iterator.next()) != null)
            closure.call(c);
        return iterator.result();
    }

    static Tensor eachInTree(Tensor t, Closure<Tensor> closure) {
        return eachInTree(t, TraverseGuide.ALL, closure)
    }

    static Tensor eachInTreeReverse(Tensor t, Closure<Tensor> closure) {
        return eachInTreeReverse(t, TraverseGuide.ALL, closure)
    }

    /*
    * Tree modification
    */

    static Tensor transformEachInTree(Tensor t, TraverseGuide guide, Closure<Tensor> closure) {
        SubstitutionIterator iterator = new SubstitutionIterator(t, guide);
        Tensor c;
        while ((c = iterator.next()) != null)
            iterator.safeSet(closure.call(c));

        return iterator.result();
    }

    static Tensor transformEachInTree(Tensor t, Closure<Tensor> closure) {
        return transformEachInTree(t, TraverseGuide.ALL, closure);
    }

    /*
    * Transformations
    */

    static Transformation and(Transformation tr1, Transformation tr2) {
        def transformations = [];
        if (tr1 instanceof TransformationCollection)
            transformations.addAll(tr1.transformations)
        else
            transformations << tr1

        if (tr2 instanceof TransformationCollection)
            transformations.addAll(tr2.transformations)
        else
            transformations << tr2

        new TransformationCollection(transformations)
    }

    static Transformation or(Transformation tr1, Transformation tr2) {
        def expressions = [];
        if (tr1 instanceof SubstitutionContainer)
            expressions.addAll(tr1.expressions)
        else if (tr1 instanceof Expression)
            expressions << tr1
        else throw new IllegalArgumentException()

        if (tr2 instanceof SubstitutionContainer)
            expressions.addAll(tr2.expressions)
        else if (tr2 instanceof Expression)
            expressions << tr2
        else throw new IllegalArgumentException()

        new SubstitutionContainer(expressions)
    }

    private static final class SubstitutionContainer implements Transformation {
        final List expressions;

        SubstitutionContainer(expressions) {
            this.expressions = expressions
        }

        @Override
        Tensor transform(Tensor t) {
            return new SubstitutionTransformation(false, * expressions).transform(t)
        }
    }

    private static boolean isCollectionOfType(collection, Class type) {
        for (t in collection)
            if (!type.isAssignableFrom(t.class))
                return false;
        return true;
    }

    static Tensor rightShift(Transformation transformation, Tensor tensor) {
        return transformation.transform(tensor);
    }

    static Tensor rightShift(String transformation, Tensor tensor) {
        return parseExpression(transformation).transform(tensor);
    }

    static Tensor rightShift(Collection transformations, Tensor tensor) {

        transformations = transformations.collect { if (it instanceof String) parse(it); else it; }

        if (isCollectionOfType(transformations, Expression))
            return new SubstitutionTransformation(transformations as Expression[]).transform(tensor);
        def t = tensor
        for (Transformation tr in transformations)
            t = tr.transform(t);
        return t;
    }

    static Tensor leftShift(Tensor tensor, Transformation transformation) {
        return transformation.transform(tensor);
    }

    static Tensor leftShift(Tensor tensor, Collection<Transformation> transformations) {
        if (isCollectionOfType(transformations, Expression))
            return new SubstitutionTransformation(transformations as Expression[]).transform(tensor);
        def t = tensor
        for (Transformation tr in transformations)
            t = tr.transform(t);
        return t;
    }

    /*
     * Type conversions
     */

    static Object asType(Collection collection, Class clazz) {
        if (clazz == Indices)
            return IndicesFactory.create(* collection)
        return DefaultGroovyMethods.asType(collection, clazz);
    }


    static Tensor asType(Number num, Class clazz) {
        if (clazz == Tensor)
            return number2Complex(num)
        return DefaultGroovyMethods.asType(num, clazz)
    }


    static Object asType(String string, Class clazz) {
        if (clazz == Tensor)
            return parse(string);
        return DefaultGroovyMethods.asType(string, clazz);
    }

    /*
     * Comparison
     */

    static boolean equals(Tensor a, Tensor b) {
        return TensorUtils.equals(a, b);
    }

    static MappingsPort mod(Tensor a, Tensor b) {
        return new IndexMappingBufferMappingsPort(IndexMappings.createPort(a, b));
    }

    static boolean asBoolean(MappingsPort port) {
        return port.take() != null;
    }

    static void each(MappingsPort port, Closure closure) {
        IndexMappingBuffer buffer;
        while ((buffer = port.take()) != null)
            closure.call(buffer);
    }

    static IndexMappingBuffer positive(MappingsPort port, Closure closure) {
        return port.take();
    }

    static final class IndexMappingBufferMappingsPort implements IndexMappingBuffer, MappingsPort {
        @Delegate
        IndexMappingBuffer indexMappingBuffer;
        @Delegate
        final MappingsPort mappingsPort;

        IndexMappingBufferMappingsPort(MappingsPort mappingsPort) {
            this.mappingsPort = mappingsPort;
        }

        IndexMappingBufferMappingsPort next() {
            indexMappingBuffer = mappingsPort.take();
            if (indexMappingBuffer == null)
                return null
            return this;
        }

        @Override
        public String toString() {
            return indexMappingBuffer.toString();
        }

        @Override
        IndexMappingBuffer clone() {
            throw new IllegalStateException('Clone() cannot be invoked on this class.');
        }
    }

    /*
     * Matrix descriptors
     */

    static MatrixDescriptor getCovector(IndexType type) {
        return new MatrixDescriptor(type, 0, 1);
    }

    static MatrixDescriptor getVector(IndexType type) {
        return new MatrixDescriptor(type, 1, 0);
    }

    static MatrixDescriptor getMatrix(IndexType type) {
        return new MatrixDescriptor(type, 1, 1);
    }

    static MatrixDescriptor tensor(IndexType type, int upper, int lower) {
        return new MatrixDescriptor(type, upper, lower);
    }

    /*
    * Apply mappings of indices. E.g. 'a -> b, _a -> ^b, ....' >> tensor
    */

    static Tensor rightShift(IndexMappingBuffer buffer, Tensor tensor) {
        return ApplyIndexMapping.applyIndexMapping(tensor, buffer);
    }

    /*
     * Parse
     */

    static Tensor getT(String string) {
        return parse(string)
    }

    static Tensor getT(Number string) {
        return number2Complex(string)
    }

    static Tensor getT(GString string) {
        return parse(string.toString())
    }

    static Indices getI(String string) {
        return ParserIndices.parseSimple(string)
    }

    /*
     * Tensor creation
     */

    static Tensor eq(String lhs, Tensor rhs) {
        return expression(parse(lhs), rhs)
    }

    static Tensor eq(Tensor lhs, Tensor rhs) {
        return expression(lhs, rhs)
    }

}
