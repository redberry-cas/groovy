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

import static cc.redberry.core.indices.IndexType.LatinLower
import static cc.redberry.core.tensor.Tensors.*
import static cc.redberry.core.transformations.Expand.expand
import static cc.redberry.core.utils.TensorUtils.isZero
import cc.redberry.core.context.OutputFormat

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
RedberryGroovy.withRedberry()

def s, t, r

/* ************************* *
 * Riemann tensor symmetries *
 * ************************* */

addSymmetry('R_abcd', LatinLower, true, 1, 0, 2, 3)
addSymmetry('R_abcd', LatinLower, false, 2, 3, 0, 1)
//Young projector
s = parse('R_abcd = 1/3*(2*R_abcd-R_adbc+R_acbd)')

t = parse('2*R_abcd*R^acbd - R_abcd*R^abcd')
r = expand(s >> t)
assert isZero(r)

t = parse('R_abcd*R^acbd + R_abcd*R^abcd - 3*R_abcd*R^acbd')
r = expand(s >> t)
assert isZero(r)

/* ************************* *
* Weyl tensor symmetries *
* ************************* */

addSymmetry('W_abcd', LatinLower, true, 0, 1, 3, 2)
addSymmetry('W_abcd', LatinLower, false, 2, 3, 0, 1)

s = parse('W_mnpq = 2/3*W_mnpq-1/3*W_mqnp+1/3*W_mpnq')
t = parse('W^p_q^r_s*W_p^t_r^u*W_tv^qw*W_u^vs_w'
        + '-W^p_q^r_s*W_p^qtu*W_rvtw*W^sv_u^w'
        + '-W_mn^ab*W^n_pb^c*W^ms_cd*W_s^pd_a'
        + '+1/4*W_mn^ab*W^ps_ba*W^m_p^c_d*W^n_s^d_c')
r = expand(s >> t)
assert isZero(r)