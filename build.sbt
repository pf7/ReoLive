// scalajs ////////////////////////////////

//enablePlugins(ScalaJSPlugin, WorkbenchPlugin)

// all ///////////////////////////////////

name := "reotools"

version := "1.0"

scalaVersion := "2.12.4"

// more warnings
scalacOptions ++= Seq("-unchecked", "-deprecation","-feature")

libraryDependencies ++= Seq(
  "junit" % "junit" % "4.12",
//  "org.choco-solver" % "choco-solver" % "3.3.1-j7",
  "org.choco-solver" % "choco-solver" % "4.0.6",
  "org.scala-lang" % "scala-compiler" % scalaVersion.value,
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.6"
)

// scalajs //////////////////////////////////////

//libraryDependencies ++= Seq(
//  "be.doeraene" %%% "scalajs-jquery" % "0.9.1",
//  "org.scala-js" %%% "scalajs-dom" % "0.9.1",
//  "com.lihaoyi" %%% "scalatags" % "0.6.7"
//)

//libraryDependencies += "org.singlespaced" %%% "scalajs-d3" % "0.3.4"

// play2 //////////////////////////////////////


lazy val `reotools` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play" % "2.6.11"
)

libraryDependencies ++= Seq( jdbc , ehcache , ws , specs2 % Test , guice )

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )


// including source code from preo (needed for JavaScript compilation)
unmanagedSourceDirectories in Compile += baseDirectory.value / "lib/preo/src/main/scala"