#!/bin/sh

sbt fastOptJS
rm server/app/assets/javascripts/local_js-opt.js
cp localJS/target/scala-2.12/local_js-fastopt.js server/app/assets/javascripts/local_js-opt.js
rm server/app/assets/javascripts/remote_js-opt.js
cp remoteJS/target/scala-2.12/remote_js-fastopt.js server/app/assets/javascripts/remote_js-opt.js
sbt server/compile