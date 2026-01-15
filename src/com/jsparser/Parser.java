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

    private Expr primary() {
        if (match(TokenType.FALSE))
            return new Expr.Literal(false);
        if (match(TokenType.TRUE))
            return new Expr.Literal(true);
        if (match(TokenType.NULL))
            return new Expr.Literal(null);

        if (match(TokenType.NUMBER, TokenType.STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(TokenType.IDENTIFIER)) {
            return new Expr.Identifier(previous());
        }
        if (match(TokenType.LEFT_PAREN)) {
            Expr expr = AssignmentExpression();
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }
        throw error(peek(), "Expect expression.");
    }

    private Expr BitwiseORExpression() {
        Expr expr = primary();
        return expr;
    }

    private Expr call() {
        Expr expr = primary();
        return expr;
    }

    private Expr unary() {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right, true);
        }

        return call();
    }

    private Expr factor() {
        var expr = unary();
        while (match(TokenType.SLASH, TokenType.STAR)) {
            var operator = previous();
            var right = unary();
            expr = new Expr.Binary(expr, operator, right);

        }
        return expr;
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
        Expr expr = comparison();

        return expr;
    }

    private Expr LogicalANDExpression() {
        var expr = equality();
        while (match(TokenType.AND)) {
            var operator = previous();
            var right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
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
        var expr = LogicalORExpression();
        if (match(TokenType.EQUAL)) {
            Token equals = previous();
            Expr value = ConditionalExpression();
            if (expr instanceof Expr.Identifier) {

                return new Expr.Assign(expr, equals, value);
            }
        }
        return expr;
    }

    private Expr AssignmentExpression() {
        var expr = ConditionalExpression();
        return expr;
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