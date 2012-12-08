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

package cc.redberry.groovy.scripts

import cc.redberry.groovy.InverseOrderOfGammas
import cc.redberry.groovy.Redberry
import cc.redberry.core.parser.preprocessor.GeneralIndicesInsertion
import cc.redberry.core.context.CC

import static cc.redberry.core.tensor.Tensors.parse
import static cc.redberry.core.indices.IndexType.LatinLower1
import static cc.redberry.core.indices.IndexType.LatinUpper1
import static cc.redberry.core.tensor.Tensors.expression
import cc.redberry.core.tensor.Tensor
import cc.redberry.core.tensor.Product

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
use(Redberry) {
    GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
    CC.current().parseManager.defaultParserPreprocessors.add(indicesInsertion);
    def setMatrix = {
        a, b ->
            if (a instanceof String)
                a = parse(a)
            indicesInsertion.addInsertionRule(a, b)
    }

    setMatrix(''' G_a^a'_b' ''', LatinLower1)
    setMatrix(''' pv_b' ''', LatinLower1)
    setMatrix(''' v^b' ''', LatinLower1)

    println InverseOrderOfGammas.InverseOrderOfGammas >> 'pv*G_a*G_b*v'.t
}