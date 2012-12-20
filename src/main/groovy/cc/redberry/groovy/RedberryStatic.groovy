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

import cc.redberry.core.context.CC
import cc.redberry.core.context.NameDescriptor
import cc.redberry.core.context.NameDescriptorImpl
import cc.redberry.core.context.OutputFormat
import cc.redberry.core.indices.IndexType
import cc.redberry.core.indices.IndicesTypeStructure
import cc.redberry.core.indices.IndicesUtils
import cc.redberry.core.indices.SimpleIndices
import cc.redberry.core.parser.preprocessor.GeneralIndicesInsertion
import cc.redberry.core.tensor.SimpleTensor
import cc.redberry.core.tensor.Tensor
import cc.redberry.core.tensor.Tensors
import cc.redberry.core.transformations.ContractIndices
import cc.redberry.core.transformations.Differentiate
import cc.redberry.core.transformations.RemoveDueToSymmetry
import cc.redberry.core.transformations.Transformation
import cc.redberry.core.transformations.TransformationCollection
import cc.redberry.core.utils.ByteBackedBitArray

import static cc.redberry.groovy.Redberry.number2Complex

class RedberryStatic {

    /**
     * Expands out products and positive integer powers.
     */
    public static final Transformation Expand = new Transformation() {
        @Override
        Tensor transform(Tensor t) { cc.redberry.core.transformations.expand.Expand.expand(t) }

        Transformation getAt(Transformation... transformations) { new cc.redberry.core.transformations.expand.Expand(transformations) }
    }

    /**
     * Expands out all products and integer powers in any part of expression.
     */
    public static final Transformation ExpandAll = new Transformation() {
        @Override
        Tensor transform(Tensor t) { cc.redberry.core.transformations.expand.ExpandAll.expandAll(t) }

        Transformation getAt(Transformation... transformations) { new cc.redberry.core.transformations.expand.ExpandAll(transformations) }
    }

    /**
     * Expands out products and powers that appear as denominators.
     */
    public static final Transformation ExpandNumerator = new Transformation() {
        @Override
        Tensor transform(Tensor t) { cc.redberry.core.transformations.expand.ExpandNumerator.expandNumerator(t) }

        Transformation getAt(Transformation... transformations) { new cc.redberry.core.transformations.expand.ExpandNumerator(transformations) }
    }

    /**
     * Expands out products and powers that appear in the numerator.
     */
    public static final Transformation ExpandDenominator = new Transformation() {
        @Override
        Tensor transform(Tensor t) { cc.redberry.core.transformations.expand.ExpandDenominator.expandDenominator(t) }

        Transformation getAt(Transformation... transformations) { new cc.redberry.core.transformations.expand.ExpandDenominator(transformations) }
    }

    /**
     * Gives a partial derivative.
     */
    public static final StaticDifferentiate Differentiate = new StaticDifferentiate();

    static class StaticDifferentiate {
        Transformation getAt(SimpleTensor var) {
            return new Differentiate(var);
        }

        Transformation getAt(Collection args) {
            use(Redberry) {
                int i;
                def vars = []
                def transformations = []
                args.each { arg ->
                    if (arg instanceof String) arg = arg.t

                    if (arg instanceof SimpleTensor)
                        vars.add(arg)
                    else if (arg instanceof Transformation)
                        transformations.add(arg)
                    else
                        throw new IllegalArgumentException();
                }
                return new Differentiate(transformations as Transformation[], vars as SimpleTensor[]);
            }
        }
    }

    /**
     * Eliminates metrics and Kronecker deltas
     */
    public static final Transformation EliminateMetrics = ContractIndices.ContractIndices

    /**
     * Expands out product of sums and positive integer powers and
     * permanently eliminates metric and Kronecker deltas
     */
    public static final Transformation ExpandAndEliminate = new TransformationCollection(
            new cc.redberry.core.transformations.expand.Expand(ContractIndices.ContractIndices),
            ContractIndices.ContractIndices)

    /**
     * Gives the numerator of expression.
     */
    public static final Transformation Numerator = cc.redberry.core.transformations.fractions.Numerator.NUMERATOR

    /**
     * Gives the denominator of expression.
     */
    public static final Transformation Denominator = cc.redberry.core.transformations.fractions.Denominator.DENOMINATOR

    /**
     * Removes parts of expressions, which are zero because of the symmetries (symmetric and antisymmetric at the same time).
     */
    public static final Transformation EliminateFromSymmetries = RemoveDueToSymmetry.INSTANCE;

    //todo incorporate with Factor
    /**
     * Puts terms in a sum over a common denominator, and cancels factors in the result.
     */
    public static final Transformation Together = cc.redberry.core.transformations.fractions.Together.INSTANCE;

    /**
     * Replaces complex numbers in the expression to their complex conjugation.
     */
    public static final Transformation Conjugate = cc.redberry.core.transformations.ComplexConjugate.CONJUGATE;

    /**
     * Gives the numerical value of expression.
     */
    public static final Transformation Numeric = cc.redberry.core.transformations.ToNumeric.TO_NUMERIC;

    /**
     * Collects similar scalar factors in products.
     */
    public static final Transformation CollectScalars = cc.redberry.core.transformations.CollectScalarFactors.COLLECT_SCALAR_FACTORS

    /**
     * Puts terms in a sum together factoring out all scalars in each term.
     */
    public static final Transformation CollectNonScalars = cc.redberry.core.transformations.CollectNonScalars.CollectNonScalars;

    public static IndexType defaultMatrixType = IndexType.LatinLower1

    private static GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
    static {
        CC.current().getParseManager().defaultParserPreprocessors.add(indicesInsertion);
    }

    //todo refactor this block

    /**
     * Defines matrix
     */
    public static void setMatrix(matrix, Map<IndexType, Collection> types) {
        use(Redberry) {
            SimpleTensor temp = matrix instanceof String ? matrix.t : matrix
            NameDescriptor nd = CC.getNameDescriptor(temp.name)
            IndicesTypeStructure[] st = nd.getIndicesTypeStructures().clone();

            int[] allTypesCounts = st[0].typesCounts;
            ByteBackedBitArray[] allStates = st[0].states;

            types.each { type, ul ->
                type = type.getType()
                if (allTypesCounts[type] != 0)
                    throw new IllegalArgumentException();
                allTypesCounts[type] = ul[0] + ul[1]
                allStates[type] = new ByteBackedBitArray(ul[0] + ul[1])
                for (int i = 0; i < ul[0]; ++i)
                    allStates[type].set(i)
            }
            st[0] = new IndicesTypeStructure(allTypesCounts, allStates);
            types.keySet().each {
                indicesInsertion.addInsertionRule(CC.getNameManager().mapNameDescriptor(nd.getName(null), st), it)
            }
        }
    }

    public static void setMatrix(String matrix, IndexType matrixType) {
        setMatrix(matrix, [(matrixType): [1, 1]]);
    }

    public static void setVector(String vector, IndexType matrixType) {
        setMatrix(vector, [(matrixType): [1, 0]]);
    }

    public static void setCoVector(String covector, IndexType matrixType) {
        setMatrix(covector, [(matrixType): [0, 1]]);
    }

    public static void setVector(String vector) {
        setVector(vector, defaultMatrixType)
    }

    public static void setCoVector(String covector) {
        setCoVector(covector, defaultMatrixType)
    }

    public static void setMatrix(String matrix) {
        setMatrix(matrix, defaultMatrixType)
    }

    /**
     * Defines matrix
     */
    public static void setMatrices(String... matrices) {
        matrices.each { setMatrix(it, defaultMatrixType) }
    }

    /*
     * Utilities
     */

    public static long timing(Closure closure) {
        long start = System.currentTimeMillis();
        closure.call();
        long stop = System.currentTimeMillis();
        println('Time: ' + (stop - start) + ' ms.')
        return (stop - start)
    }

    /*
     * Math opertaions
     */

    static Tensor sin(Tensor a) { Tensors.sin(a); }

    static Tensor sin(Number a) { Tensors.sin(number2Complex(a)); }

    static Tensor cos(Tensor a) { Tensors.cos(a); }

    static Tensor cos(Number a) { Tensors.cos(number2Complex(a)); }

    static Tensor tan(Tensor a) { Tensors.tan(a); }

    static Tensor tan(Number a) { Tensors.tan(number2Complex(a)); }

    static Tensor cot(Tensor a) { Tensors.cot(a); }

    static Tensor cot(Number a) { Tensors.cot(number2Complex(a)); }

    static Tensor arcsin(Tensor a) { Tensors.arcsin(a); }

    static Tensor arcsin(Number a) { Tensors.arcsin(number2Complex(a)); }

    static Tensor arccos(Tensor a) { Tensors.arccos(a); }

    static Tensor arccos(Number a) { Tensors.arccos(number2Complex(a)); }

    static Tensor arctan(Tensor a) { Tensors.arctan(a); }

    static Tensor arctan(Number a) { Tensors.arctan(number2Complex(a)); }

    static Tensor arccot(Tensor a) { Tensors.arccot(a); }

    static Tensor arccot(Number a) { Tensors.arccot(number2Complex(a)); }
}
