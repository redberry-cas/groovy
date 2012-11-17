package cc.redberry.groovy.scripts

import cc.redberry.core.transformations.ContractIndices
import cc.redberry.core.transformations.Transformation
import cc.redberry.core.transformations.expand.Expand
import cc.redberry.groovy.RedberryGroovy

import static cc.redberry.core.tensor.Tensors.parse
import static cc.redberry.groovy.RedberryGroovy.timing
import cc.redberry.core.transformations.expand.ExpandNumerator
import cc.redberry.core.transformations.expand.ExpandDenominator

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
RedberryGroovy.withRedberry()

def v = parse('(a+b)/(A_m^m+B)**2')
def tr = ExpandDenominator.EXPAND_DENOMINATOR
println tr >> v


def t = parse('''(g_af*g_bc+g_bf*g_ac+g_cf*g_ba)*(T_d*T_e+g_de)*
                    (g^db*g^ae + g^de*g^ab)''')

def contract = [ContractIndices.ContractIndices, parse('d_a^a = 4')] as Transformation[]

for (i in 1..1000) {
    Expand.expand(t, contract)
    contract >> Expand.expand(t)
}
println Expand.expand(t, contract)

timing { Expand.expand(t, contract)}
timing { (contract >> Expand.expand(t))}

