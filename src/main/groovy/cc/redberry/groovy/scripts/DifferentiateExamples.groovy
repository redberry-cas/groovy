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

import static cc.redberry.core.tensor.Tensors.*
import static cc.redberry.core.transformations.Differentiate.differentiate

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */

def var, tensor, result
var = parse('f_mn')
tensor = parse('Sin[f_ab*f^ab]')
println differentiate(tensor, var)

addAntiSymmetry('R_ab', 1, 0)
tensor = parse('R_mn')
var = parse('R_ab')
println differentiate(tensor, var)

addAntiSymmetry('R_abcd', 1, 0, 2, 3)
addSymmetry('R_abcd', 2, 3, 0, 1)
tensor = parse('R_mnab*R^pqnm*Sin[R_ijkv*R^ijkv]')
var1 = parse('R_abmn')
var2 = parse('R^pqmn')
println differentiate(tensor, var1, var2)
