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

import sun.reflect.UTF8

import static cc.redberry.core.context.CC.setDefaultOutputFormat
import static cc.redberry.core.context.OutputFormat.Redberry
import static cc.redberry.core.context.OutputFormat.WolframMathematica
import static cc.redberry.core.tensor.Tensors.parse

//you can specify the default output format
setDefaultOutputFormat(Redberry)
def t = parse("F_mn^{\\alpha\\beta}/(a+b)")

//default print format gives
//(b+a)**(-1)*F_{mn}^{\alpha\beta}
println t

//LaTeX format gives
//\frac{1}{(a+b)} F_{mn}{}^{\alpha\beta}
println t.toString(LaTeX)

//UTF8 format will print greek characters
//(a+b)**(-1)*F_{mn}^{αβ}
println t.toString(UTF8)

//WolframMathematica format 
//Power[b+a, -1]*F_{mn}^{\alpha\beta}
println t.toString(WolframMathematica)