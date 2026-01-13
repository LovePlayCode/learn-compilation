//> Appendix II expr
package com.craftinginterpreters.lox;

import java.util.List;

/**
 * 表达式抽象基类
 * 
 * Lox 语言中所有表达式类型的基类。
 * 使用访问者模式(Visitor Pattern)来实现对不同表达式类型的操作，
 * 避免在每个表达式类中添加新方法，实现开闭原则。
 */
abstract class Expr {
  /**
   * 访问者接口
   * 
   * 定义了访问每种表达式类型的方法。
   * 解释器、打印器等需要遍历 AST 的类都需要实现此接口。
   * 
   * @param <R> 访问方法的返回类型
   */
  interface Visitor<R> {
    R visitAssignExpr(Assign expr);

    R visitBinaryExpr(Binary expr);

    R visitCallExpr(Call expr);

    R visitFunctionExpr(Function expr);

    R visitGetExpr(Get expr);

    R visitGroupingExpr(Grouping expr);

    R visitLiteralExpr(Literal expr);

    R visitLogicalExpr(Logical expr);

    R visitSetExpr(Set expr);

    R visitSuperExpr(Super expr);

    R visitThisExpr(This expr);

    R visitUnaryExpr(Unary expr);

    R visitVariableExpr(Variable expr);
  }

  /**
   * 赋值表达式
   * 
   * 表示变量赋值操作，如: a = 1, x = y + z
   * 
   * @field name  被赋值的变量名
   * @field value 赋给变量的值表达式
   */
  static class Assign extends Expr {
    Assign(Token name, Expr value) {
      this.name = name;
      this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitAssignExpr(this);
    }

    final Token name;
    final Expr value;
  }

  /**
   * 二元表达式
   * 
   * 表示有两个操作数的运算，如: a + b, x * y, m > n
   * 包括算术运算(+, -, *, /)、比较运算(>, <, >=, <=, ==, !=)等
   * 
   * @field left     左操作数
   * @field operator 运算符
   * @field right    右操作数
   */
  static class Binary extends Expr {
    Binary(Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitBinaryExpr(this);
    }

    final Expr left;
    final Token operator;
    final Expr right;
  }

  /**
   * 函数调用表达式
   * 
   * 表示函数或方法的调用，如: foo(), bar(1, 2), obj.method()
   * 
   * @field callee    被调用的函数表达式（可以是变量名或属性访问）
   * @field paren     右括号 token，用于错误报告时定位
   * @field arguments 参数列表
   */
  static class Call extends Expr {
    Call(Expr callee, Token paren, List<Expr> arguments) {
      this.callee = callee;
      this.paren = paren;
      this.arguments = arguments;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitCallExpr(this);
    }

    final Expr callee;
    final Token paren;
    final List<Expr> arguments;
  }

  /**
   * 匿名函数表达式（Lambda）
   * 
   * 表示匿名函数定义，如: fun (a, b) { return a + b; }
   * 可以作为参数传递或立即调用
   * 
   * @field params 参数列表
   * @field body   函数体语句列表
   */
  static class Function extends Expr {
    Function(List<Token> params, List<Stmt> body) {
      this.params = params;
      this.body = body;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitFunctionExpr(this);
    }

    final List<Token> params;
    final List<Stmt> body;
  }

  /**
   * 属性访问表达式（Get）
   * 
   * 表示访问对象的属性，如: obj.name, person.age
   * 
   * @field object 被访问的对象表达式
   * @field name   属性名
   */
  static class Get extends Expr {
    Get(Expr object, Token name) {
      this.object = object;
      this.name = name;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitGetExpr(this);
    }

    final Expr object;
    final Token name;
  }

  /**
   * 分组表达式（括号表达式）
   * 
   * 表示用括号包裹的表达式，用于改变运算优先级，如: (a + b) * c
   * 
   * @field expression 括号内的表达式
   */
  static class Grouping extends Expr {
    Grouping(Expr expression) {
      this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitGroupingExpr(this);
    }

    final Expr expression;
  }

  /**
   * 字面量表达式
   * 
   * 表示直接书写的值，如: 123, "hello", true, false, nil
   * 
   * @field value 字面量的值（可以是 Number, String, Boolean, null）
   */
  static class Literal extends Expr {
    Literal(Object value) {
      this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitLiteralExpr(this);
    }

    final Object value;
  }

  /**
   * 逻辑表达式
   * 
   * 表示逻辑运算 and 和 or，如: a and b, x or y
   * 与 Binary 分开是因为逻辑运算符有短路求值特性
   * 
   * @field left     左操作数
   * @field operator 逻辑运算符（and 或 or）
   * @field right    右操作数
   */
  static class Logical extends Expr {
    Logical(Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitLogicalExpr(this);
    }

    final Expr left;
    final Token operator;
    final Expr right;
  }

  /**
   * 属性设置表达式（Set）
   * 
   * 表示给对象属性赋值，如: obj.name = "John", person.age = 25
   * 
   * @field object 被设置属性的对象表达式
   * @field name   属性名
   * @field value  要设置的值表达式
   */
  static class Set extends Expr {
    Set(Expr object, Token name, Expr value) {
      this.object = object;
      this.name = name;
      this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitSetExpr(this);
    }

    final Expr object;
    final Token name;
    final Expr value;
  }

  /**
   * super 表达式
   * 
   * 表示访问父类的方法，如: super.init(), super.toString()
   * 用于在子类中调用被覆盖的父类方法
   * 
   * @field keyword super 关键字 token
   * @field method  要调用的父类方法名
   */
  static class Super extends Expr {
    Super(Token keyword, Token method) {
      this.keyword = keyword;
      this.method = method;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitSuperExpr(this);
    }

    final Token keyword;
    final Token method;
  }

  /**
   * this 表达式
   * 
   * 表示当前对象实例的引用，如: this.name, this.method()
   * 只能在类的方法内部使用
   * 
   * @field keyword this 关键字 token
   */
  static class This extends Expr {
    This(Token keyword) {
      this.keyword = keyword;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitThisExpr(this);
    }

    final Token keyword;
  }

  /**
   * 一元表达式
   * 
   * 表示只有一个操作数的运算，如: -a, !flag
   * 包括负号(-)和逻辑非(!)
   * 
   * @field operator 一元运算符
   * @field right    操作数（在运算符右侧）
   */
  static class Unary extends Expr {
    Unary(Token operator, Expr right) {
      this.operator = operator;
      this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitUnaryExpr(this);
    }

    final Token operator;
    final Expr right;
  }

  /**
   * 变量表达式
   * 
   * 表示对变量的引用（读取变量值），如: a, count, userName
   * 
   * @field name 变量名
   */
  static class Variable extends Expr {
    Variable(Token name) {
      this.name = name;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitVariableExpr(this);
    }

    final Token name;
  }

  /**
   * 接受访问者方法
   * 
   * 访问者模式的核心方法，每个具体表达式类都需要实现此方法，
   * 调用访问者对应的 visit 方法来处理自己。
   * 
   * @param visitor 访问者对象
   * @param <R>     返回类型
   * @return 访问者处理后的结果
   */
  abstract <R> R accept(Visitor<R> visitor);
}
// < Appendix II expr
