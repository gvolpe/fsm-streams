import Dependencies._
import com.scalapenos.sbt.prompt.SbtPrompt.autoImport._
import com.scalapenos.sbt.prompt._
import com.typesafe.sbt.packager.docker._

ThisBuild / scalaVersion := "2.13.4"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.gvolpe"

promptTheme := PromptTheme(
  List(
    text("[sbt] ", fg(105)),
    text(_ => "fsm-streams", fg(15)).padRight(" λ ")
  )
)

val nixDockerSettings = List(
  name := "sbt-nix-fsm-streams",
  dockerCommands := Seq(
    Cmd("FROM", "base-jre:latest"),
    Cmd("COPY", "1/opt/docker/lib/*.jar", "/lib/"),
    Cmd("COPY", "2/opt/docker/lib/*.jar", "/app.jar"),
    ExecCmd("ENTRYPOINT", "java", "-cp", "/app.jar:/lib/*", "com.gvolpe.fsm-streams.Hello")
  )
)

lazy val root = (project in file("."))
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(DockerPlugin)
  .settings(
    licenses += ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt")),
    scalacOptions += "-Ymacro-annotations",
    testFrameworks += new TestFramework("munit.Framework"),
    libraryDependencies ++= Seq(
      CompilerPlugins.betterMonadicFor,
      CompilerPlugins.contextApplied,
      CompilerPlugins.kindProjector,
      fs2Core,
      monocleCore,
      monocleMacro,
      newtype,
      munitCore       % Test,
      munitScalaCheck % Test
    )
  )
  .settings(nixDockerSettings: _*)
