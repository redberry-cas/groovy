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
import cc.redberry.core.transformations.ContractIndices
import cc.redberry.core.transformations.Differentiate
import cc.redberry.core.transformations.Transformation

class RedberryStatic {

    /*
     * Expand transformations
     */

    public static final Transformation Expand = new Transformation() {
        @Override
        Tensor transform(Tensor t) { cc.redberry.core.transformations.expand.Expand.expand(t) }

        Transformation getAt(Transformation... transformations) { new cc.redberry.core.transformations.expand.Expand(transformations) }
    }

    public static final Transformation ExpandAll = new Transformation() {
        @Override
        Tensor transform(Tensor t) { cc.redberry.core.transformations.expand.ExpandAll.expandAll(t) }

        Transformation getAt(Transformation... transformations) { new cc.redberry.core.transformations.expand.ExpandAll(transformations) }
    }


    public static final Transformation ExpandNumerator = new Transformation() {
        @Override
        Tensor transform(Tensor t) { cc.redberry.core.transformations.expand.ExpandNumerator.expandNumerator(t) }

        Transformation getAt(Transformation... transformations) { new cc.redberry.core.transformations.expand.ExpandNumerator(transformations) }
    }

    public static final Transformation ExpandDenominator = new Transformation() {
        @Override
        Tensor transform(Tensor t) { cc.redberry.core.transformations.expand.ExpandDenominator.expandDenominator(t) }

        Transformation getAt(Transformation... transformations) { new cc.redberry.core.transformations.expand.ExpandDenominator(transformations) }
    }

    /*
     * Differentiate transformation
     */

    public static final StaticDifferentiate Differentiate = new StaticDifferentiate();

    static class StaticDifferentiate {
        Transformation getAt(Tensor... s) {
            return new Differentiate(s as SimpleTensor[]);
        }

        Transformation getAt(Transformation transformations, Tensor... s) {
            return new Differentiate(transformations, s as SimpleTensor[]);
        }

        Transformation getAt(String s) {
            use(Redberry) {
                return new Differentiate(s.t);
            }
        }

        Transformation getAt(String... s) {
            use(Redberry) {
                return new Differentiate(s.collect { it.t } as SimpleTensor[]);
            }
        }

        Transformation getAt(Transformation transformations, String s) {
            use(Redberry) {
                return new Differentiate(transformations, s.t);
            }
        }

        Transformation getAt(Transformation transformations, String... s) {
            use(Redberry) {
                return new Differentiate(transformations, s.collect { it.t } as SimpleTensor[]);
            }
        }
    }

    /**
     * Eliminates metrics & Kronecker deltas
     */
    public static final Transformation EliminateMetrics = ContractIndices.ContractIndices

}
