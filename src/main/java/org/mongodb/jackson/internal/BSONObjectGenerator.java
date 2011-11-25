package org.mongodb.jackson.internal;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.codehaus.jackson.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON generator that actually generates a BSON object
 */
public class BSONObjectGenerator extends JsonGenerator {
    private final ObjectNode rootNode = new ObjectNode(null);
    private ObjectCodec objectCodec;
    private Node currentNode;
    private boolean closed = false;

    public DBObject getDBObject() {
        return rootNode.get();
    }

    @Override
    public JsonGenerator enable(Feature f) {
        return this;
    }

    @Override
    public JsonGenerator disable(Feature f) {
        return this;
    }

    @Override
    public boolean isEnabled(Feature f) {
        return false;
    }

    @Override
    public JsonGenerator setCodec(ObjectCodec oc) {
        objectCodec = oc;
        return this;
    }

    @Override
    public ObjectCodec getCodec() {
        return objectCodec;
    }

    @Override
    public JsonGenerator useDefaultPrettyPrinter() {
        return this;
    }

    @Override
    public void writeStartArray() throws IOException {
        if (currentNode == null) {
            currentNode = rootNode;
        } else {
            currentNode = new ArrayNode(currentNode);
        }
    }

    @Override
    public void writeEndArray() throws IOException {
        Object array = currentNode.get();
        currentNode = currentNode.getParent();
        if (currentNode != null) {
            currentNode.set(array);
        }
    }

    @Override
    public void writeStartObject() throws IOException {
        if (currentNode == null) {
            currentNode = rootNode;
        } else {
            currentNode = new ObjectNode(currentNode);
        }
    }

    @Override
    public void writeEndObject() throws IOException {
        Object object = currentNode.get();
        currentNode = currentNode.getParent();
        if (currentNode != null) {
            currentNode.set(object);
        }
    }

    @Override
    public void writeFieldName(String name) throws IOException {
        currentNode.setName(name);
    }

    @Override
    public void writeString(String text) throws IOException {
        currentNode.set(text);
    }

    @Override
    public void writeString(char[] text, int offset, int len) throws IOException {
        currentNode.set(new String(text, offset, len));
    }

    @Override
    public void writeRawUTF8String(byte[] text, int offset, int length) throws IOException {
        currentNode.set(new String(text, offset, length, "UTF-8"));
    }

    @Override
    public void writeUTF8String(byte[] text, int offset, int length) throws IOException {
        currentNode.set(new String(text, offset, length, "UTF-8"));
    }

    @Override
    public void writeRaw(String text) throws IOException {
        throw new UnsupportedOperationException("Writing raw not supported");
    }

    @Override
    public void writeRaw(String text, int offset, int len) throws IOException {
        throw new UnsupportedOperationException("Writing raw not supported");
    }

    @Override
    public void writeRaw(char[] text, int offset, int len) throws IOException {
        throw new UnsupportedOperationException("Writing raw not supported");
    }

    @Override
    public void writeRaw(char c) throws IOException {
        throw new UnsupportedOperationException("Writing raw not supported");
    }

    @Override
    public void writeRawValue(String text) throws IOException {
        currentNode.set(text);
    }

    @Override
    public void writeRawValue(String text, int offset, int len) throws IOException {
        currentNode.set(text.substring(offset, offset + len));
    }

    @Override
    public void writeRawValue(char[] text, int offset, int len) throws IOException {
        currentNode.set(new String(text, offset, len));
    }

    @Override
    public void writeBinary(Base64Variant b64variant, byte[] data, int offset, int len) throws IOException {
        if (offset != 0 || len != data.length) {
            byte[] subset = new byte[len];
            System.arraycopy(data, offset, subset, 0, len);
            data = subset;
        }
        currentNode.set(data);
    }

    @Override
    public void writeNumber(int v) throws IOException {
        currentNode.set(v);
    }

    @Override
    public void writeNumber(long v) throws IOException {
        currentNode.set(v);
    }

    @Override
    public void writeNumber(BigInteger v) throws IOException {
        currentNode.set(v);
    }

    @Override
    public void writeNumber(double d) throws IOException {
        currentNode.set(d);
    }

    @Override
    public void writeNumber(float f) throws IOException {
        currentNode.set(f);
    }

    @Override
    public void writeNumber(BigDecimal dec) throws IOException {
        currentNode.set(dec);
    }

    @Override
    public void writeNumber(String encodedValue) throws IOException, UnsupportedOperationException {
        currentNode.set(encodedValue);
    }

    @Override
    public void writeBoolean(boolean state) throws IOException {
        currentNode.set(state);
    }

    @Override
    public void writeNull() throws IOException {
        currentNode.set(null);
    }

    @Override
    public void writeObject(Object pojo) throws IOException {
        currentNode.set(pojo);
    }

    @Override
    public void writeTree(JsonNode rootNode) throws IOException {
        throw new UnsupportedClassVersionError("Writing JSON nodes not supported");
    }

    @Override
    public void copyCurrentEvent(JsonParser jp) throws IOException {
        JsonToken t = jp.getCurrentToken();
        switch(t) {
        case START_OBJECT:
            writeStartObject();
            break;
        case END_OBJECT:
            writeEndObject();
            break;
        case START_ARRAY:
            writeStartArray();
            break;
        case END_ARRAY:
            writeEndArray();
            break;
        case FIELD_NAME:
            writeFieldName(jp.getCurrentName());
            break;
        case VALUE_STRING:
            if (jp.hasTextCharacters()) {
                writeString(jp.getTextCharacters(), jp.getTextOffset(), jp.getTextLength());
            } else {
                writeString(jp.getText());
            }
            break;
        case VALUE_NUMBER_INT:
            switch (jp.getNumberType()) {
            case INT:
                writeNumber(jp.getIntValue());
                break;
            case BIG_INTEGER:
                writeNumber(jp.getBigIntegerValue());
                break;
            default:
                writeNumber(jp.getLongValue());
            }
            break;
        case VALUE_NUMBER_FLOAT:
            switch (jp.getNumberType()) {
            case BIG_DECIMAL:
                writeNumber(jp.getDecimalValue());
                break;
            case FLOAT:
                writeNumber(jp.getFloatValue());
                break;
            default:
                writeNumber(jp.getDoubleValue());
            }
            break;
        case VALUE_TRUE:
            writeBoolean(true);
            break;
        case VALUE_FALSE:
            writeBoolean(false);
            break;
        case VALUE_NULL:
            writeNull();
            break;
        case VALUE_EMBEDDED_OBJECT:
            writeObject(jp.getEmbeddedObject());
            break;
        }
    }

    @Override
    public void copyCurrentStructure(JsonParser jp) throws IOException {
        JsonToken t = jp.getCurrentToken();

        // Let'string handle field-name separately first
        if (t == JsonToken.FIELD_NAME) {
            writeFieldName(jp.getCurrentName());
            t = jp.nextToken();
            // fall-through to copy the associated value
        }

        switch (t) {
        case START_ARRAY:
            writeStartArray();
            while (jp.nextToken() != JsonToken.END_ARRAY) {
                copyCurrentStructure(jp);
            }
            writeEndArray();
            break;
        case START_OBJECT:
            writeStartObject();
            while (jp.nextToken() != JsonToken.END_OBJECT) {
                copyCurrentStructure(jp);
            }
            writeEndObject();
            break;
        default: // others are simple:
            copyCurrentEvent(jp);
        }
    }

    @Override
    public JsonStreamContext getOutputContext() {
        return currentNode;
    }

    @Override
    public void flush() throws IOException {
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void close() throws IOException {
        closed = true;
    }

    /**
     * A node that we are currently building from
     */
    private abstract class Node extends JsonStreamContext {
        private final Node parent;
        private String name;

        private Node(Node parent, int contextType) {
            this.parent = parent;
            _type = contextType;
            _index = -1;
        }

        public Node getParent() {
            return parent;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String getCurrentName() {
            return name;
        }

        abstract void set(Object value);

        abstract Object get();
    }

    /**
     * A node that represents an object
     */
    private class ObjectNode extends Node {
        private final BasicDBObject object;

        private ObjectNode(Node parent) {
            super(parent, JsonStreamContext.TYPE_OBJECT);
            object = new BasicDBObject();
        }

        void set(Object value) {
            object.put(getCurrentName(), value);
        }

        @Override
        DBObject get() {
            return object;
        }
    }

    /**
     * A node that represents an array
     */
    private class ArrayNode extends Node {
        private final List<Object> array = new ArrayList<Object>();

        private ArrayNode(Node parent) {
            super(parent, JsonStreamContext.TYPE_ARRAY);
        }

        @Override
        void set(Object value) {
            array.add(value);
        }

        @Override
        List<Object> get() {
            return array;
        }
    }
}