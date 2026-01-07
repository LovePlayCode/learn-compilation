package com.jsparser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * JavaScript 词法分析器入口类
 * 提供便捷的 API 来解析 JS 源代码
 */
public class JSLexer {
    private boolean hadError = false;

    /**
     * 解析源代码字符串
     *
     * @param source JS 源代码
     * @return Token 列表
     */
    public List<Token> tokenize(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        if (scanner.hasErrors()) {
            hadError = true;
            for (String error : scanner.getErrors()) {
                System.err.println(error);
            }
        }

        return tokens;
    }

    /**
     * 解析 JS 文件
     *
     * @param path 文件路径
     * @return Token 列表
     * @throws IOException 文件读取错误
     */
    public List<Token> tokenizeFile(String path) throws IOException {
        String source = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
        return tokenize(source);
    }

    /**
     * 是否有错误
     */
    public boolean hadError() {
        return hadError;
    }

    /**
     * 重置错误状态
     */
    public void reset() {
        hadError = false;
    }

    /**
     * 主方法：演示词法分析器
     */
    public static void main(String[] args) {
        // 示例 JS 代码
        String jsCode = """
            // 这是一个示例
            var x = 10;
            var name = "hello";
            
            function add(a, b) {
                return a + b;
            }
            
            if (x >= 5 && x <= 20) {
                console.log("x is in range");
            }
            
            /* 多行注释
               测试 */
            var hex = 0xFF;
            var float = 3.14e-2;
            var arr = [1, 2, 3];
            var obj = { key: 'value' };
            
            x++;
            x === 10;
            x !== 5;
            """;

        System.out.println("========== JavaScript Lexer Demo ==========");
        System.out.println("Source code:");
        System.out.println(jsCode);
        System.out.println("========== Tokens ==========");

        JSLexer lexer = new JSLexer();
        List<Token> tokens = lexer.tokenize(jsCode);

        for (Token token : tokens) {
            System.out.println(token);
        }

        if (lexer.hadError()) {
            System.out.println("\nLexer encountered errors.");
        } else {
            System.out.println("\nLexer completed successfully. Total tokens: " + tokens.size());
        }
    }
}
