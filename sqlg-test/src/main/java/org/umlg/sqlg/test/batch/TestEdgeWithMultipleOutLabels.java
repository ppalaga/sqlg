package org.umlg.sqlg.test.batch;

import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.umlg.sqlg.test.BaseTest;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Date: 2016/08/09
 * Time: 9:10 AM
 */
public class TestEdgeWithMultipleOutLabels extends BaseTest {

    @Before
    public void beforeTest() {
        Assume.assumeTrue(this.sqlgGraph.getSqlDialect().supportsBatchMode());
    }

    @Test
    public void testBatchModeEdgeMultipleOutLabels() {
        sqlgGraph.tx().normalBatchModeOn();
        for (int i = 0; i < 5; i++) {
            Vertex a1 = this.sqlgGraph.addVertex(T.label, "A1");
            Vertex a2  = this.sqlgGraph.addVertex(T.label, "A2");
            Vertex b = this.sqlgGraph.addVertex(T.label, "B");
            a1.addEdge("address", b);
            a2.addEdge("address", b);
        }
        this.sqlgGraph.tx().commit();
        assertEquals(5, this.sqlgGraph.traversal().V().hasLabel("A1").count().next().intValue());
        assertEquals(5, this.sqlgGraph.traversal().V().hasLabel("A2").count().next().intValue());
        assertEquals(5, this.sqlgGraph.traversal().V().hasLabel("A1").out("address").count().next().intValue());
        assertEquals(5, this.sqlgGraph.traversal().V().hasLabel("A2").out("address").count().next().intValue());

        sqlgGraph.tx().normalBatchModeOn();
        for (int i = 0; i < 5; i++) {
            Vertex a1 = this.sqlgGraph.addVertex(T.label, "A1");
            Vertex a2  = this.sqlgGraph.addVertex(T.label, "A2");
            Vertex b = this.sqlgGraph.addVertex(T.label, "B");
            a1.addEdge("address", b);
            a2.addEdge("address", b);
        }
        this.sqlgGraph.tx().commit();
        assertEquals(10, this.sqlgGraph.traversal().V().hasLabel("A1").count().next().intValue());
        assertEquals(10, this.sqlgGraph.traversal().V().hasLabel("A2").count().next().intValue());
        assertEquals(10, this.sqlgGraph.traversal().V().hasLabel("A1").out("address").count().next().intValue());
        assertEquals(10, this.sqlgGraph.traversal().V().hasLabel("A2").out("address").count().next().intValue());
    }

    @Test
    public void issue57() {
        sqlgGraph.tx().normalBatchModeOn();
        String key = "AbstractTinkerPopFhirGraph.Prop.ID";
        int indexCount = 100;
        for (int index = 0; index < indexCount; index++) {
            Vertex v1 = sqlgGraph.addVertex(T.label, "Patient", key, index);
            Vertex v2 = sqlgGraph.addVertex(T.label, "HumanNameDt", key, UUID.randomUUID().toString());
            v1.addEdge("name", v2);

            Vertex v3 = sqlgGraph.addVertex(T.label, "Condition", key, index);
            Vertex v4 = sqlgGraph.addVertex(T.label, "CodeableConceptDt", key, UUID.randomUUID().toString());
            Vertex v5 = sqlgGraph.addVertex(T.label, "CodingDt", key, UUID.randomUUID().toString());
            v3.addEdge("code", v4);
            v4.addEdge("coding", v5);

            v3.addEdge("patient", v1);

            Vertex v6 = sqlgGraph.addVertex(T.label, "CodeableConcept", key, UUID.randomUUID().toString());
            v3.addEdge("category", v6);

            Vertex v7 = sqlgGraph.addVertex(T.label, "CodingDt", key, UUID.randomUUID().toString());
            v6.addEdge("coding", v7);
        }
        sqlgGraph.tx().commit();

        assertEquals(indexCount, this.sqlgGraph.traversal().V().hasLabel("Patient").count().next().intValue());
        assertEquals(indexCount, this.sqlgGraph.traversal().V().hasLabel("HumanNameDt").count().next().intValue());
        assertEquals(indexCount, this.sqlgGraph.traversal().V().hasLabel("Patient").out("name").count().next().intValue());
        this.sqlgGraph.traversal().V().hasLabel("Patient").out("name").forEachRemaining(v -> assertTrue(v.label().equals("HumanNameDt")));

        assertEquals(indexCount, this.sqlgGraph.traversal().V().hasLabel("Condition").count().next().intValue());
        assertEquals(indexCount, this.sqlgGraph.traversal().V().hasLabel("CodeableConceptDt").count().next().intValue());
        assertEquals(indexCount * 2, this.sqlgGraph.traversal().V().hasLabel("CodingDt").count().next().intValue());
        assertEquals(indexCount, this.sqlgGraph.traversal().V().hasLabel("Condition").out("code").count().next().intValue());
        assertEquals(indexCount, this.sqlgGraph.traversal().V().hasLabel("CodeableConceptDt").out("coding").count().next().intValue());

        this.sqlgGraph.traversal().V().hasLabel("Condition").out("code").forEachRemaining(v -> assertTrue(v.label().equals("CodeableConceptDt")));
        this.sqlgGraph.traversal().V().hasLabel("Condition").out("coding").forEachRemaining(v -> assertTrue(v.label().equals("CodingDt")));
        this.sqlgGraph.traversal().V().hasLabel("Condition").out("patient").forEachRemaining(v -> assertTrue(v.label().equals("Patient")));

        assertEquals(indexCount, this.sqlgGraph.traversal().V().hasLabel("CodeableConcept").count().next().intValue());
        assertEquals(indexCount, this.sqlgGraph.traversal().V().hasLabel("Condition").out("category").count().next().intValue());
        this.sqlgGraph.traversal().V().hasLabel("Condition").out("category").forEachRemaining(v -> assertTrue(v.label().equals("CodeableConcept")));

        assertEquals(indexCount, this.sqlgGraph.traversal().V().hasLabel("CodeableConcept").out("coding").count().next().intValue());
        this.sqlgGraph.traversal().V().hasLabel("CodeableConcept").out("coding").forEachRemaining(v -> assertTrue(v.label().equals("CodingDt")));

    }


}
