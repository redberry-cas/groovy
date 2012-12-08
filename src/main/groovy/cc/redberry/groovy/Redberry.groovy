/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2012:
 *   Stanislav Poslavsky   <stvlpos@mail.ru>
 *   Bolotin Dmitriy       <bolotin.dmitriy@gmail.com>
 *
 * This file is part of Redberry.
 *
 * Redberry is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
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
import cc.redberry.core.tensor.Expression
import cc.redberry.core.tensor.Tensor
import cc.redberry.core.tensor.Tensors
import cc.redberry.core.tensor.iterator.TensorFirstIterator
import cc.redberry.core.tensor.iterator.TensorLastIterator
import cc.redberry.core.transformations.Transformation
import cc.redberry.core.transformations.substitutions.Substitution
import cc.redberry.core.utils.TensorUtils

import static cc.redberry.core.tensor.Tensors.*
import cc.redberry.core.transformations.TransformationCollection
import org.codehaus.groovy.runtime.DefaultGroovyMethods
import cc.redberry.core.tensor.ApplyIndexMapping

import cc.redberry.core.tensor.iterator.TraverseGuide
import cc.redberry.core.transformations.substitutions.SubstitutionIterator

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
class Redberry {

    /*
    * Arithmetic operations
    */

    static Tensor plus(Tensor a, Tensor b) {
        return sum(a, b);
    }

    static Tensor plus(Tensor a, Number b) {
        //todo
        return sum(a, b);
    }

    static Tensor multiply(Tensor a, Tensor b) {
        return multiplyAndRenameConflictingDummies(a, b);
    }

    static Tensor negative(Tensor a) {
        return negate(a);
    }

    static Tensor positive(Tensor a) {
        return a;
    }

    static Tensor sin(Tensor a) {
        return Tensors.sin(a);
    }

    static Tensor getAt(Tensor a, int position) {
        return a.get(position);
    }

    static Tensor getAt(Tensor a, int ... position) {
        //todo
        return a.get(position);
    }

    /*
    * Tensor traversing
    */

    static Tensor eachInTree(Tensor t, Object guide, Closure<Tensor> closure) {
        TensorLastIterator iterator = new TensorLastIterator(t, guide);
        Tensor c;
        while ((c = iterator.next()) != null)
            closure.call(c);
        return iterator.result();
    }

    static Tensor eachInTreeReverse(Tensor t, TraverseGuide guide, Closure<Tensor> closure) {
        TensorFirstIterator iterator = new TensorFirstIterator(t, guide);
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

        transformations = transformations.collect { if (it instanceof String) parse(it) else it }

        if (isCollectionOfType(transformations, Expression))
            return new Substitution(transformations as Expression[]).transform(tensor);
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
            return new Substitution(transformations as Expression[]).transform(tensor);
        def t = tensor
        for (Transformation tr in transformations)
            t = tr.transform(t);
        return t;
    }

    static Object asType(Collection collection, Class clazz) {
        if (clazz == Transformation)
            return new TransformationCollection(collection.collect { if (it instanceof String) parse(it) else it })
        return DefaultGroovyMethods.asType(collection, clazz);
    }

    /*
   * Comparison
    */

    static boolean equals(Tensor a, Tensor b) {
        return TensorUtils.equals(a, b);
    }


    static boolean equals(Tensor a, String b) {
        return TensorUtils.equals(a, parse(b));
    }

    static boolean equals(String b, Tensor a) {
        return TensorUtils.equals(a, parse(b));
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
    * Apply mappings of indices. E.g. 'a -> b, _a -> ^b, ....' >> tensor
    */

    static Tensor rightShift(IndexMappingBuffer buffer, Tensor tensor) {
        return ApplyIndexMapping.applyIndexMapping(tensor, buffer);
    }

    /*
    * Type conversions
    */

    static Object asType(String string, Class clazz) {
        if (clazz == Tensor)
            return parse(string);
        return string.asType(clazz);
    }

    /*
     * Parse
     */

    static Tensor getT(String string) {
        return parse(string)
    }

    static Tensor eq(String string, Tensor tensor) {
        return expression(parse(string), tensor)
    }

    static Tensor getT(GString string) {
        return parse(string.toString())
    }

    /*
    * Utility static functions
    */

    public static void timing(Closure closure) {
        long start = System.currentTimeMillis();
        closure.call();
        long stop = System.currentTimeMillis();
        println('Time: ' + (stop - start) + ' ms.')
    }


}
