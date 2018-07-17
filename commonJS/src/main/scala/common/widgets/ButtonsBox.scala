package common.widgets

import org.scalajs.dom.EventTarget

import scala.scalajs.js.UndefOr

class ButtonsBox(reload:() => Unit, inputBox: InputBox)
    extends PanelBox[String]("examples", None){

  private val buttons = Seq(
    "writer"->"writer", "reader"->"reader",
    "fifo"->"fifo",     "merger"->"merger",
    "dupl"->"dupl",     "drain"->"drain",
    "fifo*writer ; drain"->"fifo*writer ; drain",
    "\\x . fifo^x*writer ; drain^2" -> "\\x . fifo^x*writer ; drain^2",
    "(\\x.fifo^x) ; (\\n.drain^n)" -> "(\\x.fifo^x) ; (\\n.drain^n)",
    //    "\\b:B . (b? fifo + dupl) & merger" -> "\\b:B . (b? fifo + dupl) & merger",
    "\\b:B . (b? fifo + lossy*lossy) ; merger" -> "\\b:B . (b? fifo + lossy*lossy) ; merger",
    "(\\x .drain^(x-1)) 3" -> "(\\x .drain^(x-1)) 3",
    "(\\x. lossy^x |x>2) ; ..." -> "(\\x. lossy^x |x>2) ; (\\n. merger^n | n>1 & n<6)",
    ".. ; merger!" -> "writer^8 ; merger! ; merger! ; reader!",
    "x;y{x=..,y=..}" -> "x ; y {x = lossy * fifo , y = merger}",
    "exrouter=.."->"""dupl ; dupl*id ;
                     |(lossy;dupl)*(lossy;dupl)*id ;
                     |id*merger*id*id ;
                     |id*id*swap ;
                     |id*drain*id""".stripMargin,
    //    "exrouter=.."->"writer ; dupl ; dupl*id ; (lossy*lossy ; dupl*dupl ; id*swap*id ; id*id*merger)*id ; id*id*drain ; reader^2",
    "zip=.."-> """zip 3
{
zip =
  \n.Tr((2*n)*(n-1))
    ((id^(n-x)*sym(1,1)^x*id^(n-x))^x<--n;
     sym((2*n)*(n-1),2*n))
}""",
    "unzip=.." -> """unzip 3
{
unzip =
 \n.Tr((2*n)*(n-1))
   (((id^(x+1)*sym(1,1)^((n-x)-1)*id^(x+1))^x<--n);
    sym((2*n)*(n-1),2*n))
}""",
    "sequencer=.."-> """writer^3 ; sequencer 3 ; reader^3
                       |
                       |{
                       |zip =
                       |  \n.Tr((2*n)*(n-1))
                       |  ((id^(n-x)*sym(1,1)^x*id^(n-x))^x<--n;
                       |   sym((2*n)*(n-1),2*n)),
                       |
                       |unzip =
                       |  \n.Tr((2*n)*(n-1))
                       |  (((id^(x+1)*sym(1,1)^((n-x)-1)*id^(x+1))^x<--n);
                       |   sym((2*n)*(n-1),2*n)),
                       |
                       |fifoloop = \n. Tr(n)(
                       |   sym(n-1,1);
                       |  (fifofull; dupl) * (fifo; dupl)^(n-1);
                       |  unzip n ),
                       |
                       |sequencer =
                       |  \n.(dupl^n; unzip n) * fifoloop n ;
                       |    id^n * (zip n; drain^n)
                       |}""".stripMargin,
    "nexrouters = ..." -> """writer ; nexrouter(3) ; reader!
                            |{
                            |  unzip =
                            |    \n.Tr((2*n)*(n - 1))
                            |    (((((id^(x+1))*(sym(1,1)^((n-x)-1)))*(id^(x+1)))^(x<--n));
                            |     sym((2*n)*(n-1),2*n))
                            |  ,
                            |  dupls =
                            |    \n.Tr(n-1)(id*(dupl^(n-1)) ; sym(1,(n-1)*2))
                            |  ,
                            |  mergers =
                            |    \n.Tr(n-1)(sym((n-1)*2,1) ; (id*(merger^(n-1))))
                            |  ,
                            |  nexrouter =
                            |    \n. (
                            |      (dupls(n+1)) ;
                            |      ((((lossy ; dupl)^n) ; (unzip(n)))*id) ;
                            |      ((id^n)*(mergers(n))*id) ;
                            |      ((id^n)*drain))
                            |}
                            |""".stripMargin,
    "Prelude"->
      """id
        |{
        |  writer    = writer,
        |  reader    = reader,
        |  fifo      = fifo,
        |  fifofull  = fifofull,
        |  drain     = drain,
        |  id        = id,
        |  dupl      = dupl,
        |  lossy     = lossy,
        |  merger    = merger,
        |  swap      = swap,
        |  exrouter  = exrouter,
        |  exrouters = exrouters,
        |  ids       = ids,
        |  node      = node,
        |  dupls     = dupls,
        |  mergers   = mergers,
        |  zip       = zip,
        |  unzip     = unzip,
        |  fifoloop  = fifoloop,
        |  sequencer = sequencer
        |}
      """.stripMargin
  )



  override def get: String = "No class can be dependent on this one!"

  override def init(div: Block): Unit = {
    val buttonsDiv = super.panelBox(div,true).append("div")
      .attr("id", "buttons")
      .attr("style","padding: 2pt;")

    buttonsDiv
      .style("display:block; padding:2pt")

    for (ops <- buttons ) yield genButton(ops,buttonsDiv)


  }

  override def update: Unit = ()

  private def genButton(ss:(String,String),buttonsDiv:Block): Unit = {
    val button = buttonsDiv.append("button")
      .text(ss._1)

    button.on("click",{(e: EventTarget, a: Int, b:UndefOr[Int])=> {
      inputBox.inputAreaDom.value = ss._2
      reload()
    }} : button.DatumFunction[Unit])
  }
}
