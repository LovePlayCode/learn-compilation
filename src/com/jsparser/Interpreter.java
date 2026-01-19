package com.jsparser;

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

public class Interpreter implements Expr.Visitor<Object> {

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

    @Override
    public Object visitBinaryExpr(Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case MINUS:
                return (double) left - (double) right;
            case SLASH:
                return (double) left / (double) right;
            case STAR:
                return (double) left * (double) right;
            // +号可以用来拼接字符串或计算
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                }

                if (left instanceof String && right instanceof String) {
                    return (String) left + (String) right;
                }

                break;
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

}
