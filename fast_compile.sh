#!/bin/sh

# Compile all JavaScript (JS), both in localJS and in RemoteJS
sbt fastOptJS

# Copy JS from localJS to the server
cp localJS/target/scala-2.12/local_js-fastopt.js \
   server/app/assets/javascripts/local_js-opt.js
cp localJS/target/scala-2.12/local_js-fastopt.js.map \
   server/app/assets/javascripts/

# Copy JS from remoteJS to the server
cp remoteJS/target/scala-2.12/remote_js-fastopt.js \
   server/app/assets/javascripts/remote_js-opt.js
cp remoteJS/target/scala-2.12/remote_js-fastopt.js.map \
   server/app/assets/javascripts/

# Copy stylesheet from localJS to server
cp localJS/src/main/resources/css/style.css \
   server/app/assets/stylesheets/

# Compile the server, after having the updated JS
sbt server/compile

echo "\n - Run the server using 'sbt server/run', and access it via http://localhost:9000."