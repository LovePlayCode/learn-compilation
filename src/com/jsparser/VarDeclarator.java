package com.jsparser;

/**
 * 变量声明器
 *
 * 表示 var 语句中的单个变量声明: var name = init
 */
class VarDeclarator {
    VarDeclarator(Token name, Expr init) {
        this.name = name;
        this.init = init;
    }

    final Token name;   // 变量名
    final Expr init;    // 初始化表达式（可为 null）
}
