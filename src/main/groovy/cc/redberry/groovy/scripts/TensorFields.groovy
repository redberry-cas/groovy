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

import cc.redberry.core.transformations.ContractIndices

import static cc.redberry.core.tensor.Tensors.parse
import static cc.redberry.core.transformations.Expand.expand

def field = parse("F_{ij}[p_a, q_b] = "
        + "g_{ij}*p_a*q^a - (p_i*q_j + p_j*q_i)");

//parsing some expression 
def e = parse("E = F_ab[k^n - p^n, q_n] * F^ab[q_n, k_n]");

//substituting field value in expression
e = field.transform(e);
//expand and contract indices
e = expand(e,
        //contract indices while expand
        ContractIndices.ContractIndices,
        //substitute kronecker trace while expand
        parse("d_a^a=4"));
//result: 
//E = 2*q^{f}*q_{d}*k_{f}*k^{d}+2*q^{b}*q_{b}*k^{a}*k_{a}
//-2*q^{a}*q_{d}*p_{a}*k^{d}-2*q^{b}*q_{b}*p_{a}*k^{a}
println e