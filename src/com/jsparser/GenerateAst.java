package com.jsparser;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

/**
 * JavaScript AST 代码生成器
 * 
 * 根据 ES5 语法规范，自动生成表达式(Expr)和语句(Stmt)的 AST 类。
 * 使用访问者模式(Visitor Pattern)来支持对 AST 的各种操作。
 * 
 * 用法: java GenerateAst <output directory>
 */
public class GenerateAst {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(64);
        }
        String outputDir = args[0];

        // 生成表达式类
        defineAst(outputDir, "Expr", Arrays.asList(
                // ========== 字面量 ==========
                "Literal        : Object value",                                    // 字面量: null, true, false, 数字, 字符串
                "ArrayLiteral   : List<Expr> elements",                             // 数组字面量: [1, 2, 3]
                "ObjectLiteral  : List<Property> properties",                       // 对象字面量: { key: value }

                // ========== 标识符和 this ==========
                "Identifier     : Token name",                                      // 标识符: foo, bar
                "This           : Token keyword",                                   // this 关键字

                // ========== 运算符表达式 ==========
                "Unary          : Token operator, Expr operand, boolean prefix",    // 一元: -x, !x, ++x, x++
                "Binary         : Expr left, Token operator, Expr right",           // 二元: a + b, a * b
                "Logical        : Expr left, Token operator, Expr right",           // 逻辑: a && b, a || b
                "Conditional    : Expr condition, Expr consequent, Expr alternate", // 三元: a ? b : c
                "Sequence       : List<Expr> expressions",                          // 逗号: a, b, c

                // ========== 赋值表达式 ==========
                "Assign         : Expr target, Token operator, Expr value",         // 赋值: a = 1, a += 1

                // ========== 成员访问 ==========
                "Member         : Expr object, Token property, boolean computed",   // 成员: obj.prop, obj[expr]

                // ========== 函数相关 ==========
                "Call           : Expr callee, Token paren, List<Expr> arguments",  // 调用: foo(a, b)
                "New            : Token keyword, Expr callee, List<Expr> arguments",// new: new Foo(a)
                "Function       : Token name, List<Token> params, List<Stmt> body", // 函数表达式: function(a) {}

                // ========== 特殊运算符 ==========
                "Typeof         : Token operator, Expr operand",                    // typeof x
                "Void           : Token operator, Expr operand",                    // void x
                "Delete         : Token operator, Expr operand",                    // delete obj.prop
                "Instanceof     : Expr left, Token operator, Expr right",           // a instanceof B
                "In             : Expr left, Token operator, Expr right",           // "prop" in obj

                // ========== 分组 ==========
                "Grouping       : Expr expression"                                  // 括号: (a + b)
        ));

        // 生成语句类
        defineAst(outputDir, "Stmt", Arrays.asList(
                // ========== 基本语句 ==========
                "Expression     : Expr expression",                                 // 表达式语句: foo();
                "Block          : List<Stmt> statements",                           // 块语句: { ... }
                "Empty          : Token semicolon",                                 // 空语句: ;

                // ========== 声明语句 ==========
                "Var            : List<VarDeclarator> declarations",                // var a = 1, b = 2;
                "Function       : Token name, List<Token> params, List<Stmt> body", // function foo() {}

                // ========== 控制流 ==========
                "If             : Expr condition, Stmt consequent, Stmt alternate", // if-else
                "While          : Expr condition, Stmt body",                       // while
                "DoWhile        : Stmt body, Expr condition",                       // do-while
                "For            : Stmt init, Expr condition, Expr update, Stmt body", // for
                "ForIn          : Stmt left, Expr right, Stmt body",                // for-in
                "Switch         : Expr discriminant, List<SwitchCase> cases",       // switch

                // ========== 跳转语句 ==========
                "Break          : Token keyword, Token label",                      // break, break label
                "Continue       : Token keyword, Token label",                      // continue, continue label
                "Return         : Token keyword, Expr argument",                    // return, return x

                // ========== 异常处理 ==========
                "Throw          : Token keyword, Expr argument",                    // throw error
                "Try            : Stmt block, CatchClause handler, Stmt finalizer", // try-catch-finally

                // ========== 其他 ==========
                "With           : Expr object, Stmt body",                          // with (obj) {}
                "Labeled        : Token label, Stmt body",                          // label: statement
                "Debugger       : Token keyword"                                    // debugger;
        ));

        // 生成辅助类
        defineHelperClasses(outputDir);

        System.out.println("AST classes generated successfully in: " + outputDir);
    }

    /**
     * 生成 AST 基类和所有子类
     */
    private static void defineAst(String outputDir, String baseName, List<String> types) throws IOException {
        String path = outputDir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, "UTF-8");

        writer.println("package com.jsparser;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println("/**");
        writer.println(" * JavaScript " + baseName + " AST 节点基类");
        writer.println(" * ");
        writer.println(" * 使用访问者模式支持对 AST 的各种操作（解释执行、代码生成、静态分析等）");
        writer.println(" */");
        writer.println("abstract class " + baseName + " {");

        // 生成 Visitor 接口
        defineVisitor(writer, baseName, types);

        writer.println();

        // 生成所有 AST 子类
        for (String type : types) {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer, baseName, className, fields);
        }

        // 生成抽象 accept 方法
        writer.println();
        writer.println("    /**");
        writer.println("     * 接受访问者");
        writer.println("     * @param visitor 访问者对象");
        writer.println("     * @param <R> 返回类型");
        writer.println("     * @return 访问结果");
        writer.println("     */");
        writer.println("    abstract <R> R accept(Visitor<R> visitor);");

        writer.println("}");
        writer.close();
    }

    /**
     * 生成 Visitor 接口
     */
    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
        writer.println("    /**");
        writer.println("     * 访问者接口");
        writer.println("     * @param <R> 访问方法的返回类型");
        writer.println("     */");
        writer.println("    interface Visitor<R> {");

        for (String type : types) {
            String typeName = type.split(":")[0].trim();
            writer.println("        R visit" + typeName + baseName + "(" + typeName + " " + baseName.toLowerCase() + ");");
        }

        writer.println("    }");
    }

    /**
     * 生成单个 AST 类型
     */
    private static void defineType(PrintWriter writer, String baseName, String className, String fieldList) {
        writer.println();
        writer.println("    /**");
        writer.println("     * " + className + " " + baseName.toLowerCase());
        writer.println("     */");
        writer.println("    static class " + className + " extends " + baseName + " {");

        // 构造函数
        String constructorFields = fieldList;
        if (constructorFields.length() > 64) {
            constructorFields = constructorFields.replace(", ", ",\n                ");
        }
        writer.println("        " + className + "(" + constructorFields + ") {");

        // 初始化字段
        String[] fields = fieldList.split(", ");
        for (String field : fields) {
            String name = field.split(" ")[1];
            writer.println("            this." + name + " = " + name + ";");
        }
        writer.println("        }");

        // accept 方法
        writer.println();
        writer.println("        @Override");
        writer.println("        <R> R accept(Visitor<R> visitor) {");
        writer.println("            return visitor.visit" + className + baseName + "(this);");
        writer.println("        }");

        // 字段声明
        writer.println();
        for (String field : fields) {
            writer.println("        final " + field + ";");
        }

        writer.println("    }");
    }

    /**
     * 生成辅助类（Property, VarDeclarator, SwitchCase, CatchClause）
     */
    private static void defineHelperClasses(String outputDir) throws IOException {
        // Property 类 - 对象字面量的属性
        writeHelperClass(outputDir, "Property", """
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
""");

        // VarDeclarator 类 - 变量声明器
        writeHelperClass(outputDir, "VarDeclarator", """
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
""");

        // SwitchCase 类 - switch 的 case 子句
        writeHelperClass(outputDir, "SwitchCase", """
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
""");

        // CatchClause 类 - catch 子句
        writeHelperClass(outputDir, "CatchClause", """
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
""");
    }

    /**
     * 写入辅助类文件
     */
    private static void writeHelperClass(String outputDir, String className, String content) throws IOException {
        String path = outputDir + "/" + className + ".java";
        PrintWriter writer = new PrintWriter(path, "UTF-8");
        writer.print(content);
        writer.close();
    }
}
