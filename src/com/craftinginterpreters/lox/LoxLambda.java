package com.craftinginterpreters.lox;

import java.util.List;

/**
 * 匿名函数（Lambda）运行时表示
 * 
 * 与 LoxFunction 类似，但不绑定到名称
 */
class LoxLambda implements LoxCallable {
    private final Expr.Function declaration;

    LoxLambda(Expr.Function declaration) {
        this.declaration = declaration;
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(interpreter.globals);
        for (int i = 0; i < declaration.params.size(); i++) {
            environment.define(declaration.params.get(i).lexeme, arguments.get(i));
        }

        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (Return returnValue) {
            return returnValue.value;
        }

        return null;
    }

    @Override
    public String toString() {
        return "<fn lambda>";
    }
}
