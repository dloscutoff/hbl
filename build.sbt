enablePlugins(ScalaJSPlugin)

name := "Half-Byte Lisp"

val hblVersion = "0.1.2"
version := hblVersion

scalaVersion := "3.0.2"

Global / onChangedBuildSource := ReloadOnSourceChanges

// This is an application with a main method
scalaJSUseMainModuleInitializer := true
