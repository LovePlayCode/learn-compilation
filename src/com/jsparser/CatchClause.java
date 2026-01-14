package com.jsparser;

/**
 * Try 语句的 catch 子句
 *
 * catch (param) { body }
 */
class CatchClause {
    CatchClause(Token param, Stmt body) {
        this.param = param;
        this.body = body;
    }

    final Token param;  // 异常参数名
    final Stmt body;    // catch 块（Block 语句）
}
