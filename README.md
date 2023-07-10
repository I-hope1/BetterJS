# BetterJS

a mod for mindustry.

## 添加内容
### $AX
> 添加`$AX`变量，支持获取所有使用属性（包括私有）：

#### `wa`（包装对象）
将js原始对象包装成新rhino（mod）对象

#### `efm`（方法优先）
> ```java
> public class Test {
> 	public static final int a = 0;
> 	public static void a() {
> 		System.out.println(a);
> 	}
> }
> ```
> ```javascript
> Test.a // 0
> $AX.efm(Test, Test => Test.a) // void a();
> ```

*****************************
#### `ef`（字段优先，rhino默认就是这样的）
> ```java
> public class Test {
> 	public static  int apublic  = 0;
> 	private static int aprivate = 1;
> }
> ```
> ```javascript
> Test.apublic // 0
> Test.aprivate // undefined
> $AX.ef(Test, Test => Test.aprivate) // 1
> ```

*****************************
#### 优化js代码的运行效率
> TODO
*****************************

#### 支持设置final值（内联变量改了也没用）

> ```java
> public class Test {
> 	public final int a = 0;
> 	public void print() {
> 		System.out.println(a);
> 	}
> }
> ```
> ```javascript
> let test = new Test()
> test.a = 2023;
> test.print(); // 2023
> ```

*****************************
#### 支持创建枚举
创建新的枚举（也就可以访问私有构造器）
> ```java
> public enum Category{
> 	// xxxx
> }
> ```
> ```javascript
> // 作者: I hope...
> const all = Array.from(Category.all)
> const Cat = $AX.wa(Category)
> 
> function newCat(name) {
> 	exports[name] = new Cat(name, all.length)
> 	all.push(exports[name])
> 	return exports[name]
> }
> 
> // 创建3个新的Category
> newCat("simple") 
> newCat("simple2")
> newCat("simple3")
> 
> Cat.all = all
> ```
*****************************

### 继承提升
> 使用`$AX.extend`<br>
> 添加继承private（boot类不行）<br>
> 可以继承包私有<br>
> 不过私有构造器暂时不行
**例子:**
> ```java
> import arc.util.Log;
>
> public class TestPrivate {
> 	void a()  {
>		Log.info("a");
>	}
> }
> ```
> ```javascript
> let obj = $AX.extend(TestPrivate, {
>     a() {
>         Log.info("before a");
>         this.super$a(); // a
>         Log.info("after a");
>     }
> })
> ```
*****************************
#### 接口访问
> 使用`$AX.in`<br>
> 可以实现私有接口（例如：TypeParser）<br>
> 不过也可以使用`$AX.extend`<br>

*****************************
*****************************
*****************************

## 使用案例
#### 复写解析json的ContentParser
> ```javascript
> const { TypeParser } = ContentParser
> const req = $AX.efm(() => Blocks.arc.requirements)
> 
> $AX.efm(() => {
> 
> let { parser } = Vars.mods;
> let parserMap = parser.parsers;
> 
> const ctype = ContentType.item;
> parserMap.put(ctype, $AX.in(TypeParser, {
>   parse(mod, name, value){
>     let item;
>     if(parser.locate(ctype, name) != null){
>       item = parser.locate(ctype, name);
>       parser.readBundle(ctype, name, value);
>     }else{
>       parser.readBundle(ctype, name, value);
>       item = parser.make(parser.resolve(value.getString("type", null), Item), mod + "-" + name);
>       value.remove("type");
>     }
>     parser.currentContent = item;
>     // 打印物品名
>     print(item)
>     parser.read(() => parser.readFields(item, value));
>     return item;
>   }
> }))
> 
> })
> ```
#### 新的逻辑
> ```javascript
> $AX.efm(LogicOp, Op => {
>   let newArr = Op.all.slice();
>   function op(name, symbol, opl) {
>     let cType = new Op(name, newArr.length, symbol,
>       $AX.extend(Op.OpLambda2, {get:opl})
>     );
>     newArr.push(cType)
>   }
>   op("mp", "***", (a, b) => a * b * b * 10)
>   Op.$VALUES = Op.all = newArr
> })
> ```