package cc.redberry.groovy.scripts

import cc.redberry.core.context.CC
import cc.redberry.core.context.OutputFormat
import cc.redberry.core.indices.IndexType
import cc.redberry.core.number.Complex
import cc.redberry.core.parser.preprocessor.GeneralIndicesInsertion
import cc.redberry.core.transformations.ComplexConjugate
import cc.redberry.core.transformations.factor.Factor
import cc.redberry.core.transformations.fractions.Together
import cc.redberry.core.utils.TensorUtils
import cc.redberry.groovy.InverseOrderOfGammas
import cc.redberry.groovy.Redberry
import cc.redberry.physics.feyncalc.DiracTrace

import static cc.redberry.core.tensor.Tensors.parse
import static cc.redberry.core.transformations.ContractIndices.ContractIndices
import static cc.redberry.core.transformations.ContractIndices.contract
import static cc.redberry.core.transformations.expand.ExpandAll.EXPAND_ALL
import static cc.redberry.core.transformations.expand.ExpandAll.expandAll
import static cc.redberry.core.transformations.factor.Factor.*
import static cc.redberry.groovy.RedberryPhysics.setMandelstam

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */

use(Redberry) {
    GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
    CC.current().parseManager.defaultParserPreprocessors.add(indicesInsertion);
    indicesInsertion.addInsertionRule(parse(''' G_a^a'_b' '''), IndexType.LatinLower1)
    indicesInsertion.addInsertionRule(parse(''' pv_b'[x_m] '''), IndexType.LatinLower1)
    indicesInsertion.addInsertionRule(parse(''' v^b'[x_m] '''), IndexType.LatinLower1)
    indicesInsertion.addInsertionRule(parse(''' V^a'_{b' m}  '''), IndexType.LatinLower1)
    indicesInsertion.addInsertionRule(parse(''' D^a'_b'[x_m]  '''), IndexType.LatinLower1)


    def V = 'V_m = -I*e*G_m'.t,
        D = 'D[p_m] = -I*(m + p_m*G^m)/(m**2 - p_m*p^m)'.t,
        F1 = 'pv[p2_m]*V_m*e^m[k2_m]*D[k1_m+p1_m]*V_n*e^n[k1_m]*v[p1_m]'.t,
        F2 = 'pv[p2_m]*V_m*e^m[k1_m]*D[p1_m-k2_m]*V_n*e^n[k2_m]*v[p1_m]'.t,
        M = F1 + F2

    M = [V, D] >> M
    def mandelstam = setMandelstam(['p1_m': 'm', 'k1_m': '0', 'p2_m': 'm', 'k2_m': '0']);
    M = expandAll(M)
    M = mandelstam >> M
    def MC = M

    MC = 'v[p1_m]*pv[p2_m] = v[p2_m]*pv[p1_m]' >> MC
    MC = expandAll(MC)
    MC = InverseOrderOfGammas.inverseOrderOfGammas(MC, 'G_a'.t)

    //complex conjugate
    MC = MC.transformEachInTree { a -> a.class == Complex ? a.conjugate() : a }
    def M2 = expandAll(M * MC)

    //photon polarizations
    M2 = ['e_m[k1_a]*e_n[k1_a] = -g_mn',
            'e_m[k2_a]*e_n[k2_a] = -g_mn'] >> M2

    //electron polarizations
    M2 = ['v[p2_m]*pv[p2_m] =  m + p2^m*G_m',
            'v[p1_m]*pv[p1_m] = m + p1^m*G_m'] >> M2

    M2 = expandAll(M2, ContractIndices)

    //gamma traces
    M2 = DiracTrace.trace(M2)
    M2 = expandAll(M2, ContractIndices)
    M2 = contract(M2)
    M2 = 'd_m^m = 4' >> M2
    M2 = mandelstam >> M2
    M2 = 'u = 2*m**2 -s-t' >> M2
    assert TensorUtils.isSymbolic(M2)
    println factor(M2)
}