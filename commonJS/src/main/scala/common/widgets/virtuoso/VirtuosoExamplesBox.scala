package common.widgets.virtuoso

import common.widgets.{ButtonsBox, Setable}

class VirtuosoExamplesBox(reload: => Unit, inputBox: Setable[String])
  extends ButtonsBox(reload, inputBox, inputBox){

  override protected val buttons: Seq[((String,String),String)] = Seq(
    "Alternating Port"->"" ->
      """// Alternating port
        |...""".stripMargin,
    "Test"->""->
      """// experiments
        |mainHub
        |{
        |  [hide,T:intt, full:false, N:4] de1 = fifo,   // dataEvent
        |  [hide,T:intt, full:true] de2 = fifofull, // dataEvent
        |  dupl3 = dupls 3,
        |
        |   mainHub(s1?,p1?,s2?,p2?,g!) =
        |     drain(s1,x2)
        |     drain(x1,s2)
        |     dupl3(p1,x1,x3,x4)
        |     dupl3(p2,x2,x11,x12)
        |     drain(x3,x5) dupl(x9,x5,x6)
        |     de1(x6,x7) dupl(x7,x10,x8)
        |     de2(x8,x9) drain(x10,x11)
        |     merger(x4,x12,g) // port?
        |}
        |""".stripMargin
  )

}