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
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class RedberryPhysics {

    public static Transformation setMandelstam(Map<String, String> momentumMasses) {
        if (momentumMasses.size() != 4)
            throw new IllegalArgumentException();
        Tensor[][] result = new Tensor[4][2];
        int i = 0;
        momentumMasses.each { a, b -> result[i][0] = parse(a); result[i++][1] = parse(b); }
        return new TransformationCollection(FeynCalcUtils.setMandelstam(result));
    }

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

    public static final GUnitaryTrace UnitaryTrace = new GUnitaryTrace();

    static final class GUnitaryTrace {

        Transformation getAt(Collection args) {
            use(Redberry) {
                args = args.collect { if (it instanceof String) it.t else it }
                return new UnitaryTraceTransformation(* args);
            }
        }
    }

    public static final GUnitarySimplify UnitarySimplify = new GUnitarySimplify();

    static final class GUnitarySimplify {

        Transformation getAt(Collection args) {
            use(Redberry) {
                args = args.collect { if (it instanceof String) it.t else it }
                return new UnitarySimplifyTransformation(* args);
            }
        }
    }

    public static final GLeviCivita LeviCivitaSimplify = new GLeviCivita();

    static final class GLeviCivita {

        LeviCivitaSpace getMinkowski() {
            return new LeviCivitaSpace(true);
        }

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

    public static OneLoopCounterterms oneloopdiv2(Expression KInv,
                                                  Expression K,
                                                  Expression S,
                                                  Expression W,
                                                  Expression F) {
        OneLoopInput input = new OneLoopInput(2, KInv, K, S, W, null, null, F)
        return OneLoopCounterterms.calculateOneLoopCounterterms(input);
    }

    public static OneLoopCounterterms oneloopdiv2(Expression KInv,
                                                  Expression K,
                                                  Expression S,
                                                  Expression W,
                                                  Expression F,
                                                  Transformation transformation) {
        OneLoopInput input = new OneLoopInput(2, KInv, K, S, W, null, null, F, transformation)
        return OneLoopCounterterms.calculateOneLoopCounterterms(input);
    }


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
