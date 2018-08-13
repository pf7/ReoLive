Reo Live
========================

This project aims at compiling a set of Java/Scala based Reo related tools, using a web frontend to interact with them.
A snapshot of Reo Live can be found in https://reolanguage.github.io/ReoLive/snapshot. 

It uses ScalaJS to generate JavaScript, and imports independent Reo related tools using git submodules feature.


How to compile local javascript and try-out
==============
* Pull the git submodules:

> git submodule update --init

* Generate the JavaScript code using sbt:

> sbt ~localJS/fastOptJS

* Copy the `local_js-fastopt.js` file in the target folder, 
 into the `resources/js` folder

* Open the `index-dev.html` file, already linking to the generated JavaScript code:

> open src/main/resources/index-dev.html

How to compile client-server project and try-out
==============

* Compile files using the script

> fast_compile.sh

* Start the server using sbt:

>sbt \~server/run





