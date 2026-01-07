package com.jsparser;

/**
 * JavaScript ES5 词法单元类型枚举
 */
public enum TokenType {
    // ========== 字面量 ==========
    IDENTIFIER,     // 标识符
    NUMBER,         // 数字字面量
    STRING,         // 字符串字面量

    // ========== 关键字 ==========
    // 声明
    VAR,
    FUNCTION,

    // 控制流
    IF,
    ELSE,
    FOR,
    WHILE,
    DO,
    SWITCH,
    CASE,
    DEFAULT,
    BREAK,
    CONTINUE,
    RETURN,

    // 异常处理
    TRY,
    CATCH,
    FINALLY,
    THROW,

    // 运算符关键字
    TYPEOF,
    INSTANCEOF,
    IN,
    NEW,
    DELETE,
    VOID,

    // 字面量关键字
    TRUE,
    FALSE,
    NULL,
    THIS,

    // 其他
    WITH,
    DEBUGGER,

    // ========== 算术运算符 ==========
    PLUS,           // +
    MINUS,          // -
    STAR,           // *
    SLASH,          // /
    PERCENT,        // %

    // ========== 自增自减 ==========
    PLUS_PLUS,      // ++
    MINUS_MINUS,    // --

    // ========== 赋值运算符 ==========
    EQUAL,          // =
    PLUS_EQUAL,     // +=
    MINUS_EQUAL,    // -=
    STAR_EQUAL,     // *=
    SLASH_EQUAL,    // /=
    PERCENT_EQUAL,  // %=
    AND_EQUAL,      // &=
    OR_EQUAL,       // |=
    XOR_EQUAL,      // ^=
    LEFT_SHIFT_EQUAL,   // <<=
    RIGHT_SHIFT_EQUAL,  // >>=
    UNSIGNED_RIGHT_SHIFT_EQUAL, // >>>=

    // ========== 比较运算符 ==========
    EQUAL_EQUAL,        // ==
    EQUAL_EQUAL_EQUAL,  // ===
    BANG_EQUAL,         // !=
    BANG_EQUAL_EQUAL,   // !==
    LESS,               // <
    LESS_EQUAL,         // <=
    GREATER,            // >
    GREATER_EQUAL,      // >=

    // ========== 逻辑运算符 ==========
    AND_AND,        // &&
    OR_OR,          // ||
    BANG,           // !

    // ========== 位运算符 ==========
    AND,            // &
    OR,             // |
    XOR,            // ^
    TILDE,          // ~
    LEFT_SHIFT,     // <<
    RIGHT_SHIFT,    // >>
    UNSIGNED_RIGHT_SHIFT, // >>>

    // ========== 分隔符 ==========
    LEFT_PAREN,     // (
    RIGHT_PAREN,    // )
    LEFT_BRACE,     // {
    RIGHT_BRACE,    // }
    LEFT_BRACKET,   // [
    RIGHT_BRACKET,  // ]
    COMMA,          // ,
    DOT,            // .
    SEMICOLON,      // ;
    COLON,          // :
    QUESTION,       // ?

    // ========== 特殊 ==========
    EOF             // 文件结束
}
