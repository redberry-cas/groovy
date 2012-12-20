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

package cc.redberry.groovy

import cc.redberry.core.indices.IndexType
import junit.framework.Assert
import org.junit.Test
import static cc.redberry.groovy.RedberryPhysics.*
import static cc.redberry.groovy.RedberryStatic.*
import static junit.framework.Assert.assertTrue

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
class RedberryPhysicsTest {

    @Test
    public void testDiracTrace1() {
        use(Redberry) {
            setMatrix('G_a')
            assertTrue DiracTrace['G_a'] >> 'Tr[G_a*G_b]'.t == '4*g_ab'.t
            setMatrix('G_\\alpha', IndexType.LatinUpper1)
            assertTrue DiracTrace['G_\\alpha'] >> 'Tr[G_\\alpha*G_\\beta]'.t == '4*g_\\alpha\\beta'.t
        }
    }

    @Test
    public void testSUNTrace1() {
        use(Redberry) {
            setMatrix('T_a')
            assertTrue UnitaryTrace >> 'Tr[T_a*T_b]'.t == '(1/2)*g_{ba}'.t

            setMatrix('T_A', IndexType.LatinUpper1)
            assertTrue UnitaryTrace['T_A', 'f_ABC', 'd_ABC', 'N'] >> 'Tr[T_A*T_B]'.t == '(1/2)*g_{AB}'.t
        }
    }

    @Test
    public void testLeviCivita() {
        use(Redberry) {
            assertTrue LeviCivitaSimplify >> 'e_abcd*e^abcd'.t == '-24'.t
        }
    }
}
