Simple Java Ray Tracer
======================

[![Build Status](https://travis-ci.org/SingingBush/java-ray-tracer.svg?branch=master)](https://travis-ci.org/SingingBush/java-ray-tracer)

This repository was initialised with some Java code I came across back in 2012. It was a blog post by [Barak Cohen](https://plus.google.com/109999908224705846661/posts) titled 'Ray Tracing: A Simple Java, Open Source Implementation'. It was posted back in 2008 and provided an attached Eclipse project written by Barak Cohen and Gur Dotan.

The project has been updated to use Gradle and is now separated into sub-modules. This should make it easier to make sense of, as each module has a specific purpose.

The original authors chose to use SWT ([The Standard Widget Toolkit](https://www.eclipse.org/swt/)) for the gui so I have added the 64-bit swt libs for Windows, Mac and Linux (GTK). The Gradle build will use the appropriate jar when compiling; however this makes the application platform specific.

Some work has been done toward creating a replacement UI using JavaFX.

To build and run the project simply run the Gradle wrapper in the projects root directory:

```
./gradlew run
```

![alt text](test-render.png "Running under Windows")

On Mac will need `-XstartOnFirstThread` when running the SWT based UI.