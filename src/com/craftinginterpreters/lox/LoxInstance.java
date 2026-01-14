package com.craftinginterpreters.lox;

import java.util.HashMap;
import java.util.Map;

class LoxInstance {
    private LoxClass klass;
    private final Map<String, Object> fields = new HashMap<>();

    LoxInstance(LoxClass klass) {
        this.klass = klass;
    }

    void set(Token name, Object value) {
        fields.put(name.lexeme, value);
    }

    /**
     * 字段和属性的区别
     * 当访问一个属性时，可能会得到一个字段(存储在实例上的状态值)，或者得到一个实例类中定义的方法
     * 
     * @param name
     * @return
     */
    Object get(Token name) {
        if (fields.containsKey(name.lexeme)) {
            return fields.get(name.lexeme);
        }
        // 新增部分开始
        LoxFunction method = klass.findMethod(name.lexeme);
        // 将方法体绑定隐含作用域的 this
        if (method != null)
            return method.bind(this);
        throw new RuntimeError(name,
                "Undefined property '" + name.lexeme + "'.");
    }

    @Override
    public String toString() {
        return klass.name + " instance";
    }
}