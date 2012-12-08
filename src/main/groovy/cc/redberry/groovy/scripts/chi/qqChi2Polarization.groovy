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

import cc.redberry.core.context.CC
import cc.redberry.core.context.OutputFormat
import cc.redberry.core.indices.IndexType
import cc.redberry.core.number.Complex
import cc.redberry.core.parser.preprocessor.GeneralIndicesInsertion
import cc.redberry.core.tensor.SimpleTensor
import cc.redberry.core.tensor.Tensor
import cc.redberry.core.transformations.Transformation
import cc.redberry.core.transformations.fractions.Together
import cc.redberry.core.utils.TensorUtils
import cc.redberry.groovy.RedberryGroovy
import cc.redberry.physics.feyncalc.DiracTrace
import cc.redberry.physics.feyncalc.FeynCalcUtils
import cc.redberry.physics.feyncalc.LeviCivitaSimplify

import static cc.redberry.core.tensor.Tensors.*
import static cc.redberry.core.transformations.ContractIndices.ContractIndices
import static cc.redberry.core.transformations.Differentiate.differentiate
import static cc.redberry.core.transformations.expand.ExpandAll.expandAll
//import cc.redberry.core.transformations.factor.Factor
import cc.redberry.groovy.RedberryPhysics

import static cc.redberry.groovy.RedberryPhysics.setMandelstam
import static cc.redberry.groovy.RedberryPhysics.setMandelstam
import cc.redberry.groovy.Redberry
//import cc.redberry.core.transformations.factor.Factor

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
use(Redberry) {


    GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
    CC.current().parseManager.defaultParserPreprocessors.add(indicesInsertion);
    indicesInsertion.addInsertionRule(parse(''' G_a^a'_b' '''), IndexType.LatinLower1)
    indicesInsertion.addInsertionRule(parse(''' T_A^A'_B' '''), IndexType.LatinUpper1)
    indicesInsertion.addInsertionRule(parse(''' D^a'_b'[p_m] '''), IndexType.LatinLower1)
    indicesInsertion.addInsertionRule(parse(''' M_axvA^a'A'_b'B' '''), IndexType.LatinLower1)
    indicesInsertion.addInsertionRule(parse(''' M_axvA^a'A'_b'B' '''), IndexType.LatinUpper1)

    addAntiSymmetry('e_abcd', 1, 0, 2, 3)
    addAntiSymmetry('e_abcd', 1, 2, 3, 0)

    def tr
    //quark propagator
    def D = 'D[p_m] = (m + p_m*G^m)/(p_m*p^m - m**2)'.t
    //sum over gluon polarizations
    addSymmetry('P_mn[k_a]', 1, 0)
    def gluonPolarization = 'P_mn[k_a] = -g_mn '.t

    //gluon propagator
    def G = 'G_mn[k_a] = P_mn[k_a]/(k_a*k^a)'.t
    G = gluonPolarization >> G


    def n2 = 'n_a*n^a = 0'.t
    def J = 'J_ab = - g_ab + P_a*P_b/(2*m)**2 '.t
    addSymmetry('J_ab', 1, 0)

    def JT = ['S_m = k1_m+k2_m',
            'a = -1/2',
            'b = (4*m**2 + s)/(s-4*m**2)**2',
            'c = -2*s/(s-4*m**2)**2',
            'd = -8*m**2/(s-4*m**2)**2'] >> 'JT_mn = a*g_mn + b*(P_m*S_n + P_n*S_m) + c*P_m*P_n + d*S_m*S_n'.t

    addSymmetry('JT_mn', 1, 0)

    def J0 = expandAll([J, JT] >> 'J0_abmn = 2/3*(J_ab-3/2*JT_ab)*(J_mn-3/2*JT_mn)'.t)
    def J1 = expandAll([J, JT] >> 'J1_abmn = 1/2*(J_am*J_bn+J_an*J_bm - JT_am*JT_bn - JT_an*JT_bm) + J_ab*JT_mn + JT_ab*J_mn-J_ab*J_mn-JT_ab*JT_mn'.t)
    def J2 = expandAll([J, JT] >> 'J2_abmn = 1/2*(JT_am*JT_bn+JT_an*JT_bm - JT_ab*JT_mn)'.t)
    def JAll = expandAll([J, JT] >> 'J_abcd = (J_ac*J_bd + J_ad*J_bc)/2 - J_ab*J_cd/3'.t)

    ['J0_mnab', 'J1_mnab', 'J2_mnab', 'J_mnab'].each {
        addSymmetry(it, 1, 0, 2, 3)
        addSymmetry(it, 0, 1, 3, 2)
        addSymmetry(it, 2, 3, 0, 1)
    }

    def polarizations = [JAll, J0, J1, J2]

    //effective vertex
    def A = '''A_{mn a}[k1_m, k2_n] = (1/2)*Tr[
                  (G_m*D[p1_m-k1_m]*G_n + G_n*D[p1_m-k2_m]*G_m)
                  *(p2_m*G^m - m)*G_a*(G_m*P^m + 2*m)*(p1_m*G^m + m)] '''.t

    A = [D, 'p1_m = P_m/2 + q_m', 'p2_m = P_m/2 - q_m'] >> A
    A = DiracTrace.trace(A)

    def A1 = differentiate(A[1], 'q^b'.t)
    A1 = 'q_a = 0' >> A1

    tr = [ContractIndices, 'd^a_a = 4', 'P_m*P^m = 4*m**2']

    A1 = tr >> expandAll(A1, tr as Transformation)

    //  def Aj0 = "A_{mn ab}[k1_m, k2_m] = $A1".t
    def Aj0 = 'A_{mn ab}[k1_m, k2_m]'.eq A1


    def mandelstam = setMandelstam(['k1_m': '0', 'k2_m': '0', 'k3_m': '0', 'P_m': '2*m'])

    tr = [ContractIndices, * mandelstam, 'd^a_a = 4', n2, 'u = 4*m**2 -s - t'];

    def M = parse('M_pxyA = T_A*G_m*G^mn[k1_m+k2_m]*A_{npxy}[k1_m + k2_m, -k3_m]')
    M = G >> M
    M = Aj0 >> M
    M = tr >> expandAll(M, tr as Transformation)
    M = expandAll(M)

    def M2_Sum = M >> '2M2Sum = Tr[k2_m*G^m*M_pxyA*k1_n*G^n*M_qrs^A*P^pq[k3_m]]*J^xyrs'.t
    // |h| = 0
    def M2_0 = M >> '2M20 = Tr[k2_m*G^m*M_pxyA*k1_n*G^n*M_qrs^A*P^pq[k3_m]]*J0^xyrs'.t
    // |h| = 1
    def M2_1 = M >> '2M21 = Tr[k2_m*G^m*M_pxyA*k1_n*G^n*M_qrs^A*P^pq[k3_m]]*J1^xyrs'.t
    // |h| = 2
    def M2_2 = M >> '2M22 = Tr[k2_m*G^m*M_pxyA*k1_n*G^n*M_qrs^A*P^pq[k3_m]]*J2^xyrs'.t

    tr << 'P_a*J^abcd = 0' << 'P_a*J0^abcd = 0' << 'P_a*J1^abcd = 0' << 'P_a*J2^abcd = 0'

    for (M2 in [M2_Sum, M2_0, M2_1, M2_2]) {

        M2 = gluonPolarization >> M2
        M2 = expandAll(M2)
        M2 = 'Tr[T_A*T^A] = 3/2' >> M2
        M2 = DiracTrace.trace(M2)
        M2 = tr >> expandAll(M2, tr as Transformation)
        M2 = polarizations >> M2
        M2 = tr >> expandAll(M2, tr as Transformation)

        println TensorUtils.isSymbolic(M2)
//        def sout = System.out;
//        System.setOut(new PrintStream(new OutputStream() {
//            @Override
//            public void write(int b) throws IOException {
//            }
//        }))
//        def rr = Factor.factor(M2)
//        System.setOut(sout)
//        println rr.toString(OutputFormat.WolframMathematica)
//        println 'factor done'
        new File('/home/stas/Projects/redberry/' + M2[0]).delete()
        new File('/home/stas/Projects/redberry/' + M2[0]) << M2[1].toString(OutputFormat.WolframMathematica)
    }
}