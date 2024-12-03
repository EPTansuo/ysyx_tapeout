import mill._, scalalib._
import os.Path
//import $file.dependencies.cde.build

// object cde extends CDE
//   trait CDE extends millbuild.dependencies.cde.common.CDEModule with ScalaModule {
//     def scalaVersion: T[String] = T("2.12.12")
//     override def millSourcePath = os.pwd / "dependencies" / "cde" / "cde"
//   }

object ysyx_23060246 extends ScalaModule {
  def scalaVersion = "2.12.10"
  override def millSourcePath = os.pwd
  def scalaOptions = Seq(
    "-language:reflectiveCalls",
    "-deprecation",
    "-feature",
    "-Xcheckinit",
    "-P:chiselplugin:genBundleElements",
    //"-Xsource:2.13"
  )

  override def ivyDeps = Agg(
    ivy"edu.berkeley.cs::chisel3:3.5.0",
    //ivy"edu.berkeley.cs::rocketchip:1.2.6",
    //ivy"org.chipsalliance::chisel:6.3.0",
  )
  
  override def scalacPluginIvyDeps = Agg(
    ivy"edu.berkeley.cs:::chisel3-plugin:3.5.0",
    //ivy"org.chipsalliance:::chisel-plugin:6.3.0"
  )
   //override def moduleDeps = super.moduleDeps ++ Seq(cde)
} 
