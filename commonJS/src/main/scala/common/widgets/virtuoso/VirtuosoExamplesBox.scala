package common.widgets.virtuoso

import common.widgets.{ButtonsBox, Setable}

class VirtuosoExamplesBox(reload: => Unit, inputBox: Setable[String])
  extends ButtonsBox(reload, inputBox, inputBox){

  override protected val buttons: Seq[((String,String),String)] = Seq(
    "Port"->"" ->
      """// Port Hub
        |// Forwards data from its source to its sink, acting
        |// as a synchronisation mechanism between two tasks.
        |// There is no buffer capacity, i.e. data is transfer
        |// directly between the two tasks.
        | port """.stripMargin,
    "Port - 2 sources"->"" ->
      """// Merging Port Hub
        |// Similar to the simple Port, but uses only
        |// one of its source points.
        | merger """.stripMargin,
    "Port - 2 sinks"->"" ->
      """// XOR Port Hub
        |// Similar to the simple Port, but uses only
        |// one of its sink points.
        | xor """.stripMargin,
    "Duplicator"->"" ->
      """// Duplicator
        | dupl """.stripMargin,
    "Semaphore"->"" ->
      """// Semaphore
        |// Has two interaction points: to signal the semaphore and
        |// increment its internal counter c, and to test if the
        |// semaphore is set, i.e., c ≥ 0, in which case succeeds
        |// and decrements its counter, otherwise it can wait.
        | semaphore """.stripMargin,
    "Event"->"" ->
      """// Event
        | event """.stripMargin,
    "DataEvent"->"" ->
      """// DataEvent
        | dataEvent """.stripMargin,
    "Fifo"->"" ->
      """// Fifo
        | fifo """.stripMargin,
    "Blackboard"->"" ->
      """// Blackboard
        | blackboard """.stripMargin,
    "Resource"->"" ->
      """// Resource
        | resource """.stripMargin,
    "Alternator" -> "" ->
      "dupl*dupl;\nfifo*drain*id;\nmerger",
    "Custom"->""->
        """
          |// Round robin between 2 tasks, sending to an actuator
          |t1 * t2;
          |coord;
          |act
          |{
          |  dupl1 = dupls 3,
          |  dupl2 = dupls 3,
          |
          |  coord(s1?,p1?,s2?,p2?,get!) =
          |    dupl1(p1,d11,d12,d13)
          |    dupl2(p2,d21,d22,d23)
          |    drain(s1,d21) drain(s2,d11)
          |    drain(d12,d42) drain(d22,d32)
          |    dupl(e1,d41,d42) dupl(e2,d32,d31)
          |    event(d31,e1) eventFull(d41,e2)
          |    merger(d13,d23,get),
          |  [hide] t1 = writer*writer,
          |  [hide] t2 = writer*writer,
          |  [hide] act = reader
          |}
        """.stripMargin,
    "Custom Open"->""->
      """
        |// Round robin between 2 tasks, sending to an actuator
        |s1 * p1 * s2 * p2;
        |coord;
        |get
        |{
        |  dupl1 = dupls 3,
        |  dupl2 = dupls 3,
        |
        |  coord(s1?,p1?,s2?,p2?,get!) =
        |    dupl1(p1,d11,d12,d13)
        |    dupl2(p2,d21,d22,d23)
        |    drain(s1,d21) drain(s2,d11)
        |    drain(d12,d42) drain(d22,d32)
        |    dupl(e1,d41,d42) dupl(e2,d32,d31)
        |    event(d31,e1) eventFull(d41,e2)
        |    merger(d13,d23,get)
        |}
      """.stripMargin
//      """// Round robin between 2 tasks, sending to an actuator
//        |t1 * t2;
//        |coord;
//        |act
//        |{
//        |  coord(s1?,p1?,s2?,p2?,get!) =
//        |    drain(s1,p2) drain(s2,p1)
//        |    drain(p1,f1) drain(p2,f2)
//        |    sync(p1,get) sync(p2,get)
//        |    event(f1,f2) eventFull(f2,f1),
//        |  [hide] t1 = writer*writer,
//        |  [hide] t2 = writer*writer,
//        |  [hide] act = reader
//        |}
//      """.stripMargin
//    "Alternating Port"->"" ->
//      """// Alternating port
//        |...""".stripMargin,
//    "Test"->""->
//      """// experiments
//        |mainHub
//        |{
//        |  [hide,T:intt, full:false, N:4] de1 = fifo,   // dataEvent
//        |  [hide,T:intt, full:true] de2 = fifofull, // dataEvent
//        |  dupl3 = dupls 3,
//        |
//        |   mainHub(s1?,p1?,s2?,p2?,g!) =
//        |     drain(s1,x2)
//        |     drain(x1,s2)
//        |     dupl3(p1,x1,x3,x4)
//        |     dupl3(p2,x2,x11,x12)
//        |     drain(x3,x5) dupl(x9,x5,x6)
//        |     de1(x6,x7) dupl(x7,x10,x8)
//        |     de2(x8,x9) drain(x10,x11)
//        |     merger(x4,x12,g) // port?
//        |}
//        |""".stripMargin
  )

}