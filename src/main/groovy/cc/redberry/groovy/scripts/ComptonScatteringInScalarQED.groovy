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

import static cc.redberry.core.context.ToStringMode.WolframMathematica
import static cc.redberry.core.tensor.Tensors.parse
import static cc.redberry.core.transformations.ContractIndices.ContractIndices
import static cc.redberry.core.transformations.Expand.expand
import static cc.redberry.core.transformations.Together.together

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

//************************************************//
//******** Compton scattering in scalar QED ******//
//************************************************//


//photon-scalar-scalar vertex
def V1 = parse("V_{i}[p_a, q_b] = -i*e*(p_i+q_i)")
//photon-photon-scalar-scalar vertex
def V2 = parse("V_{ij} = 2*i*e**2*g_ij");
//scalar propagator
def P = parse("D[k_a] = -i/(k^a*k_a-m**2)");
//matrix element
def M = parse("M^ij ="
        + "V^i[p1_a,p1_a+k1_a]*D[p1_a+k1_a]*V^j[-p2_a,-p1_a-k1_a]"
        + "+V^j[p1_a,p1_a-k2_a]*D[p1_a-k2_a]*V^i[-p1_a+k2_a,-p2_a]+V^ij");
//substituting vertex and propagator in matrix element
M = M << V2 << V1 << P

//to common denominator
M = together(M);
//expand transformation
M = expand(M, ContractIndices);

//defining mass shell and Mandelstam variables
def mandelstam = [
        parse("k1_a*k1^a = 0"),
        parse("k2_a*k2^a = 0"),
        parse("p1_a*p1^a = m**2"),
        parse("p2_a*p2^a = m**2"),
        parse("2*p1_a*k1^a = s-m**2"),
        parse("2*p2_a*k2^a = s-m**2"),
        parse("-2*k1_a*k2^a = t"),
        parse("-2*p1_a*p2^a = t-2*m**2"),
        parse("-2*k1_a*p2^a = u-m**2"),
        parse("-2*p1_a*k2^a = u-m**2")
]

//subsituting in matrix element
M = mandelstam >> M;

//squared matrix element with sum over final photon polarizations
//and averaging over initial photon polarizations
//here minus is due to complex conjugation
def M2 = M >> parse("M2 = -(1/2)*M_ij*M^ij")

//expand squared matrix element and contract indices
M2 = expand(M2, ContractIndices)
M2 = parse("d_i^i = 4") >> M2

//substituting mass shell and Mandelstam definitions
M2 = mandelstam >> M2
M2 = parse("u=2*m**2-s-t") >> M2;

//some simplifications
M2 = together(M2);
M2 = expand(M2);

//final cross section
cs = M2 >> parse("1/(64*pi**2*s)*M2")

//print in default Redberry notation
println cs

println ''

//print in Wolfram Mathematica nonation
println cs.toString(WolframMathematica)