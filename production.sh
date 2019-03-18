echo "Make sure you first modify the 'server/conf/application.conf' file."
echo "Modify the session key 'play.http.secret.key'."
echo ""
echo "## Removing previous standalone"
rm -rf server/target/universal/server-1.0*
echo "## Building standalone - assuming 'compile.sh' was executed beforehand."
sbt dist
echo "## Unzipping standalone"
cd server/target/universal
unzip server-1.0.zip
cd ../../../
echo "## Executing  the server - './server/target/univeral/server-1.0/bin/server"
./server/target/universal/server-1.0/bin/server -J-Xms2048m -J-Xmx2048m
