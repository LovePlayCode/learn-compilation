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
    static boolean astOnly = false; // 仅打印 AST，不执行
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
        // 解析命令行参数
        int fileArgIndex = 0;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--ast")) {
                astOnly = true;
            } else {
                fileArgIndex = i;
                break;
            }
        }

        int remainingArgs = args.length - (astOnly ? 1 : 0);

        if (remainingArgs > 1) {
            System.out.println("Usage: jlox [--ast] [script]");
            System.exit(64);
        } else if (remainingArgs == 1) {
            runFile(args[fileArgIndex]);
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
            run(line);
            hadError = false;
        }
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        // Stop if there was a syntax error.
        if (hadError)
            return;

        if (astOnly) {
            // 仅打印 AST 树形结构
            System.out.println(new AstTreePrinter().print(statements));
        } else {
            // 打印 AST + 执行代码
            System.out.println("=== AST ===");
            System.out.println(new AstTreePrinter().print(statements));
            System.out.println("=== Output ===");
            Resolver resolver = new Resolver(interpreter);
            resolver.resolve(statements);

            if (hadError)
                return;
            interpreter.interpret(statements);
        }
    }
}