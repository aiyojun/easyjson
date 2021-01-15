package com.jpro.easyjson;

import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;

import java.util.Collection;

/**
 * JsonArray type, prepared for Json!
 * You shouldn't create JsonArray by this class.
 * Create JsonArray by Json::array() method, instead!
 */
public class Array extends Json {

    /**
     * Construct an empty JsonArray!
     */
    public Array () {
        root = JsonNodeFactory.instance.arrayNode();
    }

    /**
     * Construct an initialized JsonArray by the specific collection!
     */
    public <E> Array(Collection<E> collection) { this(); collection.forEach(this::add); }

    /**
     * Adding element of JsonArray.
     */
    public <E> Array add(E v) {
        if (v instanceof Json) {
            ((ArrayNode) root).add(((Json) v).root);
        } else if (v instanceof String) {
            ((ArrayNode) root).add((String) v);
        } else if (v instanceof Integer) {
            ((ArrayNode) root).add((Integer) v);
        } else if (v instanceof Long) {
            ((ArrayNode) root).add((Long) v);
        } else if (v instanceof Boolean) {
            ((ArrayNode) root).add((Boolean) v);
        } else {
            throw new RuntimeException("Unknown type");
        }
        return this;
    }

    /**
     * To JSON string of JsonArray!
     */
    public String dumps() { return root.toString(); }

    /**
     * Same as Array::dumps() method!
     */
    @Override
    public String toString() { return dumps(); }
}
