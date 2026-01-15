package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class LoxClass implements LoxCallable {
    final String name;
    final List<LoxClass> superclasses;  // 支持多继承

    private final Map<String, LoxFunction> methods;

    LoxFunction findMethod(String name) {
        // 先在自己的方法中查找
        if (methods.containsKey(name)) {
            return methods.get(name);
        }

        // 按顺序在所有父类中查找（先声明的父类优先）
        for (LoxClass superclass : superclasses) {
            LoxFunction method = superclass.findMethod(name);
            if (method != null) {
                return method;
            }
        }

        return null;
    }

    LoxClass(String name, List<LoxClass> superclasses, Map<String, LoxFunction> methods) {
        this.name = name;
        this.methods = methods;
        this.superclasses = superclasses != null ? superclasses : new ArrayList<>();
    }

    // 兼容旧代码
    LoxClass(String name, LoxClass superclass, Map<String, LoxFunction> methods) {
        this.name = name;
        this.methods = methods;
        this.superclasses = new ArrayList<>();
        if (superclass != null) {
            this.superclasses.add(superclass);
        }
    }

    /**
     * 在所有父类中查找方法（用于 super 调用）
     * 与 findMethod 不同，这个方法只在父类中查找，不查找自身
     */
    LoxFunction findMethodInSuperclasses(String name) {
        for (LoxClass superclass : superclasses) {
            LoxFunction method = superclass.findMethod(name);
            if (method != null) {
                return method;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int arity() {
        LoxFunction initializer = findMethod("init");
        if (initializer == null)
            return 0;
        return initializer.arity();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        LoxInstance instance = new LoxInstance(this);
        LoxFunction initializer = findMethod("init");
        if (initializer != null) {
            initializer.bind(instance).call(interpreter, arguments);
        }
        return instance;
    }
}