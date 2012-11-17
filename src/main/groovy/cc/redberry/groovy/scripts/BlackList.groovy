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
import cc.redberry.groovy.RedberryGroovy

import static cc.redberry.core.indices.IndexType.LatinLower
import static cc.redberry.core.tensor.Split.splitIndexless
import static cc.redberry.core.tensor.Tensors.addSymmetry
import static cc.redberry.core.tensor.Tensors.parse
import static cc.redberry.core.transformations.expand.Expand.expand

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */

//println parse("F^A*F_{BA_{21}C\\mu\\nu} * a").indices
//println parse("F{}^a")
//println CC.current().getIndexConverterManager().getSymbol(IndicesUtils.createIndex(0, (byte) 2, false), LaTeX) + CC.current().getIndexConverterManager().getSymbol(0, LaTeX)
//println parse("\\Gamma*\\Lambda{}^a{}_b*Power[a,2]").toString(LaTeX)
//
//println parse("Sin[0*3]").toString() + "a"
//println parse("F_a*(A^a*B_a)**2")
//println parse("a*F_mn*F^mn+b*F_mn*F^mn")
//def t;
//t = parse("(a**c)**b")
//t = parse("a*F_mn+(b-a)*F_mn")
//t = parse("a*(a_m^m+b_z^z)*F_mn")
//def split = Split.splitIndexless(t)
//println split.factor.indices.size()
//assert split.summand.indices.size() == 0
//println t

//def ll = new ArrayList<>(10000)
//println parse("2/3-27**(1/3)/9")
//for (int i = 0; i < 10000; ++i) {
//    def s = System.currentTimeMillis()
//    ll.add(parse("2**44497 - 1"))
//    println System.currentTimeMillis() - s
//}
RedberryGroovy.withRedberry()

//CC.setDefaultOutputFormat(OutputFormat.LaTeX)
def s = parse('x = x_a^a')
def t = parse('(x*f_a + x*y_a)*(x*f_b + z_b)')
println s >> t


int number = 1;
//new File("/home/stas/Projects/redberry/result").eachLine {
//    line ->
//    if (number == 1) {
//        t = parse(line)
//    }
//    number++
//}
//t = parse('(103/320)*la**4*R**2*(la+1)**(-4)+(17/480)*la**9*R**2*(la+1)**(-5)-(7349/11520)*la**7*R**2*(la+1)**(-4)-(281/60)*la**4*R**2*(la+1)**(-3)-(1109/288)*la**5*R**2*(la+1)**(-3)+(859/480)*la**7*R**2*(la+1)**(-5)-(4955/2304)*la**6*R**2*(la+1)**(-3)+(1627/2880)*la**5*R**2*(la+1)**(-5)+(3311/1920)*la**6*R**2*(la+1)**(-5)-(533/480)*la**7*R**2*(la+1)**(-3)-(59/12)*la**2*R**2*(la+1)**(-1)-(7651/1440)*la**4*R**2*(la+1)**(-1)+(34979/5760)*la**5*R**2*(la+1)**(-2)-(43/960)*la**6*R**2*(la+1)**(-6)+(9/20)*la**4*R**2+(157/60)*la**2*R**2+(181/120)*la**3*R**2+(31751/2880)*la**4*R**2*(la+1)**(-2)-(139/960)*la**9*R**2*(la+1)**(-6)-(3223/360)*la**3*R**2*(la+1)**(-1)-(1/30)*la**10*R**2*(la+1)**(-6)+(4/3)*la**2*R**2*(la+1)**(-2)-(20419/11520)*la**6*R**2*(la+1)**(-4)+(-(5477/2880)*la**6*(la+1)**(-5)+(10771/1152)*la**6*(la+1)**(-3)-(571/240)*la**7*(la+1)**(-5)+(701/36)*la**2*(la+1)**(-1)-(803/1440)*la**5*(la+1)**(-5)+(731/5760)*la**6*(la+1)**(-4)+(3487/240)*la**4*(la+1)**(-3)-(18917/960)*la**5*(la+1)**(-2)+(953/1440)*la**9*(la+1)**(-6)-(107/30)*la+(127/30)*la*(la+1)**(-1)+(1067/1440)*la**7*(la+1)**(-6)-(3697/2880)*la**8*(la+1)**(-5)+(4043/240)*la**4*(la+1)**(-1)+7/6+(1631/720)*la**7*(la+1)**(-3)-(23/30)*la**4-(299/60)*la**3-(541/60)*la**2-(2081/72)*la**3*(la+1)**(-2)+(2009/2880)*la**5*(la+1)**(-4)-(2533/720)*la**6*(la+1)**(-2)+(7/45)*la**10*(la+1)**(-6)-(25/48)*la**8*(la+1)**(-4)+(33/160)*la**4*(la+1)**(-4)+(5177/180)*la**3*(la+1)**(-1)+(2611/360)*la**3*(la+1)**(-3)+(79/30)*la**5*(la+1)**(-1)-(95/9)*la**2*(la+1)**(-2)+(101/96)*la**8*(la+1)**(-6)-(5099/5760)*la**7*(la+1)**(-4)-(15563/480)*la**4*(la+1)**(-2)-(179/720)*la**9*(la+1)**(-5)-((1/3)*la**6*(la+1)**(-3)+(1/6)*la**4*(la+1)**(-1)-(1/12)*la**7*(la+1)**(-4)+(2/3)*la**6*(la+1)**(-4)-5*la**2*(la+1)**(-1)-(5/3)*la**3*(la+1)**(-3)+(5/6)*la**4*(la+1)**(-4)-(5/12)*la**5*(la+1)**(-2)-(17/3)*la**3*(la+1)**(-1)+(19/12)*la**5*(la+1)**(-4)+(31/3)*la**3*(la+1)**(-2)+(31/3)*la**4*(la+1)**(-2)-(61/12)*la**5*(la+1)**(-3)-(85/12)*la**4*(la+1)**(-3))+(281/1440)*la**6*(la+1)**(-6)+(343/24)*la**5*(la+1)**(-3))*R^{\\mu\\nu}*R_{\\mu\\nu}+(3833/5760)*la**8*R**2*(la+1)**(-5)-(91/60)*la**5*R**2*(la+1)**(-1)+(7/12)*R**2-(667/360)*la**3*R**2*(la+1)**(-3)+(13/10)*la*R**2-(15/64)*la**8*R**2*(la+1)**(-6)+(919/480)*la**6*R**2*(la+1)**(-2)-(3181/5760)*la**5*R**2*(la+1)**(-4)-(161/960)*la**7*R**2*(la+1)**(-6)+(601/72)*la**3*R**2*(la+1)**(-2)-(13/10)*la*R**2*(la+1)**(-1)+(25/96)*la**8*R**2*(la+1)**(-4)');
//t = Expand.expand(Together.together(Expand.expand(t)))
//println t
//String res =  t.toString(OutputFormat.WolframMathematica)
//
//new File("/home/stas/Projects/redberry/result1") << res

s = parse('F_ab[x_i:_i] = g_ab*x_i*x^i  + x_a*x_b')
t = parse('F_ab[1/2*k_q-p_q] * F^ab[1/2*k_q+p_q]')
t = s >> t
t = expand(t, ContractIndices.ContractIndices)
t = parse('d_i^i = 4') >> t
t = parse('k_i*k^i = m**2') >> t
//t = expand(t)
println t

s = parse('F_ij[x_m, y_m] = x_i*y_j')
t = parse('T^ab*F_ab[p^a - q^a, p^a + q^a]')
t = s >> t
println t

println()

s = parse('F_i[x_mn] = x_ik*f^k')
t = parse('F_k[x_i*y_j]')// same as parse('F_k[x_i*y_j:_ij]')
println s >> t
t = parse('F_k[x_i*y_j:_ji]')
println s >> t


addSymmetry('R_mnp', LatinLower, true, 2, 1, 0)
s = parse('f_m + R_bma*F^ba - R_ljm*F^lj =  R_bam*F^ab')
t = parse('f_i + R_ijk*F^jk + R_ijk*F^kj - R_kij*F^jk')
println s >> t


s = parse('(A^ab - A^ba)*K_ajp*K^jcpm = F^bcm')
t = parse('F^c*(A_cb - A_bc)*K^bjp*K_japm*F^a + F^c*F^b*F_bcm')
println s >> t


String.metaClass._() {
    parse(delegate)
}

s = parse('1/(a*b) = c')
println s >> 'x/(a*b)'._()
println s >> 'x/(a*b*y)'._()


println parse('f[x, y, x + z] = x + y + z') >> parse('f[a,b, c]')

println parse('F_mn*(A^ab+M_m*N^mab)')


t = parse('(x_a^a+y_b^b)*X_m*X^m+(z_n^n-y_d^d)*X_a*X^a')
def u = parse('(x_a^a+y_b^b)*X_m*X^m')
def v = parse('(z_n^n-y_d^d)*X_a*X^a')

def sU = splitIndexless(u)
sU.factor.each {it -> assert it.indices.size() != 0}
println sU.factor
assert sU.summand.indices.size() == 0
println sU.summand

def sV = splitIndexless(v)
assert sV.factor.indices.size() != 0
println sV.factor
assert sV.summand.indices.size() == 0
println sV.summand
