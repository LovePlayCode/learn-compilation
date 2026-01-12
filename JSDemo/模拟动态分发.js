// ==========================================
// 1. 模拟动态分发 (替代 if/else)
// ==========================================

// 定义 True 对象：执行第一个函数 (then)
const TrueObject = {
  ifElse: (thenBranch, elseBranch) => thenBranch(),
};

// 定义 False 对象：执行第二个函数 (else)
const FalseObject = {
  ifElse: (thenBranch, elseBranch) => elseBranch(),
};

// 辅助函数：将 JS 原生布尔值转为我们的对象
// (在纯粹的 Lox 实现中，比较操作符 < 会直接返回 TrueObject 或 FalseObject)
const toBoolObj = (nativeBool) => (nativeBool ? TrueObject : FalseObject);

// ==========================================
// 2. 模拟尾递归 (替代 for/while)
// ==========================================

const loop = (i, currentSum) => {
  // 这里的 (i < 1000) 产生了原生布尔值，我们转为对象
  // 核心在于：后续的逻辑没有任何 if 语句，全靠 condition.ifElse
  const condition = toBoolObj(i < 10000);

  return condition.ifElse(
    // Then 分支 (True): 继续循环
    // 这是一个尾调用：函数结束时直接返回对自身的调用
    () => loop(i + 1, currentSum + i),

    // Else 分支 (False): 结束循环，返回结果
    () => currentSum
  );
};

// ==========================================
// 3. 执行
// ==========================================

// 初始状态：i=0, sum=0
const result = loop(0, 0);
console.log("sum:", result); // 输出 499500
