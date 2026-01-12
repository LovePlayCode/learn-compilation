package com.craftinginterpreters.lox;

import java.util.List;

/**
 * AST 树形打印器
 * 以可视化的树形结构打印 AST
 */
class AstTreePrinter implements Expr.Visitor<String>, Stmt.Visitor<String> {
    private static final String BRANCH = "├── ";
    private static final String LAST_BRANCH = "└── ";
    private static final String VERTICAL = "│   ";
    private static final String SPACE = "    ";

    /**
     * 打印语句列表
     */
    public String print(List<Stmt> statements) {
        StringBuilder builder = new StringBuilder();
        builder.append("Program\n");
        for (int i = 0; i < statements.size(); i++) {
            boolean isLast = (i == statements.size() - 1);
            builder.append(printStmt(statements.get(i), "", isLast));
        }
        return builder.toString();
    }

    /**
     * 打印单个表达式
     */
    public String print(Expr expr) {
        return expr.accept(this);
    }

    private String printStmt(Stmt stmt, String prefix, boolean isLast) {
        if (stmt == null) {
            return prefix + (isLast ? LAST_BRANCH : BRANCH) + "null\n";
        }
        String branch = isLast ? LAST_BRANCH : BRANCH;
        String childPrefix = prefix + (isLast ? SPACE : VERTICAL);
        
        StringBuilder builder = new StringBuilder();
        builder.append(prefix).append(branch);
        
        // 保存当前前缀用于子节点
        String savedPrefix = this.currentPrefix;
        this.currentPrefix = childPrefix;
        builder.append(stmt.accept(this));
        this.currentPrefix = savedPrefix;
        
        return builder.toString();
    }

    private String currentPrefix = "";

    private String printExpr(Expr expr, String prefix, boolean isLast) {
        if (expr == null) {
            return prefix + (isLast ? LAST_BRANCH : BRANCH) + "null\n";
        }
        String branch = isLast ? LAST_BRANCH : BRANCH;
        String childPrefix = prefix + (isLast ? SPACE : VERTICAL);
        
        StringBuilder builder = new StringBuilder();
        builder.append(prefix).append(branch);
        
        String savedPrefix = this.currentPrefix;
        this.currentPrefix = childPrefix;
        builder.append(expr.accept(this));
        this.currentPrefix = savedPrefix;
        
        return builder.toString();
    }

    // ==================== Stmt Visitors ====================

    @Override
    public String visitBlockStmt(Stmt.Block stmt) {
        StringBuilder builder = new StringBuilder();
        builder.append("Block\n");
        List<Stmt> statements = stmt.statements;
        for (int i = 0; i < statements.size(); i++) {
            boolean isLast = (i == statements.size() - 1);
            builder.append(printStmt(statements.get(i), currentPrefix, isLast));
        }
        return builder.toString();
    }

    @Override
    public String visitClassStmt(Stmt.Class stmt) {
        StringBuilder builder = new StringBuilder();
        builder.append("Class: ").append(stmt.name.lexeme).append("\n");
        
        if (stmt.superclass != null) {
            builder.append(currentPrefix).append(BRANCH).append("superclass: ")
                   .append(stmt.superclass.name.lexeme).append("\n");
        }
        
        List<Stmt.Function> methods = stmt.methods;
        for (int i = 0; i < methods.size(); i++) {
            boolean isLast = (i == methods.size() - 1);
            builder.append(printStmt(methods.get(i), currentPrefix, isLast));
        }
        return builder.toString();
    }

    @Override
    public String visitExpressionStmt(Stmt.Expression stmt) {
        StringBuilder builder = new StringBuilder();
        builder.append("ExprStmt\n");
        builder.append(printExpr(stmt.expression, currentPrefix, true));
        return builder.toString();
    }

    @Override
    public String visitFunctionStmt(Stmt.Function stmt) {
        StringBuilder builder = new StringBuilder();
        builder.append("Function: ").append(stmt.name.lexeme).append("(");
        for (int i = 0; i < stmt.params.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(stmt.params.get(i).lexeme);
        }
        builder.append(")\n");
        
        List<Stmt> body = stmt.body;
        for (int i = 0; i < body.size(); i++) {
            boolean isLast = (i == body.size() - 1);
            builder.append(printStmt(body.get(i), currentPrefix, isLast));
        }
        return builder.toString();
    }

    @Override
    public String visitIfStmt(Stmt.If stmt) {
        StringBuilder builder = new StringBuilder();
        builder.append("If\n");
        
        builder.append(currentPrefix).append(BRANCH).append("condition:\n");
        builder.append(printExpr(stmt.condition, currentPrefix + VERTICAL, true));
        
        builder.append(currentPrefix).append(BRANCH).append("then:\n");
        builder.append(printStmt(stmt.thenBranch, currentPrefix + VERTICAL, true));
        
        if (stmt.elseBranch != null) {
            builder.append(currentPrefix).append(LAST_BRANCH).append("else:\n");
            builder.append(printStmt(stmt.elseBranch, currentPrefix + SPACE, true));
        }
        return builder.toString();
    }

    @Override
    public String visitPrintStmt(Stmt.Print stmt) {
        StringBuilder builder = new StringBuilder();
        builder.append("Print\n");
        builder.append(printExpr(stmt.expression, currentPrefix, true));
        return builder.toString();
    }

    @Override
    public String visitReturnStmt(Stmt.Return stmt) {
        StringBuilder builder = new StringBuilder();
        builder.append("Return\n");
        if (stmt.value != null) {
            builder.append(printExpr(stmt.value, currentPrefix, true));
        }
        return builder.toString();
    }

    @Override
    public String visitVarStmt(Stmt.Var stmt) {
        StringBuilder builder = new StringBuilder();
        builder.append("Var: ").append(stmt.name.lexeme).append("\n");
        if (stmt.initializer != null) {
            builder.append(printExpr(stmt.initializer, currentPrefix, true));
        }
        return builder.toString();
    }

    @Override
    public String visitWhileStmt(Stmt.While stmt) {
        StringBuilder builder = new StringBuilder();
        builder.append("While\n");
        
        builder.append(currentPrefix).append(BRANCH).append("condition:\n");
        builder.append(printExpr(stmt.condition, currentPrefix + VERTICAL, true));
        
        builder.append(currentPrefix).append(LAST_BRANCH).append("body:\n");
        builder.append(printStmt(stmt.body, currentPrefix + SPACE, true));
        
        return builder.toString();
    }

    // ==================== Expr Visitors ====================

    @Override
    public String visitAssignExpr(Expr.Assign expr) {
        StringBuilder builder = new StringBuilder();
        builder.append("Assign: ").append(expr.name.lexeme).append("\n");
        builder.append(printExpr(expr.value, currentPrefix, true));
        return builder.toString();
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        StringBuilder builder = new StringBuilder();
        builder.append("Binary: ").append(expr.operator.lexeme).append("\n");
        builder.append(printExpr(expr.left, currentPrefix, false));
        builder.append(printExpr(expr.right, currentPrefix, true));
        return builder.toString();
    }

    @Override
    public String visitCallExpr(Expr.Call expr) {
        StringBuilder builder = new StringBuilder();
        builder.append("Call\n");
        builder.append(currentPrefix).append(BRANCH).append("callee:\n");
        builder.append(printExpr(expr.callee, currentPrefix + VERTICAL, true));
        
        if (!expr.arguments.isEmpty()) {
            builder.append(currentPrefix).append(LAST_BRANCH).append("arguments:\n");
            for (int i = 0; i < expr.arguments.size(); i++) {
                boolean isLast = (i == expr.arguments.size() - 1);
                builder.append(printExpr(expr.arguments.get(i), currentPrefix + SPACE, isLast));
            }
        }
        return builder.toString();
    }

    @Override
    public String visitGetExpr(Expr.Get expr) {
        StringBuilder builder = new StringBuilder();
        builder.append("Get: ").append(expr.name.lexeme).append("\n");
        builder.append(printExpr(expr.object, currentPrefix, true));
        return builder.toString();
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        StringBuilder builder = new StringBuilder();
        builder.append("Grouping\n");
        builder.append(printExpr(expr.expression, currentPrefix, true));
        return builder.toString();
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null) {
            return "Literal: nil\n";
        }
        if (expr.value instanceof String) {
            return "Literal: \"" + expr.value + "\"\n";
        }
        return "Literal: " + expr.value + "\n";
    }

    @Override
    public String visitLogicalExpr(Expr.Logical expr) {
        StringBuilder builder = new StringBuilder();
        builder.append("Logical: ").append(expr.operator.lexeme).append("\n");
        builder.append(printExpr(expr.left, currentPrefix, false));
        builder.append(printExpr(expr.right, currentPrefix, true));
        return builder.toString();
    }

    @Override
    public String visitSetExpr(Expr.Set expr) {
        StringBuilder builder = new StringBuilder();
        builder.append("Set: ").append(expr.name.lexeme).append("\n");
        builder.append(currentPrefix).append(BRANCH).append("object:\n");
        builder.append(printExpr(expr.object, currentPrefix + VERTICAL, true));
        builder.append(currentPrefix).append(LAST_BRANCH).append("value:\n");
        builder.append(printExpr(expr.value, currentPrefix + SPACE, true));
        return builder.toString();
    }

    @Override
    public String visitSuperExpr(Expr.Super expr) {
        return "Super: " + expr.method.lexeme + "\n";
    }

    @Override
    public String visitThisExpr(Expr.This expr) {
        return "This\n";
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        StringBuilder builder = new StringBuilder();
        builder.append("Unary: ").append(expr.operator.lexeme).append("\n");
        builder.append(printExpr(expr.right, currentPrefix, true));
        return builder.toString();
    }

    @Override
    public String visitVariableExpr(Expr.Variable expr) {
        return "Variable: " + expr.name.lexeme + "\n";
    }
}
