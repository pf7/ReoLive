Reo Live
========================

This project aims at compiling a set of Java/Scala based Reo related tools, using a web frontend to interact with them.
A snapshot of Reo Live can be found in https://reolanguage.github.io/ReoLive/snapshot. 

It uses ScalaJS to generate JavaScript, and imports independent Reo related tools using git submodules feature.


How to compile local javascript
==============
* Pull the git submodules:

> git submodule update --init

* Run the compilation script:

> ./compile.sh


How to run the framework
=====

* For the static version, open the `index.html` file, already linking to the generated JavaScript code:

> open localJS/src/main/resources/index.html

* For the server-based version, start the server using sbt and open localhost:9000

> sbt server/run
> open http://localhost:9000



