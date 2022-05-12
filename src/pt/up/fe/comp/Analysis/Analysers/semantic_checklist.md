# Semantic Analysis Checklist

The following are the analyses that we will test and that must report an error.

### Symbol Table

* Has information about imports and the declared class
* Has information about extends, fields and methods
* Has information about the parameters and local variables of each method

### Type Verification

- [ ] Verify if variable names used in the code have a corresponding declaration, either as a local variable, a method parameter or a field of the class (if applicable).
- [ ] Operands of an operation must types compatible with the operation (e.g. int + boolean is an error because + expects two integers.)
- [ ] Array cannot be used in arithmetic operations (e.g. array1 + array2 is an error)
- [X] Array access is done over an array
- [X] Array access index is an expression of type integer
- [ ] Type of the assignee must be compatible with the assigned (an_int = a_bool is an error)
- [ ] Expressions in conditions must return a boolean (if(2+3) is an error)

### Function Verification

- [ ] When calling methods of the class declared in the code, verify if the types of arguments of the call are compatible with the types in the method declaration
- [ ] In case the method does not exist, verify if the class extends another class and report an error if it does not. Assume the method exists in one of the super classes, and that is being correctly called
- [ ] When calling methods that belong to other classes other than the class declared in the code, verify if the classes are being imported
