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

import cc.redberry.core.tensor.Tensor
import cc.redberry.core.tensor.Product
import gnu.trove.map.hash.TIntObjectHashMap
import cc.redberry.core.tensor.SimpleTensor
import cc.redberry.core.tensor.Tensors

import static cc.redberry.core.tensor.Tensors.multiply

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
class PrettyMatrixPrint {
    private TIntObjectHashMap<SimpleTensor> matrices = new TIntObjectHashMap<SimpleTensor>();

    public void addMatrix(SimpleTensor tensor) {
        if (!matrices.containsKey(tensor.name))
            matrices.put(tensor.name, tensor)
    }

    public void leftShift(t) {
        if (t instanceof Collection)
            for (c in t)
                addMatrix(c)
        else addMatrix(t)
    }

    String toStringProduct(Product product) {
        def matrices = [], tensors = []
        for (Tensor f in product)
            isMatrix(f) ? matrices << f : tensors << f
        def matrixPart = multiply(matrices as Tensor[])

    }

    boolean isMatrix(Tensor t) {
        if (t instanceof SimpleTensor)
            matrices.contains(t.name)
        else false
    }

}
