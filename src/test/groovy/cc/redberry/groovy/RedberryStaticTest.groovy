package cc.redberry.groovy

import org.junit.Assert
import org.junit.Test

import static cc.redberry.groovy.RedberryStatic.*
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

class RedberryStaticTest {
    @Test
    void testExpand() throws Exception {
        use(Redberry) {
            def tensor = '(f+d)*(f+d)'.t;
            assertTrue tensor << Expand['f**2=1'.t &
                    'd=c'.t] == 'c**2+1+2*f*c'.t
        }
    }

    @Test
    void testDifferentiate() throws Exception {
        use(Redberry) {
            def tensor = '(f+d)*(f+d+c)'.t;
            assertTrue tensor << Differentiate['d=0'.t, 'f'] == '2*f+c'.t
        }
    }
}
