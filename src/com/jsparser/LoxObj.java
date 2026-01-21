package com.jsparser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoxObj implements LoxCallable {
    private final Map<String, Object> properties;

    public LoxObj(Map<String, Object> properties) {
        this.properties = properties;
    }

    Object get(Token name) {
        if (properties.containsKey(name.lexeme)) {
            var value = properties.get(name.lexeme);
            if (value instanceof LoxFunction) {
                return ((LoxFunction) value).bind(this);
            }
            return value;
        }

        throw new RuntimeError(name,
                "Undefined property '" + name.lexeme + "'.");
    }

    @Override
    public int arity() {
        return 0;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {

        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{ ");
        boolean first = true;
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            if (!first) {
                sb.append(", ");
            }
            first = false;
            sb.append(entry.getKey()).append(": ");
            Object value = entry.getValue();
            if (value instanceof String) {
                sb.append("\"").append(value).append("\"");
            } else {
                sb.append(value);
            }
        }
        sb.append(" }");
        return sb.toString();
    }
}
