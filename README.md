# goCompiler
Interface that enables remote compilation of code written in C language.


The project consists of an Android app and a remote HTTP server placed in AWS EC2 instance.

The app is written in Java and enables users to upload one or multiple files, set compilation flags, choose the main file, see the results of compilation and optionally fix all errors and warnings using a built-in code editor.
The HTTP server is written in Go. It receives a POST request from the client app, compiles the received files using chosen flags and returns the results of compilation to client.

Here are some screenshots from the app:

![Initial screen](/compiler_1.png)
![Choose main file](/compiler_5.png)
![Choose flags](/compiler_2.png)
![List of errors and warnings](/compiler_3.png)
![Code editor](/compiler_4.png)
![Success](/compiler_6.png)
