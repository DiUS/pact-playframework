name := "pact-play-test"

organization := "com.dius"

version := "1.0-SNAPSHOT"

scalaVersion := "2.10.3"


resolvers ++= Seq("spray repo" at "http://repo.spray.io",
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Sonatype OSS Releases"  at "http://oss.sonatype.org/content/repositories/releases/",
  "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository")

publishTo := Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/.m2/repository")))

libraryDependencies ++= Seq(
  "com.dius" %% "pact-model-jvm" % "1.0-SNAPSHOT",
  "com.dius" %% "pact-runner-jvm" % "1.0-SNAPSHOT",
  "com.typesafe.play" %% "play-json" % "2.2.0",
  "com.typesafe.play" %% "play-ws" % "2.3-20131213005945Z",
  "com.typesafe.play" %% "play-test" % "2.3-20131213005945Z",
  "org.specs2" %% "specs2-core" % "2.3.7"
)




