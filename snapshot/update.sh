#!/bin/sh

cd ../
sbt fullOptJS
cp localJS/src/main/resources/js/codemirror/mode/preo/preo.js snapshot/js/codemirror/mode/preo
cp localJS/src/main/resources/css/codemirror/codemirror.css   snapshot/css/codemirror
cp localJS/target/scala-2.12/local_js-opt.js*                 snapshot/js/

echo "Done. Open 'index.html' to try the current version."