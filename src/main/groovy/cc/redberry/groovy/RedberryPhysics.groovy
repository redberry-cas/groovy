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

import cc.redberry.core.tensor.SimpleTensor
import cc.redberry.core.tensor.Tensor
import cc.redberry.core.transformations.Transformation
import cc.redberry.core.transformations.TransformationCollection
import cc.redberry.physics.feyncalc.FeynCalcUtils

import static cc.redberry.core.tensor.Tensors.parse

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

    static class GDiracTrace implements Transformation {
        @Override
        Tensor transform(Tensor t) {
            return cc.redberry.physics.feyncalc.DiracTrace.trace(t)
        }

        Transformation getAt(String gamma) {
            use(Redberry) {
                return new cc.redberry.physics.feyncalc.DiracTrace(gamma.t);
            }
        }

        Transformation getAt(SimpleTensor gamma) {
            return new cc.redberry.physics.feyncalc.DiracTrace(gamma);
        }

        Transformation getAt(Collection args) {
            use(Redberry) {
                args = args.collect { if (it instanceof String) it.t else it }
                return new cc.redberry.physics.feyncalc.DiracTrace(* args);
            }
        }
    }

    public static final GUnitaryTrace UnitaryTrace = new GUnitaryTrace();

    static class GUnitaryTrace implements Transformation {
        @Override
        Tensor transform(Tensor t) {
            return cc.redberry.physics.feyncalc.UnitaryTrace.unitaryTrace(t)
        }

        Transformation getAt(Collection args) {
            use(Redberry) {
                args = args.collect { if (it instanceof String) it.t else it }
                return new cc.redberry.physics.feyncalc.UnitaryTrace(* args);
            }
        }
    }

    public static final GLeviCivita LeviCivitaSimplify = new GLeviCivita();

    static class GLeviCivita implements Transformation {
        @Override
        Tensor transform(Tensor t) {
            use(Redberry) {
                return cc.redberry.physics.feyncalc.LeviCivitaSimplify.simplifyLeviCivita(t, 'e_abcd'.t)
            }
        }

        Transformation getAt(leviCivita) {
            use(Redberry) {
                if (leviCivita instanceof String) leviCivita = leviCivita.t
                return new cc.redberry.physics.feyncalc.LeviCivitaSimplify(leviCivita);
            }
        }
    }

    public static final GInverseOrderOfGammas InverseOrderOfGammas = new GInverseOrderOfGammas();

    static class GInverseOrderOfGammas implements Transformation {
        @Override
        Tensor transform(Tensor t) {
            use(Redberry) {
                return TrInverseOrderOfGammas.inverseOrderOfGammas(t, 'G_a'.t)
            }
        }

        Transformation getAt(gamma) {
            use(Redberry) {
                if (gamma instanceof String) gamma = gamma.t
                return new TrInverseOrderOfGammas(gamma);
            }
        }
    }


}
