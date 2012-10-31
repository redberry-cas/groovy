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

package cc.redberry.groovy.scripts

import cc.redberry.core.indices.Indices
import cc.redberry.groovy.RedberryGroovy

import static cc.redberry.core.indices.IndexType.LatinLower
import static cc.redberry.core.indices.IndicesUtils.areContracted
import static cc.redberry.core.indices.IndicesUtils.getNameWithType
import static cc.redberry.core.tensor.Tensors.parse

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */


RedberryGroovy.withRedberry()

def getDummy = {
    t1, t2, indexType ->
    def ind1 = t1.indices.free,
        ind2 = t2.indices.free,
        dummy = []
    for (def i in 0..ind1.size(indexType) - 1)
        for (def j in 0..ind2.size(indexType) - 1)
            if (areContracted(ind1[indexType, i], ind2[indexType, j]))
                dummy << getNameWithType(ind1[indexType, i])
    dummy as int[]
}

println getDummy(parse("a*T_mn"), parse("T^mna"), LatinLower) as Indices
