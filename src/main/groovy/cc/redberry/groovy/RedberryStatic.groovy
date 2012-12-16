package cc.redberry.groovy

import cc.redberry.core.tensor.SimpleTensor
import cc.redberry.core.tensor.Tensor
import cc.redberry.core.transformations.Differentiate
import cc.redberry.core.transformations.Transformation

class RedberryStatic {
    static final Transformation Expand = new Transformation() {
        @Override
        Tensor transform(Tensor t) { cc.redberry.core.transformations.expand.Expand.expand(t) }

        Transformation getAt(Transformation... transformations) { new cc.redberry.core.transformations.expand.Expand(transformations) }
    }

    static final StaticDifferentiate Differentiate = new StaticDifferentiate();

    static class StaticDifferentiate {
        Transformation getAt(Tensor... s) {
            return new Differentiate(s as SimpleTensor[]);
        }

        Transformation getAt(Transformation transformations, Tensor... s) {
            return new Differentiate(transformations, s as SimpleTensor[]);
        }

        Transformation getAt(String s) {
            use(Redberry) {
                return new Differentiate(s.t);
            }
        }

        Transformation getAt(String... s) {
            use(Redberry) {
                return new Differentiate(s.collect { it.t } as SimpleTensor[]);
            }
        }

        Transformation getAt(Transformation transformations, String s) {
            use(Redberry) {
                return new Differentiate(transformations, s.t);
            }
        }

        Transformation getAt(Transformation transformations, String... s) {
            use(Redberry) {
                return new Differentiate(transformations, s.collect { it.t } as SimpleTensor[]);
            }
        }
    }
}
