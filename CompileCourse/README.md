# jack语言（java实现）
jack 语言是我《计算机系统要素》这本书中学会的一门类java语言。
主要用来学习编译器的基本原理。   
这门语言特征是：  
* 面向对象：简单的面向对象语言，但暂时不支持继承，对象属性完全封装(相当于 java 的 private 属性)，只能通过方法访问。  
* 静态类型：支持 int、boolean、char 基础数据类型，数组和字符串用自定义类的方式支持。
* 表达式：支持 加减乘除、与或非异或，大于小于等于，取负数取反，暂不支持运算符的优先级，需要加上括号。  

### 构建和开发
获取 CompileCourse 文件夹后，可以基于源代码构建一个项目，比如IDEA项目。  
本项目我使用的是jdk 1.8_191版本开发

### 运行方法
在运行之前：
* 要设置好本机的java环境；     
* 想不通过IDEA而直接用命令行命令执行，请设置好CLASSPATH,让java能够找到该项目中的类。    
* 使用命令行工具，执行 java REPL, 这将启动一个REPL界面，在里面输入源码,或者直接复制测试文件下的源码粘贴,。   
* 使用IDEA工具，直接在IDEA项目中打开 REPL.java 然后点击run按钮启动一个REPL界面，同样输入源码，即可返回结果。
* 当然还有其他几个类（Parser, Tokenizer）中有 main 函数可以运行进行单独测试，路径在下面

### Jack语言结构
 * [进入介绍 README](img/README.md)

### 项目中主要的示例代码
* [REPL.java](src/REPL.java) 读取-计算-打印 循环器。
* [Compiler.java](src/Compiler.java) 编译器
* [Parser.java](src/Parser.java) 语法分析器
* [Tokenizer.java](src/Tokenizer.java) 分词器
* [ASTEvaluator.java](src/Tokenizer.java) 解释器，对AST遍历求值。
* [type](src/type) 包含 token 类型和 AST结点 类型
* [node](src/node) 包含 token 和 AST结点 
---
## 注意
该目录下的代码会随时更新。
