package com.craftinginterpreters.lox;

/**
 * 类型比较功能测试类
 * 测试新的异类型数字转换比较逻辑
 */
public class TypeComparisonTest {
    
    public static void main(String[] args) {
        System.out.println("=== TypeComparator 异类型数字转换比较测试 ===\n");
        
        // 测试数字转换成功的情况
        testNumericConversionSuccess();
        
        // 测试数字转换失败的情况
        testNumericConversionFailure();
        
        // 测试边界情况
        testEdgeCases();
        
        // 测试同类型比较（确保向后兼容）
        testSameTypeComparison();
        
        System.out.println("\n=== 所有测试完成 ===");
    }
    
    /**
     * 测试数字转换成功的情况
     */
    private static void testNumericConversionSuccess() {
        System.out.println("1. 测试数字转换成功的情况:");
        
        // 字符串数字 vs 数字
        testComparison("123", 456.0, "字符串数字 vs 数字");
        testComparison(123.0, "456", "数字 vs 字符串数字");
        testComparison("123", "456", "字符串数字 vs 字符串数字");
        
        // 布尔值 vs 数字 (false=0, true=1)
        testComparison(false, 0.5, "false vs 0.5");
        testComparison(true, 0.5, "true vs 0.5");
        testComparison(false, true, "false vs true");
        
        System.out.println();
    }
    
    /**
     * 测试数字转换失败的情况
     */
    private static void testNumericConversionFailure() {
        System.out.println("2. 测试数字转换失败的情况 (所有比较应返回 false):");
        
        // 混合字符串无法转换为数字
        testComparison("A13", 13.0, "混合字符串 A13 vs 数字 13");
        testComparison(13.0, "A13", "数字 13 vs 混合字符串 A13");
        testComparison("13A", "A13", "混合字符串 13A vs A13");
        
        // 纯字母字符串
        testComparison("hello", 42.0, "字符串 hello vs 数字 42");
        testComparison(42.0, "world", "数字 42 vs 字符串 world");
        
        // nil 参与比较
        testComparison(null, 42.0, "nil vs 数字 42");
        testComparison(42.0, null, "数字 42 vs nil");
        
        System.out.println();
    }
    
    /**
     * 测试边界情况
     */
    private static void testEdgeCases() {
        System.out.println("3. 测试边界情况:");
        
        // 空字符串和空白字符串
        testComparison("", 0.0, "空字符串 vs 数字 0");
        testComparison("   ", 0.0, "空白字符串 vs 数字 0");
        
        // 特殊数字格式
        testComparison("3.14", 2.71, "字符串 3.14 vs 数字 2.71");
        testComparison("-123", 456.0, "负数字符串 -123 vs 正数 456");
        testComparison("0", false, "字符串 0 vs false");
        testComparison("1", true, "字符串 1 vs true");
        
        System.out.println();
    }
    
    /**
     * 测试同类型比较（确保向后兼容）
     */
    private static void testSameTypeComparison() {
        System.out.println("4. 测试同类型比较 (向后兼容性):");
        
        // 数字比较
        testComparison(123.0, 456.0, "数字 123 vs 456");
        
        // 字符串比较
        testComparison("apple", "banana", "字符串 apple vs banana");
        
        // 布尔值比较
        testComparison(false, true, "布尔值 false vs true");
        
        // nil 比较
        testComparison(null, null, "nil vs nil");
        
        System.out.println();
    }
    
    /**
     * 执行单个比较测试
     */
    private static void testComparison(Object left, Object right, String description) {
        System.out.printf("  %s:\n", description);
        System.out.printf("    %s < %s = %b\n", stringify(left), stringify(right), 
                         TypeComparator.less(left, right));
        System.out.printf("    %s > %s = %b\n", stringify(left), stringify(right), 
                         TypeComparator.greater(left, right));
        System.out.printf("    %s <= %s = %b\n", stringify(left), stringify(right), 
                         TypeComparator.lessEqual(left, right));
        System.out.printf("    %s >= %s = %b\n", stringify(left), stringify(right), 
                         TypeComparator.greaterEqual(left, right));
        System.out.println();
    }
    
    /**
     * 将对象转换为字符串表示，用于测试输出
     */
    private static String stringify(Object object) {
        if (object == null) return "nil";
        if (object instanceof String) return "\"" + object + "\"";
        return object.toString();
    }
}