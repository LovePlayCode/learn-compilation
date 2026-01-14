package com.jsparser;

/**
 * 对象字面量的属性
 *
 * 支持三种形式:
 * - 普通属性: { key: value }
 * - getter: { get prop() { return x; } }
 * - setter: { set prop(v) { x = v; } }
 */
class Property {
    enum Kind {
        INIT,   // 普通属性
        GET,    // getter
        SET     // setter
    }

    Property(Expr key, Expr value, Kind kind) {
        this.key = key;
        this.value = value;
        this.kind = kind;
    }

    final Expr key;     // 属性名（Identifier 或 Literal）
    final Expr value;   // 属性值（表达式或函数）
    final Kind kind;    // 属性类型
}
