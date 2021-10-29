name := "Half-Byte Lisp"

val hblVersion = "0.1"
version := hblVersion

scalaVersion := "3.0.2"

Global / onChangedBuildSource := ReloadOnSourceChanges

libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.10"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.10" % "test"

//This makes a file called hbl-0.1.jar right in the hbl directory
assembly / assemblyOutputPath := new java.io.File(s"hbl-$hblVersion.jar")

Test / logBuffered := false
