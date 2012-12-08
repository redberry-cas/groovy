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

import cc.redberry.groovy.RedberryGroovy

import static cc.redberry.core.tensor.Tensors.parse
import cc.redberry.core.tensor.SimpleTensor
import cc.redberry.core.number.Complex
import cc.redberry.physics.feyncalc.FeynCalcUtils
import cc.redberry.core.tensor.Tensor
import cc.redberry.core.parser.preprocessor.GeneralIndicesInsertion
import cc.redberry.core.context.CC

import static cc.redberry.core.indices.IndexType.LatinLower1
import static cc.redberry.core.indices.IndexType.LatinUpper1
import cc.redberry.core.indexmapping.IndexMapping
import cc.redberry.core.indexmapping.IndexMappings

import static cc.redberry.core.tensor.Tensors.addSymmetry
import static cc.redberry.core.tensor.Tensors.addAntiSymmetry

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
//RedberryGroovy.withRedberry()

//def x
//x = parse('(m**4+(1/2)*m**2*M**2+(1/16)*M**4)**(-1)*(-m**2-(1/4)*M**2)**(-1)*(-m**2*M-(1/4)*M**3)**(-1)*((8*m**2*(-m**2-(1/4)*M**2)*N*M**3*(-m**2*M-(1/4)*M**3)+48*m**2*M*(m**4+(1/2)*m**2*M**2+(1/16)*M**4)*N*(-m**2*M-(1/4)*M**3)-4*(m**4+(1/2)*m**2*M**2+(1/16)*M**4)*N*M**3*(-m**2*M-(1/4)*M**3)-16*m**2*(m**4+(1/2)*m**2*M**2+(1/16)*M**4)*(-m**2-(1/4)*M**2)*N*M**2+2*(-m**2-(1/4)*M**2)*N*M**4*(-m**2*M-(1/4)*M**3)*m+8*m**3*(-m**2-(1/4)*M**2)*N*M**2*(-m**2*M-(1/4)*M**3)+8*(m**4+(1/2)*m**2*M**2+(1/16)*M**4)*N*M**2*(-m**2*M-(1/4)*M**3)*m)*k1+(-4*(m**4+(1/2)*m**2*M**2+(1/16)*M**4)*N*M**3*(-m**2*M-(1/4)*M**3)+2*(m**4+(1/2)*m**2*M**2+(1/16)*M**4)*(-m**2-(1/4)*M**2)*N*M**4-4*(-m**2-(1/4)*M**2)*N*M**4*(-m**2*M-(1/4)*M**3)*m-(-m**2-(1/4)*M**2)*N*M**5*(-m**2*M-(1/4)*M**3)-8*m**2*(m**4+(1/2)*m**2*M**2+(1/16)*M**4)*(-m**2-(1/4)*M**2)*N*M**2-4*m**2*(-m**2-(1/4)*M**2)*N*M**3*(-m**2*M-(1/4)*M**3)-8*(m**4+(1/2)*m**2*M**2+(1/16)*M**4)*N*M**2*(-m**2*M-(1/4)*M**3)*m)*k2)')
//println x.toString(OutputFormat.WolframMathematica)
//println parse('(a-b)')
println([1, 2] + [2, 2])
//addAntiSymmetry('R_mnp', 2, 1, 0)
//def s = parse('f_m + R_bma*F^ba - R_ljm*F^lj = R_bam*F^ab')
//def t = parse('f_i + R_ijk*F^jk + R_ijk*F^kj - R_kij*F^jk')
//println s >> t
//
//s = parse('K_a * (A^ab - A^ba)= F^a*A_a^b')
//t = parse('K_p * (A^qp - A^pq) + F^a*A_a^q')
//println s >> t
//addSymmetry('A_abc', 1, 0, 2)
////addAntiSymmetry('A_abc', 0, 2, 1)
//
//def lhs = [parse('x'), parse('y')] as Tensor[]
//def rhs = [parse('a + b'), parse('c+a')] as Tensor[]
//def subs = new Substitution(lhs, rhs)
//println subs >> [parse('x+y'), parse('x-y')]
//
//def s1 = parse('f[x, y] = x + y'),
//    s2 = parse('z = a + b')
//t = parse('z + f[a, b]')
//println([s1, s2] >> t)
//v = parse('z*f[a, b]')
//println([s1, s2] >> [t, v])
//
//Class.metaClass.getAt {
//    if (delegate == Differentiate) {
//        it = it.collect { it instanceof String ? parse(it) : it}
//        return Differentiate.differentiate(it[0], it[1..it.size() - 1] as SimpleTensor[])
//    }
//    return delegate.getAt(it)
//}
//
//println Differentiate['x**2', 'x']

//def timing = { closure ->
//    def start = System.currentTimeMillis()
//    closure.call()
//    def now = System.currentTimeMillis()
//    println('Time: ' + (now - start) + ' ms')
//}

//def t = [parse('x = y'), parse('d = d')] as Transformation[]
//println t.class.getSimpleName()
//

//def t = [parse('a'), parse('b')]
//t << parse('a') << parse('b')
//println t

RedberryGroovy.withRedberry()

GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
CC.current().parseManager.defaultParserPreprocessors.add(indicesInsertion);
def setMatrix = {
    a, b ->
    if (a instanceof String)
        a = parse(a)
    indicesInsertion.addInsertionRule(a, b)
}

setMatrix(''' G_a^a'_b' ''', LatinLower1)
setMatrix(''' G5^v'_a' ''', LatinLower1)

def t = parse(' G_a*G5*G_b*G_c*G_e*G5 ')

def commutator = [parse(' G5*G_b = - G_b*G5'), parse('G5*G5 = 1')]

def old
while (true) {
    old = t
    t = commutator >> t
    if (t == old)
        break;
}

addAntiSymmetry('R_ab', 1, 0)
def from = parse('R_{ab}*A_c+R_{bc}*A_a'),
    to = parse('R_{ij}*A_k+R_{jk}*A_i')
def port = IndexMappings.createPort(from, to)
def mapping
while ((mapping = port.take()) != null)
    println mapping

println t
