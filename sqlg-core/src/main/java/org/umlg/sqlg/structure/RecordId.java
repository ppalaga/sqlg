package org.umlg.sqlg.structure;

import org.apache.tinkerpop.gremlin.structure.io.graphson.AbstractObjectDeserializer;
import org.apache.tinkerpop.shaded.jackson.core.JsonGenerationException;
import org.apache.tinkerpop.shaded.jackson.core.JsonGenerator;
import org.apache.tinkerpop.shaded.jackson.core.JsonProcessingException;
import org.apache.tinkerpop.shaded.jackson.databind.SerializerProvider;
import org.apache.tinkerpop.shaded.jackson.databind.jsontype.TypeSerializer;
import org.apache.tinkerpop.shaded.jackson.databind.ser.std.StdSerializer;
import org.apache.tinkerpop.shaded.kryo.Kryo;
import org.apache.tinkerpop.shaded.kryo.KryoSerializable;
import org.apache.tinkerpop.shaded.kryo.io.Input;
import org.apache.tinkerpop.shaded.kryo.io.Output;
import org.umlg.sqlg.util.SqlgUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Date: 2015/02/21
 * Time: 8:50 PM
 */
public class RecordId implements KryoSerializable {

    @SuppressWarnings("WeakerAccess")
    public final static String RECORD_ID_DELIMITER = ":::";
    private SchemaTable schemaTable;
    private Long id;

    //For Kryo
    public RecordId() {
    }

    private RecordId(SchemaTable schemaTable, Long id) {
        this.schemaTable = schemaTable;
        this.id = id;
    }

    private RecordId(String label, Long id) {
        this.schemaTable = SqlgUtil.parseLabel(label);
        this.id = id;
    }

    public static RecordId from(SchemaTable schemaTable, Long id) {
        return new RecordId(schemaTable, id);
    }

    public static List<RecordId> from(Object... elementId) {
        List<RecordId> result = new ArrayList<>(elementId.length);
        for (Object o : elementId) {
            if (o instanceof RecordId) {
                result.add((RecordId) o);
            } else {
                result.add(RecordId.from(o));
            }
        }
        return result;
    }

    public static RecordId from(Object vertexId) {
        if (!(vertexId instanceof String)) {
            throw SqlgExceptions.invalidId(vertexId.toString());
        }
        String stringId = (String) vertexId;
        String[] splittedId = stringId.split(RECORD_ID_DELIMITER);
        if (splittedId.length != 2) {
            throw SqlgExceptions.invalidId(vertexId.toString());
        }
        String label = splittedId[0];
        String id = splittedId[1];
        try {
            Long labelId = Long.valueOf(id);
            return new RecordId(label, labelId);
        } catch (NumberFormatException e) {
            throw SqlgExceptions.invalidId(vertexId.toString());
        }
    }

    public SchemaTable getSchemaTable() {
        return schemaTable;
    }

    public Long getId() {
        return id;
    }

    static Map<SchemaTable, List<Long>> normalizeIds(List<RecordId> vertexId) {
        Map<SchemaTable, List<Long>> result = new HashMap<>();
        for (RecordId recordId : vertexId) {
            List<Long> ids = result.get(recordId.getSchemaTable());
            if (ids == null) {
                ids = new ArrayList<>();
                result.put(recordId.getSchemaTable(), ids);
            }
            ids.add(recordId.getId());
        }
        return result;
    }

    @Override
    public String toString() {
        return this.schemaTable.toString() +
                RECORD_ID_DELIMITER +
                this.id.toString();
    }

    @Override
    public int hashCode() {
        return (this.schemaTable + this.id.toString()).hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (other == this) {
            return true;
        }
        if (!(other instanceof RecordId)) {
            return false;
        }
        RecordId otherRecordId = (RecordId) other;
        return this.schemaTable.equals(otherRecordId.getSchemaTable()) && this.id.equals(otherRecordId.getId());
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeString(this.getSchemaTable().getSchema());
        output.writeString(this.getSchemaTable().getTable());
        output.writeLong(this.getId());
    }

    @Override
    public void read(Kryo kryo, Input input) {
        this.schemaTable = SchemaTable.of(input.readString(), input.readString());
        this.id = input.readLong();
    }

    @SuppressWarnings("DuplicateThrows")
    static class RecordIdJacksonSerializerV2d0 extends StdSerializer<RecordId> {
        RecordIdJacksonSerializerV2d0() {
            super(RecordId.class);
        }

        @Override
        public void serialize(final RecordId recordId, final JsonGenerator jsonGenerator, final SerializerProvider serializerProvider)
                throws IOException, JsonGenerationException {
            // when types are not embedded, stringify or resort to JSON primitive representations of the
            // type so that non-jvm languages can better interoperate with the TinkerPop stack.
            jsonGenerator.writeString(recordId.toString());
        }

        @Override
        public void serializeWithType(final RecordId recordId, final JsonGenerator jsonGenerator,
                                      final SerializerProvider serializerProvider, final TypeSerializer typeSerializer) throws IOException, JsonProcessingException {
            // when the type is included add "class" as a key and then try to utilize as much of the
            // default serialization provided by jackson data-bind as possible.  for example, write
            // the uuid as an object so that when jackson serializes it, it uses the uuid serializer
            // to write it out with the type.  in this way, data-bind should be able to deserialize
            // it back when types are embedded.
            typeSerializer.writeTypePrefixForScalar(recordId, jsonGenerator);
            final Map<String, Object> m = new HashMap<>();
            m.put("schemaTable", recordId.getSchemaTable());
            m.put("id", recordId.getId());
            jsonGenerator.writeObject(m);
            typeSerializer.writeTypeSuffixForScalar(recordId, jsonGenerator);
        }
    }

    static class RecordIdJacksonDeserializerV2d0 extends AbstractObjectDeserializer<RecordId> {
        RecordIdJacksonDeserializerV2d0() {
            super(RecordId.class);
        }

        @Override
        public RecordId createObject(final Map data) {
            return RecordId.from((SchemaTable) data.get("schemaTable"), (Long) data.get("id"));
        }
    }

}
