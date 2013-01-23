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

import cc.redberry.core.context.CC
import cc.redberry.core.indices.IndexType
import cc.redberry.core.indices.StructureOfIndices
import cc.redberry.core.parser.ParseTokenSimpleTensor
import cc.redberry.core.parser.preprocessor.GeneralIndicesInsertion
import cc.redberry.core.tensor.SimpleTensor
import cc.redberry.core.tensor.Tensor
import cc.redberry.core.tensor.Tensors
import cc.redberry.core.transformations.*
import cc.redberry.core.transformations.expand.ExpandAllTransformation
import cc.redberry.core.transformations.expand.ExpandDenominatorTransformation
import cc.redberry.core.transformations.expand.ExpandNumeratorTransformation
import cc.redberry.core.transformations.expand.ExpandTransformation
import cc.redberry.core.transformations.factor.FactorTransformation
import cc.redberry.core.transformations.fractions.GetDenominatorTransformation
import cc.redberry.core.transformations.fractions.GetNumeratorTransformation
import cc.redberry.core.transformations.fractions.TogetherTransformation
import cc.redberry.core.utils.ByteBackedBitArray

import static cc.redberry.groovy.Redberry.number2Complex

class RedberryStatic {

    /**
     * Expands out products and positive integer powers.
     */
    public static final Transformation Expand = new Transformation() {
        @Override
        Tensor transform(Tensor t) { ExpandTransformation.expand(t) }

        Transformation getAt(Transformation... transformations) { new ExpandTransformation(transformations) }
    }

    /**
     * Expands out all products and integer powers in any part of expression.
     */
    public static final Transformation ExpandAll = new Transformation() {
        @Override
        Tensor transform(Tensor t) { ExpandAllTransformation.expandAll(t) }

        Transformation getAt(Transformation... transformations) { new ExpandAllTransformation(transformations) }
    }

    /**
     * Expands out products and powers that appear as denominators.
     */
    public static final Transformation ExpandNumerator = new Transformation() {
        @Override
        Tensor transform(Tensor t) { ExpandNumeratorTransformation.expandNumerator(t) }

        Transformation getAt(Transformation... transformations) { new ExpandNumeratorTransformation(transformations) }
    }

    /**
     * Expands out products and powers that appear in the numerator.
     */
    public static final Transformation ExpandDenominator = new Transformation() {
        @Override
        Tensor transform(Tensor t) { ExpandDenominatorTransformation.expandDenominator(t) }

        Transformation getAt(Transformation... transformations) { new ExpandDenominatorTransformation(transformations) }
    }

    /**
     * Gives a partial derivative.
     */
    public static final StaticDifferentiate Differentiate = new StaticDifferentiate();

    static class StaticDifferentiate {
        Transformation getAt(SimpleTensor var) {
            return new DifferentiateTransformation(var);
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
                return new DifferentiateTransformation(transformations as Transformation[], vars as SimpleTensor[]);
            }
        }
    }

    /**
     * Eliminates metrics and Kronecker deltas
     */
    public static final Transformation EliminateMetrics = EliminateMetricsTransformation.ELIMINATE_METRICS

    /**
     * Expands out product of sums and positive integer powers and
     * permanently eliminates metric and Kronecker deltas
     */
    public static final Transformation ExpandAndEliminate = new TransformationCollection(
            new ExpandTransformation(EliminateMetricsTransformation.ELIMINATE_METRICS),
            EliminateMetricsTransformation.ELIMINATE_METRICS)

    /**
     * Gives the numerator of expression.
     */
    public static final Transformation Numerator = GetNumeratorTransformation.GET_NUMERATOR

    /**
     * Gives the denominator of expression.
     */
    public static final Transformation Denominator = GetDenominatorTransformation.GET_DENOMINATOR

    /**
     * Removes parts of expressions, which are zero because of the symmetries (symmetric and antisymmetric at the same time).
     */
    public static final Transformation EliminateFromSymmetries = EliminateFromSymmetriesTransformation.ELIMINATE_FROM_SYMMETRIES;

    //todo incorporate with Factor
    /**
     * Puts terms in a sum over a common denominator, and cancels factors in the result.
     */
    public static final Transformation Together = TogetherTransformation.TOGETHER;

    /**
     * Puts terms in a sum over a common denominator, and cancels all symbolic factors in the result.
     */
    public static final Transformation TogetherFactor = TogetherTransformation.TOGETHER_FACTOR;

    /**
     * Replaces complex numbers in the expression to their complex conjugation.
     */
    public static final Transformation Conjugate = ComplexConjugateTransformation.COMPLEX_CONJUGATE;

    /**
     * Gives the numerical value of expression.
     */
    public static final Transformation Numeric = ToNumericTransformation.TO_NUMERIC;

    /**
     * Collects similar scalar factors in products.
     */
    public static final Transformation CollectScalars = CollectScalarFactorsTransformation.COLLECT_SCALAR_FACTORS

    /**
     * Puts terms in a sum together factoring out all scalars in each term.
     */
    public static final Transformation CollectNonScalars = CollectNonScalarsTransformation.COLLECT_NON_SCALARS;

    /**
     * Factors a polynomial over the integers.
     */
    public static final Transformation Factor = FactorTransformation.FACTOR;

    public static IndexType defaultMatrixType = IndexType.Matrix1

    private static GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
    static {
        CC.current().getParseManager().defaultParserPreprocessors.add(indicesInsertion);
    }

    /**
     * Matrices definition
     */
    //public static void defineMatrices(Collection<String> tensors, MatrixDescriptor... descriptors) {
    //    tensors.each { defineMatrices(it, descriptors) }
    //}

    public static void defineMatrices(Object... objs) {
        def bufferOfTensors = [], bufferOfDescriptors = [];
        objs.each { obj ->
            if (obj instanceof MatrixDescriptor)
                bufferOfDescriptors << obj
            else {
                if (bufferOfDescriptors) {
                    bufferOfTensors.each { it -> defineMatrices(it, * bufferOfDescriptors) }
                    bufferOfTensors = []
                    bufferOfDescriptors = []
                }
                bufferOfTensors << obj
            }
        }
        bufferOfTensors.each { it -> defineMatrices(it, * bufferOfDescriptors) }
        //int index = objs.findIndexOf { it instanceof MatrixDescriptor }
        //objs[0..<index].each { defineMatrices(it, * (objs[index..-1])) }
    }

    public static void defineMatrix(String tensor, MatrixDescriptor... descriptors) {
        ParseTokenSimpleTensor token = CC.current().parseManager.parser.parse(tensor);

        use(Redberry) {
            StructureOfIndices[] st = token.indicesTypeStructureAndName.structure;

            int[] allTypesCounts = st[0].typesCounts;
            def ByteBackedBitArray[] allStates = st[0].states;

            descriptors.each { descriptor ->
                def type = descriptor.type.type
                if (allTypesCounts[type] != 0)
                    throw new IllegalArgumentException()
                allTypesCounts[type] = descriptor.lower + descriptor.upper
                allStates[type] = new ByteBackedBitArray(allTypesCounts[type])
                for (int i = 0; i < descriptor.upper; ++i)
                    allStates[type].set(i)
            }
            st[0] = new StructureOfIndices(allTypesCounts, allStates);
            descriptors.each {
                indicesInsertion.addInsertionRule(CC.getNameManager().mapNameDescriptor(token.name, st),
                        it.type)
            }
        }
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
