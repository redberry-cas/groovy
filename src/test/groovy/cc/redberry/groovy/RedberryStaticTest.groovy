package cc.redberry.groovy

import cc.redberry.core.transformations.RemoveDueToSymmetry
import org.junit.Test

import static cc.redberry.core.tensor.Tensors.addAntiSymmetry
import static cc.redberry.core.tensor.Tensors.addSymmetry
import static cc.redberry.groovy.RedberryStatic.*
import static org.junit.Assert.assertTrue

class RedberryStaticTest {
    @Test
    void testExpand1() throws Exception {
        use(Redberry) {
            def tensor = '(f+d)*(f+d)'.t;
            assertTrue tensor << Expand['f**2=1'.t &
                    'd=c'.t] == 'c**2+1+2*f*c'.t
        }
    }

    @Test
    void testExpand2() throws Exception {
        use(Redberry) {
            def tensor = '(g_mn*g_ab + g_ma*g_nb)*(g^mn*g^ab + g^ma*g^nb)'.t;
            assertTrue tensor << Expand[EliminateMetrics & 'd^m_m = 4'.t] == '40'.t
            assertTrue tensor << ExpandAll[EliminateMetrics & 'd^m_m = 4'.t] == '40'.t
        }
    }


    @Test
    void testDifferentiate1() throws Exception {
        use(Redberry) {
            def tensor = '(f+d)*(f+d+c)'.t;
            assertTrue tensor << Differentiate['d=0'.t, 'f'] == '2*f+c'.t
        }
    }

    @Test
    void testDifferentiate2() throws Exception {
        use(Redberry) {
            addAntiSymmetry('R_abcd', 1, 0, 2, 3)
            addAntiSymmetry('R_abcd', 0, 1, 3, 2)
            addSymmetry('R_abcd', 2, 3, 0, 1)

            def tensor = 'R^acbd*Sin[R_abcd*R^abcd]'.t;
            def tr =
                Differentiate[ExpandAndEliminate, 'R^ma_m^b', 'R^mc_m^d'] & EliminateFromSymmetries & 'd_m^m = 4'.t & 'R^a_man = R_mn'.t & 'R^a_a = R'.t

            assertTrue tr >> tensor == '6*R*Cos[R_{abcd}*R^{abcd}]-4*Sin[R_{abcd}*R^{abcd}]*R_{ab}*R_{cd}*R^{acbd}'.t
        }
    }

    @Test
    void testNumerator1() throws Exception {
        use(Redberry) {
            assertTrue Numerator >> '1/a+1/b'.t == '1/a+1/b'.t
            assertTrue Denominator >> '1/a+1/b'.t == '1'.t
        }
    }
}

