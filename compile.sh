#!/bin/sh

# clean files that will be overridden by symbolic links
cd localJS/src/main/resources/js/
rm local_js-fastopt.js local_js-fastopt.js.map local_js-opt.js \
   local_js-opt.js.map remote_js-opt.js remote_js-opt.js.map
cd ../../../../../server/public
rm -rf *
cd ../../

# create symbolic links
## to have the JS at the localJS resources (html)
cd localJS/src/main/resources/js/
ln -s ../../../../target/scala-2.12/local_js-fastopt.js     # used by local index
ln -s ../../../../target/scala-2.12/local_js-fastopt.js.map # used by local index
ln -s ../../../../target/scala-2.12/local_js-opt.js         # used by snapshot + server
ln -s ../../../../target/scala-2.12/local_js-opt.js.map     # used by snapshot + server
ln -s ../../../../../remoteJS/target/scala-2.12/remote_js-opt.js     # used by snapshot + server
ln -s ../../../../../remoteJS/target/scala-2.12/remote_js-opt.js.map # used by snapshot + server

## to run the html provided by the play-server
cd ../../../../../server/public
ln -s ../../snapshot/content
ln -s ../../snapshot/css
ln -s ../../snapshot/favicon.ico
ln -s ../../snapshot/favicon.svg
ln -s ../../snapshot/fonts
ln -s ../../snapshot/index.html
ln -s ../../localJS/src/main/resources/js  # JS directly from  
cd ../../

# warning
echo "--------------------------------------------------------------"
echo "- Run snapshot/update.sh if you want to update the JS and CS -"
echo "-   before publishing. Not needed by the server.             -"
echo "- Compiling everything - the first time may take a while.    -"
echo "--------------------------------------------------------------"

# Compile all JavaScript (JS), both in localJS and in RemoteJS
sbt localJS/fastOptJS fullOptJS server/compile

echo ""
echo "- Static version: open 'localJS/src/main/resources/index.html'"
echo "- Dynamic version: run the server using 'sbt server/run', and access it via http://localhost:9000."