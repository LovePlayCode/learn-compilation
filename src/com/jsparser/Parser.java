package com.jsparser;

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.Arrays;
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

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON)
                return;

            switch (peek().type) {

                case TokenType.FUNCTION:
                case TokenType.VAR:
                case TokenType.FOR:
                case TokenType.IF:
                case TokenType.WHILE:
                case TokenType.RETURN:
                    return;
            }

            advance();
        }
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

    private Expr FunctionExpression() {
        // 函数名是可选的（匿名函数 vs 命名函数表达式）
        Token nameToken = null;
        if (match(TokenType.IDENTIFIER)) {
            nameToken = previous();
        }

        consume(TokenType.LEFT_PAREN, "Expect '(' after function name.");

        // 解析参数列表
        List<Token> params = new ArrayList<>();
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                params.add(consume(TokenType.IDENTIFIER, "Expect parameter name."));
            } while (match(TokenType.COMMA));
        }
        consume(TokenType.RIGHT_PAREN, "Expect ')' after parameters.");

        // 函数体必须是 Block
        consume(TokenType.LEFT_BRACE, "Expect '{' before function body.");
        Stmt.Block body = (Stmt.Block) Block();

        // Expr.Function 期望 List<Stmt>，从 Block 中提取
        return new Expr.Function(nameToken, params, body.getStatements());
    }

    private Expr PrimaryExpression() {
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
        if (match(TokenType.LEFT_BRACE)) {
            List<Property> properties = new ArrayList<>();
            if (!check(TokenType.RIGHT_BRACE)) {
                do {
                    if (match(TokenType.IDENTIFIER, TokenType.STRING, TokenType.NUMBER)) {
                        Token nameToken = previous();
                        // 将 Token 转换为 Expr（Identifier 或 Literal）
                        Expr key = (nameToken.type == TokenType.IDENTIFIER)
                                ? new Expr.Identifier(nameToken)
                                : new Expr.Literal(nameToken.literal);
                        consume(TokenType.COLON, "Expect ':' after property name.");
                        Expr value = AssignmentExpression();
                        properties.add(
                                new Property(key, value, Property.Kind.INIT));
                    }
                } while (match(TokenType.COMMA) && !isAtEnd() && !check(TokenType.RIGHT_BRACE));
            }
            consume(TokenType.RIGHT_BRACE, "Expect '}' after object literal.");
            return new Expr.ObjectLiteral(properties);
        }
        if (match(TokenType.FUNCTION)) {
            return FunctionExpression();
        }
        if (match(TokenType.THIS)) {
            return new Expr.This(previous());
        }
        throw error(peek(), "Expect expression.");
    }

    private Expr BitwiseORExpression() {
        Expr expr = PrimaryExpression();
        return expr;
    }

    private Expr call() {
        Expr expr = PrimaryExpression();
        return expr;
    }

    private Expr UnaryExpression() {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            Token operator = previous();
            Expr right = UnaryExpression();
            return new Expr.Unary(operator, right, true);
        }

        return call();
    }

    private Expr factor() {
        var expr = UnaryExpression();
        while (match(TokenType.SLASH, TokenType.STAR)) {
            var operator = previous();
            var right = UnaryExpression();
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

    // 接收 callee 参数，而不是重新解析
    private Expr CallExpression(Expr callee) {
        List<Expr> args = Arguments();
        Token paren = previous();
        Expr expr = new Expr.Call(callee, paren, args);

        while (true) {
            if (check(TokenType.LEFT_PAREN)) {
                args = Arguments();
                paren = previous();
                expr = new Expr.Call(expr, paren, args);
            } else if (match(TokenType.DOT)) {
                Token name = consume(TokenType.IDENTIFIER, "Expect property name after '.'.");
                expr = new Expr.Member(expr, name, false);
            } else if (match(TokenType.LEFT_BRACKET)) {
                Expr index = Expression();
                consume(TokenType.RIGHT_BRACKET, "Expect ']' after index.");
                expr = new Expr.Member(expr, index, true);
            } else {
                break;
            }
        }
        return expr;
    }

    private Expr Expression() {
        Expr expr = AssignmentExpression();
        while (match(TokenType.COMMA)) {
            Token operator = previous();
            Expr right = AssignmentExpression();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
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
            expr = PrimaryExpression();
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
            expr = CallExpression(expr);
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

    private Expr UnaryExpressionExpression() {
        // 先检查前缀运算符
        if (match(TokenType.BANG, TokenType.PLUS_PLUS, TokenType.MINUS_MINUS, TokenType.MINUS, TokenType.PLUS,
                TokenType.TYPEOF)) {
            Token operator = previous();
            Expr right = UnaryExpressionExpression();
            return new Expr.Unary(operator, right, true);
        }
        return PostfixExpression();
    }

    private Expr MultiplicativeExpression() {
        var expr = UnaryExpressionExpression();
        while (match(TokenType.STAR, TokenType.SLASH, TokenType.PERCENT)) {
            var operator = previous();
            var right = UnaryExpressionExpression();
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

    private Stmt ExpressionStatement() {
        var expr = Expression();
        consume(TokenType.SEMICOLON, "语句必须以分号结尾");
        return new Stmt.Expression(expr);
    }

    private Stmt IfStatement() {
        consume(TokenType.LEFT_PAREN, "期望一个(在 if 后面.");
        var condition = Expression();
        consume(TokenType.RIGHT_PAREN, "期望一个)在 if 后面.");
        var thenBranch = Statement();
        Stmt elseBranch = null;
        if (match(TokenType.ELSE)) {
            elseBranch = Statement();
        }
        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt WhileStatement() {
        consume(TokenType.LEFT_PAREN, "期望一个(在 while 后面.");
        var condition = Expression();
        consume(TokenType.RIGHT_PAREN, "期望一个)在 while 后面.");
        var body = Statement();
        return new Stmt.While(condition, body);
    }

    private List<Stmt> StatementList() {
        List<Stmt> list = new ArrayList<Stmt>();
        while (!isAtEnd() && !check(TokenType.RIGHT_BRACE)) {
            list.add(Statement());
        }
        return list;
    }

    private Stmt Block() {
        var stmtList = StatementList();
        consume(TokenType.RIGHT_BRACE, "期望一个}在 block 后面.");
        return new Stmt.Block(stmtList);
    }

    private Stmt ForInit() {

        if (match(TokenType.VAR)) {
            return VariableDeclarationList();
        }
        return new Stmt.Expression(Expression());
    }

    /**
     * 将 for 循环语句转换为 while 循环语句
     * 
     * @return
     */
    private Stmt ForStatement() {
        consume(TokenType.LEFT_PAREN, "期望一个(在 for 后面.");
        Stmt initializer;
        if (match(TokenType.SEMICOLON)) {
            initializer = null;
        } else {
            initializer = ForInit();
        }

        consume(TokenType.SEMICOLON, "期望一个;在 for 后面.");

        Expr condition = null;
        if (!check(TokenType.SEMICOLON)) {
            condition = Expression();
        }
        consume(TokenType.SEMICOLON, "期望一个;在 for 后面.");
        Expr update = null;
        if (!check(TokenType.RIGHT_PAREN)) {
            update = Expression();
        }
        consume(TokenType.RIGHT_PAREN, "期望一个)在 for 后面.");
        Stmt body = Statement();
        if (update != null) {
            body = new Stmt.Block(Arrays.asList(body, new Stmt.Expression(update)));
        }
        if (condition == null) {
            condition = new Expr.Literal(true);
        }
        body = new Stmt.While(condition, body);
        if (initializer != null) {
            body = new Stmt.Block(Arrays.asList(initializer, body));
        }
        return body;
    }

    private Stmt ReturnStatement() {
        Token keyword = previous();
        Expr value = null;
        if (!check(TokenType.SEMICOLON)) {
            value = Expression();
        }
        consume(TokenType.SEMICOLON, "期望一个;在 return 后面.");
        return new Stmt.Return(keyword, value);
    }

    private Stmt Statement() {
        if (match(TokenType.VAR)) {
            return VariableStatement();
        }
        if (match(TokenType.SEMICOLON)) {
            return new Stmt.Empty(previous());
        }
        if (match(TokenType.IF)) {
            return IfStatement();
        }
        if (match(TokenType.WHILE)) {
            return WhileStatement();
        }
        if (match(TokenType.FOR)) {
            return ForStatement();
        }
        if (match(TokenType.LEFT_PAREN)) {
            return Block();
        }
        if (match(TokenType.RETURN)) {
            return ReturnStatement();
        }
        return ExpressionStatement();
    }

    private List<Token> FormalParameterList() {
        List<Token> params = new ArrayList<>();
        params.add(consume(TokenType.IDENTIFIER, "期望一个标识符在函数声明后面."));
        while (match(TokenType.COMMA)) {
            params.add(consume(TokenType.IDENTIFIER, "期望一个标识符在函数声明后面."));
        }

        return params;
    }

    private Stmt FunctionDeclaration() {
        var name = consume(TokenType.IDENTIFIER, "期望一个标识符在函数声明后面.");
        consume(TokenType.LEFT_PAREN, "期望一个(在函数声明后面.");
        List<Token> params = new ArrayList<>();
        if (match(TokenType.IDENTIFIER)) {
            params = FormalParameterList();
        }
        consume(TokenType.RIGHT_PAREN, "期望一个)在函数声明后面.");
        Stmt.Block body = null;
        if (match(TokenType.LEFT_BRACE)) {
            body = (Stmt.Block) Block();
        } else {
            body = new Stmt.Block(null);
        }

        return new Stmt.Function(name, params, body);
    }

    private Stmt SourceElements() {
        if (match(TokenType.FUNCTION)) {
            return FunctionDeclaration();
        }
        return Statement();
    }

    private List<Stmt> Program() {
        var statements = new ArrayList<Stmt>();
        while (!isAtEnd()) {
            statements.add(SourceElements());
        }
        return statements;
    }

    public List<Stmt> parse() {
        try {
            return Program();
        } catch (ParseError error) {
            return null;
        }
    }
}