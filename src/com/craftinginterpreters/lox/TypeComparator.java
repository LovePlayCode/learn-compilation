package com.craftinginterpreters.lox;

/**
 * 混合类型比较器
 * 
 * 负责处理 Lox 语言中不同类型之间的比较操作，支持以下比较规则：
 * 
 * 1. 同类型比较：
 *    - 数字：按数值大小
 *    - 字符串：按字典序（Unicode 码点）
 *    - 布尔值：false < true
 *    - nil：nil == nil
 * 
 * 2. 异类型比较：
 *    - 按类型优先级：nil < boolean < number < string
 *    - 例如：nil < false < true < -100 < 0 < 100 < "a" < "z"
 * 
 * 3. 特殊情况：
 *    - 任何值与 nil 比较时，nil 总是较小（除了 nil == nil）
 *    - 数字与字符串比较时，所有数字都小于所有字符串
 */
public class TypeComparator {
    
    /**
     * 比较两个对象的大小
     * 
     * @param left 左操作数
     * @param right 右操作数
     * @return 负数表示 left < right，0表示相等，正数表示 left > right
     * @throws RuntimeError 如果比较过程中发生错误
     */
    public static int compare(Object left, Object right) {
        TypePriority leftType = TypePriority.getTypePriority(left);
        TypePriority rightType = TypePriority.getTypePriority(right);
        
        // 如果类型相同，使用类型内部的比较逻辑
        if (leftType == rightType) {
            return compareWithinType(left, right, leftType);
        }
        
        // 不同类型，尝试数字转换比较
        Integer numericResult = compareAsNumbers(left, right);
        if (numericResult != null) {
            return numericResult;
        }
        
        // 数字转换失败，返回0表示不可比较（所有比较操作将返回false）
        return 0;
    }
    
    /**
     * 相同类型内部的比较逻辑
     * 
     * @param left 左操作数
     * @param right 右操作数
     * @param type 类型优先级
     * @return 比较结果
     */
    private static int compareWithinType(Object left, Object right, TypePriority type) {
        switch (type) {
            case NIL:
                // nil == nil，总是相等
                return 0;
                
            case BOOLEAN:
                // false < true
                boolean leftBool = (Boolean) left;
                boolean rightBool = (Boolean) right;
                return Boolean.compare(leftBool, rightBool);
                
            case NUMBER:
                // 按数值大小比较
                double leftNum = (Double) left;
                double rightNum = (Double) right;
                return Double.compare(leftNum, rightNum);
                
            case STRING:
                // 按字典序比较
                String leftStr = (String) left;
                String rightStr = (String) right;
                return leftStr.compareTo(rightStr);
                
            default:
                // 理论上不会到达这里
                throw new IllegalArgumentException("Unknown type: " + type);
        }
    }
    
    /**
     * 尝试将对象转换为数字
     * 
     * @param value 要转换的对象
     * @return 转换成功返回 Double，失败返回 null
     */
    private static Double tryConvertToNumber(Object value) {
        if (value == null) return null;
        
        if (value instanceof Double) {
            return (Double) value;
        }
        
        if (value instanceof Boolean) {
            // 布尔值转换：false = 0, true = 1
            return ((Boolean) value) ? 1.0 : 0.0;
        }
        
        if (value instanceof String) {
            String str = ((String) value).trim();
            if (str.isEmpty()) return null; // 空字符串无法转换
            
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException e) {
                return null; // 转换失败
            }
        }
        
        return null; // 其他类型无法转换
    }
    
    /**
     * 异类型数字比较
     * 
     * @param left 左操作数
     * @param right 右操作数  
     * @return 转换成功返回比较结果，失败返回 null 表示不可比较
     */
    private static Integer compareAsNumbers(Object left, Object right) {
        Double leftNum = tryConvertToNumber(left);
        Double rightNum = tryConvertToNumber(right);
        
        if (leftNum == null || rightNum == null) {
            return null; // 转换失败，不可比较
        }
        
        return Double.compare(leftNum, rightNum);
    }
    
    /**
     * 检查两个对象是否可以进行比较
     * 在当前实现中，所有 Lox 类型都可以相互比较
     * 
     * @param left 左操作数
     * @param right 右操作数
     * @return 总是返回 true
     */
    public static boolean isComparable(Object left, Object right) {
        // 在我们的设计中，所有类型都可以比较
        return true;
    }
    
    /**
     * 大于比较：left > right
     */
    public static boolean greater(Object left, Object right) {
        TypePriority leftType = TypePriority.getTypePriority(left);
        TypePriority rightType = TypePriority.getTypePriority(right);
        
        // 如果类型相同，使用类型内部的比较逻辑
        if (leftType == rightType) {
            return compareWithinType(left, right, leftType) > 0;
        }
        
        // 不同类型，尝试数字转换比较
        Integer numericResult = compareAsNumbers(left, right);
        if (numericResult != null) {
            return numericResult > 0;
        }
        
        // 数字转换失败，返回 false
        return false;
    }
    
    /**
     * 大于等于比较：left >= right
     */
    public static boolean greaterEqual(Object left, Object right) {
        TypePriority leftType = TypePriority.getTypePriority(left);
        TypePriority rightType = TypePriority.getTypePriority(right);
        
        // 如果类型相同，使用类型内部的比较逻辑
        if (leftType == rightType) {
            return compareWithinType(left, right, leftType) >= 0;
        }
        
        // 不同类型，尝试数字转换比较
        Integer numericResult = compareAsNumbers(left, right);
        if (numericResult != null) {
            return numericResult >= 0;
        }
        
        // 数字转换失败，返回 false
        return false;
    }
    
    /**
     * 小于比较：left < right
     */
    public static boolean less(Object left, Object right) {
        TypePriority leftType = TypePriority.getTypePriority(left);
        TypePriority rightType = TypePriority.getTypePriority(right);
        
        // 如果类型相同，使用类型内部的比较逻辑
        if (leftType == rightType) {
            return compareWithinType(left, right, leftType) < 0;
        }
        
        // 不同类型，尝试数字转换比较
        Integer numericResult = compareAsNumbers(left, right);
        if (numericResult != null) {
            return numericResult < 0;
        }
        
        // 数字转换失败，返回 false
        return false;
    }
    
    /**
     * 小于等于比较：left <= right
     */
    public static boolean lessEqual(Object left, Object right) {
        TypePriority leftType = TypePriority.getTypePriority(left);
        TypePriority rightType = TypePriority.getTypePriority(right);
        
        // 如果类型相同，使用类型内部的比较逻辑
        if (leftType == rightType) {
            return compareWithinType(left, right, leftType) <= 0;
        }
        
        // 不同类型，尝试数字转换比较
        Integer numericResult = compareAsNumbers(left, right);
        if (numericResult != null) {
            return numericResult <= 0;
        }
        
        // 数字转换失败，返回 false
        return false;
    }
    
    /**
     * 获取比较操作的描述信息，用于调试和错误报告
     * 
     * @param left 左操作数
     * @param right 右操作数
     * @param operator 操作符
     * @return 描述字符串
     */
    public static String getComparisonDescription(Object left, Object right, String operator) {
        TypePriority leftType = TypePriority.getTypePriority(left);
        TypePriority rightType = TypePriority.getTypePriority(right);
        
        return String.format("Comparing %s(%s) %s %s(%s)", 
            leftType.getTypeName(), stringify(left),
            operator,
            rightType.getTypeName(), stringify(right));
    }
    
    /**
     * 将对象转换为字符串表示，用于调试输出
     */
    private static String stringify(Object object) {
        if (object == null) return "nil";
        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        if (object instanceof String) {
            return "\"" + object + "\"";
        }
        return object.toString();
    }
}