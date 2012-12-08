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

package cc.redberry.groovy.scripts.chi

import static cc.redberry.core.tensor.Tensors.parse
import static cc.redberry.core.tensor.Tensors.parse
import static cc.redberry.core.tensor.Tensors.parse
import static cc.redberry.core.tensor.Tensors.parse
import static cc.redberry.core.tensor.Tensors.parse
import static cc.redberry.core.tensor.Tensors.parse
import static cc.redberry.core.tensor.Tensors.addSymmetry
import static cc.redberry.core.tensor.Tensors.parse
import static cc.redberry.core.tensor.Tensors.addSymmetry
import cc.redberry.groovy.RedberryGroovy

import static cc.redberry.physics.feyncalc.FeynCalcUtils.setMandelstam
import cc.redberry.core.transformations.fractions.Together
import cc.redberry.core.tensor.SimpleTensor
import cc.redberry.core.number.Complex
import cc.redberry.physics.feyncalc.FeynCalcUtils
import cc.redberry.core.tensor.Tensor
import cc.redberry.core.transformations.expand.Expand
import cc.redberry.core.transformations.Transformation

import static cc.redberry.core.transformations.expand.Expand.expand
import cc.redberry.core.transformations.ContractIndices

import static cc.redberry.core.transformations.ContractIndices.contract
import static cc.redberry.core.transformations.fractions.Together.together
import cc.redberry.core.transformations.expand.ExpandNumerator

import static cc.redberry.core.transformations.expand.ExpandNumerator.expandNumerator
import cc.redberry.core.utils.TensorUtils

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
RedberryGroovy.withRedberry()
def setMandelstam = {
    List list ->
    //parsing strings if they are not already parsed
    list = list.collect {
        [it[0] instanceof String ? parse(it[0]) : it[0],
                it[1] instanceof String ? parse(it[1]) : it[1]]
    }
    list.each { e -> e.each { ee -> assert ee instanceof SimpleTensor || Complex } }
    FeynCalcUtils.setMandelstam(list as Tensor[][]) as List
}
def J = parse('J_ab = - g_ab + P_a*P_b/(2*m)**2 ')

def JT = [parse('S_m = k1_m+k2_m'),
        parse('a = -1/2'),
        parse('b = (4*m**2 + s)/(s-4*m**2)**2'),
        parse('c = -2*s/(s-4*m**2)**2'),
        parse('d = -8*m**2/(s-4*m**2)**2')] >> parse('JT_mn = a*g_mn + b*(P_m*S_n + P_n*S_m) + c*P_m*P_n + d*S_m*S_n')
addSymmetry('JT_mn', 1, 0)
def JL = [J, JT] >> parse('JL_ab = J_ab - JT_ab')
addSymmetry('JL_mn', 1, 0)

def mandelstam = setMandelstam([['k1_m', '0'], ['k2_m', '0'], ['k3_m', '0'], ['P_m', '2*m']])

def combintaions = [parse('P_m*JT^mn'), parse('(k1_m+k2_m)*JT^mn'), parse("JT_mn*JT^ma + JT_n^a/2")]
for (a in combintaions) {
    a = JT >> a
    a = expand(a)
    a = contract(a)
    a = mandelstam >> a
    a = together(a)
    a = parse('u = 4*m**2-s-t') >> a
    a = expandNumerator(a)
    assert TensorUtils.isZero(a)
}
def a = parse('(k1_m+k2_m)*JL^mn')
a = JL >> a
a = expand(a)
a = contract(a)
a = mandelstam >> a
a = together(a)
a = parse('u = 4*m**2-s-t') >> a
a = expandNumerator(a)
println a
a = parse('JT_m^m')
a = JT >> a
a = expand(a)
a = contract(a)
a = parse('d^a_a = 4') >> a
a = mandelstam >> a
a = together(a)
a = parse('u = 4*m**2-s-t') >> a
a = expandNumerator(a)
println a
