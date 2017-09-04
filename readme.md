Reo Tools
========================

This project is the home of *Reo Live*, and aims at compiling a set of Java/Scala based Reo related tools.
It is starting as a restructured version of [parameterised-connectors (reojs branch)](https://github.com/joseproenca/parameterised-connectors/tree/reojs).
A snapshot of Reo Live can be found temporarily in http://jose.proenca.org/reolive/. 

It uses ScalaJS to generate JavaScript, and imports independent Reo related tools using git submodules feature.


How to compile and try-out
==============
* Pull the git submodules:

> git submodule update --init

* Generate the JavaScript code using sbt:

> sbt \~fastOptJS

* Open the `index-dev.html` file, already linking to the generated JavaScript code:

> open src/main/resources/index-dev.html



