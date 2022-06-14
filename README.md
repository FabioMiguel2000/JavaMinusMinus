# Compilers Project

## GROUP 2C: 

- NAME1: Bruno Pinheiro , NR1: 201705562, GRADE1:20, CONTRIBUTION1: 20%
- NAME2: Fabio Huang, NR2: 201806829, GRADE2: 20, CONTRIBUTION2: 30%
- NAME3: Gabriel Alves, NR2: 201709532, GRADE3: 20, CONTRIBUTION3: 25%
- NAME4: Ivo Ribeiro, NR2: 201307718, GRADE4: 20, CONTRIBUTION4: 25%


 


GLOBAL Grade of the project: <0 to 20>

 


## Summary

This project aims to apply the knowledge and principles learnt during the course of Compilers, by designing and building a working compiler for a simple, but realistic high-level programming language called Java-- (a subset of Java). The structure of this compiler is divided into the following phases:
1. Parsing: the input file is parsed, and an AST is generated.
2. Semantic analysis: AST is analyzed, a symbol table is generated.
3. High-level and low-level optimization: high-level optimizations are applied to the AST, OLLIR is generated, and low-level optimizations are applied in OLLIR.
4. Code generation: Jasmin code is generated from the input OLLIR.
 
## Semantic Analysis


### Symbol Table

- [X] Has information about imports and the declared class
- [X] Has information about extends, fields and methods
- [X] Has information about the parameters and local variables of each method

### Type Verification

- [ ] Verify if variable names used in the code have a corresponding declaration, either as a local variable, a method parameter or a field of the class (if applicable).
- [X] Operands of an operation must types compatible with the operation (e.g. int + boolean is an error because + expects two integers.)
- [X] Array cannot be used in arithmetic operations (e.g. array1 + array2 is an error)
- [X] Array access is done over an array
- [ ] Array access index is an expression of type integer
- [X] Type of the assignee must be compatible with the assigned (an_int = a_bool is an error)
- [ ] Expressions in conditions must return a boolean (if(2+3) is an error)

### Function Verification

- [ ] When calling methods of the class declared in the code, verify if the types of arguments of the call are compatible with the types in the method declaration
- [ ] In case the method does not exist, verify if the class extends another class and report an error if it does not. Assume the method exists in one of the super classes, and that is being correctly called
- [ ] When calling methods that belong to other classes other than the class declared in the code, verify if the classes are being imported
 

## Code Generation

A Java-- Parser was developed to perform membership checking, to see if the code belongs to the language. This is achieved by tokenizing the source code and producing tokens that will be checked using CFG. By the end of the parser phase, an Abstract Syntax Tree (AST) will be generated annotating the nodes with some extra information. The AST will be then used to create a Symbol Table, which will contain the information of the class and its methods (return type, parameters, local variables) and fields. This Symbol Table will be helpful during the Semantic Analysis Phase (where semantic errors will be checked) and during the Optimization Phase where OLLIR code is produced and code is optimized. Finally, using the OLLIR code generated, Jasmin is used to generating JVM Bytecodes.

### OLLIR 
- [x] Basic structure of code (imports, class and fields)
- [x] Invocation of functions (invokestatic and invokevirtual)
- [x] Arithmetic expression (+, -, *, /)
- [x] Boolean operations (!, <)
- [x] Conditional instructions (if-else)
- [x] Loops (while)
- [x] Arrays

### JASMIN 
- [x] Basic structure of code (imports, class and fields)
- [x] Invocation of functions (invokestatic, invokevirtual and invokespecial)
- [x] Arithmetic expression (+, -, *, /)
- [x] Boolean operations (!, <)
- [x] Control flow (if-else, while)
- [x] Arrays
- [x] Limit stacks and locals

### Optimizations
- [ ] Constant folding
- [ ] Optimized JVM instructions
- [ ] Simple dead code elimination (ifs/whiles with a constant condition)

# Pros
There is nothing too special about our project...

# Cons
- Did not implement optimizations
- Generation of OLLIR code could be improved
- Jasmin could be improved
- Semantic analysis is not yet complete


For this project, you need to install [Java](https://jdk.java.net/), [Gradle](https://gradle.org/install/), and [Git](https://git-scm.com/downloads/) (and optionally, a [Git GUI client](https://git-scm.com/downloads/guis), such as TortoiseGit or GitHub Desktop). Please check the [compatibility matrix](https://docs.gradle.org/current/userguide/compatibility.html) for Java and Gradle versions.

## Project setup

There are three important subfolders inside the main folder. First, inside the subfolder named ``javacc`` you will find the initial grammar definition. Then, inside the subfolder named ``src`` you will find the entry point of the application. Finally, the subfolder named ``tutorial`` contains code solutions for each step of the tutorial. JavaCC21 will generate code inside the subfolder ``generated``.

## Compile and Running

To compile and install the program, run ``gradle installDist``. This will compile your classes and create a launcher script in the folder ``./build/install/comp2022-00/bin``. For convenience, there are two script files, one for Windows (``comp2022-00.bat``) and another for Linux (``comp2022-00``), in the root folder, that call tihs launcher script.

After compilation, a series of tests will be automatically executed. The build will stop if any test fails. Whenever you want to ignore the tests and build the program anyway, you can call Gradle with the flag ``-x test``.

## Test

To test the program, run ``gradle test``. This will execute the build, and run the JUnit tests in the ``test`` folder. If you want to see output printed during the tests, use the flag ``-i`` (i.e., ``gradle test -i``).
You can also see a test report by opening ``./build/reports/tests/test/index.html``.

## Checkpoint 1
For the first checkpoint the following is required:

1. Convert the provided e-BNF grammar into JavaCC grammar format in a .jj file
2. Resolve grammar conflicts, preferably with lookaheads no greater than 2
3. Include missing information in nodes (i.e. tree annotation). E.g. include the operation type in the operation node.
4. Generate a JSON from the AST

### JavaCC to JSON
To help converting the JavaCC nodes into a JSON format, we included in this project the JmmNode interface, which can be seen in ``src-lib/pt/up/fe/comp/jmm/ast/JmmNode.java``. The idea is for you to use this interface along with the Node class that is automatically generated by JavaCC (which can be seen in ``generated``). Then, one can easily convert the JmmNode into a JSON string by invoking the method JmmNode.toJson().

Please check the JavaCC tutorial to see an example of how the interface can be implemented.

### Reports
We also included in this project the class ``src-lib/pt/up/fe/comp/jmm/report/Report.java``. This class is used to generate important reports, including error and warning messages, but also can be used to include debugging and logging information. E.g. When you want to generate an error, create a new Report with the ``Error`` type and provide the stage in which the error occurred.


### Parser Interface

We have included the interface ``src-lib/pt/up/fe/comp/jmm/parser/JmmParser.java``, which you should implement in a class that has a constructor with no parameters (please check ``src/pt/up/fe/comp/CalculatorParser.java`` for an example). This class will be used to test your parser. The interface has a single method, ``parse``, which receives a String with the code to parse, and returns a JmmParserResult instance. This instance contains the root node of your AST, as well as a List of Report instances that you collected during parsing.

To configure the name of the class that implements the JmmParser interface, use the file ``config.properties``.

### Compilation Stages 

The project is divided in four compilation stages, that you will be developing during the semester. The stages are Parser, Analysis, Optimization and Backend, and for each of these stages there is a corresponding Java interface that you will have to implement (e.g. for the Parser stage, you have to implement the interface JmmParser).


### config.properties

The testing framework, which uses the class TestUtils located in ``src-lib/pt/up/fe/comp``, has methods to test each of the four compilation stages (e.g., ``TestUtils.parse()`` for testing the Parser stage). 

In order for the test class to find your implementations for the stages, it uses the file ``config.properties`` that is in root of your repository. It has four fields, one for each stage (i.e. ``ParserClass``, ``AnalysisClass``, ``OptimizationClass``, ``BackendClass``), and initially it only has one value, ``pt.up.fe.comp.SimpleParser``, associated with the first stage.

During the development of your compiler you will update this file in order to setup the classes that implement each of the compilation stages.
