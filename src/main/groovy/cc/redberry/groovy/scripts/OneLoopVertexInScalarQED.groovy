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

import cc.redberry.core.transformations.Transformation

import static cc.redberry.core.tensor.Tensors.parse
import cc.redberry.core.transformations.Expand

import static cc.redberry.core.transformations.Expand.expand
import cc.redberry.core.transformations.Together

import static cc.redberry.core.transformations.Together.together
import cc.redberry.core.transformations.ContractIndices

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */

Transformation.metaClass.rightShift {
    delegate.transform(it)
}
Transformation.metaClass.leftShift {
    it.transform(delegate)
}
List.metaClass.rightShift {
    a = it
    for (b in delegate)
        a = a << b
    a
}

//photon-scalar-scalar vertex
def V1 = parse("V_{i}[p_a, q_b] = -I*e*(p_i+q_i)")
//scalar propagator
def P = parse("D[k_a] = -I/(k^a*k_a-m**2)")
//photon propagator
def D = parse("D_mn[k_a] = -I*g_mn/(k^a*k_a)")
//matrix element
def Loop = parse('L^i = V_{m}[-k_a,-p_a]*D^{mn}[p_a-k_a]*D[k_a]*V^{i}[k_a,k_a-p_a+q_a]*D[k_a-p_a+q_a]*V_{n}[-q_a,-k_a+p_a-q_a]')

Loop = Loop << D << P << V1
Loop = together(expand(Loop, ContractIndices.ContractIndices))
println Loop
