name := "paxos"
version := "0.1"
scalaVersion := "2.13.3"

lazy val basic = (project in file("modules/basic"))
  .settings(Settings.commonSettings)
  .settings(
    scalaVersion := "2.13.3",
    name := "paxos-basic"
  )

lazy val effect = (project in file("modules/effects"))
  .settings(Settings.commonSettings)
  .settings(
    scalaVersion := "2.13.3",
    name := "paxos-effect",
    libraryDependencies ++= Seq(
      "org.typelevel"     %% "cats-effect"    % "2.1.3",
      "io.chrisdavenport" %% "log4cats-core"  % "1.1.1",
      "io.chrisdavenport" %% "log4cats-slf4j" % "1.1.1"
    )
  )
  .dependsOn(basic)
  .aggregate(basic)

lazy val example = (project in file("modules/example"))
  .settings(Settings.commonSettings)
  .settings(
    scalaVersion := "2.13.3",
    name := "paxos-example",
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.2.3"
    )
  )
  .aggregate(basic, effect)
  .dependsOn(basic, effect)

lazy val root = (project in file("."))
  .aggregate(example)
  .settings(
    scalaVersion := "2.13.3",
    moduleName := "paxos"
  )
