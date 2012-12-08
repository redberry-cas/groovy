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

import cc.redberry.core.indexmapping.IndexMapping
import cc.redberry.core.indexmapping.IndexMappings
import cc.redberry.core.tensor.Product
import cc.redberry.core.tensor.SimpleTensor
import cc.redberry.core.tensor.Tensor
import cc.redberry.core.tensor.Tensors
import cc.redberry.core.tensor.iterator.TensorLastIterator
import cc.redberry.core.transformations.Transformation

import static cc.redberry.core.tensor.Tensors.expression

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
class InverseOrderOfGammas implements Transformation {
    public static final InverseOrderOfGammas InverseOrderOfGammas = new InverseOrderOfGammas();

    private final SimpleTensor GAMMA;

    private InverseOrderOfGammas(GAMMA) {
        this.GAMMA = GAMMA
    }

    InverseOrderOfGammas() {
        this.GAMMA = Tensors.parse(''' G_a^a'_b' ''')
    }

    @Override
    Tensor transform(Tensor t) {
        return inverseOrderOfGammas(t, GAMMA)
    }

    private static Tensor reverseProduct(Product tensor, SimpleTensor GAMMA) {
        int n = 0
        for (m in tensor)
            if (IndexMappings.getFirst(GAMMA, m) != null)
                ++n
        if (n == 0)
            return tensor
        def lhs = new StringBuilder()
        char a = 'a'
        for (; ;) {
            lhs << 'G_{' << (a++) << '}';
            if (((int) a - (int) 'a') == n)
                break;
            lhs << '*'
        }
        def rhs = new StringBuilder()
        for (; ;) {
            rhs << 'G_{' << (--a) << '}';
            if (((int) a - (int) 'a') == 0)
                break;
            rhs << '*'
        }
        rhs = Tensors.parse(rhs.toString())
        lhs = Tensors.parse(lhs.toString())
        return expression(lhs, rhs).transform(tensor)
    }

    static Tensor inverseOrderOfGammas(Tensor t, SimpleTensor GAMMA) {
        def iterator = new TensorLastIterator(t);
        def c;
        while ((c = iterator.next()) != null)
            if (c instanceof Product)
                iterator.set(reverseProduct(c, GAMMA))
        return iterator.result()
    }
}
