package com.craftinginterpreters.lox;

/**
 * 定义 Lox 语言中各数据类型的比较优先级
 * 
 * 优先级规则（从低到高）：
 * 1. NIL (null) - 最低优先级
 * 2. BOOLEAN (true/false) - 布尔值，false < true
 * 3. NUMBER (Double) - 数字类型，按数值大小比较
 * 4. STRING (String) - 字符串类型，按字典序比较，最高优先级
 * 
 * 设计理念：
 * - 参考 Python 3.x 之前的版本，支持异构类型比较
 * - nil 作为"无值"概念，优先级最低
 * - 数字和字符串分别按自然顺序排序
 * - 布尔值作为特殊的二元值，介于 nil 和数字之间
 */
public enum TypePriority {
    NIL(0, "nil"),
    BOOLEAN(1, "boolean"), 
    NUMBER(2, "number"),
    STRING(3, "string");
    
    private final int priority;
    private final String typeName;
    
    TypePriority(int priority, String typeName) {
        this.priority = priority;
        this.typeName = typeName;
    }
    
    public int getPriority() {
        return priority;
    }
    
    public String getTypeName() {
        return typeName;
    }
    
    /**
     * 根据运行时对象确定其类型优先级
     * 
     * @param value 运行时对象
     * @return 对应的类型优先级
     */
    public static TypePriority getTypePriority(Object value) {
        if (value == null) {
            return NIL;
        }
        if (value instanceof Boolean) {
            return BOOLEAN;
        }
        if (value instanceof Double) {
            return NUMBER;
        }
        if (value instanceof String) {
            return STRING;
        }
        
        // 理论上不应该到达这里，因为 Lox 只支持这四种类型
        throw new IllegalArgumentException("Unknown type: " + value.getClass().getSimpleName());
    }
    
    /**
     * 比较两个类型的优先级
     * 
     * @param other 另一个类型优先级
     * @return 负数表示当前类型优先级更低，0表示相等，正数表示更高
     */
    public int compareTypePriority(TypePriority other) {
        return Integer.compare(this.priority, other.priority);
    }
    
    /**
     * 检查两个对象是否为相同类型
     * 
     * @param left 左操作数
     * @param right 右操作数
     * @return 如果类型相同返回 true
     */
    public static boolean isSameType(Object left, Object right) {
        return getTypePriority(left) == getTypePriority(right);
    }
}