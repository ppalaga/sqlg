package org.umlg.sqlg;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.umlg.sqlg.test.vertex.TestOtherVertex;

/**
 * Date: 2014/07/16
 * Time: 12:10 PM
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        TestOtherVertex.class,
//        TestRepeatStepGraphOut.class,
//        TestRepeatStepGraphIn.class,
//        TestRepeatStepVertexOut.class,
//        TestRepeatStepGraphBoth.class,
//        TestGremlinOptional.class
//        TestGremlinCompileVertexStep.class
})
public class AnyTest {
}
