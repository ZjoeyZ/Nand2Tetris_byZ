
 ### 介绍
 本项目基于《Nand2Tetirs》(《计算机系统要素》)第9章之后的内容实现。
 许多人把这本书当做初学者的入门读物来推荐，那这和让初学者去看《计算机科学导论》有什么区别？  
 在我看来《计算机系统要素》其实是一本贯穿CS本科学习的任务书，每当你完成特定的本科课程你就有能力完成这个书上的一个任务，点亮地图中的一个角落。 
    
* 学完了《数字电路和逻辑设计》，你就有能力完成本书的1-3章，自己动手实现加法器，存储器、计数器。  
* 学完了《计算机组成原理》，你就有能力完成本书的4-5章，实现的CPU和内存，搭建起一个简单的计算机。并且别看你实现的这个计算机结构简单，但你掌握了计算机的工作流程和输入输出，在你的脑海里搭建起了一个计算机的工作模型，这个是无价的。  
* 学完了《汇编语言》，并且会一门高级语言（C、Ptyhon、Java...），你就有能力完成本书的6章，为你之前实现的计算机的机器语言设计一门汇编语言来简化编程，为了能够实现汇编语言到机器语言的转会，你用自己学会的高级语言实现一个汇编器。  
* 掌握了一点《数据结构》，你就有能力完成本书的7-8章实现一个虚拟机，感受抽象的美。虚拟机为用户提供了一个数据结构——栈。用户向虚拟机输入虚拟机语言，虚拟机就让栈内数据变化。虚拟机的用户只能看到栈，看到自己发出一条指令后，栈里的数据就会变化。而作虚拟机的设计者，你要把用户发出的虚拟机机语言转化为汇编语言，把用户以为的对栈的操作，落实到具体对哪个内存的操作，简直不要太cool。  
* 学完《汇编原理》，你就可以完成9-11张，实现自己的高级语言和它的编译器。 学完了《操作系统》，你就可以完成第12章，为你的高级语言赋予操作系统级的能力。

就像我说的这本书就是一本任务书，在每个章节作者都描述了你要完成什么任务，帮助你理解要实现的东西是什么，如何完成这个任务，在你实现之后，还有大量辅助的工具来用来进行测试，甚至学完之后你还可以贪得无厌，去翻翻作者的源代码，看他是怎么实现这些工具的。
 
本书豆瓣地址：https://book.douban.com/subject/1998341/  
本书官网地址：https://www.nand2tetris.org/  
本书作者教学视频：https://www.coursera.org/learn/build-a-computer  
### 我完成的项目
* [1.Assembler](./1.Assembler) 第 6 章的翻译器。
* [2.VirtuMachine](./2.VirtualMachine) 第 7-8 章的虚拟机。
* [3.Compile](./3.Compile) 第 9-11 章的翻译器(没有做完，我在选择在 [CompileCourse](./CompileCourse) 将它继续完善。

