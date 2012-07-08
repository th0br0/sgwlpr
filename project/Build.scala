import sbt._
import sbt.Keys._

object ProjectBuild extends Build {

  

  val Settings = Project.defaultSettings ++ Seq(
    resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases",
    resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
    scalaVersion := "2.9.2",
    
    libraryDependencies ++= Seq(
      "com.typesafe.akka" % "akka-actor" % "2.0.2",
      "com.novus" %% "salat-core" % "1.9.0"
    ),
    scalacOptions ++= Seq("-unchecked", "-deprecation")
  )

  lazy val root = Project(
    id = "root",
    base = file("."),
    settings = Settings ++ Seq(
      name := "SGWLPR",
      organization := "sgwlpr",
      version := "0.1-SNAPSHOT",
      scalaVersion := "2.9.2"
    )
  ) dependsOn(login, registration, outpost)
 
  lazy val codegen = Project(
    id = "codegen",
    base = file("codegen"),
    settings = Project.defaultSettings ++ Seq(
      scalaVersion := "2.9.1",
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

  lazy val outpost = Project(
    id = "outpost", base = file("outpost"), settings = Settings
  ) dependsOn(framework, packets, login, db)


}
