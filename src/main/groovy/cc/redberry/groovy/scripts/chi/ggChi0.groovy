package cc.redberry.groovy.scripts.chi

import cc.redberry.core.context.CC
import cc.redberry.core.number.Complex
import cc.redberry.core.parser.preprocessor.GeneralIndicesInsertion
import cc.redberry.core.tensor.SimpleTensor
import cc.redberry.core.tensor.Tensor
import cc.redberry.core.transformations.Transformation
import cc.redberry.core.utils.TensorUtils
import cc.redberry.groovy.RedberryGroovy
import cc.redberry.physics.feyncalc.DiracTrace
import cc.redberry.physics.feyncalc.FeynCalcUtils
import cc.redberry.physics.feyncalc.UnitaryTrace

import static cc.redberry.core.indices.IndexType.LatinLower1
import static cc.redberry.core.indices.IndexType.LatinUpper1
import static cc.redberry.core.tensor.Tensors.*
import static cc.redberry.core.transformations.ComplexConjugate.CONJUGATE
import static cc.redberry.core.transformations.ContractIndices.ContractIndices
import static cc.redberry.core.transformations.Differentiate.differentiate
import static cc.redberry.core.transformations.expand.Expand.expand
import static cc.redberry.core.transformations.expand.ExpandAll.expandAll

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
def setMatrix = {
    a, b ->
    if (a instanceof String)
        a = parse(a)
    indicesInsertion.addInsertionRule(a, b)
}

setMatrix(''' G_a^a'_b' ''', LatinLower1)
setMatrix(''' T_A^A'_B' ''', LatinUpper1)


setSymmetric('d_{ABC}')
addAntiSymmetry('f_{ABC}', 1, 0, 2)
addSymmetry('f_{ABC}', 2, 0, 1)
def SUNTrace = new UnitaryTrace(parse(''' T^{A'}_{B'A} '''), parse('f_{ABC}'),
        parse('d_{ABC}'), parse('3'))
def GTrace = new DiracTrace(parse(''' G^a'_b'a '''))
//quark propagator
setMatrix(''' D^a'_b'[p_m] ''', LatinLower1)
def D = parse('D[p_m] = (m + p_m*G^m)/(p_m*p^m - m**2)')
//sum over gluon polarizations
addSymmetry('P_mn[k_a]', 1, 0)
def gluonPolarization = parse('P_mn[k_a] = -g_mn + 1/(k_a*n^a)*(k_m*n_n + k_n*n_m) + 1/(k_a*n^a)**2 * k_m*k_n')
def n2 = parse('n_a*n^a = 1')

//gluon propagator
def G = parse('G_mn[k_a] = P_mn[k_a]/(k_a*k^a)')
G = gluonPolarization >> G
//3-gluon vertex
def V = parse('V_{mnr}^{ABC}[k1_m, k2_m, k3_m] = f^{ABC}*(g_mn*(k2_r - k1_r) + g_nr*(k3_m - k2_m) + g_mr*(k1_n-k3_n))')

setMatrix(''' Pr^{a'}_{b'a}[p1_m, p2_m] ''', LatinLower1)
def projector = parse(' Pr_a[p1_m, p2_m] = (p2_m*G^m + m)*G_a*(-p1_m*G^m + m)')

//Clebsch–Gordan coefficient
def J = parse('J_ab = P_a*P_b/(2*m)**2 - g_ab')
addSymmetry('J_ab', 1, 0)


def mandelstam = setMandelstam([['k1_m', '0'], ['k2_m', '0'], ['k3_m', '0'], ['P_m', '2*m']])
def tr = [ContractIndices];
tr.addAll(mandelstam)
tr.add(parse('d^a_a = 4'))
tr.addAll([n2, parse('k1_a*n^a = kn1'), parse('k2_a*n^a = kn2'), parse('k3_a*n^a = kn3')])
tr.add(parse('k2_m*P^mn[k2_m] = 0'))
tr.add(parse('P_a*J^ab = 0'))
tr.add(parse('d^A_A = 3'))
tr.add(parse('u = 4*m**2 -s - t'))
tr = tr as Transformation[]

/******************
 ***** gq part ****
 ***************** */
def gq1 = parse('gq1^{ABC}_{abc p}[k1_m, k2_m, k3_m] = Tr[T^{A}*T^{B}*T^{C}*I*G_a*D[k1_m - p1_m]*G_b*D[k1_m+k2_m-p1_m]*G_c*Pr_p[p1_m,p2_m]]')
gq1 = SUNTrace >> gq1
def gq2 = gq1 >> parse(''' gq1^{ABC}_{abc p}[k1_m, k2_m, k3_m] + gq1^{BAC}_{bac p}[k2_m, k1_m, -k3_m] +
                           gq1^{CBA}_{cba p}[-k3_m, k2_m, k1_m] + gq1^{ACB}_{acb p}[k1_m, -k3_m, k2_m] +
                           gq1^{CAB}_{cab p}[-k3_m, k1_m, k2_m] + gq1^{BCA}_{bca p}[k2_m, -k3_m, k1_m] ''')

gq2 = [D, projector] >> gq2
def gq3 = [parse('p1_m = P_m/2 + q_m'), parse('p2_m = P_m/2 - q_m')] >> gq2
gq3 = parse('q_a = 0') >> differentiate(gq3, parse('q^q')) * parse('J^pq')
gq = expression(parse('gq^{ABC}_{abc}'), gq3)
gq = tr >> expandAll(gq, tr)
gq = tr >> (GTrace >> gq)
//println gq

/******************
 ******* Aj0 ******
 ***************** */
def A = parse('''A_{mn a}[k1_m, k2_m] = Tr[
                  (G_m*D[p1_m-k1_m]*G_n + G_n*D[p1_m-k2_m]*G_m)
                  *(p2_m*G^m - m)*G_a*(G_m*P^m + 2*m)*(p1_m*G^m + m)] ''')

A = [D, parse('p1_m = P_m/2 + q_m'), parse('p2_m = P_m/2 - q_m')] >> A
A = GTrace >> A

def A1 = differentiate(A[1], parse('q^b')) * parse('J^ab')
A1 = [parse('q_a = 0'), J] >> A1

def tr1 = [ContractIndices, parse('d^a_a = 4'), parse('P_m*P^m = 4*m**2')] as Transformation[]
A1 = tr1 >> expandAll(A1, tr1)

def Aj0 = expression(parse('A_{mn}[k1_m, k2_m]'), A1)

/******************
 ***** gg part ****
 ***************** */
def gg1 = parse('gg1^ABC_{abc}[k1_m, k2_m, k3_m] = V^{CAB}_{cad}[-k3_m,k1_m,k3_m-k1_m]*G^{kd}[k3_m-k1_m]*A_{kb}[k1_m - k3_m, k2_m]')
def gg2 = gg1 >> parse('gg2^ABC_{abc}[k1_m, k2_m, k3_m] = gg1^ABC_{abc}[k1_m, k2_m, k3_m] + gg1^BAC_{bac}[k2_m, k1_m, k3_m]')
def gg3 = parse('gg3^ABC_{abc}[k1_m, k2_m, k3_m] = V^{ACB}_{adb}[k1_m,-k1_m-k2_m,k2_m]*G^{kd}[-k1_m-k2_m]*A_{kc}[k1_m + k2_m, -k3_m]')
def gg = [gg2, gg3] >> parse('gg^ABC_{abc} = gg2^ABC_{abc}[k1_m, k2_m, k3_m] + gg3^BAC_{bac}[k2_m, k1_m, k3_m]')
gg = [G, Aj0, V] >> gg
gg = tr >> expandAll(gg, tr)
//println gg

/*****************************
 ******* Matrix element ******
 ***************************** */

def M = parse('M^ABC_{abc} = gq^ABC_{abc} + gg^ABC_{abc}')
M = [gq, gg] >> M
M = parse('d_ABC = 0') >> M
println M[1].size()
def CM = expression(parse('CM^ABC_{abc}'), CONJUGATE >> M[1])
def M2 = parse(' M2 = M^ABC_{abc} * CM_ABC_{pqr}*P^{ap}[k1_m]*P^{bq}[k2_m]*P^{cr}[k3_m]')
M2 = [M, CM] >> M2

tr = tr as List
tr << parse('k1_m*P^mn[k1_m] = 0') << parse('k3_m*P^mn[k3_m] = 0') << parse('d_ABC*d^ABC = 40/3') << parse('f_ABC*f^ABC = 24')
tr = tr as Transformation[]
M2 = tr >> expand(M2, tr)
new File('/home/stas/Projects/redberry/temp1').delete()
new File('/home/stas/Projects/redberry/temp1') << M[1].toString()
M2 = [gluonPolarization, J] >> M2
M2 = tr >> expand(M2, tr)
new File('/home/stas/Projects/redberry/temp2').delete()
new File('/home/stas/Projects/redberry/temp2') << M[1].toString()
println TensorUtils.isSymbolic(M2)
//M = G >> M
//M = Aj0 >> M
//M = tr >> expandAll(M, tr as Transformation[])
//M = expandAll(M)
//
//println 'zzz'
//def M2 = M >> parse('M2 = Tr[k3_m*G^m*M_pxA*k1_n*G^n*M_qy^A*P^pq[k2_m]]*J^xy')
//
//println M2
//M2 = expandAll(M2)
//M2 = parse('Tr[T_A*T^A] = N/2') >> M2
//M2 = DiracTrace.trace(M2)
//println 'ddd'
//tr.add(parse('P_a*J^ab = 0'))
//M2 = tr >> expandAll(M2, tr as Transformation[])
//M2 = J >> M2
//M2 = gluonPolarization >> M2
//M2 = tr >> expandAll(M2, tr as Transformation[])
//
////println M2
//
//println 'zzz'
//M2 = parse('P_m*n^m = kn1 + kn2 - kn3') >> M2
////M2 = tr >> expandAll(M2, tr as Transformation[])
//println 'xxx1'
////M2 = expandAll(M2)
//println 'xxx2'
//println TensorUtils.isSymbolic(M2)
//new File('/home/stas/Projects/redberry/temp1').delete()
//new File('/home/stas/Projects/redberry/temp1') << M2[1].toString(OutputFormat.WolframMathematica)
//M2 = Together.together(M2);
//new File('/home/stas/Projects/redberry/temp1together') << M2[1].toString(OutputFormat.WolframMathematica)
