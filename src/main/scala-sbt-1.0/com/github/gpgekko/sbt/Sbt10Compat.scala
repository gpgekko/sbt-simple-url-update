package com.github.gpgekko.sbt

import sbt.PathFinder

object Sbt10Compat {
   val SbtIoPath = sbt.io.Path

   def allPaths( pathFinder: PathFinder ): PathFinder = {
      pathFinder.allPaths
   }
}