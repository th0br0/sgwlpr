import sbt._
import sbt.Keys._

object ProjectBuild extends Build {

  

  val Settings = Project.defaultSettings ++ Seq(
    resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases",
  //  resolvers += "repo.novus rels" at "http://repo.novus.com/releases/",
    resolvers += "Novus Snapshots" at "http://repo.novus.com/snapshots/",
    scalaVersion := "2.9.2",
    
    libraryDependencies ++= Seq(
      "com.typesafe.akka" % "akka-actor" % "2.0.2",
      "com.novus" %% "salat-core" % "0.0.8-SNAPSHOT"
    ),
    scalacOptions ++= Seq("-unchecked", "-deprecation")
  )

  lazy val root = Project(
    id = "root",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      resolvers += "Novus Snapshots" at "http://repo.novus.com/snapshots/",
      name := "SGWLPR",
      organization := "sgwlpr",
      version := "0.1-SNAPSHOT",
      scalaVersion := "2.9.2"
    )
  ) dependsOn(login, registration)
 
  lazy val codegen = Project(
    id = "codegen",
    base = file("codegen"),
    settings = Project.defaultSettings ++ Seq(
      scalaVersion := "2.9.2",
      libraryDependencies += "org.clapper" %% "scalasti" % "0.5.8"
    )
  )
  
lazy val framework = Project(
    id = "framework", base = file("framework"),
    settings = Settings
  ) 

  lazy val packets = Project(
    id = "packets",
    base = file("packets"),
    settings = Settings
  ) dependsOn(framework)

  lazy val db = Project(
    id = "db", base = file("db"), settings = Settings
  ) dependsOn(framework)

  lazy val login = Project(
    id = "login", base = file("login"), settings = Settings
  ) dependsOn(framework, packets, db)

  lazy val registration = Project(
    id = "registration", base = file("registration"), settings = Settings
  ) dependsOn(framework, packets, login, db)


}
