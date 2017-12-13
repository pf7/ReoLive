Reo Live
========================

This project aims at compiling a set of Java/Scala based Reo related tools, using a web frontend to interact with them.
A snapshot of Reo Live can be found in https://reolanguage.github.io/ReoLive/snapshot. 

It uses ScalaJS to generate JavaScript, and imports independent Reo related tools using git submodules feature.


How to compile and try-out
==============
* Pull the git submodules:

> git submodule update --init

* Generate the JavaScript code using sbt:

> sbt \~fastOptJS

* Open the `index-dev.html` file, already linking to the generated JavaScript code:

> open src/main/resources/index-dev.html



