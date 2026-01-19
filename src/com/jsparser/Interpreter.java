package com.jsparser;

import java.util.List;

import com.jsparser.Expr.ArrayLiteral;
import com.jsparser.Expr.Assign;
import com.jsparser.Expr.Binary;
import com.jsparser.Expr.Call;
import com.jsparser.Expr.Conditional;
import com.jsparser.Expr.Delete;
import com.jsparser.Expr.Function;
import com.jsparser.Expr.Grouping;
import com.jsparser.Expr.Identifier;
import com.jsparser.Expr.In;
import com.jsparser.Expr.Instanceof;
import com.jsparser.Expr.Literal;
import com.jsparser.Expr.Logical;
import com.jsparser.Expr.Member;
import com.jsparser.Expr.New;
import com.jsparser.Expr.ObjectLiteral;
import com.jsparser.Expr.Sequence;
import com.jsparser.Expr.This;
import com.jsparser.Expr.Typeof;
import com.jsparser.Expr.Unary;
import com.jsparser.Expr.Void;
import com.jsparser.Stmt.Block;
import com.jsparser.Stmt.Break;
import com.jsparser.Stmt.Continue;
import com.jsparser.Stmt.Debugger;
import com.jsparser.Stmt.DoWhile;
import com.jsparser.Stmt.Empty;
import com.jsparser.Stmt.Expression;
import com.jsparser.Stmt.For;
import com.jsparser.Stmt.ForIn;
import com.jsparser.Stmt.If;
import com.jsparser.Stmt.Labeled;
import com.jsparser.Stmt.Return;
import com.jsparser.Stmt.Switch;
import com.jsparser.Stmt.Throw;
import com.jsparser.Stmt.Try;
import com.jsparser.Stmt.Var;
import com.jsparser.Stmt.While;
import com.jsparser.Stmt.With;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    @Override
    public Object visitArrayLiteralExpr(ArrayLiteral expr) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitAssignExpr(Assign expr) {
        // TODO Auto-generated method stub
        return null;
    }

    private String stringify(Object object) {
        if (object == null)
            return "nil";

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return object.toString();
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null)
            return true;
        if (a == null)
            return false;

        return a.equals(b);
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double)
            return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double)
            return;

        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    @Override
    public Object visitBinaryExpr(Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case GREATER:
                checkNumberOperands(expr.operator, left, right);

                return (double) left > (double) right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);

                return (double) left >= (double) right;
            case LESS:
                checkNumberOperands(expr.operator, left, right);

                return (double) left < (double) right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);

                return (double) left <= (double) right;
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left - (double) right;
            case SLASH:
                checkNumberOperands(expr.operator, left, right);

                return (double) left / (double) right;
            case STAR:
                checkNumberOperands(expr.operator, left, right);

                return (double) left * (double) right;
            // +号可以用来拼接字符串或计算
            case PLUS:

                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                }

                if (left instanceof String && right instanceof String) {
                    return (String) left + (String) right;
                }
                // 替换部分开始
                throw new RuntimeError(expr.operator,
                        "Operands must be two numbers or two strings.");
            case BANG_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);
        }

        // Unreachable.
        return null;

    }

    @Override
    public Object visitCallExpr(Call expr) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitConditionalExpr(Conditional expr) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitDeleteExpr(Delete expr) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitFunctionExpr(Function expr) {
        // TODO Auto-generated method stub
        return null;
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    /**
     * 计算括号表达式，递归子表达式求值即可。
     */
    @Override
    public Object visitGroupingExpr(Grouping expr) {

        return evaluate(expr.expression);
    }

    @Override
    public Object visitIdentifierExpr(Identifier expr) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitInExpr(In expr) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitInstanceofExpr(Instanceof expr) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitLiteralExpr(Literal expr) {
        // TODO Auto-generated method stub
        return expr.value;
    }

    @Override
    public Object visitLogicalExpr(Logical expr) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitMemberExpr(Member expr) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitNewExpr(New expr) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitObjectLiteralExpr(ObjectLiteral expr) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitSequenceExpr(Sequence expr) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitThisExpr(This expr) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitTypeofExpr(Typeof expr) {
        // TODO Auto-generated method stub
        return null;
    }

    private boolean isTruthy(Object object) {
        if (object == null)
            return false;
        if (object instanceof Boolean)
            return (boolean) object;
        return true;
    }

    @Override
    public Object visitUnaryExpr(Unary expr) {
        // TODO: 有前缀和后缀的区别，需要进行完善
        Object right = evaluate(expr.operand);
        if (expr.prefix) {
            switch (expr.operator.type) {
                case BANG:
                    return !isTruthy(right);
                case MINUS:
                    checkNumberOperand(expr.operator, right);

                    return -(double) right;
            }
        }

        // Unreachable.
        return null;

    }

    @Override
    public Object visitVoidExpr(Void expr) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitExpressionStmt(Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitBlockStmt(Block stmt) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitBlockStmt'");
    }

    @Override
    public Void visitEmptyStmt(Empty stmt) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitEmptyStmt'");
    }

    @Override
    public Void visitVarStmt(Var stmt) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitVarStmt'");
    }

    @Override
    public Void visitFunctionStmt(com.jsparser.Stmt.Function stmt) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitFunctionStmt'");
    }

    @Override
    public Void visitIfStmt(If stmt) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitIfStmt'");
    }

    @Override
    public Void visitWhileStmt(While stmt) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitWhileStmt'");
    }

    @Override
    public Void visitDoWhileStmt(DoWhile stmt) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitDoWhileStmt'");
    }

    @Override
    public Void visitForStmt(For stmt) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitForStmt'");
    }

    @Override
    public Void visitForInStmt(ForIn stmt) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitForInStmt'");
    }

    @Override
    public Void visitSwitchStmt(Switch stmt) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitSwitchStmt'");
    }

    @Override
    public Void visitBreakStmt(Break stmt) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitBreakStmt'");
    }

    @Override
    public Void visitContinueStmt(Continue stmt) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitContinueStmt'");
    }

    @Override
    public Void visitReturnStmt(Return stmt) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitReturnStmt'");
    }

    @Override
    public Void visitThrowStmt(Throw stmt) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitThrowStmt'");
    }

    @Override
    public Void visitTryStmt(Try stmt) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitTryStmt'");
    }

    @Override
    public Void visitWithStmt(With stmt) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitWithStmt'");
    }

    @Override
    public Void visitLabeledStmt(Labeled stmt) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitLabeledStmt'");
    }

    @Override
    public Void visitDebuggerStmt(Debugger stmt) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitDebuggerStmt'");
    }

}
