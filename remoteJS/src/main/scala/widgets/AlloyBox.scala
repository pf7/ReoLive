package widgets

import common.widgets.Ifta.IFTABox
import common.widgets.{Box, GraphBox, OutputArea}
import ifta.backend.{IftaAutomata, Show}
import ifta.{DSL, Feat, NIFTA}
import org.scalajs.dom
import org.scalajs.dom.raw.MouseEvent
import org.scalajs.dom.{EventTarget, html}
import preo.ast.CoreConnector
import preo.backend.Network.Mirrors
import preo.backend.{Automata, Circuit}

import scala.scalajs.js.UndefOr

/**
  * Created by guille on 16/01/2019
  */


class AlloyBox(progr:Box[String], prop:Box[String], errorBox:OutputArea)
  extends Box[String]("Reo Alloy",List(progr)){

  private var box:Block = _
  private var content:String = ""
  private var mirrors:Mirrors = _
  override def get: String = content

  //Indica o número da instância que se pretende visualizar
  private var n_sol = -1
  //#Contra-exemplos
  private var n_ce = -1
  //Propriedade a ser verificada
  private var current_prop = ""

  /**
    * Executed once at creation time, to append the content to the inside of this box
    *
    * @param div     Placeholder that will receive the "append" with the content of the box
    * @param visible is true when this box is initially visible (i.e., expanded).
    */
  override def init(div: Block, visible: Boolean): Unit = {
    box = panelBox(div,visible, buttons=List(
      Left("solve")      -> (()=> solve ,"Obtain Instances"),
      Left("next")      -> (()=> next ,"See next instance")
    ))
      .append("div")
      .attr("id","reoAlloy")

    dom.document.getElementById("Reo Alloy").firstChild.firstChild.firstChild.asInstanceOf[html.Element]
      .onclick = { e : MouseEvent => if (!isVisible) update() else update()}
  }

  /**
    * Block of code that should read the dependencies and:
    *  - update its output value, and
    *  - produce side-effects (e.g., redraw a diagram)
    */
  override def update(): Unit = {
    //Reset ao estado das instâncias obtidas / contra-exemplos
    n_sol = -1
    n_ce = -1
    current_prop = ""

    println("sending request for Alloy servive")
    //RemoteBox.remoteCall("alloyWS", progr.get, process)

    val reo = progr.get

    val msg =
      s"""
         |{
         |  "reo" : "$reo",
         |  "solve" : false,
         |  "check" : false
         |}
        """.stripMargin

    send(msg)
  }

  private def process(reply:String): Block = {
    println("got reply: "+reply)

    reply match{

      case "SAT" => box

      case "UNSAT" =>
        if(n_sol == 0)
          errorBox.message("The model is unsatisfiable, it might be inconsistent.")
        else
          errorBox.message("There are no more instances that satisfy this model.")

        box

      case "CHECK_UNSAT" =>
        if(n_ce == 0)
          errorBox.message("No counterexamples found for the current scope.")
        else
          errorBox.message("There are no more counterexamples for the given scope.")

        box

      case _ => box.html(reply)
    }


  }


  /**
    * Produzir instâncias para o circuito atual.
    */
  private def solve(){
    n_sol = 0
    errorBox.clear

    //RemoteBox.remoteCall("alloyWS", "0" + progr.get, process)
    val reo = progr.get

    val msg =
      s"""
         |{
         |  "reo" : "$reo",
         |  "num" : 0,
         |  "solve" : true,
         |  "check" : false
         |}
        """.stripMargin

    send(msg)
  }

  /**
    * Visualizar instância seguinte, a existir.
    */
  private def next: Unit ={

    if(n_sol < 0){
      errorBox.message("Please press 'solve' before attempting to visualize further instances.")
    }
    else{
      n_sol += 1
      //RemoteBox.remoteCall("alloyWS", n_sol.toString + progr.get, process)
      val reo = progr.get

      val msg =
        s"""
           |{
           |  "reo" : "$reo",
           |  "num" : $n_sol,
           |  "solve" : true,
           |  "check" : false
           |}
        """.stripMargin

      send(msg)
    }

  }

  /**
    * Verificar propriedade, e visualizar um contra-exemplo, se for o caso.
    */
  def check(): Unit ={
    n_ce = 0
    errorBox.clear

    if(prop.get.nonEmpty) {
      current_prop = prop.get
      //RemoteBox.remoteCall("alloyWS", "CHECK__" + current_prop + "__0__" + progr.get, process)
      val reo = progr.get

      val msg =
        s"""
          |{
          |  "reo" : "$reo",
          |  "prop" : "$current_prop",
          |  "num" : 0,
          |  "solve" : false,
          |  "check" : true
          |}
        """.stripMargin

      send(msg)
    }
    else{
      errorBox.message("Write the property to be verified.")
    }
  }

  /**
    * Se existir, apresenta o contra-exemplo seguinte.
    */
  def next_ce(): Unit ={

    if(n_ce < 0 || prop.get != current_prop){
      errorBox.message("Please press 'check' before attempting to visualize further counterexamples.")
    }
    else {
      n_ce += 1
      //RemoteBox.remoteCall("alloyWS", "CHECK__" +  current_prop + "__" + n_ce + "__" + progr.get, process)
      val reo = progr.get

      val msg =
        s"""
           |{
           |  "reo" : "$reo",
           |  "prop" : "$current_prop",
           |  "num" : $n_ce,
           |  "solve" : false,
           |  "check" : true
           |}
        """.stripMargin

      send(msg)
    }

  }

  /**
    * Envia pedido para o web service do ReoAlloy
    * @param msg mensagem
    */
  private def send(msg : String): Unit ={
    RemoteBox.remoteCall("alloyWS", msg, process)
  }

//  try {
//
//      val ...
//
//      iftaAut = Automata[IftaAutomata](progr.get,mirrors)
//
//      var nifta:NIFTA = NIFTA(iftaAut.nifta)
//      var fmInfo =  s"""{ "fm":     "${Show(nifta.fm)}", """ +
//                    s"""  "feats":  "${nifta.iFTAs.flatMap(i => i.feats).mkString("(",",",")")}" }"""
//
//      RemoteBox.remoteCall("ifta", fmInfo, showProducts)
//
//    } catch {
//      case e:Throwable =>
//        errorBox.error(e.getMessage)
//    }



}
