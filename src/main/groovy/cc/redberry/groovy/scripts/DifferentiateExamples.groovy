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

import cc.redberry.core.transformations.ContractIndices
import cc.redberry.core.transformations.RemoveDueToSymmetry
import cc.redberry.core.transformations.Transformation
import cc.redberry.core.transformations.expand.Expand
import cc.redberry.core.utils.TensorUtils
import cc.redberry.groovy.RedberryGroovy

import static cc.redberry.core.tensor.Tensors.*
import static cc.redberry.core.transformations.Differentiate.differentiate
import static cc.redberry.groovy.RedberryGroovy.timing

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */

RedberryGroovy.withRedberry()

def var, tensor, result
var = parse('f_mn')
tensor = parse('Sin[f_ab*f^ab]')
println differentiate(tensor, var)

addAntiSymmetry('T_ab', 1, 0)
tensor = parse('T_mn')
var = parse('T_ab')
println differentiate(tensor, var)

addAntiSymmetry('R_abcd', 1, 0, 2, 3)
addSymmetry('R_abcd', 2, 3, 0, 1)
tensor = parse('R_mnab*R^pqnm*Sin[R_ijkv*R^ijkv]')
var1 = parse('R_abmn')
var2 = parse('R^pqmn')
def diff = differentiate(tensor, [Expand.EXPAND, ContractIndices.ContractIndices] as Transformation[], var1, var2)

//Performance

addAntiSymmetry('R_abcd', 1, 0, 2, 3)
addSymmetry('R_abcd', 2, 3, 0, 1)
tensor = parse('R^acbd*Sin[R_abcd*R^abcd]')
var1 = parse('R^ma_m^b')
var2 = parse('R^mc_m^d')

tr = [Expand.EXPAND,
        ContractIndices.ContractIndices] as Transformation[]

//burn the JVM
for (i in 1..10)
    (tr >> differentiate(tensor, var2, var1))

timing {
    diff1 = differentiate(tensor, var2, var1)
    diff1 = tr >> diff1
}

timing {
    diff2 = differentiate(tensor, tr as Transformation[], var2, var1)
}

assert TensorUtils.equals(diff1, diff2)

addSymmetry('R_ab', 1, 0)
tensor = parse('(R^sa_s^g*R^e_re^b - R^s_rs^g*R^ea_e^b)*(R^s_{gma}*R^r_{nsb}+R^s_{amg}*R^r_{snb})')
var1 = parse('R_mxn^x')
var2 = parse('R^r_y^ty')
tr = [
        Expand.EXPAND,
        ContractIndices.ContractIndices,
        parse('d_i^i = 4'),
        parse('R_ab^m_m = 0'),
        parse('R^a_cad = R_cd'),
        parse('R^a_a = R'),
        RemoveDueToSymmetry.INSTANCE
]

//burn the JVM
for (int i in 1..20)
    differentiate(tensor, var2, var1)

timing {
    diff1 = differentiate(tensor, var2, var1)
    diff1 = tr >> diff1
    println diff1
}

timing {
    diff2 = differentiate(tensor, tr[0..3] as Transformation[], var2, var1)
    diff2 = tr >> diff2
    println diff2
}


assert TensorUtils.equals(diff1, diff2)