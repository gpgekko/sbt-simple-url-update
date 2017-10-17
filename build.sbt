import sbt.URL

sbtPlugin := true


// Basic application information.
// ---------------------------------------
organization := "com.github.gpgekko"
organizationName := "gpgekko"
organizationHomepage := Some(new URL("https://github.com/gpgekko"))
name := "sbt-simple-url-update"
homepage := Some(new URL("https://github.com/gpgekko/sbt-simple-url-update"))
startYear := Some(2017)


// Scala version to use.
// ---------------------------------------
crossSbtVersions := Seq("0.13.16", "1.0.2")


// Plugins.
// ---------------------------------------
addSbtPlugin("com.typesafe.sbt" % "sbt-web" % "1.4.3")


// Scripted.
// ---------------------------------------
scriptedLaunchOpts ++= Seq(s"-Dplugin.version=${version.value}")
scriptedBufferLog := false


publishMavenStyle := false

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))