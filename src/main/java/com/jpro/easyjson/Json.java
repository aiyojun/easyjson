package com.jpro.easyjson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * EasyJson API: Make jackson using more easily!
 * Including YAML module, you can convert string
 * to Map<String, Object> by using YAML::load() method.
 * Then, using json to access!
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
     * Jackson support!
     */
    public Json(JsonNode node) { root = node; }

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

    public Array toArray() {
        final JsonNode pr = root;
        return new Array() {{root = pr;}};
    }

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
                    } else if (vv instanceof Map) {
                        here.put(k, Json.array(((List<?>) v).stream().map(every -> parse((Map<String, Object>) every)).collect(Collectors.toList())));
                    } else {
                        throw new RuntimeException("Error json array! Key : " + k);
                    }
                }
            } else if (v instanceof Map) {
                here.put(k, parse((Map<String, Object>) v));
            } else {
                throw new RuntimeException("Error json type! Value : " + v);
            }
        }
        return here;
    }

    public Json merge(Json other) {
        Json _r = new Json();
        Iterator<String> keys = root.fieldNames();
        while (keys.hasNext()) {
            String key = keys.next();
            _r.put(key, root.get(key));
        }
        keys = other.root.fieldNames();
        while (keys.hasNext()) {
            String key = keys.next();
            _r.put(key, other.root.get(key));
        }
        return _r;
    }

    public Json update(Json other) {
        Iterator<String> keys;
        keys = other.root.fieldNames();
        while (keys.hasNext()) {
            String key = keys.next();
            put(key, other.root.get(key));
        }
        return this;
    }

    public boolean has(String... keys) {
        return get(keys).root != null;
    }

    /**
     * Value accessing by the passing keys!
     */
    public Json get(String... keys) {
        JsonNode last = root;
        for (int i = 0; i < keys.length - 1; i++) {
            if (!last.has(keys[i])) return new Json() {{root = null;}};
            if (last.get(keys[i]).isObject())
                last = last.get(keys[i]);
            else
                throw new RuntimeException("Json access error!");
        }
        final JsonNode innerLast = last;
        return new Json() {{root = innerLast.get(keys[keys.length - 1]);}};
    }

    public <T> T getAs(String... keys) {
        JsonNode last = root;
        for (int i = 0; i < keys.length - 1; i++) {
            if (!last.has(keys[i])) return null;
            if (last.get(keys[i]).isObject())
                last = last.get(keys[i]);
            else
                throw new RuntimeException("Json access error!");
        }
        final JsonNode innerLast = last;
        JsonNode v = innerLast.get(keys[keys.length - 1]);
        T _r;
        try {
            _r = (T) extract(v);
        } catch (Exception e) {
            throw new RuntimeException("Type error!");
        }
        return _r;
    }

    public String getAsText(String... keys) {
        Json ref = get(keys);
        if (ref == null) throw new RuntimeException("Null value.");
        if (ref.root.isTextual()) return ref.root.asText();
        throw new RuntimeException("Type error! Not string.");
    }

    /**
     * JSON modify!
     */
    public <E> Json put(String k, E v) {
        if (v == null) {
            ((ObjectNode) root).putNull(k);
        } else if (v instanceof Json) {
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
        } else if (v instanceof JsonNode) {
            ((ObjectNode) root).put(k, (JsonNode) v);
        } else {
            throw new RuntimeException("Unknown type");
        }
        return this;
    }

    public List<String> keys() {
        List<String> _r = new ArrayList<>(root.size());
        Iterator<String> keys = root.fieldNames();
        while (keys.hasNext()) {
            _r.add(keys.next());
        }
        return _r;
    }

    public Map<String, Object> asMap() {
        List<String> ks = keys();
        Map<String, Object> inner = new HashMap<>();
        for (String k : ks) {
            if (root.get(k).isObject()) {
                inner.put(k, new Json(root.get(k)).asMap());
            } else if (root.get(k).isArray()) {
                inner.put(k, new Json(root.get(k)).toArray().asList());
            } else if (root.get(k).isTextual()) {
                inner.put(k, root.get(k).asText());
            } else if (root.get(k).isBoolean()) {
                inner.put(k, root.get(k).asBoolean());
            } else if (root.get(k).isLong()) {
                inner.put(k, root.get(k).asLong());
            } else if (root.get(k).isInt()) {
                inner.put(k, root.get(k).asInt());
            } else if (root.get(k).isDouble()) {
                inner.put(k, root.get(k).asDouble());
            } else if (root.get(k).isFloat()) {
                inner.put(k, root.get(k).asDouble());
            } else if (root.get(k).isNull()) {
                inner.put(k, null);
            } else {
                throw new RuntimeException("Unknown key(" + k + ") type : " + root.get(k).getNodeType().toString());
            }
        }
        return inner;
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

    public JsonNode getRoot() { return root; }

    /**
     * Inner using!
     */
    private Object extract(JsonNode node) {
        if (node == null) return null;
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

    public boolean equal(Json json, StringBuilder k) {
        k.delete(0, k.length());
        return equal(this, json, k);
    }

    public static boolean equal(Json json, Json template, StringBuilder k) {
        JsonNode comp = template.root;
        JsonNode roof = json.root;
        if (comp.isArray()) {
            if (roof.isArray()) {
                if (comp.toString().equals(roof.toString())) {
                    return true;
                } else {
                    k.append("[A] Top value not equal");
                    return false;
                }
            } else {
                k.append("[O] Top different type");
                return false;
            }
        }
        Iterator<String> keys = comp.fieldNames();
        while (keys.hasNext()) {
            String key = keys.next();
            if (!roof.has(key)) {k.append("No ").append(key);return false;}
            if (comp.get(key).isObject()) {
                if (roof.get(key).isObject())
                    return equal(new Json() {{root = roof.get(key);}}, new Json() {{root = comp.get(key);}}, k);
                else {
                    k.append("[O] Different key[").append(key).append("] value type");
                    return false;
                }
            }

            if (comp.get(key).isArray()) {
                if (roof.get(key).isArray()) {
                    if (comp.get(key).toString().equals(roof.get(key).toString())) {
                        System.out.println(comp.get(key).toString());
                        System.out.println(roof.get(key).toString());
                        continue;
                    } else {
                        k.append("[A] Key[").append(key).append("] value not equal.");
                        return false;
                    }
                } else {
                    k.append("[A] Different key[").append(key).append("] value type");
                    return false;
                }
            }
            if (comp.get(key).isTextual() ) {if (roof.get(key).isTextual() ) {if (comp.get(key).asText().equals(roof.get(key).asText()))     continue; else {k.append("[S] Key[").append(key).append("] value not equal.");return false;}} else {k.append("[S] Different key[").append(key).append("] value type"); return false;}}
            if (comp.get(key).isBoolean() ) {if (roof.get(key).isBoolean() ) {if (comp.get(key).asBoolean() == roof.get(key).asBoolean())    continue; else {k.append("[B] Key[").append(key).append("] value not equal.");return false;}} else {k.append("[B] Different key[").append(key).append("] value type"); return false;}}
            if (comp.get(key).isInt()     ) {if (roof.get(key).isInt()     ) {if (comp.get(key).asInt() == roof.get(key).asInt())            continue; else {k.append("[I] Key[").append(key).append("] value not equal.");return false;}} else {k.append("[I] Different key[").append(key).append("] value type"); return false;}}
            if (comp.get(key).isLong()    ) {if (roof.get(key).isLong()    ) {if (comp.get(key).isLong() == roof.get(key).isLong())          continue; else {k.append("[L] Key[").append(key).append("] value not equal.");return false;}} else {k.append("[L] Different key[").append(key).append("] value type"); return false;}}
            if (comp.get(key).isDouble()  ) {if (roof.get(key).isDouble()  ) {if (comp.get(key).asDouble() == roof.get(key).asDouble())      continue; else {k.append("[D] Key[").append(key).append("] value not equal.");return false;}} else {k.append("[D] Different key[").append(key).append("] value type"); return false;}}
            if (comp.get(key).isNull()    && roof.get(key).isNull()   ) continue;
            throw new RuntimeException("Unknown type." + comp.get(key));
        }
        return true;
    }

    public boolean check(Json template, StringBuilder k) {
        return check(this, template, k);
    }

    public static boolean check(Json json, Json template, StringBuilder k) {
        JsonNode comp = template.root;
        JsonNode roof = json.root;
        Iterator<String> keys = comp.fieldNames();
        while (keys.hasNext()) {
            String key = keys.next();
//            System.out.println("json key : " + key);
            if (!roof.has(key)) {k.append("No ").append(key);return false;}
            if (comp.get(key).isObject()) {
                if (roof.get(key).isObject()) {
                    if (!check(new Json() {{root = roof.get(key);}}, new Json() {{root = comp.get(key);}}, k))
                        return false;
                } else {
                    k.append("Error Type of ").append(key);
                    return false;
                }
            }
//            if (comp.get(key).isObject()
//                && !check(new Json() {{root = roof.get(key);}}, new Json() {{root = comp.get(key);}}, k))
//                return false;
            if (comp.get(key).isArray()   && !roof.get(key).isArray()  ) {k.append("Error Type of ").append(key);return false;}
            if (comp.get(key).isTextual() && !roof.get(key).isTextual()) {k.append("Error Type of ").append(key);return false;}
            if (comp.get(key).isBoolean() && !roof.get(key).isBoolean()) {k.append("Error Type of ").append(key);return false;}
            if (comp.get(key).isNumber()  && !roof.get(key).isNumber() ) {k.append("Error Type of ").append(key);return false;}
            if (comp.get(key).isNull()    && !roof.get(key).isNull()   ) {k.append("Error Type of ").append(key);return false;}
        }
        return true;
    }
}
