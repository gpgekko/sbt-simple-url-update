package com.github.gpgekko.sbt

import sbt.PathFinder

object Sbt10Compat {
   object DummyPath

   val SbtIoPath: DummyPath.type = DummyPath

   def allPaths( pathFinder: PathFinder ): PathFinder = {
      pathFinder.***
   }
}