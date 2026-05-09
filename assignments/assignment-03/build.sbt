name := "assignment-3"
version := "0.1"
scalaVersion := "3.3.3" // O 2.13.12

val pekkoVersion = "1.0.2"

libraryDependencies ++= Seq(
  "org.apache.pekko" %% "pekko-actor-typed" % pekkoVersion,
  "org.apache.pekko" %% "pekko-stream"      % pekkoVersion,
  "org.apache.pekko" %% "pekko-slf4j"       % pekkoVersion,
  "ch.qos.logback"    % "logback-classic"   % "1.4.12"
)