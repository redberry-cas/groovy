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
import cc.redberry.core.context.OutputFormat
import cc.redberry.core.transformations.Expand
import cc.redberry.core.transformations.Together

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
RedberryGroovy.withRedberry()

def x
x = parse('(31751/2880)*(la+1)**(-2)*la**4*R**2-(139/960)*(la+1)**(-6)*la**9*R**2-(3223/360 )*(la+1)**(-1)*la**3*R**2-(1/30)*(la+1)**(-6)*la**10*R**2+(4/3)*(la+1)**(-2)*la* *2*R**2-(20419/11520)*(la+1)**(-4)*la**6*R**2+(9/20)*la**4*R**2+(3833/5760)*(la+ 1)**(-5)*la**8*R**2-(91/60)*(la+1)**(-1)*la**5*R**2+(-(4619/5760)*(la+1)**(-4)* la**7-(2533/720)*(la+1)**(-2)*la**6-(23/30)*la**4+(7/45)*(la+1)**(-6)*la**10-( 299/60)*la**3-(541/60)*la**2-(25/48)*(la+1)**(-4)*la**8-(2551/2880)*(la+1)**(-4) *la**5+(79/30)*(la+1)**(-1)*la**5-(95/9)*(la+1)**(-2)*la**2+(101/96)*(la+1)**(-6 )*la**8+(10387/1152)*(la+1)**(-3)*la**6+(155/8)*(la+1)**(-3)*la**5-(179/720)*(la +1)**(-5)*la**9-(2825/72)*(la+1)**(-2)*la**3-(107/30)*la-(301/480)*(la+1)**(-4)* la**4+(281/1440)*(la+1)**(-6)*la**6-(5477/2880)*(la+1)**(-5)*la**6-(18517/960)*( la+1)**(-2)*la**5-(571/240)*(la+1)**(-5)*la**7+(3211/360)*(la+1)**(-3)*la**3-( 3109/5760)*(la+1)**(-4)*la**6+7/6-(803/1440)*(la+1)**(-5)*la**5+(881/36)*(la+1)* *(-1)*la**2+(953/1440)*(la+1)**(-6)*la**9+(6197/180)*(la+1)**(-1)*la**3+(1067/ 1440)*(la+1)**(-6)*la**7-(3697/2880)*(la+1)**(-5)*la**8+(127/30)*la*(la+1)**(-1) +(4003/240)*(la+1)**(-1)*la**4+(1631/720)*(la+1)**(-3)*la**7-(6841/160)*(la+1)** (-2)*la**4+(1729/80)*(la+1)**(-3)*la**4)*R^{\\mu\\nu}*R_{\\mu\\nu}+(7/12)*R**2+(157/ 60)*la**2*R**2+(181/120)*la**3*R**2-(667/360)*(la+1)**(-3)*la**3*R**2-(15/64)*( la+1)**(-6)*la**8*R**2+(919/480)*(la+1)**(-2)*la**6*R**2-(3181/5760)*(la+1)**(-4 )*la**5*R**2-(161/960)*(la+1)**(-6)*la**7*R**2+(601/72)*(la+1)**(-2)*la**3*R**2+ (25/96)*(la+1)**(-4)*la**8*R**2+(103/320)*(la+1)**(-4)*la**4*R**2+(17/480)*(la+1 )**(-5)*la**9*R**2+(13/10)*la*R**2-(7349/11520)*(la+1)**(-4)*la**7*R**2-(281/60) *(la+1)**(-3)*la**4*R**2-(1109/288)*(la+1)**(-3)*la**5*R**2+(859/480)*(la+1)**(- 5)*la**7*R**2-(4955/2304)*(la+1)**(-3)*la**6*R**2+(3311/1920)*(la+1)**(-5)*la**6 *R**2+(1627/2880)*(la+1)**(-5)*la**5*R**2-(533/480)*(la+1)**(-3)*la**7*R**2-(13/ 10)*la*(la+1)**(-1)*R**2-(59/12)*(la+1)**(-1)*la**2*R**2-(7651/1440)*(la+1)**(-1 )*la**4*R**2+(34979/5760)*(la+1)**(-2)*la**5*R**2-(43/960)*(la+1)**(-6)*la**6*R* *2')
x = Expand.expand(Together.together(x))
//println Expand.expand(Together.together(x))

println x //.toString(OutputFormat.WolframMathematica)

//println parse('(a-b)')

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