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

import static cc.redberry.core.tensor.Tensors.parse
import static cc.redberry.core.transformations.ContractIndices.contract
import static cc.redberry.core.transformations.expand.Expand.expand
import static cc.redberry.core.transformations.fractions.Together.together

//all equal terms are collected by default
def t = parse("a*F_ik*F^k_p+b*F_iz*F^z_p")
//(a+b)*F^{k}_{p}*F_{ik}
println t

//simple substitutions
t = parse("p_k = b_k+c_k")
        .transform(parse("(p_i*p_j-g_ij)*(g^ij-p^i*p^j)/(a+b)"
        + "-1/(m**2-c_k*c^k)"))
//expand transformation
t = expand(t)
//to common denominator
t = together(t);
//contract indices
t = contract(t)
//more complicated substitutions
t = parse("b_k*b^k = 0").transform(t)
t = parse("c_k*c^k = 0").transform(t)
t = parse("c_k*b^k = m**2").transform(t)
//kronecker trace
t = parse("d_k^k = 2").transform(t)
//to common denominator
t = together(t);
//m**(-2)*(-2*m**2+4*m**4-4*m**6-b-a)*(b+a)**(-1)
println t