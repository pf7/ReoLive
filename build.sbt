ThisBuild / scalaVersion := "2.12.6"
val circeVersion = "0.9.3"

lazy val common_settings = Seq(
  scalaVersion := "2.12.4",
  libraryDependencies ++= Seq(
    "junit" % "junit" % "4.12",
    "org.choco-solver" % "choco-solver" % "4.0.6",
    "org.scala-lang" % "scala-compiler" % scalaVersion.value,
    "org.ow2.sat4j" % "org.ow2.sat4j.core" % "2.3.5" withSources() withJavadoc(),
//    "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.6",
    // Last stable release
    "org.scalanlp" %% "breeze" % "0.13.2",
    // Native libraries are not included by default. add this if you want them (as of 0.7)
    // Native libraries greatly improve performance, but increase jar sizes.
    // It also packages various blas implementations, which have licenses that may or may not
    // be compatible with the Apache License. No GPL code, as best I know.
    "org.scalanlp" %% "breeze-natives" % "0.13.2",
    // The visualization library is distributed separately as well.
    // It depends on LGPL code
    "org.scalanlp" %% "breeze-viz" % "0.13.2"
    //"edu.mit.csail.sdg" % "alloy" % "4.2" from "file:./lib/alloy4.2_2015-02-22.jar"
  ),
  unmanagedJars in Compile ++= Seq(
      baseDirectory.value / "../lib/alloy4.2_2015-02-22.jar"
      )
)


lazy val server = (project in file("server"))
  .dependsOn(localJS, remoteJS)
  .enablePlugins(PlayScala)
  .disablePlugins(ScalaJSPlugin, WorkbenchPlugin)
  .settings(
    common_settings,
    name := "server",
    version := "1.0",
    scalacOptions ++= Seq("-unchecked", "-deprecation","-feature"),
    resolvers ++= Seq(
      "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases",
      "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/",
      "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
    ),
    libraryDependencies ++= Seq(
      "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.0",
      "com.typesafe.play" %% "play" % "2.6.11",
      "javax.xml.bind" % "jaxb-api" % "2.3.0",
      // "com.typesafe.play" %% "play-json" % "2.6.9",
      jdbc , ehcache , ws , specs2 % Test , guice
    ),
    //    unmanagedResourceDirectories in Test +=  Seq(baseDirectory ( _ /"target/web/public/test" )),
    unmanagedSourceDirectories in Compile ++= Seq(
      baseDirectory.value / "../lib/preo/src/main/scala",
      baseDirectory.value / "../lib/hprog/src/main/scala",
      baseDirectory.value / "../lib/ifta/src/main/scala",
      baseDirectory.value / "../lib/virtuoso/src/main/scala",
      baseDirectory.value / "../lib/reoalloy/src/main/scala"
    )
  )

lazy val javascript_settings = Seq(
//  Compile/run := {},
  version := "1.0",
  scalacOptions ++= Seq("-unchecked", "-deprecation","-feature"),
  //    hello := {println("Hello World!")},
  libraryDependencies ++= Seq(
    "be.doeraene" %%% "scalajs-jquery" % "0.9.1",
    /////
    "org.scala-js" %%% "scalajs-dom" % "0.9.1",
    "com.lihaoyi" %%% "scalatags" % "0.6.7",
    "org.singlespaced" %%% "scalajs-d3" % "0.3.4",
    "org.scala-lang.modules" %%% "scala-parser-combinators" % "1.1.0",
    "io.circe" %% "circe-core" % circeVersion, // json parser
    "io.circe" %% "circe-generic" % circeVersion,
    "io.circe" %% "circe-parser" % circeVersion
  ),
  unmanagedSourceDirectories in Compile ++= Seq(
    baseDirectory.value / "../lib/preo/src/main/scala",
    baseDirectory.value / "../lib/hprog/src/main/scala",
    baseDirectory.value / "../lib/ifta/src/main/scala",
    baseDirectory.value / "../lib/virtuoso/src/main/scala",
    baseDirectory.value / "../lib/reoalloy/src/main/scala"
  )
)



lazy val commonJS = (project in file("commonJS"))
  .enablePlugins(ScalaJSPlugin, WorkbenchPlugin)
  .disablePlugins(PlayScala)
  .settings(
    common_settings,
    name := "common_js",
    javascript_settings
  )

lazy val localJS = (project  in file("localJS"))
  .dependsOn(commonJS)
  .enablePlugins(ScalaJSPlugin)
  .disablePlugins(PlayScala)
  .settings(
    common_settings,
    name := "local_js",
    javascript_settings,
  )

lazy val remoteJS= (project in file("remoteJS"))
  .dependsOn(commonJS)
  .enablePlugins(ScalaJSPlugin)
  .disablePlugins(PlayScala)
  .settings(
    common_settings,
    name := "remote_js",
    javascript_settings,
    resolvers ++= Seq(
      "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
    ),
    libraryDependencies ++= Seq(
      // "com.typesafe.play" %% "play" % "2.6.11"
      "com.typesafe.play" %% "play-json" % "2.6.9"
    )
  )



// todo: add here a task for, when compiling the server, copying the content into the app/...
//
//lazy val reotools = (project in file("."))
//  .aggregate(server)
//  .settings(
//    Compile/mainClass := server/Compile/mainClass
//  )
