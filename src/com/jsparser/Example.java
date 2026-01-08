package com.jsparser;

import java.util.List;

/**
 * JavaScript 词法分析器使用示例
 */
public class Example {

    public static void main(String[] args) {
        // 示例 1: 解析简单表达式
        System.out.println("=== 示例 1: 简单表达式 ===");
        tokenize("var x = 10 + 20;");

        // 示例 2: 函数定义
        System.out.println("\n=== 示例 2: 函数定义 ===");
        tokenize("""
                function greet(name) {
                    return "Hello, " + name;
                }
                """);

        // 示例 3: 各种运算符
        System.out.println("\n=== 示例 3: 运算符 ===");
        tokenize("a === b && c !== d || x >= y");

        // 示例 4: 数字字面量
        System.out.println("\n=== 示例 4: 数字字面量 ===");
        tokenize("var a = 42; var b = 3.14; var c = 1e10; var d = 0xFF;");

        // 示例 5: 字符串转义
        System.out.println("\n=== 示例 5: 字符串 ===");
        tokenize("var s = \"Hello\\nWorld\"; var s2 = 'Tab:\\tHere';");
        // 示例 6: 字符串单引号
        System.out.println("\n=== 示例 5: 字符串 ===");
        tokenize("var s = \'Hello\\nWorld\'; var s2 = 'Tab:\\tHere';");
        // 示例 6: 控制流
        System.out.println("\n=== 示例 6: 控制流 ===");
        tokenize("""
                if (x > 0) {
                    console.log("positive");
                } else {
                    console.log("non-positive");
                }
                """);

        // 示例 7: 数组和对象
        System.out.println("\n=== 示例 7: 数组和对象 ===");
        tokenize("var arr = [1, 2, 3]; var obj = { name: 'test', value: 100 };");

        // 示例 8: 位运算和移位
        System.out.println("\n=== 示例 8: 位运算 ===");
        tokenize("var a = x & y; var b = x | y; var c = x << 2; var d = x >>> 1;");
    }

    private static void tokenize(String source) {
        System.out.println("源码: " + source.replace("\n", "\\n"));
        System.out.println("Tokens:");

        JSLexer lexer = new JSLexer();
        List<Token> tokens = lexer.tokenize(source);

        for (Token token : tokens) {
            if (token.getType() != TokenType.EOF) {
                System.out.printf("  [%d:%d] %-20s '%s'", token.getLine(), token.getColumn(), token.getType(), token.getLexeme());
                if (token.getLiteral() != null) {
                    System.out.printf(" -> %s", token.getLiteral());
                }
                System.out.println();
            }
        }
    }
}
