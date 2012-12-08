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
//import cc.redberry.core.transformations.factor.Factor

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
RedberryGroovy.withRedberry()
def setMandelstam = {
    List list ->
    //parsing strings if they are not already parsed
    list = list.collect {
        [it[0] instanceof String ? parse(it[0]) : it[0],
                it[1] instanceof String ? parse(it[1]) : it[1]]
    }
    list.each { e -> e.each { ee -> assert ee instanceof SimpleTensor || Complex } }
    FeynCalcUtils.setMandelstam(list as Tensor[][]) as List
}


GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
CC.current().parseManager.defaultParserPreprocessors.add(indicesInsertion);
indicesInsertion.addInsertionRule(parse(''' G_a^a'_b' '''), IndexType.LatinLower1)
indicesInsertion.addInsertionRule(parse(''' T_A^A'_B' '''), IndexType.LatinUpper1)
indicesInsertion.addInsertionRule(parse(''' D^a'_b'[p_m] '''), IndexType.LatinLower1)
indicesInsertion.addInsertionRule(parse(''' M_axA^a'A'_b'B' '''), IndexType.LatinLower1)
indicesInsertion.addInsertionRule(parse(''' M_axA^a'A'_b'B' '''), IndexType.LatinUpper1)

addAntiSymmetry('e_abcd', 1, 0, 2, 3)
addAntiSymmetry('e_abcd', 1, 2, 3, 0)

def tr
//quark propagator
def D = parse('D[p_m] = (m + p_m*G^m)/(p_m*p^m - m**2)')
//sum over gluon polarizations
addSymmetry('P_mn[k_a]', 1, 0)
def gluonPolarization = parse('P_mn[k_a] = -g_mn ')

def simplifyLeviCivita = new LeviCivitaSimplify(parse('e_abcd'))

//gluon propagator
def G = parse('G_mn[k_a] = P_mn[k_a]/(k_a*k^a)')
G = gluonPolarization >> G


def n2 = parse('n_a*n^a = 0')
def J = parse('J_ab = - g_ab + P_a*P_b/(2*m)**2 ')
addSymmetry('J_ab', 1, 0)
def JT = [parse('S_m = k1_m+k2_m'),
        parse('a = -1/2'),
        parse('b = (4*m**2 + s)/(s-4*m**2)**2'),
        parse('c = -2*s/(s-4*m**2)**2'),
        parse('d = -8*m**2/(s-4*m**2)**2')] >> parse('JT_mn = a*g_mn + b*(P_m*S_n + P_n*S_m) + c*P_m*P_n + d*S_m*S_n')

addSymmetry('JT_mn', 1, 0)
def JL = [J, JT] >> parse('JL_ab = J_ab - JT_ab')
addSymmetry('JL_mn', 1, 0)

def polarizations = [J, JT, JL]

//effective vertex
def A = parse('''A_{mn a}[k1_m, k2_n] = (1/2)*Tr[
                  (G_m*D[p1_m-k1_m]*G_n + G_n*D[p1_m-k2_m]*G_m)
                  *(p2_m*G^m - m)*G_a*(G_m*P^m + 2*m)*(p1_m*G^m + m)] ''')

A = [D, parse('p1_m = P_m/2 + q_m'), parse('p2_m = P_m/2 - q_m')] >> A
A = DiracTrace.trace(A)

def A1 = differentiate(A[1], parse('q^b')) * parse('e^{ab}_{cd}*P^c')
A1 = parse('q_a = 0') >> A1

tr = [ContractIndices, parse('d^a_a = 4'), parse('P_m*P^m = 4*m**2')]
A1 = tr >> expandAll(A1, tr as Transformation[])

def Aj0 = expression(parse('A_{mn d}[k1_m, k2_m]'), A1)


def mandelstam = setMandelstam([['k1_m', '0'], ['k2_m', '0'], ['k3_m', '0'], ['P_m', '2*m']])
tr = [ContractIndices];
tr.add(simplifyLeviCivita)
tr.addAll(mandelstam)
tr.add(parse('d^a_a = 4'))
tr.add(n2)
tr.add(parse('u = 4*m**2 -s - t'))
def M = parse('M_pxA = T_A*G_m*G^mn[k1_m+k2_m]*A_{npx}[k1_m + k2_m, -k3_m]')
M = G >> M
M = Aj0 >> M
M = tr >> expandAll(M, tr as Transformation[])
M = expandAll(M)

// |h| = 0 - transverse polarization
def M2_Sum = M >> parse('M2Sum = Tr[k2_m*G^m*M_pxA*k1_n*G^n*M_qy^A*P^pq[k3_m]]*J^xy')
// |h| = 0 - transverse polarization
def M2_0 =   M >> parse('M2L   = Tr[k2_m*G^m*M_pxA*k1_n*G^n*M_qy^A*P^pq[k3_m]]*JT^xy')
// |h| = 1 - longitudial polarization
def M2_1 =   M >> parse('M2T   = Tr[k2_m*G^m*M_pxA*k1_n*G^n*M_qy^A*P^pq[k3_m]]*JL^xy')

def M2_All = [M2_Sum, M2_0, M2_1]
for (M2 in M2_All) {

    M2 = gluonPolarization >> M2
    M2 = expandAll(M2)
    M2 = parse('Tr[T_A*T^A] = 3/2') >> M2
    M2 = DiracTrace.trace(M2)
    tr.add(parse('P_a*J^ab = 0'))
    tr.add(parse('P_a*JT^ab = 0'))
    tr.add(parse('P_a*JL^ab = 0'))
    def nSub = parse("u = 4*m**2 - s - t") >> [
            parse('k1_m*n^m = s**(1/2)*t/(4*m**2 - s)'),
            parse('k2_m*n^m = s**(1/2)*u/(4*m**2 - s)'),
            parse('k3_m*n^m = (s-4*m**2)/s**(1/2)'),
            parse('P_m*n^m = 4*m**2/s**(1/2)')]
    tr.addAll(nSub)
    M2 = tr >> expandAll(M2, tr as Transformation[])


    M2 = polarizations >> M2
    M2 = tr >> expandAll(M2, tr as Transformation[])

    println TensorUtils.isSymbolic(M2)
//    println Factor.factor(M2).toString(OutputFormat.WolframMathematica)
    new File('/home/stas/Projects/redberry/' + M2[0]).delete()
    new File('/home/stas/Projects/redberry/' + M2[0]) << M2[1].toString(OutputFormat.WolframMathematica)
}