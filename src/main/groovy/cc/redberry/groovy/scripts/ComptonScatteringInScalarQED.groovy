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

import cc.redberry.core.number.Complex
import cc.redberry.core.tensor.SimpleTensor
import cc.redberry.core.utils.TensorUtils
import cc.redberry.groovy.RedberryGroovy

import static cc.redberry.core.context.OutputFormat.WolframMathematica
import static cc.redberry.core.tensor.Tensors.*
import static cc.redberry.core.transformations.ContractIndices.ContractIndices
import static cc.redberry.core.transformations.Expand.expand
import static cc.redberry.core.transformations.Together.together

RedberryGroovy.withRedberry()

def setMandelstam = {
    List list ->
    assert list.size() == 4
    list.each { e ->
        assert e instanceof List
        assert e.size() == 2
    }

    //parsing strings if they are not already parsed
    list = list.collect {
        [it[0] instanceof String ? parse(it[0]) : it[0],
                it[1] instanceof String ? parse(it[1]) : it[1]]
    }
    list.each { e -> e.each { ee -> assert ee instanceof SimpleTensor || Complex } }

    def contract = {
        SimpleTensor a, SimpleTensor b ->
        a * simpleTensor(b.name, a.indices.inverse)
    }
    def square = {
        SimpleTensor tensor ->
        tensor * simpleTensor(tensor.name, tensor.indices.inverse)
    }

    def s = parse('s'), t = parse('t'), u = parse('u')

    def result = []
    // (k1,k1) = m1^2, (k2,k2) = m2^2, (k3,k3) = m3^2, (k4,k4) = m4^2
    list.each { e -> result << expression(square(e[0]), e[1] ^ 2)}

    //2(k1, k2) = s - k1^2 - k2^2
    //2(k3, k4) = s - k3^2 - k4^2
    for (i in [[0, 1], [2, 3]])
        result << expression(2 * contract(list[i[0]][0], list[i[1]][0]),
                s - (list[i[0]][1] ^ 2) - (list[i[1]][1] ^ 2))
    //-2(k1, k3) = t - k1^2 - k3^2
    //-2(k2, k4) = t - k2^2 - k4^2
    for (i in [[0, 2], [1, 3]])
        result << expression(-2 * contract(list[i[0]][0], list[i[1]][0]),
                t - (list[i[0]][1] ^ 2) - (list[i[1]][1] ^ 2))
    //-2(k1, k4) = u - k1^2 - k4^2
    //-2(k2, k3) = u - k2^2 - k3^2
    for (i in [[0, 3], [1, 2]])
        result << expression(-2 * contract(list[i[0]][0], list[i[1]][0]),
                u - (list[i[0]][1] ^ 2) - (list[i[1]][1] ^ 2))
    result
}

//************************************************//
//******** Compton scattering in scalar QED ******//
//************************************************//

//photon-scalar-scalar vertex
def V1 = parse("V_{i}[p_a, q_b] = -I*e*(p_i+q_i)")
//photon-photon-scalar-scalar vertex
def V2 = parse("V_{ij} = 2*I*e**2*g_ij")
//scalar propagator
def P = parse("D[k_a] = -I/(k^a*k_a-m**2)")
//matrix element
def M = parse("M^ij ="
        + "V^i[p1_a,p1_a+k1_a]*D[p1_a+k1_a]*V^j[-p2_a,-p1_a-k1_a]"
        + "+V^j[p1_a,p1_a-k2_a]*D[p1_a-k2_a]*V^i[-p1_a+k2_a,-p2_a]"
        + "+V^ij")
//substituting vertices and propagator in matrix element
M = [V1, V2, P] >> M

//squared matrix element
//here minus is due to complex conjugation
def M2 = M >> parse("M2 = -M_ij*M^ij")

//expand squared matrix element and contract indices
M2 = expand(M2, ContractIndices)
M2 = parse("d_i^i = 4") >> M2

//defining mass shell and Mandelstam variables
def mandelstam = setMandelstam([['k1_i', '0'],
        ['p1_i', 'm'], ['k2^i', '0'], ['p2_i', 'm']])
//equivalent
//def mandelstam = [
//        parse("k1_a*k1^a = 0"),
//        parse("k2_a*k2^a = 0"),
//        parse("p1_a*p1^a = m**2"),
//        parse("p2_a*p2^a = m**2"),
//        parse("2*p1_a*k1^a = s-m**2"),
//        parse("2*p2_a*k2^a = s-m**2"),
//        parse("-2*k1_a*k2^a = t"),
//        parse("-2*p1_a*p2^a = t-2*m**2"),
//        parse("-2*k1_a*p2^a = u-m**2"),
//        parse("-2*p1_a*k2^a = u-m**2")
//]
//substituting mass shell and Mandelstam definitions
M2 = mandelstam >> M2
M2 = parse("u=2*m**2-s-t") >> M2

//to common denominator
M2 = expand(together(M2))
M2 = together(M2)

println M2.toString(WolframMathematica)

//final cross section
cs = M2 >> parse("1/(64*pi**2*s)*M2")

//print in default Redberry notation
println cs

assert TensorUtils.equals(cs, parse('(1/64)*(-s+m**2-t)**(-2)*s**(-1)*pi**(-2)*(s-m**2)**(-2)*(8*s**2*e**4*t**2-32*s*m**6*e**4+8*m**4*e**4*t**2+8*s**4*e**4+16*s**3*t*e**4+8*m**8*e**4-32*s**3*m**2*e**4-32*s**2*m**2*t*e**4+48*s**2*m**4*e**4+16*s*m**4*t*e**4)'))
println ''

//print in Wolfram Mathematica nonation
println cs.toString(WolframMathematica)