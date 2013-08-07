AstTransformations
==================

I have tried to keep the number of tools needed to compile this code minimal.  Any working groovy 2.1.x installation should work fine to compile this code.  However, this does mean you will need to use groovyc, javac, etc. to compile the code.

The other reason for using groovyc directly is that (as you see below) I use different compiler settings to compile code differently to make the magic of AST transformations work.  Some of these options are simply easier to show using groovyc directly, and some are very new and don't appear to be supported yet by some build tools.

Compiling the Ast Transformations
=================================
Before compiling any code, create an ```output``` directory.  For this README, I will assume that it is named "output" and is a subfolder of the project folder.

To compile the ast transformations, switch to the transformations directory and execute the following:

```groovyc -d ../output *.groovy```

To compile the java code for benchmarking numeric integrations, switch to the java directory and execute the following:

```javac -d ../output/ *.java```

To compile the ShowIntegrate, ShowAsyncPair, and UseUnless demos, switch to the demo directory and execute the following:

```groovyc -d ../output *.groovy```

To compile the Person class, switch to the sql directory.  You will need to have a persons table set up in a relational database, configure the connection information in the Person.groovy file, and make sure that your jdbc driver is on the classpath when you invoke groovyc.  To compile the class using Postgresql, I used the following command:

```groovyc -cp postgresql-8.4-701.jdbc3.jar:../output/ -d ../output/ Person.groovy```

To compile the Noisy class, switch to the configscript directory.  The following command line will tell groovyc to apply the NoPrinln annotation to the Noisy class and place the class file in the output folder"

```groovyc -cp ../output/ -d ../output --configscript config.gconfig Noisy.groovy```

Invoking the Groovy Code
========================
To invoke the code I set an environment variable called ```THE_CLASSATH``` which has the current directory and the groovy library I can then change to the output directory and invoke the code like this:

```java -cp $THE_CLASSATH Noisy```

to invoke the Noisy demo.  Replace Noisy with ShowAsyncPair, ShowIntegrate, UseUnless to show those demos.  To run the java integration code, you can simply run:

```java JavaIntegrate```

and then compare the time it takes to run to the time it takes the ShowIntegrate demo to run (the groovy version of the same code).

Finally, to view the methods that were generated when Person.groovy was compiled, make sure you are in the output directory and run the following command to view the methods in the compiled Person class:

```javap Person```

Viewing Transformations in groovyConsole
========================================
You can view the transformations that will happen at compile time using the groovyConsole.  To do this, change to the directory where the script is and invoke the groovyConsole, passing in the classpath of the compiled transformations.  For example, to see the transformation of the ShowAsyncPair.groovy file, change to the demo directory and then execute this command:

```groovyConsole -cp ../output/ ShowAsyncPair.groovy```