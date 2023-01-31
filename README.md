# BetterJS
a mod for mindustry.
## content
1. 添加`$AX`变量，支持获取所有使用属性：
- `wa`（包装对象）
- `efm`（方法优先）
```java
public class Test {
	public static final int a = 0;
	public static void a() {
		System.out.println(a);
	}
}
```
```javascript
Test.a // 0
$AX.efm(Test, Test => Test.a) // void a();
```

- `ef`（字段优先（rhino默认就是这样的））
```java
public class Test {
	public static int apublic = 0;
	private static int aprivate = 1;
}
```
```javascript
Test.apublic // 0
Test.aprivate // undefined
$AX.ef(Test, Test => Test.aprivate) // 1
```

2. 优化js代码的运行效率
3. 支持设置final值（内联变量改了也没用），支持创建枚举
```java
public class Test {
	public final int a = 0;
	public void print() {
		System.out.println(a);
	}
}
```
```javascript
let test = new Test()
test.a = 2023;
test.print(); // 2023
```
