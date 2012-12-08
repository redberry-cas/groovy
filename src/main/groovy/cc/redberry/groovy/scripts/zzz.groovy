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
 * the Free Software Foundation, either version 2 of the License, or
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

import cc.redberry.groovy.Redberry
import cc.redberry.core.indexmapping.MappingsPort
import cc.redberry.core.indexmapping.IndexMappingBuffer

use(Redberry) {

//    def a = 'a'.t, b = 'b'.t;
//    def c = a + b
//
//    if (a % b)
//        println 'a'
//    if (a % a)
//        println 'c'
//
////    def v = 'a = b' >> t
////    def gg = c + v
//    println c
//    def g = c.setEach { it * it}
//    println g

//    def f = ['k1_m': '0', 'k2_m': '0', 'k3_m': '0', 'P_m': '2*m']
//    println f

//def setMandelstam = {
//    momentumMasses ->
//    if (momentumMasses.size() != 4)
//        throw new IllegalArgumentException();
//    Tensor[][] result = new Tensor[4][2];
//    int i = 0;
//    momentumMasses.each { a, b -> result[i][0] = Tensors.parse(a); result[i++][1] = Tensors.parse(b);}
//    return FeynCalcUtils.setMandelstam(result);
//}

//    def fdf = RedberryPhysics.setMandelstam(['k1_m': '0', 'k2_m': '0', 'k3_m': '0', 'P_m': '2*m'])
//    println fdf

//    setAntiSymmetric('R_ab')
//    def from = 'R_{ab}*A_c + R_{bc}*A_a'.t,
//        to = 'R_{ij}*A_k + R_{jk}*A_i'.t
//
//    (from % to).each { println it }
//
//    setSymmetric('A_mnc')
//    setSymmetric('C_mn')
//    def t = 'A_a^mn*A_m^br*C_rn'.t
//    (t % t).each {
//        println it
//        println it >> t
//    }
//
//    def t = 'a + Sin[x + y]'.t
//    def closure = {print it.toString() + ', ' }
//    t.eachInTree closure
//    println();
//    t.eachInTreeReverse closure
//    println();
//    t = 'a*b + c + Sin[x + y]'.t
//    def guide = {
//        tensor, parent, positionInParent ->
//        if (tensor.class == Sin) ShowButNotEnter
//        else if (parent == 'a*b'.t) DontShow
//        else Enter
//    } as TraverseGuide
//    t.eachInTree(guide, closure)
//    println()
//    t.eachInTreeReverse(guide, closure)

    def subs = {
        t, expr ->
        def c
        t.transformEachInTree {
            c = +(expr[0] % it)
            if (c != null) c >> expr[1]
            else it
        }
    }

    def t = 'z_m*Cos[x_m*y^m - x_n*(z^n + t^n)] + t_m'.t
    println subs(t, 'z_a + t_a = y_a'.t)

    t = 't_a*(x + t_a*t^a*x)'.t
    println t
    println subs(t, 'x = F_a^a'.t)

    MappingsPort.metaClass.next = {
        def c = delegate.take()
        delegate = this
        return c
    }

    def a = 'g_mn'.t
    def m = a % a
    println(++m)
    println(++m >> a)
    println(++m >> a)
//    def c
//    while (c = +m != null)
//        println c

//    while (f.hasNext()) println(++f)
}