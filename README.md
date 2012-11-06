# Agent Smith

Agent Smith is an agent implementation.

An agent is a particular class that has access to an implementation of the [Instrumentation](http://docs.oracle.com/javase/1.5.0/docs/api/java/lang/instrument/Instrumentation.html) interface, available since java 5.

Smith redefines your class files as soon as you change them, even if they are stored into jars.

As in Matrix, redefinition will make your classes experience deja vu.

## What is it?

Smith is a combination of an agent implementation, a file monitor and an (optional) agent loader.

The feature Smith offers is commonly known as "hotswap": the difference with hotswap is that you don't need to start your jvm (and therefore your container) in debug mode. Smith is a pure java application that monitors only the class and jar folders you've told it to monitor, therefore having a very small footprint.

To make sense, you'll use an instance of Smith for every application you want to monitor.

If you want to give it a quick try, read the How to quickly test Smith paragraph.

## What do I need to run it?

Smith requires at least java 5. You can still write and compile your applications with older versions of java, but you need to run them with java 5+. If in your development environment (a bank?) you cannot use a more recent version of java, even if just for development or pre-production purposes, then Smith (and agents) is not for you.
How is the code? Are there any limitations?

Current JDK implementation of the Instrumentation interface does NOT support changing a class SCHEMA, meaning you cannot add/remove fields and methods. All you can do is changing your existing methods implementations. This is particularly useful if you are using some framework like Struts, where what you want to change are Actions and they all have just the "execute" method (probably).

When you'll try to make Smith redefine a class file whose schema has changed, it will complain by logging an exception. Don't worry: nothing is crashed or b0rked: as the Instrumentation javadoc says, **"If this method throws an exception, no classes have been redefined."**, so you will keep on seeing your classes behave like you haven't modified them, meaning this time you need to restart the application (as you have always done). Anyway, Smith will keep on working, eventually redefining newer classes.

If Smith will crash due some bugs, again don't worry: most probably your application (or container) won't be affected as Smith runs on its own, and your applications don't know nothing about it.

## License

Smith is covered by the Apache Software License rel. 2.0

## A brief history

Smith was born with a different aim. I was missing an hotswap-like feature in Eclipse and I've built my own. Then [I was told](http://echo.nextapp.com/site/node/3925) that actually Eclipse has such feature and that was very easy to enable it: I was just missing that damn button!

Then [someone else](http://echo.nextapp.com/site/node/3925) told me that Smith could be useful on production servers, in order to avoid starting the whole server in debug mode but enabling hotswap only for some applications.

And here we are.

## Documentation

### Javadoc

The javadoc is available here
How to quickly test Smith

1. Download the appropriate latest version of Smith from here: if you are using java5, then download the java5 version. Otherwise download the java6+ version
2. Download the test classes and unpack the zip file
3. Compile them with `javac Main.java`
4. Run them with `java -javaagent:${PATH_TO_SMITH_JAR}=${PATH_TO_CURRENT_FOLDER} Main`
5. Every path must be absolute. You should see two messages repeating every one second
```
Bar: I'm doing something
Bar$Foo: What else???
```
6. Now open another console and go to the test classes source folder
7. Edit file Bar.java and change the two text printed by System.out.println
8. Compile Bar.java with javac
9. Check the first console: do you see the messages changed?

Easier done than said, uh? :)

### How to compile Smith

Assuming you've correctly installed your jdk and ant (hey! aren't you a developer?)

1. Checkout the sources from the repository (read the instructions)
2. (Only if you are building the java6 version) Mind to put "tools.jar" in the classpath. It's available in your jdk lib folder (${JDK}/lib/tools.jar)
3. Optionally edit file build.properties to select which version of Smith do you prefer
4. Run the command `ant dist`
5. Check the dist folder for smith-${VERSION}.jar

### How to set up Tomcat to use Smith (java5 version)

Ok, there are different servlet containers out there. Since I use Tomcat, I'll describe what to do to configure it. If you are using a different container, feel free to contribute to the project with your documentation.

1. Place Smith jar into ${TOMCAT}/common/lib2
2. Open file ${TOMCAT>/bin/catalina.sh for editing
3. At the end of the first forty lines of comments, add the following line
```
JAVA_OPTS="$JAVA_OPTS -javaagent:${TOMCAT}/common/lib/smith-${VERSION}.jar=classes=${PATH_TO_WEBAPP}/WEB-INF/classes, jars=${PATH_TO_WEBAPP}/WEB-INF/lib, period=1000"
```
5. Start Tomcat and begin to code, change your classes and see what happens
