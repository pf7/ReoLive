package services

import preo.frontend.mcrl2.Model

import java.io.{File, PrintWriter}
import scala.sys.process.ProcessLogger
import sys.process._


object MCRL2Bind {
  private val mcrl2path = "/Applications/mCRL2.app/Contents/bin/"
//  private val mcrl2path = "/usr/bin/"

  def savepbes(): (Int, String) = {
    val id = Thread.currentThread().getId
    val stdout = new StringBuilder
    val stderr = new StringBuilder
    val status = s"${mcrl2path}lts2pbes /tmp/model_$id.lts /tmp/modal_$id.pbes --formula=/tmp/modal_$id.mu".!(ProcessLogger(stdout append _, stderr append _))
    if(status == 0) (status, stdout.toString)
    else (status, stderr.toString)
  }

  def solvepbes(): String = {
    val id = Thread.currentThread().getId
    s"${mcrl2path}pbes2bool /tmp/modal_$id.pbes".!!
  }

  def solvepbes2(): String = {
    val id = Thread.currentThread().getId
    s"${mcrl2path}mcrl22lps /tmp/model_$id.mcrl2 /tmp/model_$id.lps".!
    s"lps2pbes -c -f {name}/{name}.prop.mcf {name}/{name}.lps {name}/{name}.prop.pbes')"

      // .... under construction...

    s"${mcrl2path}pbessolve -v --file=/tmp/model_$id.lps /tmp/modal_$id.pbes".!!
//    s"${mcrl2path}lps2lts /tmp/modal_$id.pbes.evidence.lps /tmp/modal_$id.pbes.evidence.lts".!!
    s"${mcrl2path}lpspp /tmp/modal_$id.pbes.evidence.lps".!!

  }

  def storeInFile(model:Model): Unit = {
    val id = Thread.currentThread().getId
    val file = new File(s"/tmp/model_$id.mcrl2")
    file.setExecutable(true)
    val pw = new PrintWriter(file)
    pw.write(model.toString)
    pw.close()
  }

  def generateLPS(): Int = {
    val id = Thread.currentThread().getId
    s"${mcrl2path}mcrl22lps /tmp/model_$id.mcrl2 /tmp/model_$id.lps".!
  }

  def generateLTS(): Int = {
    val id = Thread.currentThread().getId
    generateLPS()
    s"${mcrl2path}lps2lts /tmp/model_$id.lps /tmp/model_$id.lts".!
  }

  // TODO: minimisation not in use while experimenting with formulas
  def minimiseLTS(): Int = {
    val id = Thread.currentThread().getId
    generateLTS()
//    s"${mcrl2path}ltsconvert -ebranching-bisim /tmp/model1_$id.lts /tmp/model_$id.lts".!
  }

  def callLtsGraph(): Unit = {
    val id = Thread.currentThread().getId
    minimiseLTS()
    s"${mcrl2path}ltsgraph /tmp/model_$id.lts".run()
  }

}
