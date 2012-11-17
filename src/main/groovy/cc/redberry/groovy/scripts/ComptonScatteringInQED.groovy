package cc.redberry.groovy.scripts

import cc.redberry.core.context.OutputFormat
import cc.redberry.core.number.Complex
import cc.redberry.core.tensor.SimpleTensor
import cc.redberry.core.tensor.Tensor
import cc.redberry.core.transformations.fractions.Together
import cc.redberry.groovy.RedberryGroovy
import cc.redberry.physics.feyncalc.FeynCalcUtils

import static cc.redberry.core.tensor.Tensors.parse
import static cc.redberry.core.transformations.ContractIndices.ContractIndices
import static cc.redberry.core.transformations.ContractIndices.contract
import static cc.redberry.core.transformations.expand.ExpandAll.EXPAND_ALL
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

def V = parse('''V^a'_{b' m} = I*e*G^a'_{b' m}''')
def D = parse('''D^a'_b'[p_m] = (m*d^a'_b' + p_m*G^a'_b'^m)/(m**2 - p_m*p^m)''')

def F1 = parse('''pv_a'[p2_m]*V^a'_{b' m}*e^m[k2_m]*D^b'_c'[k1_m+p1_m]*V^c'_{d' n}*e^n[k1_m]*v^d'[p1_m]'''),
    F2 = parse('''pv_a'[p2_m]*V^a'_{b' m}*e^m[k1_m]*D^b'_c'[p1_m-k2_m]*V^c'_{d' n}*e^n[k2_m]*v^d'[p1_m]''')

def M = F1 + F2

M = [V, D] >> M
def scalars = setMandelstam([['p1_m', 'm'], ['k1_m', '0'], ['p2_m', 'm'], ['k2_m', '0']]);
def polarizations = [
        parse('e_m[k1_a]*e_n[k1_a] = -g_mn'),
        parse('e_m[k2_a]*e_n[k2_a] = -g_mn')]
//Klein-Nishina
//def scalars = [
//        parse("k1_a*k1^a = 0"),
//        parse("k2_a*k2^a = 0"),
//        parse("p1_a*p1^a = m**2"),
//        parse("p2_a*p2^a = m**2"),
//        parse("p1_a*k1^a = m*k1"),
//        parse("p1_a*k2^a = m*k2"),
//        parse("k1_a*k2^a = m*(k1-k2)"),
//
//        parse("p2_a*k1^a = -m*k2"),
//        parse("p2_a*k2^a = m*(k1-k2-k2)"),
//        parse("p1_a*e^a[k1_m] = 0"),
//        parse("p1_a*e^a[k2_m] = 0"),
//        parse("e_a[k1_m]*e^a[k2_m] = Cos[chi]"),
//        parse('k2_m*e^m[k2_a] = 0'),
//        parse('k1_m*e^m[k1_a] = 0'),
//        parse('e_m[k1_a]*e^m[k1_a] = -1'),
//        parse('e_m[k1_a]*e^m[k1_a] = -1')
//]
M = expandAll(M)

//def fieldEqs = [
//        parse(''' p1^m*G^{a'}_{b'm}*v^{b'}[p1_m] = m*v^{a'}[p1_m] '''),
//        parse(''' p2^m*G^{a'}_{b'm}*v^{b'}[p2_m] = m*v^{a'}[p2_m] '''),
//        parse(''' pv_{a'}[p1_m]*p1^m*G^{a'}_{b'm} = m*pv_{b'}[p1_m] '''),
//        parse(''' pv_{a'}[p2_m]*p1^m*G^{a'}_{b'm} = m*pv_{b'}[p2_m] '''),
//]
//M = fieldEqs >> M
M = scalars >> M

def conjugate = {
    E ->
    E = parse('''pv_a'[p2_m]*v^b'[p1_m] = pv_a'[p1_m]*v^b'[p2_m]''') >> E
//    InverseProductOfMatrices.inverseProductsOfMatrices(E, IndexType.LatinLower1)
}

def MC = conjugate(M)
def M2 = expandAll(M * MC)
M2 = polarizations >> M2
//M2 = fieldEqs >> M2

//electron polarizations
M2 = parse('''pv_a'[p2_m]*v^b'[p2_m] = m*d_a'^b'+p2^m*G^b'_{a'm} ''') >> M2
M2 = parse('''pv_a'[p1_m]*v^b'[p1_m] = m*d_a'^b'+p1^m*G^b'_{a'm} ''') >> M2
M2 = expandAll(M2, ContractIndices)

//def traceSimplifications = [
//        parse(''' G^{a'}_{b'm}*G^{b'}_{c'}^{m} = d^{a'}_{c'}  '''),
//        parse(''' p1^m*p1^n*G^{a'}_{b'm}*G^{b'}_{c'n} = d^{a'}_{c'}*p1_m*p1^m  '''),
//        parse(''' p2^m*p2^n*G^{a'}_{b'm}*G^{b'}_{c'n} = d^{a'}_{c'}*p2_m*p2^m  '''),
//        parse(''' e^m[k1_m]*e^n[k1_m]*G^{a'}_{b'm}*G^{b'}_{c'n} = d^{a'}_{c'}*e_m[k1_m]*e^m[k1_m]  '''),
//        parse(''' e^m[k2_m]*e^n[k2_m]*G^{a'}_{b'm}*G^{b'}_{c'n} = d^{a'}_{c'}*e_m[k2_m]*e^m[k2_m]  ''')
//]
//for (int i in 1..10) {
//    M2 = traceSimplifications >> M2
//    M2 = contract(M2)
//}

//gamma traces
//M2 = DiracTrace.DIRAC_TRACE >> M2
M2 = expandAll(M2, ContractIndices)
M2 = contract(M2)
M2 = [parse('d_m^m = 4'), parse('''d_m'^m' = 4''')] >> M2
M2 = scalars >> M2
M2 = parse('u = 2*m**2-s-t') >> M2
M2 = Together.together(EXPAND_ALL >> (Together.together(M2)))

println M2
println M2.toString(OutputFormat.WolframMathematica)