Simple Java Ray Tracer
======================

In 2012 I stumbled upon a blog post by [Barak Cohen](https://plus.google.com/109999908224705846661/posts) titled 'Ray Tracing: A Simple Java, Open Source Implementation'.

It was posted back in 2008 and provided an attached Eclipse project written by Barak Cohen and Gur Dotan.

I have updated the project structure to build using Gradle. Unfortunately the original authors chose to use SWT ([The Standard Widget Toolkit](https://www.eclipse.org/swt/)) for the gui so builds will have to be platform specific. I've added the 64bit swt libs for Windows, Mac and Linux for convenience. The build properties will detect which lib is needed at build time.

To build and run the project simply run the Gradle wrapper in the projects root directory:

```
./gradlew run
```

![alt text](test-render.png "Running under Windows")

On Mac will need `-XstartOnFirstThread`