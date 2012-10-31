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

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
RedberryGroovy.withRedberry()

def x = parse('x')
def y = parse('y')


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