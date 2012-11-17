package cc.redberry.groovy.scripts.chi

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
def gluonPolarization = parse('P_mn[k_a] = -g_mn + 1/(k_a*n^a)*(k_m*n_n + k_n*n_m) + 1/(k_a*n^a)**2 * k_m*k_n')
def n2 = parse('n_a*n^a = 1')

def simplifyLeviCivita = new LeviCivitaSimplify(parse('e_abcd'))

//gluon propagator
def G = parse('G_mn[k_a] = P_mn[k_a]/(k_a*k^a)')
G = gluonPolarization >> G

//Clebsch–Gordan coefficient
def J = parse('J_ab = P_a*P_b/(2*m)**2 - g_ab')
addSymmetry('J_ab', 1, 0)

//effective vertex
def A = parse('''A_{mn a}[k1_m, k2_m] = (1/2)*Tr[
                  (G_m*D[p1_m-k1_m]*G_n + G_n*D[p1_m-k2_m]*G_m)
                  *(p2_m*G^m - m)*G_a*(G_m*P^m + 2*m)*(p1_m*G^m + m)] ''')

A = [D, parse('p1_m = P_m/2 + q_m'), parse('p2_m = P_m/2 - q_m')] >> A
A = DiracTrace.trace(A)

def A1 = differentiate(A[1], parse('q^b')) * parse('e^{ab}_{cd}*P^c')
A1 = parse('q_a = 0') >> A1

tr = [ContractIndices, parse('d^a_a = 4'), parse('P_m*P^m = 4*m**2')]
A1 = tr >> expandAll(A1, tr as Transformation[])

def Aj0 = expression(parse('A_{mnd}[k1_m, k2_m]'), A1)

assert (expandAll(Aj0 >> parse('A_{mnd}[k1_m, k2_m] - A_{nmd}[k2_m, k1_m]'))) == parse('0')

def temp = parse('P_m = k1_m + k2_m') >> (Aj0 >> parse('k1^m*A_{mnd}[k1_m, k2_m]'))
tr = [ContractIndices, parse('k1_m*k1^m = 0'), parse('k2_m*k2^m = 0'), parse('k1_m*k2^m = 2*m**2'), parse('d^a_a = 4'), parse(''' d^A'_A' = N '''), parse(''' d^a'_a' = 4 ''')]
temp = expandAll(temp, tr as Transformation[])
temp = tr >> temp
temp = simplifyLeviCivita >> temp
assert expandAll(temp) == parse('0')

def mandelstam = setMandelstam([['k1_m', '0'], ['k2_m', '0'], ['k3_m', '0'], ['P_m', '2*m']])
tr = [ContractIndices];
tr.add(simplifyLeviCivita)
tr.addAll(mandelstam)
tr.add(parse('d^a_a = 4'))
tr.addAll([n2, parse('k1_a*n^a = kn1'), parse('k2_a*n^a = kn2'), parse('k3_a*n^a = kn3')])
//tr.add(parse('k2_m*P^mn[k2_m] = 0'))
tr.add(parse('u = 4*m**2 -s - t'))


def M = parse('M_pxA = T_A*G_m*G^mn[k1_m-k3_m]*A_{npx}[k1_m-k3_m, k2_m]')
M = G >> M
M = Aj0 >> M
M = tr >> expandAll(M, tr as Transformation[])
M = expandAll(M)

println 'zzz'
def M2 = M >> parse('M2 = Tr[k3_m*G^m*M_pxA*k1_n*G^n*M_qy^A*P^pq[k2_m]]*J^xy')

println M2
M2 = expandAll(M2)
M2 = parse('Tr[T_A*T^A] = N/2') >> M2
M2 = DiracTrace.trace(M2)
println 'ddd'
tr.add(parse('P_a*J^ab = 0'))
M2 = tr >> expandAll(M2, tr as Transformation[])
M2 = J >> M2
M2 = gluonPolarization >> M2
M2 = tr >> expandAll(M2, tr as Transformation[])

//println M2

println 'zzz'
M2 = parse('P_m*n^m = kn1 + kn2 - kn3') >> M2
//M2 = tr >> expandAll(M2, tr as Transformation[])
println 'xxx1'
//M2 = expandAll(M2)
println 'xxx2'
println TensorUtils.isSymbolic(M2)
new File('/home/stas/Projects/redberry/temp1').delete()
new File('/home/stas/Projects/redberry/temp1') << M2[1].toString(OutputFormat.WolframMathematica)
//M2 = Together.together(M2);
//new File('/home/stas/Projects/redberry/temp1together') << M2[1].toString(OutputFormat.WolframMathematica)
