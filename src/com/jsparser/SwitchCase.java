package com.jsparser;

import java.util.List;

/**
 * Switch 语句的 case 子句
 *
 * - case expr: statements
 * - default: statements (test 为 null)
 */
class SwitchCase {
    SwitchCase(Expr test, List<Stmt> consequent) {
        this.test = test;
        this.consequent = consequent;
    }

    final Expr test;            // case 的测试表达式（default 时为 null）
    final List<Stmt> consequent; // case 体中的语句列表
}
