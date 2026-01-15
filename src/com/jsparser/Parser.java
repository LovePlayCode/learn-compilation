package com.jsparser;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    private static class ParseError extends RuntimeException {
    }

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private boolean isAtEnd() {
        return peek().getType() == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private boolean check(TokenType type) {
        if (isAtEnd())
            return false;
        return peek().getType() == type;
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private Token advance() {
        if (!isAtEnd())
            current++;
        return previous();
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private Expr BitwiseORExpression() {
        return null;
    }

    private Expr unary() {
        return null;
    }

    private Expr factor() {
        var expr = unary();
        while (match(TokenType.SLASH, TokenType.STAR)) {
            var operator = previous();
            var right = unary();
            expr = new Expr.Binary(expr, operator, right);

        }
        return null;
    }

    private Expr term() {
        var expr = factor();
        while (match(TokenType.MINUS, TokenType.PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr comparison() {
        var expr = term();
        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr equality() {

        return null;
    }

    private Expr LogicalANDExpression() {
        var expr = equality();
        while (match(TokenType.AND)) {
            var operator = previous();
            var right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }
        return null;
    }

    private Expr LogicalORExpression() {
        var expr = LogicalANDExpression();
        while (match(TokenType.OR)) {
            var operator = previous();
            var right = LogicalANDExpression();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }

    private Expr ConditionalExpression() {
        LogicalORExpression();
        return null;
    }

    private Expr AssignmentExpression() {
        ConditionalExpression();
        return null;
    }

    public VarDeclarator VariableDeclaration() {
        var name = consume(TokenType.IDENTIFIER, "不是变量声明");
        Expr initializer = null;
        if (match(TokenType.EQUAL)) {
            initializer = AssignmentExpression();
        }
        return new VarDeclarator(name, initializer);
    }

    public Stmt VariableDeclarationList() {
        var declarations = new ArrayList<VarDeclarator>();

        declarations.add(VariableDeclaration());
        while (match(TokenType.COMMA)) {

            declarations.add(VariableDeclaration());
        }
        return new Stmt.Var(declarations);
    }

    public Stmt VariableStatement() {
        Stmt stmt = VariableDeclarationList();
        System.out.println("stmt::" + stmt);
        consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.");
        return stmt;
    }

    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    private Token consume(TokenType type, String message) {
        if (check(type))
            return advance();

        throw error(peek(), message);
    }

    public Stmt declaration() {
        if (match(TokenType.VAR)) {
            return VariableStatement();
        }
        return null;
    }

    public List<Stmt> parse() {
        var statements = new ArrayList<Stmt>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }
        return statements;
    }
}