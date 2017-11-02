enablePlugins(ScalaJSPlugin, WorkbenchPlugin)

name := "reotools"

version := "1.0"

scalaVersion := "2.11.8"

// more warnings
scalacOptions ++= Seq("-unchecked", "-deprecation","-feature")

libraryDependencies ++= Seq(
  "junit" % "junit" % "4.12",
  "org.choco-solver" % "choco-solver" % "3.3.1-j7",
  "org.scala-lang" % "scala-compiler" % scalaVersion.value,
  "org.scala-lang.modules" %%% "scala-parser-combinators" % "1.0.5",
  /////
  "org.scala-js" %%% "scalajs-dom" % "0.9.1",
  "com.lihaoyi" %%% "scalatags" % "0.6.1"
)

libraryDependencies += "org.singlespaced" %%% "scalajs-d3" % "0.3.4"

// including source code from preo (needed for JavaScript compilation)
unmanagedSourceDirectories in Compile += baseDirectory.value / "lib/preo/src/main/scala"