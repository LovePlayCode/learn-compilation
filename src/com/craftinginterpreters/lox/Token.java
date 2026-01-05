package com.craftinginterpreters.lox;

/**
 * Token 类表示词法分析器扫描出的一个词法单元。
 * 例如源代码 "var x = 123;" 会被扫描成多个 Token：
 * VAR, IDENTIFIER, EQUAL, NUMBER, SEMICOLON
 */
class Token {
    // Token 的类型，如 VAR, IDENTIFIER, NUMBER, STRING 等
    final TokenType type;

    // 词素：源代码中的原始文本，如 "var", "x", "=", "123"
    final String lexeme;

    // 字面量值：仅对 NUMBER 和 STRING 类型有意义
    // NUMBER 存储 Double 值，STRING 存储去除引号后的字符串
    // 其他类型为 null
    final Object literal;

    // Token 所在的源代码行号，用于错误报告
    final int line;

    /**
     * 构造一个 Token
     * @param type    Token 类型
     * @param lexeme  原始词素文本
     * @param literal 字面量值（NUMBER/STRING 类型使用）
     * @param line    源代码行号
     */
    Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    /**
     * 返回 Token 的字符串表示，格式：类型 词素 字面量
     * 例如：NUMBER 123 123.0
     */
    public String toString() {
        return type + " " + lexeme + " " + literal;
    }
}