package common.widgets

import org.scalajs.dom.EventTarget

import scala.scalajs.js.UndefOr

class ButtonsBox(reload: => Unit, toSet: List[Setable[String]]) //inputBox: Setable[String], logicBox: Setable[String])
    extends Box[String]("Examples", Nil){

  private def descr(s1:String,s2:String):String =
    s"<p><strong>$s1</strong></p> $s2"

  /**
    * Each buttom is a list of strings: the first is the button name,
    * and each of the remaining are passed to the `toSet` objects by the same order.
    */
  protected val buttons: Seq[List[String]] = Seq(
    "writer"::"writer"::"// can always write\n<all*.writer>true"::
      descr("Writer","Primitive that is always ready to produce values")::Nil,
    "reader"::"reader"::"// can always read\n<all*.reader>true"::
      descr("Reader","Primitive that is always ready to receive values")::Nil,
    "sync"::"sync"::""::
      descr("Synchronous channel","A synchronous channel (or sync channel) has " +
        "a source end and a sink end. It accepts a datum into its source end if and only " +
        "if it can dispense it from its sink end.")::Nil,
    "lossy"::"lossy"::""::
      descr("Lossy synchronous channel","A lossy synchronous channel (or lossy " +
        "sync channel) is essentially a synchronous channel that always accepts data into its " +
        "source end. If it cannot dispense the datum out of its sink end, the datum is lost.")::Nil,
    "fifo"::"fifo"::"// can always fire\n<all*.fifo>true"::
      descr("Empty FIFO1 channel","The empty FIFO1 channel (also called the empty one " +
        "slot buffer) can store a single datum.")::Nil,
    "timer"::"timer(5)"::""::
      descr("Timer", "An asynchronous blocking channel with internal state parameterized " +
        "by an integer value t. After receiving a value, it delays for t ammount of time before " +
        "sending it through its output channel. If t is not specify it assumes t=0 and behaves as " +
        "a sync channel.")::Nil,
    "merger"::"merger"::"// can always fire\n<all*.merger>true"::
      descr("Sink node: merger","A sink node acts like a merger. A get that is performed " +
        "at a sink node receives a datum from one of its offering coincident channels. If multiple " +
        "channels can offer data, one channel is selected non-deterministically.")::Nil,
    "variable merger"::"vmerger"::""::
      descr("Variable merger","A family of synchronous mergers. " +
        "Like the merger connector but enhanced with boolean expressions over " +
        "variables associated to its input ports, allowing some of its coincident channels " +
        "to be absent. A feature model determines what are the valid connectors allowed in the family. " +
        "In this case, four connectors: all inputs present, only one (either), or none.")::Nil,
    "dupl"::"dupl"::"// can always fire\n<all*.dupl>true"::
      descr("Source node: duplicator","A source node acts like a duplicator. Any datum " +
        "that is put at a source node is replicated through all of its coincident channels if and " +
        "only if all of the channels can accept the datum.")::Nil,
    "variable dupl"::"vdupl"::""::
      descr("Variable duplicator","A family of synchronous duplicators. " +
        "Like the dupl connector but enhanced with boolean expressions over " +
        "variables associated to its output ports, allowing some of its coincident channels " +
        "to be absent. A feature model determines what are the valid connectors allowed in the family. " +
        "In this case, four connectors: all outputs present, only one (either), or none.")::Nil,
    "drain"::"drain"::"// can always fire\n<all*.drain>true"::
      descr("Synchronous drain","The synchronous drain (or syncdrain) has two source ends. " +
        "A put at either end blocks until a put is performed on the other end. Then, both data are " +
        "lost in the channel")::Nil,
    "fifo*writer ; drain"::"fifo*writer ; drain"::"// writer fails first write\n[writer]false"::Nil,
    "\\x . fifo^x*writer ; drain^2" :: "\\x . fifo^x*writer ; drain^2"::""::Nil,
    "(\\x.fifo^x) ; (\\n.drain^n)" :: "(\\x.fifo^x) ; (\\n.drain^n)"::""::Nil,
    //    "\\b:B . (b? fifo + dupl) & merger" :: "\\b:B . (b? fifo + dupl) & merger"::Nil,
    "b? fifo + lossy^2" :: "\\b:B . (b? fifo + lossy*lossy) ; merger"::""::Nil,
    "(\\x .drain^(x-1)) 3" :: "(\\x .drain^(x-1)) 3"::""::Nil,
    "(\\x. lossy^x |x>2)" :: "(\\x. lossy^x |x>2) ; (\\n. merger^n | n>1 & n<6)"::""::Nil,
    "merger!" :: "writer^8 ; merger! ; merger! ; reader!"::""::Nil,
    "x;y{x=..,y=..}" :: "x ; y ; z {\n  x = lossy * fifo ,\n  y = merger,\n  [hide] z = lossy }"::"[all*] @x <!fifo> true"::Nil,
//    "Treo" :: "alt1 {\n alt1(a?,b?,c!) =\n   drain(a, b)\n   sync(b, x)\n   fifo(x, c)\n   sync(a, c) \n ,\n alt2 =\n   dupl*dupl;\n   fifo*drain*id;\n   merger\n}"
//           :: "// sometimes the drain cannot fire\n<all*> @alt1 [!drain] false"::Nil,
    "alternator (preo)" :: "in1*in2; alt; out {\n alt =\n   dupl*dupl;\n   fifo*drain*id;\n   merger\n}"
      :: "// sometimes the drain cannot fire\n<all*> @alt [!drain] false"::Nil,
    "alternator (treo)" :: "in1*in2; alt; out {\n alt(a?,b?,c!) =\n   drain(a, b)\n   sync(b, x)\n   fifo(x, c)\n   sync(a, c)\n}"
      :: "// sometimes the drain cannot fire\n<all*> @alt [!drain] false"::Nil,
    "barrier"::"dupl*dupl ; id*drain*id"::"// drain can always fire\n<all*.drain>true"::Nil,
    "exrouter (preo)"::"""dupl ; dupl*id ;
                         |(lossy;dupl)*(lossy;dupl)*id ;
                         |id*merger*id*id ;
                         |id*id*swap ;
                         |id*drain*id ;
                         |out1*out2""".stripMargin
                     ::"""// out1 and out2 cannot go together
                         |[all*.(out1 & out2)] false
                         |// always: either out1 or out2 is active
                         |<all*.(out1 + out2)> true""".stripMargin::
                     descr("Exclusive Router","Version using the Preo language. Exclusively sends incoming data to one of 2 outputs.")::Nil,
    "exrouter (treo)" :: """in; xor ; out1*out2 {
                           | xor(a?,b!,c!) =
                           |   lossy(a,x) lossy(a,y)
                           |   sync(x,m) sync(y,m)
                           |   drain (a,m)
                           |   sync(x,b) sync(y,c)
                           |}""".stripMargin
                      :: """// out1 and out2 cannot go together
                           |[all*.(out1 & out2)] false
                           |// always: either out1 or out2 is active
                           |<all*.(out1 + out2)> true""".stripMargin::
                      descr("Exclusive Router","Version using the Treo language. Exclusively sends incoming data to one of 2 outputs.")::Nil,
    "variable exrouter"::"""vdupl ; vdupl*id ;
                         |(lossy;dupl)*(lossy;dupl)*id ;
                         |id*vmerger*id*id ;
                         |id*id*swap ;
                         |id*drain*id ;
                         |out1*out2""".stripMargin
      ::""::
      descr("Variabled Exclusive Router","Version using the Preo language. Exclusively sends incoming data to one of 2 outputs. " +
        "Enhanced with variable connectors, allowing one of its outputs to be absent, in which case it always sends the incoming data to " +
        "the available output. If the input or both outputs are absent, the entire connector is absent.")::Nil,
    //    "exrouter=.."::"writer ; dupl ; dupl*id ; (lossy*lossy ; dupl*dupl ; id*swap*id ; id*id*merger)*id ; id*id*drain ; reader^2"::Nil,
    "zip":: """zip_ 3
{
zip_ =
  \n.loop((2*n)*(n-1))
    ((id^(n-x)*sym(1,1)^x*id^(n-x))^x<--n;
     sym((2*n)*(n-1),2*n))
}"""::""::Nil,
    "unzip" :: """unzip_ 3
{
unzip_ =
 \n.loop((2*n)*(n-1))
   (((id^(x+1)*sym(1,1)^((n-x)-1)*id^(x+1))^x<--n);
    sym((2*n)*(n-1),2*n))
}"""::""::Nil,
    "sequencer":: """writer^3 ; sequencer_ 3 ; reader^3
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
                       |}""".stripMargin::""::Nil,
    "exrouters" :: """writer ; exrouters_ 3 ; reader!
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
                            |""".stripMargin::""::Nil,
    "switcher"::
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
        |}""".stripMargin::"// sometimes gateOpen cannot fire\n<all*>@switcher[gateOpen]false"
        ::descr("Switcher","Sends data along one out of 2 flows, allowing to change which flow can be active.")
        ::Nil,
    "Prelude"::
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
      """.stripMargin::"// can always fire\n<all*.sync>true"::Nil
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

  private def genButton(ss:List[String],buttonsDiv:Block): Unit = {
    ss match {
      case hd::tl =>
        val button = buttonsDiv.append("button")
          .text(hd)

        button.on("click",{(e: EventTarget, a: Int, b:UndefOr[Int])=> {
          toSet.zip(tl).foreach(pair => pair._1.setValue(pair._2))
          toSet.drop(tl.size).foreach(_.setValue(""))
          reload
        }} : button.DatumFunction[Unit])
      case Nil =>
    }
  }
}
