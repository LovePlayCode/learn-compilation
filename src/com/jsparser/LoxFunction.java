package com.jsparser;

import java.util.List;

/**
 * 可以用Stmt.Function表示的函数,但是不希望解释器的运行时阶段渗入到前端语法类中。
 * 所以创建一个新类
 */
class LoxFunction implements LoxCallable {
    private final Stmt.Function declaration;

    private final Environment closure;

    LoxFunction(Stmt.Function declaration, Environment closure) {
        this.declaration = declaration;
        this.closure = closure;
    }

    @Override
    public int arity() {

        return declaration.params.size();
    }

    @Override
    public Object call(Interpreter interpreter,
            List<Object> arguments) {
        Environment environment = new Environment(closure);
        for (int i = 0; i < declaration.params.size(); i++) {
            environment.define(declaration.params.get(i).lexeme,
                    arguments.get(i));
        }

        try {
            interpreter.executeBlock(declaration.body.getStatements(), environment);
        } catch (Return returnValue) {
            return returnValue.value;
        }
        return null;
    }

    @Override
    public String toString() {
        return "<fn " + declaration.name.lexeme + ">";
    }
}
