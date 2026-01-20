package com.jsparser;

import java.util.List;

/**
 * AST 树形打印器
 * 
 * 以树状结构打印语法树，支持已实现的节点类型
 */
class AstTreePrinter implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
    private final StringBuilder builder = new StringBuilder();
    private String prefix = "";
    private boolean isLast = true;

    public String print(List<Stmt> statements) {
        builder.setLength(0);
        builder.append("Program\n");
        
        for (int i = 0; i < statements.size(); i++) {
            isLast = (i == statements.size() - 1);
            prefix = "";
            printStatement(statements.get(i));
        }
        
        return builder.toString();
    }

    public String print(Expr expr) {
        builder.setLength(0);
        prefix = "";
        isLast = true;
        printExpr(expr);
        return builder.toString();
    }

    private void printStatement(Stmt stmt) {
        if (stmt != null) {
            stmt.accept(this);
        }
    }

    private void printExpr(Expr expr) {
        if (expr != null) {
            expr.accept(this);
        }
    }

    private void printNode(String name) {
        builder.append(prefix);
        builder.append(isLast ? "└── " : "├── ");
        builder.append(name);
        builder.append("\n");
    }

    private void pushPrefix() {
        prefix = prefix + (isLast ? "    " : "│   ");
    }

    private void popPrefix() {
        if (prefix.length() >= 4) {
            prefix = prefix.substring(0, prefix.length() - 4);
        }
    }

    // ==================== Stmt Visitors ====================

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        printNode("VarStatement");
        pushPrefix();
        
        List<VarDeclarator> declarations = stmt.declarations;
        for (int i = 0; i < declarations.size(); i++) {
            isLast = (i == declarations.size() - 1);
            printVarDeclarator(declarations.get(i));
        }
        
        popPrefix();
        return null;
    }

    private void printVarDeclarator(VarDeclarator decl) {
        printNode("VarDeclarator");
        pushPrefix();
        
        boolean hasInit = decl.init != null;
        
        // 打印变量名
        isLast = !hasInit;
        printNode("name: " + decl.name.lexeme);
        
        // 打印初始化表达式
        if (hasInit) {
            isLast = true;
            printNode("init:");
            pushPrefix();
            isLast = true;
            printExpr(decl.init);
            popPrefix();
        }
        
        popPrefix();
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        printNode("ExpressionStatement");
        pushPrefix();
        isLast = true;
        printExpr(stmt.expression);
        popPrefix();
        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        printNode("Block");
        pushPrefix();
        List<Stmt> stmts = stmt.getStatements();
        for (int i = 0; i < stmts.size(); i++) {
            isLast = (i == stmts.size() - 1);
            printStatement(stmts.get(i));
        }
        popPrefix();
        return null;
    }

    @Override
    public Void visitEmptyStmt(Stmt.Empty stmt) {
        printNode("Empty");
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        printNode("FunctionDeclaration(" + stmt.name.lexeme + ")");
        pushPrefix();
        
        // 打印参数列表
        isLast = (stmt.body == null);
        if (stmt.params.isEmpty()) {
            printNode("params: []");
        } else {
            StringBuilder params = new StringBuilder("params: [");
            for (int i = 0; i < stmt.params.size(); i++) {
                if (i > 0) params.append(", ");
                params.append(stmt.params.get(i).lexeme);
            }
            params.append("]");
            printNode(params.toString());
        }
        
        // 打印函数体
        if (stmt.body != null) {
            isLast = true;
            printNode("body:");
            pushPrefix();
            List<Stmt> stmts = stmt.body.getStatements();
            for (int i = 0; i < stmts.size(); i++) {
                isLast = (i == stmts.size() - 1);
                printStatement(stmts.get(i));
            }
            popPrefix();
        }
        
        popPrefix();
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        printNode("If");
        pushPrefix();
        
        // 条件
        isLast = false;
        printNode("condition:");
        pushPrefix();
        isLast = true;
        printExpr(stmt.condition);
        popPrefix();
        
        // consequent 分支 (then)
        isLast = (stmt.alternate == null);
        printNode("consequent:");
        pushPrefix();
        isLast = true;
        printStatement(stmt.consequent);
        popPrefix();
        
        // alternate 分支 (else)
        if (stmt.alternate != null) {
            isLast = true;
            printNode("alternate:");
            pushPrefix();
            isLast = true;
            printStatement(stmt.alternate);
            popPrefix();
        }
        
        popPrefix();
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        printNode("While");
        pushPrefix();
        
        // 条件
        isLast = false;
        printNode("condition:");
        pushPrefix();
        isLast = true;
        printExpr(stmt.condition);
        popPrefix();
        
        // 循环体
        isLast = true;
        printNode("body:");
        pushPrefix();
        isLast = true;
        printStatement(stmt.body);
        popPrefix();
        
        popPrefix();
        return null;
    }

    @Override
    public Void visitDoWhileStmt(Stmt.DoWhile stmt) {
        printNode("DoWhile");
        return null;
    }

    @Override
    public Void visitForStmt(Stmt.For stmt) {
        printNode("For");
        return null;
    }

    @Override
    public Void visitForInStmt(Stmt.ForIn stmt) {
        printNode("ForIn");
        return null;
    }

    @Override
    public Void visitSwitchStmt(Stmt.Switch stmt) {
        printNode("Switch");
        return null;
    }

    @Override
    public Void visitBreakStmt(Stmt.Break stmt) {
        printNode("Break");
        return null;
    }

    @Override
    public Void visitContinueStmt(Stmt.Continue stmt) {
        printNode("Continue");
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        printNode("Return");
        if (stmt.argument != null) {
            pushPrefix();
            isLast = true;
            printExpr(stmt.argument);
            popPrefix();
        }
        return null;
    }

    @Override
    public Void visitThrowStmt(Stmt.Throw stmt) {
        printNode("Throw");
        return null;
    }

    @Override
    public Void visitTryStmt(Stmt.Try stmt) {
        printNode("Try");
        return null;
    }

    @Override
    public Void visitWithStmt(Stmt.With stmt) {
        printNode("With");
        return null;
    }

    @Override
    public Void visitLabeledStmt(Stmt.Labeled stmt) {
        printNode("Labeled");
        return null;
    }

    @Override
    public Void visitDebuggerStmt(Stmt.Debugger stmt) {
        printNode("Debugger");
        return null;
    }

    // ==================== Expr Visitors ====================

    @Override
    public Void visitLiteralExpr(Expr.Literal expr) {
        String value;
        if (expr.value == null) {
            value = "null";
        } else if (expr.value instanceof String) {
            value = "\"" + expr.value + "\"";
        } else {
            value = expr.value.toString();
        }
        printNode("Literal(" + value + ")");
        return null;
    }

    @Override
    public Void visitIdentifierExpr(Expr.Identifier expr) {
        printNode("Identifier(" + expr.name.lexeme + ")");
        return null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping expr) {
        printNode("Grouping");
        pushPrefix();
        isLast = true;
        printExpr(expr.expression);
        popPrefix();
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary expr) {
        String type = expr.prefix ? "prefix" : "postfix";
        printNode("Unary(" + expr.operator.lexeme + ", " + type + ")");
        pushPrefix();
        isLast = true;
        printExpr(expr.operand);
        popPrefix();
        return null;
    }

    @Override
    public Void visitBinaryExpr(Expr.Binary expr) {
        printNode("Binary(" + expr.operator.lexeme + ")");
        pushPrefix();
        
        isLast = false;
        printNode("left:");
        pushPrefix();
        isLast = true;
        printExpr(expr.left);
        popPrefix();
        
        isLast = true;
        printNode("right:");
        pushPrefix();
        isLast = true;
        printExpr(expr.right);
        popPrefix();
        
        popPrefix();
        return null;
    }

    @Override
    public Void visitLogicalExpr(Expr.Logical expr) {
        printNode("Logical(" + expr.operator.lexeme + ")");
        pushPrefix();
        
        isLast = false;
        printNode("left:");
        pushPrefix();
        isLast = true;
        printExpr(expr.left);
        popPrefix();
        
        isLast = true;
        printNode("right:");
        pushPrefix();
        isLast = true;
        printExpr(expr.right);
        popPrefix();
        
        popPrefix();
        return null;
    }

    @Override
    public Void visitAssignExpr(Expr.Assign expr) {
        printNode("Assign(" + expr.operator.lexeme + ")");
        pushPrefix();
        
        isLast = false;
        printNode("target:");
        pushPrefix();
        isLast = true;
        printExpr(expr.target);
        popPrefix();
        
        isLast = true;
        printNode("value:");
        pushPrefix();
        isLast = true;
        printExpr(expr.value);
        popPrefix();
        
        popPrefix();
        return null;
    }

    @Override
    public Void visitThisExpr(Expr.This expr) {
        printNode("This");
        return null;
    }

    @Override
    public Void visitArrayLiteralExpr(Expr.ArrayLiteral expr) {
        printNode("ArrayLiteral[...]");
        return null;
    }

    @Override
    public Void visitObjectLiteralExpr(Expr.ObjectLiteral expr) {
        printNode("ObjectLiteral{...}");
        return null;
    }

    /**
     * 获取成员表达式的完整路径，如 console.log
     */
    private String getMemberPath(Expr expr) {
        if (expr instanceof Expr.Identifier) {
            return ((Expr.Identifier) expr).name.lexeme;
        } else if (expr instanceof Expr.Member) {
            Expr.Member member = (Expr.Member) expr;
            String objectPath = getMemberPath(member.object);
            if (member.computed) {
                return objectPath + "[...]";
            } else {
                return objectPath + "." + member.property.lexeme;
            }
        } else if (expr instanceof Expr.Call) {
            Expr.Call call = (Expr.Call) expr;
            return getMemberPath(call.callee) + "()";
        }
        return "?";
    }

    @Override
    public Void visitMemberExpr(Expr.Member expr) {
        // 显示完整路径，如 Member(console.log)
        String fullPath = getMemberPath(expr);
        printNode("Member: " + fullPath);
        pushPrefix();
        
        // 打印对象部分
        isLast = !expr.computed;
        printNode("object: " + getMemberPath(expr.object));
        pushPrefix();
        isLast = true;
        printExpr(expr.object);
        popPrefix();
        
        // 打印属性部分
        if (expr.computed && expr.computedProperty != null) {
            isLast = true;
            printNode("property: [computed]");
            pushPrefix();
            isLast = true;
            printExpr(expr.computedProperty);
            popPrefix();
        } else if (!expr.computed) {
            isLast = true;
            printNode("property: " + expr.property.lexeme);
        }
        
        popPrefix();
        return null;
    }

    @Override
    public Void visitCallExpr(Expr.Call expr) {
        // 显示完整调用路径，如 Call: console.log(...)
        String calleePath = getMemberPath(expr.callee);
        printNode("Call: " + calleePath + "(" + expr.arguments.size() + " args)");
        pushPrefix();
        
        // 打印被调用者
        isLast = expr.arguments.isEmpty();
        printNode("callee: " + calleePath);
        pushPrefix();
        isLast = true;
        printExpr(expr.callee);
        popPrefix();
        
        // 打印参数
        if (!expr.arguments.isEmpty()) {
            isLast = true;
            printNode("arguments: [" + expr.arguments.size() + "]");
            pushPrefix();
            for (int i = 0; i < expr.arguments.size(); i++) {
                isLast = (i == expr.arguments.size() - 1);
                printNode("[" + i + "]:");
                pushPrefix();
                isLast = true;
                printExpr(expr.arguments.get(i));
                popPrefix();
            }
            popPrefix();
        }
        
        popPrefix();
        return null;
    }

    @Override
    public Void visitNewExpr(Expr.New expr) {
        printNode("New");
        return null;
    }

    @Override
    public Void visitConditionalExpr(Expr.Conditional expr) {
        printNode("Conditional");
        return null;
    }

    @Override
    public Void visitSequenceExpr(Expr.Sequence expr) {
        printNode("Sequence");
        return null;
    }

    @Override
    public Void visitFunctionExpr(Expr.Function expr) {
        printNode("Function");
        return null;
    }

    @Override
    public Void visitTypeofExpr(Expr.Typeof expr) {
        printNode("Typeof");
        return null;
    }

    @Override
    public Void visitVoidExpr(Expr.Void expr) {
        printNode("Void");
        return null;
    }

    @Override
    public Void visitDeleteExpr(Expr.Delete expr) {
        printNode("Delete");
        return null;
    }

    @Override
    public Void visitInstanceofExpr(Expr.Instanceof expr) {
        printNode("Instanceof");
        return null;
    }

    @Override
    public Void visitInExpr(Expr.In expr) {
        printNode("In");
        return null;
    }
}
