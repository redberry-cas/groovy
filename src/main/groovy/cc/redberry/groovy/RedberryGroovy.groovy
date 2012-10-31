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

import cc.redberry.core.indices.IndexType
import cc.redberry.core.indices.Indices
import cc.redberry.core.indices.IndicesFactory
import cc.redberry.core.indices.SimpleIndices
import cc.redberry.core.number.Complex
import cc.redberry.core.parser.ParserIndices
import cc.redberry.core.tensor.Expression
import cc.redberry.core.tensor.Tensor
import cc.redberry.core.transformations.Transformation
import cc.redberry.core.transformations.substitutions.Substitution
import cc.redberry.core.utils.ArrayIterator
import cc.redberry.core.utils.IntArray

import static cc.redberry.core.tensor.Tensors.*

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
class RedberryGroovy {
    public static void withRedberry() {

        String.metaClass.asType {
            Class clazz ->
            switch (clazz) {
                case SimpleIndices:
                    return ParserIndices.parseSimple(delegate)
                case Indices:
                    return IndicesFactory.createSorted(ParserIndices.parse(delegate))
                case Tensor:
                    return parse(delegate)
                default:
                    return delegate.asType(clazz)
            }
        }

        Indices.metaClass.asType {
            Class clazz ->
            if (clazz == int[])
                delegate.allIndices.copy()
            else
                delegate.asType(clazz)
        }

        Indices.metaClass.getAt {
            IndexType type, int i -> delegate.get(type, i)
        }

        Indices.metaClass.getAt {
            int i -> delegate.get(i)
        }

        int[].metaClass.asType {
            Class clazz
            ->
            if (clazz == Indices)
                IndicesFactory.createSimple(null, delegate)
            else
                delegate.asType(clazz)
        }


        Transformation.metaClass.rightShift {
            if (it instanceof Tensor)
                return delegate.transform(it)
            else if (it instanceof Collection<Tensor>) {
                Collection r = []
                it.each { a -> r << delegate.transform(a)}
                return r
            }
            else
                throw new UnsupportedOperationException()
        }

        Transformation.metaClass.leftShift {
            it.transform(delegate)
        }

        Collection.metaClass.rightShift {
            if (delegate instanceof Collection<Expression>)
                return new Substitution((delegate as Expression[])) >> it
            else
                return delegate.rightShift(it)
        }

        Tensor.metaClass.multiply {
            b ->
            if (b instanceof Integer)
                return multiply(b, new Complex(b))
            else
                return multiply(delegate, b)
        }

        Tensor.metaClass.xor {
            int b -> pow(delegate, b)
        }

        Tensor.metaClass.minus {
            b -> sum(delegate, negate(b))
        }

        Tensor.metaClass.plus {
            b -> sum(delegate, b)
        }

        Integer.metaClass.multiply {
            b ->
            if (b instanceof Tensor)
                return multiply(new Complex(delegate), b)
            else
                return delegate.multiply(b)
        }

        IntArray.metaClass.getAt {
            int a -> delegate.get(a)
        }

        IntArray.metaClass.iterator() {
            new ArrayIterator<Integer>(delegate.copy())
        }

        IntArray.metaClass.asType {
            Class type ->
            if (type == SimpleIndices)
                return IndicesFactory.createSimple(null, delegate.copy())
            if (type == Indices)
                return IndicesFactory.createSorted(delegate.copy())
            return delegate.asType(type)
        }
    }
}

