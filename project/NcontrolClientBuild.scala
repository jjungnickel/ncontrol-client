import sbt.Keys._
import sbt._

object NcontrolClientBuild extends Build {

  lazy val ncontrol = Project(
    id = "ncontrol-client",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      name := "ncontrol-client",
      organization := "com.jungnickel",
      version := "0.1",
      scalaVersion := "2.10.4",
      libraryDependencies +=
        "net.databinder.dispatch" %% "dispatch-core" % "0.11.1"
    )
  )
}
