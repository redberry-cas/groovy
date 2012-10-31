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
import cc.redberry.core.indices.SimpleIndices
import cc.redberry.core.parser.ParserIndices

import static cc.redberry.core.indices.IndexType.LatinLower
import static cc.redberry.core.tensor.Tensors.addSymmetry
import static cc.redberry.core.tensor.Tensors.parse
import static cc.redberry.core.tensor.Tensors.setSymmetric
import static cc.redberry.core.utils.TensorUtils.findIndicesSymmetries
import static cc.redberry.core.tensor.Tensors.addAntiSymmetry
import cc.redberry.groovy.RedberryGroovy

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */

RedberryGroovy.withRedberry()

addSymmetry('R_ab', LatinLower, true, 1, 0)
def t = parse('R_ab*A_c+R_bc*A_a')
def symmetries = findIndicesSymmetries('_abc' as SimpleIndices, t)
for (s in symmetries)
    println s

addSymmetry('R_abc', LatinLower, true, 1, 0, 2)
addSymmetry('A_ab', LatinLower, false, 1, 0)
t = parse('(R_abc*A_de+R_bde*A_ac)*A^ce + R_adb')
symmetries = findIndicesSymmetries('_abd' as SimpleIndices, t)
for (s in symmetries)
    println s

//def st = parse('F_abd')
//for (s in symmetries.basis)
//    st.indices.symmetries.add(LatinLower, s)
//
//
//t = parse('R_abcdef')
//setSymmetric(t, LatinLower)
//symmetries = t.indices.symmetries
//symmetries.each {s -> println s }
//symmetries.basis.each {s -> println s }
