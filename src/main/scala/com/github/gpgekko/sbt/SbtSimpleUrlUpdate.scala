package com.github.gpgekko.sbt

import com.typesafe.sbt.web.Compat
import com.typesafe.sbt.web.PathMapping
import com.typesafe.sbt.web.SbtWeb
import com.typesafe.sbt.web.pipeline.Pipeline
import sbt.Keys._
import sbt.Task
import sbt._

object Import {
   val simpleUrlUpdate: TaskKey[Pipeline.Stage] = taskKey[Pipeline.Stage]("Update assets url in static css or js files with in asset pipeline.")

   val simpleUrlUpdateAlgorithms: SettingKey[Seq[String]] = settingKey[Seq[String]]("Types of checksum used in the digest pipeline to generate.")
}

object SbtSimpleUrlUpdate extends AutoPlugin {

   override def requires: SbtWeb.type = SbtWeb

   override def trigger = AllRequirements

   val autoImport: Import.type = Import

   import SbtWeb.autoImport._
   import WebKeys._
   import autoImport._

   override def projectSettings: Seq[Setting[_]] = Seq(
      simpleUrlUpdateAlgorithms in simpleUrlUpdate := Seq("md5"),
      includeFilter in simpleUrlUpdate := "*.css" || "*.js",
      excludeFilter in simpleUrlUpdate := HiddenFileFilter,
      resourceManaged in simpleUrlUpdate := webTarget.value / simpleUrlUpdate.key.label,
      simpleUrlUpdate := simpleURLUpdateFiles.value
   )

   private def updatePipeline(mappings: Seq[PathMapping], algorithm: String): String => String = {
      val reversePathMappings = mappings.map { case (k, v) => (v, k) }.toMap

      def checksummedPath(path: String): String = {
         val pathFile = sbt.file(path)
         reversePathMappings.get(path + "." + algorithm) match {
            case Some(file) => (pathFile.getParentFile / (IO.read(file) + "-" + pathFile.getName)).getPath
            case None => path
         }
      }
      val assetVersions = mappings.map {
         case (_, path) => path.replaceAll("\\\\","/") -> checksummedPath(path).replaceAll("\\\\","/")
      }.distinct.filterNot {
         case (originalPath, newPath) => originalPath == newPath
      }

      Function.chain(
         assetVersions.map {
            case (originalPath, newPath) => (content: String) => content.replaceAll(originalPath, newPath)
         }
      )
   }

   def simpleURLUpdateFiles: Def.Initialize[Task[Pipeline.Stage]] = Def.task {
      val algorithmsValue = (simpleUrlUpdateAlgorithms in simpleUrlUpdate).value
      val excludeFilterValue = (excludeFilter in simpleUrlUpdate).value
      val includeFilterValue = (includeFilter in simpleUrlUpdate).value
      val streamsValue = streams.value
      val label = simpleUrlUpdate.key.label
      val targetDir = (resourceManaged in simpleUrlUpdate).value

      mappings =>
         val updateMappings = mappings.filter(f => !f._1.isDirectory && includeFilterValue.accept(f._1) && !excludeFilterValue.accept(f._1))

         SbtWeb.syncMappings(
            Compat.cacheStore(streamsValue, label),
            updateMappings,
            targetDir
         )

         for {
            algorithm <- algorithmsValue
            (file, _) <- updateMappings
         } yield {
            IO.write(file, updatePipeline(mappings, algorithm)(IO.read(file)))
         }

         mappings
   }
}