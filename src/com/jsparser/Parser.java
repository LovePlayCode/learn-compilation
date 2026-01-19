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

    private Expr CallExpression() {
        return null;
    }

    private Expr Expression() {
        return null;
    }

    private List<Expr> Arguments() {
        consume(TokenType.LEFT_PAREN, "Expect '(' for arguments.");
        List<Expr> args = new ArrayList<>();

        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                args.add(AssignmentExpression());
            } while (match(TokenType.COMMA));
        }

        consume(TokenType.RIGHT_PAREN, "Expect ')' after arguments.");
        return args;
    }

    private Expr MemberExpression() {
        Expr expr;

        if (match(TokenType.NEW)) {
            Token keyword = previous();
            Expr callee = MemberExpression(); // 递归
            List<Expr> args = new ArrayList<>();
            if (check(TokenType.LEFT_PAREN)) {
                args = Arguments();
            }
            expr = new Expr.New(keyword, callee, args);
        } else {
            expr = primary();
        }

        // 解析 '[' Expression ']' 或 '.' IDENTIFIER 链
        while (true) {
            if (match(TokenType.DOT)) {
                Token name = consume(TokenType.IDENTIFIER, "Expect property name after '.'.");
                expr = new Expr.Member(expr, name, false);
            } else if (match(TokenType.LEFT_BRACKET)) {
                // computed 访问需要特殊处理
                Expr property = Expression();
                consume(TokenType.RIGHT_BRACKET, "Expect ']' after index.");
                // 需要扩展 Member 类或使用其他方式处理
                expr = new Expr.Member(expr, property, false);
            } else {
                break;
            }
        }

        return expr;
    }

    private Expr LeftHandSideExpression() {
        // 先解析 MemberExpression（含 new 处理）
        var expr = MemberExpression();

        // 如果后面跟 '('，则进入 CallExpression 处理
        if (check(TokenType.LEFT_PAREN)) {
            expr = CallExpression();
        }
        return expr;
    }

    private Expr PostfixExpression() {
        var expr = LeftHandSideExpression();
        if (match(TokenType.PLUS_PLUS, TokenType.MINUS_MINUS)) {
            var operator = previous();
            expr = new Expr.Unary(operator, expr, false);
        }
        return expr;
    }

    private Expr UnaryExpression() {
        // 先检查前缀运算符
        if (match(TokenType.BANG, TokenType.PLUS_PLUS, TokenType.MINUS_MINUS, TokenType.MINUS, TokenType.PLUS,
                TokenType.TYPEOF)) {
            Token operator = previous();
            Expr right = UnaryExpression();
            return new Expr.Unary(operator, right, true);
        }
        return PostfixExpression();
    }

    private Expr MultiplicativeExpression() {
        var expr = UnaryExpression();
        while (match(TokenType.STAR, TokenType.SLASH, TokenType.PERCENT)) {
            var operator = previous();
            var right = UnaryExpression();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr AdditiveExpression() {
        var expr = MultiplicativeExpression();
        while (match(TokenType.PLUS, TokenType.MINUS)) {
            var operator = previous();
            var right = MultiplicativeExpression();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr RelationalExpression() {
        var expr = AdditiveExpression();
        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            var operator = previous();
            var right = AdditiveExpression();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr EqualityExpression() {
        var expr = RelationalExpression();
        while (match(TokenType.EQUAL_EQUAL, TokenType.EQUAL_EQUAL_EQUAL, TokenType.BANG_EQUAL,
                TokenType.BANG_EQUAL_EQUAL)) {
            var oper = previous();
            var right = RelationalExpression();
            expr = new Expr.Binary(expr, oper, right);
        }
        return expr;
    }

    private Expr LogicalANDExpression() {
        var expr = EqualityExpression();
        while (match(TokenType.AND_AND)) {
            var operator = previous();
            var right = EqualityExpression();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }

    private Expr LogicalORExpression() {
        var expr = LogicalANDExpression();
        while (match(TokenType.OR_OR)) {
            var operator = previous();
            var right = LogicalANDExpression();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }

    private Expr ConditionalExpression() {
        var expr = LogicalORExpression();
        if (match(TokenType.QUESTION)) {
            // 三目运算符
            Token question = previous();
            Expr value = AssignmentExpression();
            if (match(TokenType.COLON)) {
                Expr elseValue = AssignmentExpression();
                expr = new Expr.Conditional(expr, value, elseValue);
            }
            throw error(question, "Expect ':' after '?'.");
        }
        return expr;
    }

    private Expr AssignmentExpression() {
        // 先解析左侧（可能是 ConditionalExpression 或 LeftHandSideExpression）
        var expr = ConditionalExpression();

        // 检查是否为赋值运算符
        if (match(TokenType.EQUAL, TokenType.PLUS_EQUAL, TokenType.MINUS_EQUAL,
                TokenType.STAR_EQUAL, TokenType.SLASH_EQUAL, TokenType.PERCENT_EQUAL)) {
            Token operator = previous();
            // 右结合：递归调用 AssignmentExpression
            Expr value = AssignmentExpression();

            // 验证左侧是有效的赋值目标（LeftHandSideExpression）
            if (expr instanceof Expr.Identifier || expr instanceof Expr.Member) {
                return new Expr.Assign(expr, operator, value);
            }
            throw error(operator, "Invalid assignment target.");
        }

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

    private Stmt Statement() {
        if (match(TokenType.VAR)) {
            return VariableStatement();
        }
        return null;
    }

    private Stmt SourceElements() {
        return Statement();
    }

    public List<Stmt> parse() {
        var statements = new ArrayList<Stmt>();
        while (!isAtEnd()) {
            statements.add(SourceElements());
        }
        return statements;
    }
}