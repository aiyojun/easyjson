package com.jpro.easyjson;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * EasyJson API: Make jackson using more easily!
 * Including YAML module, you can convert string
 * to Map<String, Object> by using YAML::load() method.
 * Than, using json to access!
 */
public class Json {
    /**
     * Core member!
     * We use jackson parse the JSON object data.
     * Therefore, It's important to maintain a JsonNode member!
     */
    protected JsonNode root;

    /**
     * Prepared for JSON string parsing!
     * If you want to parse JSON string concurrently,
     * you should add a mutex for the member accessing!
     */
    private static ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Construct an empty JSON object!
     */
    public Json() { root = JsonNodeFactory.instance.objectNode(); }

    /**
     * Construct an initialized JSON object!
     */
    public <E> Json(String k, E v) { this(); put(k, v); }

    /**
     * JSON array constructing interface!
     * Here, construct an empty JSON array!
     */
    public static Array array() { return new Array(); }

    /**
     * Construct an initialized JSON array,
     * using the specified List or other collection!
     */
    public static <E> Array array(Collection<E> collection) { return new Array(collection); }

    /**
     * If the Json is basic type, you can obtain the raw value!
     */
    public Object value() { return extract(root); }

    /**
     * Parse JSON string and construct the corresponding JSON object!
     */
    public static Json parse(String json) {
        try {
            JsonNode jsonNode = objectMapper.readTree(json);
            return new Json() {{root = jsonNode;}};
        } catch (IOException e) {
            throw new RuntimeException("Parse json error!");
        }
    }

    /**
     * Parse Map<String, Object> structure!
     * Some tools like YAML, will produce this kind of structure data!
     */
    public static Json parse(Map<String, Object> root) {
        Json here = new Json();
        for (Map.Entry<String, Object> entry : root.entrySet()) {
            String k = entry.getKey();
            Object v = entry.getValue();
            if (v instanceof String
                    || v instanceof Integer
                    || v instanceof Long
                    || v instanceof Double
                    || v instanceof Boolean
            ) {
                here.put(k, v);
            } else if (v instanceof List) {
                if (((List<Object>) v).isEmpty()) {
                    here.put(k, Json.array());
                } else {
                    Object vv = ((List<Object>) v).get(0);
                    if (vv instanceof String) {
                        here.put(k, Json.array((List<String>) v));
                    } else if (vv instanceof Integer) {
                        here.put(k, Json.array((List<Integer>) v));
                    } else if (vv instanceof Long) {
                        here.put(k, Json.array((List<Long>) v));
                    } else if (vv instanceof Double) {
                        here.put(k, Json.array((List<Double>) v));
                    } else if (vv instanceof Boolean) {
                        here.put(k, Json.array((List<Boolean>) v));
                    } else {
                        throw new RuntimeException("Error json array!");
                    }
                }
            } else if (v instanceof Map) {
                here.put(k, parse((Map<String, Object>) v));
            } else {
                throw new RuntimeException("Error json type!");
            }
        }
        return here;
    }

    /**
     * Value accessing by the passing keys!
     */
    public Json get(String... keys) {
        JsonNode last = root;
        for (int i = 0; i < keys.length - 1; i++) {
            if (!last.has(keys[i])) return null;
            if (last.get(keys[i]).isObject())
                last = last.get(keys[i]);
            else
                throw new RuntimeException("Json access error!");
        }
        final JsonNode innerLast = last;
        return new Json() {{root = innerLast.get(keys[keys.length - 1]);}};
    }

    /**
     * JSON modify!
     */
    public <E> Json put(String k, E v) {
        if (v == null) {
            /* Maybe 'com.fasterxml.jackson.databind.node.ObjectNode'
               is incompatible with 'org.codehaus.jackson.node.ObjectNode'; */
//            ((ObjectNode) root).set(k, null);
            ((ObjectNode) root).put(k, (JsonNode) null);
        } else if (v instanceof Json) {
//            ((ObjectNode) root).set(k, ((Json) v).root);
            ((ObjectNode) root).put(k, ((Json) v).root);
        } else if (v instanceof String) {
            ((ObjectNode) root).put(k, (String) v);
        } else if (v instanceof Integer) {
            ((ObjectNode) root).put(k, (Integer) v);
        } else if (v instanceof Double) {
            ((ObjectNode) root).put(k, (Double) v);
        } else if (v instanceof Long) {
            ((ObjectNode) root).put(k, (Long) v);
        } else if (v instanceof Boolean) {
            ((ObjectNode) root).put(k, (Boolean) v);
        } else {
            throw new RuntimeException("Unknown type");
        }
        return this;
    }

    /**
     * To JSON string!
     */
    public String dumps() { return root.toString(); }

    /**
     * Same as Json::dumps() method!
     */
    @Override
    public String toString() { return root.toString(); }

    /**
     * Inner using!
     */
    private Object extract(JsonNode node) {
        if (node.isObject()) {
            return new Json() {{root = node;}};
        } else if (node.isTextual()) {
            return node.asText();
        } else if (node.isDouble()) {
            return node.asDouble();
        } else if (node.isInt()) {
            return node.asInt();
        } else if (node.isLong()) {
            return node.asLong();
        } else if (node.isBoolean()) {
            return node.asBoolean();
        } else {
            throw new RuntimeException("Unknown type");
        }
    }
}
