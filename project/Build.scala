import sbt._
import sbt.Keys._

object ProjectBuild extends Build {

  val Settings = Project.defaultSettings ++ Seq(
    resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases",
    libraryDependencies += "com.typesafe.akka" % "akka-actor" % "2.0",
    scalacOptions ++= Seq("-unchecked", "-deprecation")
  )

  lazy val root = Project(
    id = "root",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      name := "GWLPR in Scala",
      organization := "sgwlpr",
      version := "0.1-SNAPSHOT",
      scalaVersion := "2.9.1"
    )
  ) dependsOn(login, registration)
 
  lazy val codegen = Project(
    id = "codegen",
    base = file("codegen"),
    settings = Project.defaultSettings ++ Seq(
      libraryDependencies += "org.clapper" %% "scalasti" % "0.5.8"
    )
  )
  
lazy val framework = Project(
    id = "framework", base = file("framework"),
    settings = Settings
  ) 

  lazy val packets = Project(
    id = "packets",
    base = file("packets")
  ) dependsOn(framework)

  lazy val login = Project(
    id = "login", base = file("login")
  ) dependsOn(framework, packets)

  lazy val registration = Project(
    id = "registration", base = file("registration")
  ) dependsOn(framework, packets, login)
}
