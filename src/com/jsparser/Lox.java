package com.jsparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
    static boolean hadError = false;
    static boolean hadRuntimeError = false;
    private static final Interpreter interpreter = new Interpreter();

    static void error(int line, String message) {
        report(line, "", message);
    }

    static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() +
                "\n[line " + error.token.line + "]");
        hadRuntimeError = true;
    }

    private static void report(int line, String where,
            String message) {
        System.err.println(
                "[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }

    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
        if (hadError)
            System.exit(65);
        if (hadRuntimeError)
            System.exit(70);

    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (;;) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null)
                break;

            // REPL 模式：智能识别表达式和语句
            runRepl(line);
            hadError = false;
        }
    }

    /**
     * REPL 智能执行：支持表达式自动求值打印和语句执行
     * 
     * 策略：
     * 1. 如果输入不以分号结尾，先尝试作为表达式解析
     * 2. 表达式解析成功则求值并打印结果
     * 3. 表达式解析失败或输入以分号结尾，则作为语句执行
     */
    private static void runRepl(String source) {
        // 先尝试作为表达式解析（如果不以分号结尾）
        if (!source.trim().endsWith(";")) {
            Scanner scanner = new Scanner(source);
            List<Token> tokens = scanner.scanTokens();
            Parser parser = new Parser(tokens);
            Expr expression = parser.parseExpression();

            if (expression != null && !hadError) {
                // 成功解析为表达式，求值并打印结果
                String result = interpreter.interpretExpression(expression);
                if (result != null) {
                    System.out.println(result);
                }
                return;
            }

            // 表达式解析失败，重置错误状态，尝试作为语句处理
            hadError = false;
        }

        // 作为语句执行
        run(source);
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        // 打印 AST 语法树
        System.out.println(new AstTreePrinter().print(statements));

        // Stop if there was a syntax error.
        if (hadError)
            return;
        Resolver resolver = new Resolver(interpreter);
        resolver.resolve(statements);
        if (hadError)
            return;

        interpreter.interpret(statements);

        // System.out.println(new AstPrinter().print(expression));
        // For now, just print the tokens.
        // for (Token token : tokens) {
        // System.out.println(token);
        // }
    }
}