package com.jsparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JavaScript ES5 词法分析器
 * 将源代码字符串转换为 Token 序列
 */
public class Scanner {
    // ES5 关键字映射表
    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        // 声明
        keywords.put("var", TokenType.VAR);
        keywords.put("function", TokenType.FUNCTION);

        // 控制流
        keywords.put("if", TokenType.IF);
        keywords.put("else", TokenType.ELSE);
        keywords.put("for", TokenType.FOR);
        keywords.put("while", TokenType.WHILE);
        keywords.put("do", TokenType.DO);
        keywords.put("switch", TokenType.SWITCH);
        keywords.put("case", TokenType.CASE);
        keywords.put("default", TokenType.DEFAULT);
        keywords.put("break", TokenType.BREAK);
        keywords.put("continue", TokenType.CONTINUE);
        keywords.put("return", TokenType.RETURN);

        // 异常处理
        keywords.put("try", TokenType.TRY);
        keywords.put("catch", TokenType.CATCH);
        keywords.put("finally", TokenType.FINALLY);
        keywords.put("throw", TokenType.THROW);

        // 运算符关键字
        keywords.put("typeof", TokenType.TYPEOF);
        keywords.put("instanceof", TokenType.INSTANCEOF);
        keywords.put("in", TokenType.IN);
        keywords.put("new", TokenType.NEW);
        keywords.put("delete", TokenType.DELETE);
        keywords.put("void", TokenType.VOID);

        // 字面量关键字
        keywords.put("true", TokenType.TRUE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("null", TokenType.NULL);
        keywords.put("this", TokenType.THIS);

        // 其他
        keywords.put("with", TokenType.WITH);
        keywords.put("debugger", TokenType.DEBUGGER);
    }

    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();

    // 扫描状态
    private int start = 0;      // 当前词素起始位置
    private int current = 0;    // 当前字符位置
    private int line = 1;       // 当前行号
    private int column = 1;     // 当前列号
    private int startColumn = 1; // 词素起始列号

    public Scanner(String source) {
        this.source = source;
    }

    /**
     * 扫描所有 Token
     */
    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            startColumn = column;
            scanToken();
        }

        tokens.add(new Token(TokenType.EOF, "", null, line, column));
        return tokens;
    }

    /**
     * 获取错误列表
     */
    public List<String> getErrors() {
        return errors;
    }

    /**
     * 是否有错误
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * 扫描单个 Token
     */
    private void scanToken() {
        char c = advance();
        switch (c) {
            // 单字符分隔符
            case '(':
                addToken(TokenType.LEFT_PAREN);
                break;
            case ')':
                addToken(TokenType.RIGHT_PAREN);
                break;
            case '{':
                addToken(TokenType.LEFT_BRACE);
                break;
            case '}':
                addToken(TokenType.RIGHT_BRACE);
                break;
            case '[':
                addToken(TokenType.LEFT_BRACKET);
                break;
            case ']':
                addToken(TokenType.RIGHT_BRACKET);
                break;
            case ',':
                addToken(TokenType.COMMA);
                break;
            case '.':
                // 检查是否是数字开头的小数 .123
                if (isDigit(peek())) {
                    number();
                } else {
                    addToken(TokenType.DOT);
                }
                break;
            case ';':
                addToken(TokenType.SEMICOLON);
                break;
            case ':':
                addToken(TokenType.COLON);
                break;
            case '?':
                addToken(TokenType.QUESTION);
                break;
            case '~':
                addToken(TokenType.TILDE);
                break;

            // 可能是多字符的运算符
            case '+':
                if (match('+')) {
                    addToken(TokenType.PLUS_PLUS);
                } else if (match('=')) {
                    addToken(TokenType.PLUS_EQUAL);
                } else {
                    addToken(TokenType.PLUS);
                }
                break;
            case '-':
                if (match('-')) {
                    addToken(TokenType.MINUS_MINUS);
                } else if (match('=')) {
                    addToken(TokenType.MINUS_EQUAL);
                } else {
                    addToken(TokenType.MINUS);
                }
                break;
            case '*':
                addToken(match('=') ? TokenType.STAR_EQUAL : TokenType.STAR);
                break;
            case '%':
                addToken(match('=') ? TokenType.PERCENT_EQUAL : TokenType.PERCENT);
                break;
            case '/':
                if (match('/')) {
                    // 单行注释
                    singleLineComment();
                } else if (match('*')) {
                    // 多行注释
                    multiLineComment();
                } else if (match('=')) {
                    addToken(TokenType.SLASH_EQUAL);
                } else {
                    addToken(TokenType.SLASH);
                }
                break;

            case '!':
                if (match('=')) {
                    addToken(match('=') ? TokenType.BANG_EQUAL_EQUAL : TokenType.BANG_EQUAL);
                } else {
                    addToken(TokenType.BANG);
                }
                break;
            case '=':
                if (match('=')) {
                    addToken(match('=') ? TokenType.EQUAL_EQUAL_EQUAL : TokenType.EQUAL_EQUAL);
                } else {
                    addToken(TokenType.EQUAL);
                }
                break;
            case '<':
                if (match('<')) {
                    addToken(match('=') ? TokenType.LEFT_SHIFT_EQUAL : TokenType.LEFT_SHIFT);
                } else if (match('=')) {
                    addToken(TokenType.LESS_EQUAL);
                } else {
                    addToken(TokenType.LESS);
                }
                break;
            case '>':
                if (match('>')) {
                    if (match('>')) {
                        addToken(match('=') ? TokenType.UNSIGNED_RIGHT_SHIFT_EQUAL : TokenType.UNSIGNED_RIGHT_SHIFT);
                    } else {
                        addToken(match('=') ? TokenType.RIGHT_SHIFT_EQUAL : TokenType.RIGHT_SHIFT);
                    }
                } else if (match('=')) {
                    addToken(TokenType.GREATER_EQUAL);
                } else {
                    addToken(TokenType.GREATER);
                }
                break;
            case '&':
                if (match('&')) {
                    addToken(TokenType.AND_AND);
                } else if (match('=')) {
                    addToken(TokenType.AND_EQUAL);
                } else {
                    addToken(TokenType.AND);
                }
                break;
            case '|':
                if (match('|')) {
                    addToken(TokenType.OR_OR);
                } else if (match('=')) {
                    addToken(TokenType.OR_EQUAL);
                } else {
                    addToken(TokenType.OR);
                }
                break;
            case '^':
                addToken(match('=') ? TokenType.XOR_EQUAL : TokenType.XOR);
                break;

            // 空白字符
            case ' ':
            case '\r':
            case '\t':
                // 忽略空白
                break;
            case '\n':
                line++;
                column = 1;
                break;

            // 字符串
            case '"':
            case '\'':
                string(c);
                break;

            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    error("Unexpected character: " + c);
                }
                break;
        }
    }

    // ========== 词法单元扫描方法 ==========

    /**
     * 扫描标识符或关键字
     */
    private void identifier() {
        while (isAlphaNumeric(peek())) {
            advance();
        }

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) {
            type = TokenType.IDENTIFIER;
        }
        addToken(type);
    }

    /**
     * 扫描数字字面量
     * 支持：整数、浮点数、科学计数法、十六进制
     */
    private void number() {
        // 检查十六进制
        if (peek() == 'x' || peek() == 'X') {
            if (source.charAt(start) == '0') {
                advance(); // 消费 x
                hexNumber();
                return;
            }
        }

        // 消费整数部分
        while (isDigit(peek())) {
            advance();
        }

        // 小数部分
        if (peek() == '.' && isDigit(peekNext())) {
            advance(); // 消费 .
            while (isDigit(peek())) {
                advance();
            }
        }

        // 科学计数法
        if (peek() == 'e' || peek() == 'E') {
            advance(); // 消费 e/E
            if (peek() == '+' || peek() == '-') {
                advance(); // 消费符号
            }
            if (!isDigit(peek())) {
                error("Invalid number: expected digit after exponent");
                return;
            }
            while (isDigit(peek())) {
                advance();
            }
        }

        String text = source.substring(start, current);
        try {
            double value = Double.parseDouble(text);
            addToken(TokenType.NUMBER, value);
        } catch (NumberFormatException e) {
            error("Invalid number: " + text);
        }
    }

    /**
     * 扫描十六进制数字
     */
    private void hexNumber() {
        if (!isHexDigit(peek())) {
            error("Invalid hexadecimal number");
            return;
        }

        while (isHexDigit(peek())) {
            advance();
        }

        String text = source.substring(start, current);
        try {
            // 去掉 0x 前缀解析
            long value = Long.parseLong(text.substring(2), 16);
            addToken(TokenType.NUMBER, (double) value);
        } catch (NumberFormatException e) {
            error("Invalid hexadecimal number: " + text);
        }
    }

    /**
     * 扫描字符串字面量
     */
    private void string(char quote) {
        StringBuilder value = new StringBuilder();

        while (peek() != quote && !isAtEnd()) {
            if (peek() == '\n') {
                error("Unterminated string: newline in string literal");
                return;
            }
            if (peek() == '\\') {
                advance(); // 消费反斜杠
                char escaped = escapeChar();
                if (escaped != '\0') {
                    value.append(escaped);
                }
            } else {
                value.append(advance());
            }
        }

        if (isAtEnd()) {
            error("Unterminated string");
            return;
        }

        advance(); // 消费闭合引号
        addToken(TokenType.STRING, value.toString());
    }

    /**
     * 处理转义字符
     */
    private char escapeChar() {
        if (isAtEnd()) {
            error("Unexpected end of string");
            return '\0';
        }

        char c = advance();
        switch (c) {
            case 'n':
                return '\n';
            case 't':
                return '\t';
            case 'r':
                return '\r';
            case 'b':
                return '\b';
            case 'f':
                return '\f';
            case '\\':
                return '\\';
            case '\'':
                return '\'';
            case '"':
                return '"';
            case '0':
                return '\0';
            case 'x':
                return hexEscape(2);
            case 'u':
                return hexEscape(4);
            default:
                // 未知转义，返回原字符
                return c;
        }
    }

    /**
     * 处理十六进制转义序列
     */
    private char hexEscape(int length) {
        StringBuilder hex = new StringBuilder();
        for (int i = 0; i < length; i++) {
            if (!isHexDigit(peek())) {
                error("Invalid escape sequence");
                return '\0';
            }
            hex.append(advance());
        }
        try {
            return (char) Integer.parseInt(hex.toString(), 16);
        } catch (NumberFormatException e) {
            error("Invalid escape sequence");
            return '\0';
        }
    }

    /**
     * 单行注释
     */
    private void singleLineComment() {
        while (peek() != '\n' && !isAtEnd()) {
            advance();
        }
    }

    /**
     * 多行注释
     */
    private void multiLineComment() {
        while (!isAtEnd()) {
            if (peek() == '*' && peekNext() == '/') {
                advance(); // 消费 *
                advance(); // 消费 /
                return;
            }
            if (peek() == '\n') {
                line++;
                column = 0; // advance 会加 1
            }
            advance();
        }
        error("Unterminated multi-line comment");
    }

    // ========== 辅助方法 ==========

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        column++;
        return source.charAt(current++);
    }

    private char peek() {
        if (isAtEnd()) {
            return '\0';
        }
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) {
            return '\0';
        }
        return source.charAt(current + 1);
    }

    private boolean match(char expected) {
        if (isAtEnd()) {
            return false;
        }
        if (source.charAt(current) != expected) {
            return false;
        }
        current++;
        column++;
        return true;
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isHexDigit(char c) {
        return isDigit(c) || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_' || c == '$';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line, startColumn));
    }

    private void error(String message) {
        errors.add(String.format("[%d:%d] Error: %s", line, startColumn, message));
    }
}
