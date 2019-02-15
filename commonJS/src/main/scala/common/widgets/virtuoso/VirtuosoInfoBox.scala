package common.widgets.virtuoso

import common.widgets.{Box, OutputArea}
import hub.backend.Simplify
import hub.{HubAutomata, Ltrue}
import org.scalajs.dom
import org.scalajs.dom.{MouseEvent, html}
import preo.ast.{CPrim, CoreConnector}
import preo.backend.Automata

/**
  * Created by guillecledou on 01/02/2019
  */


class VirtuosoInfoBox(dependency: Box[CoreConnector], errorBox: OutputArea)
  extends Box[HubAutomata]("Hub Automaton Analysis", List(dependency)){
  private var aut: HubAutomata = _
  private var box: Block = _

  override def get: HubAutomata = aut

  override def init(div: Block, visible: Boolean): Unit = {
    box = panelBox(div, visible).append("div")
      .attr("id", "analysisBox")
    dom.document.getElementById("Hub Automaton Analysis").firstChild.firstChild.firstChild.asInstanceOf[html.Element]
      .onclick = {e: MouseEvent => if(!isVisible) showInfo() else deleteInfo()}
  }

//<<<<<<< HEAD
  override def update(): Unit = if(isVisible) showInfo()


//  private def showInfo():Unit = try {
//    if(box!=null) box.text("")
//    aut = Automata[HubAutomata](dependency.get).serialize.simplify
//    //    aut = dependency.get.asInstanceOf[HubAutomata]
//    val (states,vars) = aut.memory
//    var varsByType = vars.groupBy(_._1)
//    if (box!=null) {
//      box.append("p")
//        .text("Memory:")
//      box.append("p")
//        .text(s"- ${states} states: ${Math.log(states) / Math.log(2)} bits \n")
//=======
  private def showInfo(): Unit = try {
    if(box!=null) {
      box.text("")
      aut = Automata.fromOneToOneSimple[HubAutomata](dependency.get).serialize.simplify
      //    aut = dependency.get.asInstanceOf[HubAutomata]

      // Memory
      //////////
      val (states, vars) = aut.memory
      var mem = 0
      var varsByType = vars.groupBy(_._1)
      mem += Math.ceil(Math.log(states) / Math.log(2)).toInt
      varsByType.foreach(v =>  mem += v._2.size * v._2.head._2)
      box.append("p")
        .append("strong")
        .text(s"Memory: $mem bit${if( mem != 1) "s" else ""} \n")
      val list = box.append("ul")
      list.attr("style","margin-bottom: 20pt;")
      list.append("li")
        .text(s"$states state(s): ${Math.ceil(Math.log(states) / Math.log(2)).toInt} bit(s) \n")
      list.append("li")
        .text(if (vars.nonEmpty) {
          varsByType.map(v => s"${v._2.size} variable(s) of type ${v._1}: ${v._2.size} * ${v._2.head._2} bit(s)").mkString("\n")
        }
        else "0 variables: 0 bits")
//      list.append("li")
//        .text(s"Total: $mem bit${if( mem != 1) "s" else ""} \n")

      // Transitions
      var loc = 0
      loc += aut.trans.size + states + vars.size
      var g = 0
      for (t <- aut.trans) {
        if (Simplify(t._2._3) != Ltrue) g += 1
      }
      loc += g
      var ups = 0
      for (t <- aut.trans) ups += t._2._4.size
      loc += ups
      box.append("p")
        .append("strong")
        .text(s"Code size estimation: $loc loc")
      val list2 = box.append("ul")
      list2.attr("style","margin-bottom: 20pt;")
      list2.append("li")
        .text(s"${aut.trans.size} transition(s), ${states} state(s), ${vars.size} variable(s) (1 loc each)")
      list2.append("li")
        .text(s"$g non-empty guards (1 loc each)")
      list2.append("li")
        .text(s"$ups assignment instructions (1 loc each)")
//      list2.append("li")
//        .text(s"Total: $loc loc${if(loc!=1)"s"} \n")


//      println(aut.show)
//      println(aut.wherePortsAre.mkString("---\n - ","\n - ","\n---"))

      // Ports in all states
      val ports = aut.wherePortsAre.filter(_._2._3==states)
      if (ports.nonEmpty) {
        box.append("p")
          .append("strong")
          .text("Always Available")
        val list3 = box.append("ul")
        list3.attr("style","margin-bottom: 20pt;")

        val (ps1, ps2) = ports.span(x => x._2._2)
        for (p <- ps1) {
          //          val grd = (p._2._3 - Ltrue).mkString(" or ")
          list3.append("li").text(getName(p._1._2.prim) + " - " + findOrderNr(p._1._1, p._1._2.ins, p._1._2.outs) +
            (if (p._2._1 == Ltrue) "" else " (has guards)"))
        }
        for (p <- ps2) {
          //          val grd = (p._2._3 - Ltrue).mkString(" or ")
          list3.append("li").text(getName(p._1._2.prim) + " - " + findOrderNr(p._1._1, p._1._2.ins, p._1._2.outs) + " (must syncrhonise) " +
            (if (p._2._1 == Ltrue) "" else " (has guards)"))
        }
      }
    }
  }
  catch Box.checkExceptions(errorBox)

  private def findOrderNr(n: Int, ins: List[Int], outs: List[Int]): String = {
    ins.indexOf(n) match {
      case -1 => outs.indexOf(n) match {
        case -1 => "[no end found]"
        case j => if (outs.size>1) s"out#${j+1}" else "out"
      }
      case i => if (ins.size>1) s"in#${i+1}" else "in"
    }
  }
  private def getName(prim: CPrim): String = {
    if (prim.name == "node") {
      if (prim.extra contains "dupl") "dupl"
      else "port"
    }
    else prim.name
  }
  private def deleteInfo():Unit =
    box.text("")
}
