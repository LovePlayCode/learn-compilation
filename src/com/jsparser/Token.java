package com.jsparser;

/**
 * JavaScript Token 类
 * 表示词法分析器扫描出的一个词法单元
 */
public class Token {
    // Token 类型
    private final TokenType type;

    // 词素：源代码中的原始文本
    private final String lexeme;

    // 字面量值：对 NUMBER 和 STRING 类型有意义
    private final Object literal;

    // 行号
    private final int line;

    // 列号
    private final int column;

    /**
     * 构造一个 Token
     *
     * @param type    Token 类型
     * @param lexeme  原始词素文本
     * @param literal 字面量值
     * @param line    行号
     * @param column  列号
     */
    public Token(TokenType type, String lexeme, Object literal, int line, int column) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
        this.column = column;
    }

    public TokenType getType() {
        return type;
    }

    public String getLexeme() {
        return lexeme;
    }

    public Object getLiteral() {
        return literal;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    @Override
    public String toString() {
        if (literal != null) {
            return String.format("%s '%s' %s [%d:%d]", type, lexeme, literal, line, column);
        }
        return String.format("%s '%s' [%d:%d]", type, lexeme, line, column);
    }
}
