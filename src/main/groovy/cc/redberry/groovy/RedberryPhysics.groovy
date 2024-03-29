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

import cc.redberry.core.indices.IndexType
import cc.redberry.core.tensor.Expression
import cc.redberry.core.tensor.SimpleTensor
import cc.redberry.core.tensor.Tensor
import cc.redberry.core.transformations.Transformation
import cc.redberry.core.transformations.TransformationCollection
import cc.redberry.physics.feyncalc.*
import cc.redberry.physics.oneloopdiv.OneLoopCounterterms
import cc.redberry.physics.oneloopdiv.OneLoopInput

import static cc.redberry.core.tensor.Tensors.parse
import static cc.redberry.core.tensor.Tensors.parseSimple

/**
 * Groovy facade for transformations and utility methods from redberry-physics.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class RedberryPhysics {

    /**
     * Returns mandelstam and mass shell substitutions following from the provided map
     * of "momentum - mass of particle".
     *
     * @param momentumMasses "momentum - mass of particle"
     * @return resulting substitutions
     */
    public static Transformation setMandelstam(Map<String, String> momentumMasses) {
        if (momentumMasses.size() != 4)
            throw new IllegalArgumentException();
        Tensor[][] result = new Tensor[4][2];
        int i = 0;
        momentumMasses.each { a, b -> result[i][0] = parse(a); result[i++][1] = parse(b); }
        return new TransformationCollection(FeynCalcUtils.setMandelstam(result));
    }

    /**
     * Calculates trace of Dirac matrices in four dimensions.
     * @see DiracTraceTransformation
     */
    public static final GDiracTrace DiracTrace = new GDiracTrace();

    static final class GDiracTrace {

        Transformation getAt(String gamma) {
            use(Redberry) {
                return new DiracTraceTransformation(gamma.t);
            }
        }

        Transformation getAt(SimpleTensor gamma) {
            return new DiracTraceTransformation(gamma);
        }

        Transformation getAt(Collection args) {
            use(Redberry) {
                args = args.collect { if (it instanceof String) it.t else it }
                return new DiracTraceTransformation(* args);
            }
        }
    }

    /**
     * Calculates trace of unitary matrices
     * @see UnitaryTraceTransformation
     */
    public static final GUnitaryTrace UnitaryTrace = new GUnitaryTrace();

    static final class GUnitaryTrace {

        Transformation getAt(Collection args) {
            use(Redberry) {
                args = args.collect { if (it instanceof String) it.t else it }
                return new UnitaryTraceTransformation(* args);
            }
        }
    }

    /**
     * Simplifies combinations of unitary matrices
     * @see UnitarySimplifyTransformation
     */
    public static final GUnitarySimplify UnitarySimplify = new GUnitarySimplify();

    static final class GUnitarySimplify {

        Transformation getAt(Collection args) {
            use(Redberry) {
                args = args.collect { if (it instanceof String) it.t else it }
                return new UnitarySimplifyTransformation(* args);
            }
        }
    }

    /**
     * Simplifies combinations of Levi-Civita tensors.
     * @see LeviCivitaSimplifyTransformation
     */
    public static final GLeviCivita LeviCivitaSimplify = new GLeviCivita();

    static final class GLeviCivita {

        /**
         * Simplifies in Minkowski space
         * @return transformation in Minkowski space
         */
        LeviCivitaSpace getMinkowski() {
            return new LeviCivitaSpace(true);
        }
        /**
         * Simplifies in Euclidean space
         * @return transformation in Euclidean space
         */
        LeviCivitaSpace getEuclidean() {
            return new LeviCivitaSpace(false);
        }
    }

    static final class LeviCivitaSpace {
        final boolean minkowskiSpace;

        LeviCivitaSpace(boolean minkowskiSpace) {
            this.minkowskiSpace = minkowskiSpace
        }

        Transformation getAt(String leviCivita) {
            return new LeviCivitaSimplifyTransformation(parseSimple(leviCivita), minkowskiSpace);
        }

        Transformation getAt(SimpleTensor leviCivita) {
            return new LeviCivitaSimplifyTransformation(leviCivita, minkowskiSpace);
        }
    }

    /**
     * Inverse matrices of specified matrix type.
     * @see InverseOrderOfMatricesTransformation
     */
    public static final GInverseOrderOfMatrices InverseOrderOfMatrices = new GInverseOrderOfMatrices();

    static final class GInverseOrderOfMatrices {

        Transformation getAt(Object... types) {
            List<Transformation> tr = new ArrayList<>();

            for (Object type : types)
                if (type instanceof IndexType)
                    tr.add(new InverseOrderOfMatricesTransformation(type));
                else if (type instanceof Collection)
                    for (IndexType type1 : type)
                        tr.add(new InverseOrderOfMatricesTransformation(type1));

            return new TransformationCollection(tr);
        }

        Transformation getAt(IndexType type) {
            new InverseOrderOfMatricesTransformation(type)
        }
    }

    /*
     * One-loop calculations
     */

    /**
     * Calculates one-loop counterterms of second order operator.
     *
     * @param KInv inverse of {@code Kn} tensor. The input
     *                      expression should be in the form {@code KINV^{...}_{...} = ...}.
     * @param K tensor {@code K} in the form {@code K^{...}_{...} = ....}.
     * @param S tensor {@code S}. Since odd terms in operator expansion
     *                      is not supported yet, this tensor should be zeroed, so
     *                      the r.h.s. of the expression should be always zero:
     * {@code S^{...}_{...} = 0}.
     * @param W tensor {@code W} in the form {@code W^{...}_{...} = ....}.
     * @param F tensor {@code F} in the form {@code F^{...}_{...} = ....}.
     * @throws IllegalArgumentException if {@code operatorOrder} is not eqaul to 2 or 4
     * @throws IllegalArgumentException if {@code S} or {@code N} are not zeroed
     * @throws IllegalArgumentException if some of the input tensors have name different
     *                                  from the specified
     * @throws IllegalArgumentException if indices number of some of the input tensors
     *                                  does not corresponds to the actual {@code operatorOrder}
     * @throws IllegalArgumentException if indices of l.h.s. of input expressions contains non Greek lowercase indices.
     * @see OneLoopInput
     * @see OneLoopCounterterms
     */
    public static OneLoopCounterterms oneloopdiv2(Expression KInv,
                                                  Expression K,
                                                  Expression S,
                                                  Expression W,
                                                  Expression F) {
        OneLoopInput input = new OneLoopInput(2, KInv, K, S, W, null, null, F)
        return OneLoopCounterterms.calculateOneLoopCounterterms(input);
    }

    /**
     * Calculates one-loop counterterms of second order operator.
     *
     * @param KInv inverse of {@code Kn} tensor. The input
     *                      expression should be in the form {@code KINV^{...}_{...} = ...}.
     * @param K tensor {@code K} in the form {@code K^{...}_{...} = ....}.
     * @param S tensor {@code S}. Since odd terms in operator expansion
     *                      is not supported yet, this tensor should be zeroed, so
     *                      the r.h.s. of the expression should be always zero:
     * {@code S^{...}_{...} = 0}.
     * @param W tensor {@code W} in the form {@code W^{...}_{...} = ....}.
     * @param F tensor {@code F} in the form {@code F^{...}_{...} = ....}.
     * @param transformation additional background conditions, such as anti de Sitter etc.
     * @throws IllegalArgumentException if {@code operatorOrder} is not eqaul to 2 or 4
     * @throws IllegalArgumentException if {@code S} or {@code N} are not zeroed
     * @throws IllegalArgumentException if some of the input tensors have name different
     *                                  from the specified
     * @throws IllegalArgumentException if indices number of some of the input tensors
     *                                  does not corresponds to the actual {@code operatorOrder}
     * @throws IllegalArgumentException if indices of l.h.s. of input expressions contains non Greek lowercase indices.
     * @see OneLoopInput
     * @see OneLoopCounterterms
     */
    public static OneLoopCounterterms oneloopdiv2(Expression KInv,
                                                  Expression K,
                                                  Expression S,
                                                  Expression W,
                                                  Expression F,
                                                  Transformation transformation) {
        OneLoopInput input = new OneLoopInput(2, KInv, K, S, W, null, null, F, transformation)
        return OneLoopCounterterms.calculateOneLoopCounterterms(input);
    }

    /**
     * Calculates one-loop countertemrs of the fourth order operator
     *
     * @param KInv inverse of {@code Kn} tensor. The input
     *                      expression should be in the form {@code KINV^{...}_{...} = ...}.
     * @param K tensor {@code K} in the form {@code K^{...}_{...} = ....}.
     * @param S tensor {@code S}. Since odd terms in operator expansion
     *                      is not supported yet, this tensor should be zeroed, so
     *                      the r.h.s. of the expression should be always zero:
     * {@code S^{...}_{...} = 0}.
     * @param W tensor {@code W} in the form {@code W^{...}_{...} = ....}.
     * @param N tensor {@code N}. Since odd terms in operator expansion
     *                      is not supported yet, this tensor should be zeroed, so
     *                      the r.h.s. of the expression should be always zero:
     * {@code N^{...}_{...} = 0}. <b>Note:</b> if
     * {@code operatorOrder = 2} this param should be {@code null}.
     * @param M tensor {@code M} in the form {@code M^{...}_{...} = ....}.
     *                      <b>Note:</b> if {@code operatorOrder = 2} this param
     *                      should be {@code null}                                    .
     * @param F tensor {@code F} in the form {@code F^{...}_{...} = ....}.
     * @throws IllegalArgumentException if {@code operatorOrder} is not eqaul to 2 or 4
     * @throws IllegalArgumentException if {@code S} or {@code N} are not zeroed
     * @throws IllegalArgumentException if some of the input tensors have name different
     *                                  from the specified
     * @throws IllegalArgumentException if indices number of some of the input tensors
     *                                  does not corresponds to the actual {@code operatorOrder}
     * @throws IllegalArgumentException if indices of l.h.s. of input expressions contains non Greek lowercase indices.
     * @see OneLoopInput
     * @see OneLoopCounterterms
     */
    public static OneLoopCounterterms oneloopdiv4(Expression KInv,
                                                  Expression K,
                                                  Expression S,
                                                  Expression W,
                                                  Expression N,
                                                  Expression M,
                                                  Expression F) {
        OneLoopInput input = new OneLoopInput(4, KInv, K, S, W, N, M, F)
        return OneLoopCounterterms.calculateOneLoopCounterterms(input);
    }

    /**
     * Calculates one-loop countertemrs of the fourth order operator
     *
     * @param KInv inverse of {@code Kn} tensor. The input
     *                      expression should be in the form {@code KINV^{...}_{...} = ...}.
     * @param K tensor {@code K} in the form {@code K^{...}_{...} = ....}.
     * @param S tensor {@code S}. Since odd terms in operator expansion
     *                      is not supported yet, this tensor should be zeroed, so
     *                      the r.h.s. of the expression should be always zero:
     * {@code S^{...}_{...} = 0}.
     * @param W tensor {@code W} in the form {@code W^{...}_{...} = ....}.
     * @param N tensor {@code N}. Since odd terms in operator expansion
     *                      is not supported yet, this tensor should be zeroed, so
     *                      the r.h.s. of the expression should be always zero:
     * {@code N^{...}_{...} = 0}. <b>Note:</b> if
     * {@code operatorOrder = 2} this param should be {@code null}.
     * @param M tensor {@code M} in the form {@code M^{...}_{...} = ....}.
     *                      <b>Note:</b> if {@code operatorOrder = 2} this param
     *                      should be {@code null}                                    .
     * @param F tensor {@code F} in the form {@code F^{...}_{...} = ....}.
     * @param transformation additional background conditions, such as anti de Sitter etc.
     * @throws IllegalArgumentException if {@code operatorOrder} is not eqaul to 2 or 4
     * @throws IllegalArgumentException if {@code S} or {@code N} are not zeroed
     * @throws IllegalArgumentException if some of the input tensors have name different
     *                                  from the specified
     * @throws IllegalArgumentException if indices number of some of the input tensors
     *                                  does not corresponds to the actual {@code operatorOrder}
     * @throws IllegalArgumentException if indices of l.h.s. of input expressions contains non Greek lowercase indices.
     * @see OneLoopInput
     * @see OneLoopCounterterms
     */
    public static OneLoopCounterterms oneloopdiv4(Expression KInv,
                                                  Expression K,
                                                  Expression S,
                                                  Expression W,
                                                  Expression N,
                                                  Expression M,
                                                  Expression F,
                                                  Transformation transformation) {
        OneLoopInput input = new OneLoopInput(4, KInv, K, S, W, N, M, F, transformation)
        return OneLoopCounterterms.calculateOneLoopCounterterms(input);
    }
}
