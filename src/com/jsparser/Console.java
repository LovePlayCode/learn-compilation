package com.jsparser;

import java.util.List;

/**
 * JavaScript console 对象的 Java 实现
 */
public class Console {
    private final LoxCallable log;

    public Console() {
        this.log = new LoxCallable() {
            @Override
            public int arity() {
                return -1; // 可变参数
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < arguments.size(); i++) {
                    if (i > 0)
                        sb.append(" ");
                    sb.append(stringify(arguments.get(i)));
                }
                System.out.println(sb.toString());
                return null; // console.log 返回 undefined
            }

            @Override
            public String toString() {
                return "function log() { [native code] }";
            }
        };
    }

    /**
     * 获取 log 方法
     */
    public LoxCallable getLog() {
        return log;
    }

    /**
     * 将值转换为字符串表示
     */
    private String stringify(Object value) {
        if (value == null)
            return "undefined";
        if (value instanceof Double) {
            String text = value.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        if (value instanceof Boolean) {
            return value.toString();
        }
        if (value instanceof String) {
            return (String) value;
        }
        if (value instanceof LoxCallable) {
            return value.toString();
        }
        return value.toString();
    }
}
