addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.6.15")

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.21")

addSbtPlugin("com.lihaoyi" % "workbench" % "0.4.1")

logLevel := Level.Warn

resolvers += Resolver.sbtPluginRepo("releases")


//resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
//
//resolvers += "Typesafe Simple Repository" at
//  "http://repo.typesafe.com/typesafe/simple/maven-releases/"

