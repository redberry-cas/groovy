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

import static cc.redberry.core.indexmapping.IndexMappings.getAllMappings
import static cc.redberry.core.tensor.Tensors.parse

//there are four predefined index types

//tensor F with latin lower case indices
def f1 = parse("F_mn")

//tensor F with Greek lower case indices
def f2 = parse("F_\\mu\\nu")

//tensor F with latin upper case indices
def f3 = parse("F_MN")

//tensor F with Greek upper case indices
def f4 = parse("F_\\Gamma\\Delta")

//All theses tensors have different mathematical nature
assert getAllMappings(f1,f2).size() == 0
assert getAllMappings(f1,f3).size() == 0
assert getAllMappings(f1,f4).size() == 0
