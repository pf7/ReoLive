package common.widgets

import org.scalajs.dom.EventTarget

import scala.scalajs.js.UndefOr

class ButtonsBox(reload: => Unit, inputBox: InputCodeBox, logicBox: LogicBox)
    extends Box[String]("examples", Nil){

  private val buttons: Seq[((String,String),String)] = Seq(
    "writer"->"writer"->"// can always write\n<all*.writer>true",
    "reader"->"reader"->"// can always read\n<all*.reader>true",
    "fifo"->"fifo"->"// can always fire\n<all*.fifo>true",
    "merger"->"merger"->"// can always fire\n<all*.merger>true",
    "dupl"->"dupl"->"// can always fire\n<all*.dupl>true",
    "drain"->"drain"->"// can always fire\n<all*.drain>true",
    "fifo*writer ; drain"->"fifo*writer ; drain"->"// writer fails first write\n[writer]false",
    "\\x . fifo^x*writer ; drain^2" -> "\\x . fifo^x*writer ; drain^2"->"",
    "(\\x.fifo^x) ; (\\n.drain^n)" -> "(\\x.fifo^x) ; (\\n.drain^n)"->"",
    //    "\\b:B . (b? fifo + dupl) & merger" -> "\\b:B . (b? fifo + dupl) & merger",
    "b? fifo + lossy^2" -> "\\b:B . (b? fifo + lossy*lossy) ; merger"->"",
    "(\\x .drain^(x-1)) 3" -> "(\\x .drain^(x-1)) 3"->"",
    "(\\x. lossy^x |x>2)" -> "(\\x. lossy^x |x>2) ; (\\n. merger^n | n>1 & n<6)"->"",
    "merger!" -> "writer^8 ; merger! ; merger! ; reader!"->"",
    "x;y{x=..,y=..}" -> "x ; y ; z {\n  x = lossy * fifo ,\n  y = merger,\n  [hide] z = lossy }"->"[all*] @x <!fifo> true",
    "Treo" -> "alt1 {\n alt1(a?,b?,c!) =\n   drain(a, b)\n   sync(b, x)\n   fifo(x, c)\n   sync(a, c) \n ,\n alt2 =\n   dupl*dupl;\n   fifo*drain*id;\n   merger\n}"
           -> "// sometimes the drain cannot fire\n<all*> @alt1 [!drain] false",
    "barrier"->"dupl*dupl ; id*drain*id"->"// drain can always fire\n<all*.drain>true",
    "exrouter"->"""dupl ; dupl*id ;
                     |(lossy;dupl)*(lossy;dupl)*id ;
                     |id*merger*id*id ;
                     |id*id*swap ;
                     |id*drain*id ;
                     |out1*out2""".stripMargin
              ->"// out1 and out2 cannot go together\n[all*.(out1 & out2)] false\n// always: either out1 or out2 is active\n<all*.(out1 + out2)> true",
    //    "exrouter=.."->"writer ; dupl ; dupl*id ; (lossy*lossy ; dupl*dupl ; id*swap*id ; id*id*merger)*id ; id*id*drain ; reader^2",
    "zip"-> """zip_ 3
{
zip_ =
  \n.loop((2*n)*(n-1))
    ((id^(n-x)*sym(1,1)^x*id^(n-x))^x<--n;
     sym((2*n)*(n-1),2*n))
}"""->"",
    "unzip" -> """unzip_ 3
{
unzip_ =
 \n.loop((2*n)*(n-1))
   (((id^(x+1)*sym(1,1)^((n-x)-1)*id^(x+1))^x<--n);
    sym((2*n)*(n-1),2*n))
}"""->"",
    "sequencer"-> """writer^3 ; sequencer_ 3 ; reader^3
                       |
                       |{
                       |zip_ =
                       |  \n.loop((2*n)*(n-1))
                       |  ((id^(n-x)*sym(1,1)^x*id^(n-x))^x<--n;
                       |   sym((2*n)*(n-1),2*n)),
                       |
                       |unzip_ =
                       |  \n.loop((2*n)*(n-1))
                       |  (((id^(x+1)*sym(1,1)^((n-x)-1)*id^(x+1))^x<--n);
                       |   sym((2*n)*(n-1),2*n)),
                       |
                       |fifoloop_ = \n. loop(n)(
                       |   sym(n-1,1);
                       |  (fifofull; dupl) * (fifo; dupl)^(n-1);
                       |  unzip n ),
                       |
                       |sequencer_ =
                       |  \n.(dupl^n; unzip_ n) * fifoloop_ n ;
                       |    id^n * (zip_ n; drain^n)
                       |}""".stripMargin->"",
    "exrouters" -> """writer ; exrouters_ 3 ; reader!
                            |{
                            |  unzip_ =
                            |    \n.loop((2*n)*(n - 1))
                            |    (((((id^(x+1))*(sym(1,1)^((n-x)-1)))*(id^(x+1)))^(x<--n));
                            |     sym((2*n)*(n-1),2*n))
                            |  ,
                            |  dupls_ =
                            |    \n.loop(n-1)(id*(dupl^(n-1)) ; sym(1,(n-1)*2))
                            |  ,
                            |  mergers_ =
                            |    \n.loop(n-1)(sym((n-1)*2,1) ; (id*(merger^(n-1))))
                            |  ,
                            |  exrouters_ =
                            |    \n. (
                            |      (dupls_(n+1)) ;
                            |      ((((lossy ; dupl)^n) ; (unzip_(n)))*id) ;
                            |      ((id^n)*(mergers_(n))*id) ;
                            |      ((id^n)*drain))
                            |}
                            |""".stripMargin->"",
    "switcher"->
      """in*chg; switcher; out1*out2
        |{
        |  [hide] xor = exrouter,
        |  [hide] alternator = xor ;
        |    dupl*id;
        |    id * (fifo*dupl;drain*id),
        |  [hide] gateOpen =
        |    dupl*loopfifos;
        |    id*drain
        |    {
        |      loopfifos =
        |        loop(1)(
        |          alternator*id;
        |          id*swap ;
        |          merger*id;
        |          fifo*id;
        |          xor*id;
        |          fifofull*drain;
        |          dupl
        |        )
        |    },
        |  [hide] gateClosed =
        |    dupl*loopfifos;
        |    id*drain
        |    {
        |      loopfifos =
        |        loop(1)(
        |          (alternator;swap)*id;
        |          id*swap ;
        |          merger*id;
        |          fifofull*id;
        |          xor*id;
        |          fifofull*drain;
        |          dupl
        |        )
        |    },
        |  switcher =
        |    xor*dupl;
        |    id*swap*id;
        |    gateOpen * gateClosed
        |}""".stripMargin->"// sometimes gateOpen cannot fire\n<all*>@switcher[gateOpen]false",
    "Prelude"->
      """id_
        |{
        |  writer_    = writer,
        |  reader_    = reader,
        |  fifo_      = fifo,
        |  fifofull_  = fifofull,
        |  drain_     = drain,
        |  id_        = id,
        |  dupl_      = dupl,
        |  lossy_     = lossy,
        |  merger_    = merger,
        |  swap_      = swap,
        |  noSrc_     = noSrc,
        |  noSnk_     = noSnk,
        |  exrouter_  = exrouter,
        |  exrouters_ = exrouters,
        |  ids_       = ids,
        |  node_      = node,
        |  dupls_     = dupls,
        |  mergers_   = mergers,
        |  zip_       = zip,
        |  unzip_     = unzip,
        |  fifoloop_  = fifoloop,
        |  sequencer_ = sequencer,
        |  barrier_   = barrier,
        |  barriers_  = barriers
        |}
      """.stripMargin->"// can always fire\n<all*.sync>true"
  )



  override def get: String = "No class can be dependent on this one!"

  override def init(div: Block, visible: Boolean): Unit = {
    val buttonsDiv = super.panelBox(div,visible).append("div")
      .attr("id", "buttons")
      .attr("style","padding: 2pt;")

    buttonsDiv
      .style("display:block; padding:2pt")

    for (ops <- buttons ) yield genButton(ops,buttonsDiv)


  }

  override def update: Unit = ()

  private def genButton(ss:((String,String),String),buttonsDiv:Block): Unit = {
    val button = buttonsDiv.append("button")
      .text(ss._1._1)

    button.on("click",{(e: EventTarget, a: Int, b:UndefOr[Int])=> {
      inputBox.setValue(ss._1._2)
      logicBox.setValue(ss._2)
      reload
    }} : button.DatumFunction[Unit])
  }
}
