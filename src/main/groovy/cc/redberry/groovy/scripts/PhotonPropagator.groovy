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

import cc.redberry.core.tensor.Tensor
import cc.redberry.core.transformations.Transformation

import static cc.redberry.core.tensor.Tensors.parse
import static cc.redberry.physics.utils.InverseTensor.findInverseWithMaple

//Calculating the photon propagator
def temporaryDir = System.getProperty("java.io.tmpdir")
def mapleBinDir = System.getenv("MAPLE")

def t = parse("K_\\mu\\nu = k_\\mu*k_\\nu - 1/a*k_\\alpha*k^\\alpha*g_\\mu\\nu")
def eq = parse("K^\\mu\\nu * D_\\nu\\alpha = d^\\mu_\\alpha")
def samples = [
        parse("g_\\mu\\nu"),
        parse("g^\\mu\\nu"),
        parse("k_\\mu"),
        parse("k^\\nu")]

def r = findInverseWithMaple(t, eq, samples as Tensor[], false, new Transformation[0], mapleBinDir, temporaryDir)

println r