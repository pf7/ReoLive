
lazy val common_settings = Seq(
  scalaVersion := "2.12.4",
  libraryDependencies ++= Seq(
    "junit" % "junit" % "4.12",
    "org.choco-solver" % "choco-solver" % "4.0.6",
    "org.scala-lang" % "scala-compiler" % scalaVersion.value,
//    "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.6",
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
      "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.6",
      "com.typesafe.play" %% "play" % "2.6.11",
      jdbc , ehcache , ws , specs2 % Test , guice
    ),
    //    unmanagedResourceDirectories in Test +=  Seq(baseDirectory ( _ /"target/web/public/test" )),
    unmanagedSourceDirectories in Compile += baseDirectory.value / "../lib/preo/src/main/scala"
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
    "org.scala-lang.modules" %%% "scala-parser-combinators" % "1.1.1"
  ),
  unmanagedSourceDirectories in Compile += baseDirectory.value / "../lib/preo/src/main/scala"
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
    javascript_settings
  )

lazy val remoteJS= (project in file("remoteJS"))
  .dependsOn(commonJS)
  .enablePlugins(ScalaJSPlugin)
  .disablePlugins(PlayScala)
  .settings(
    common_settings,
    name := "remote_js",
    javascript_settings
  )



// todo: add here a task for, when compiling the server, copying the content into the app/...
//
//lazy val reotools = (project in file("."))
//  .aggregate(server)
//  .settings(
//    Compile/mainClass := server/Compile/mainClass
//  )
