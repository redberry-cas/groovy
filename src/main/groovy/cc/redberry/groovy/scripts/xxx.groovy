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

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
counter = 0;

def calcLines(File file) {
    if (file.isDirectory()) {
        file.listFiles().each {f ->  calcLines(f)}
    }
    else {
        def temp = 0;
        file.each {
            line ->
//            if (!line.isEmpty())
            ++counter;
        }
    }
}

calcLines(new File("/home/stas/Projects/commons-math3-3.0-src/src/main/java/"))
calcLines(new File("/home/stas/Projects/redberry/redberry/src/"))
calcLines(new File("/home/stas/Projects/redberry/redberry-physics/src/"))
calcLines(new File("/home/stas/Projects/redberry/redberry-groovy/src/"))
println counter