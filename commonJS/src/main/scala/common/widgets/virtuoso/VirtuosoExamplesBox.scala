package common.widgets.virtuoso

import common.widgets.{ButtonsBox, Setable}

class VirtuosoExamplesBox(reload: => Unit, inputBox: Setable[String],msgBox:Setable[String])
  extends ButtonsBox(reload, List(msgBox, inputBox)){

  override protected val buttons: Seq[List[String]] = Seq(
    "Port"::("<p><strong>Port Hub</strong></p>Forwards data from its source to its sink, acting" +
      " as a synchronisation mechanism between two tasks." +
      " There is no buffer capacity, i.e.data is transfer" +
      " directly between the two tasks.")::
      """port """.stripMargin::Nil,
    "Port - 2 sources"
      ::"""<p><strong>Merging Port Hub</strong></p>
          | <p>Similar to the simple Port, but uses only one of its source points.</p>""".stripMargin
      :: "merger"::Nil,
    "Port - 2 sinks"
      :: ("<p><strong>XOR Port Hub</strong></p>"+
          "Similar to the simple Port, but uses only "+
          "one of its sink points.")
      :: "xor"::Nil,
    "Duplicator"
      ::("<p><<strong>Duplicator</p></strong>" +
         "Similar to the simplr Port, duplicates incoming data to all of its sink poins." +
         " It can only receive data once all its sources are ready to receive data.")
      :: "dupl"::Nil,
    "Semaphore"
      ::("<p><strong>Semaphore</strong></p>"+
      "Has two interaction points: to signal the semaphore and "+
      "increment its internal counter c, and to test if the "+
      "semaphore is set, i.e., c ≥ 0, in which case succeeds "+
      "and decrements its counter, otherwise it can wait. ")
      :: " semaphore "::Nil,
    "Event"
      :: ("<p><strong>Event</strong></p>" +
      "It has two waiting lists for two kind of requests: raise – signals the " +
      "occurrence of an event, and test – checks if an event happened, in which case " +
      "succeeds and deactivates the signal, otherwise it can wait.")
      :: "event "::Nil,
    "DataEvent"
      :: ("<p><strong>Data Event</strong></p>" +
      "Similar to the Event hub with the additional capacity to buffer " +
      "a data element sent with the raise signal. If the signal has been raised, the test " +
      "signal receives the buffered data and deactivates the signal, otherwise it can wait. " +
      "Data can be overridden, i.e.raise always succeeds. An additional waiting " +
      "lists for clear requests is used to clear the buffer, mainly to facilitate interfacing " +
      "with device drivers.taEvent" +
      "")
      :: "dataEvent "::Nil,
    "Resource"
      :: ("<p><strong>Resource</strong></p>" +
      "Has two waiting lists for two kind of requests: lock – signals the " +
      "Resource acquisition of a logical resource, which succeeds if the resource is free, otherwise " +
      "it can wait, and unlock – signals the release of an acquired resource, which " +
      "succeeds if the resource had been acquire by the same task that released it, " +
      "failing otherwise.")
      :: "resource"::Nil,
    "Fifo"
      ::("<p><strong>Fifo</strong></p>" +
      "Has two waiting lists for two kind of requests: enqueue – signals the " +
      "entering of some data into the queue, which succeeds if the queue is not full, " +
      "and dequeue – signals data leaving the queue, which succeeds if the queue is not " +
      "empty. The presented Fifo can store at most 1 element - a general Fifo can store up to a fixed number N of elements.")
      :: "fifo "::Nil,
    "Blackboard"
      ::("<p><strong>Blackboard</strong></p>" +
      "Acts like a protected shared data area. A update waiting list " +
      "is used to set its content (whereby a sequence number is incremented), a read " +
      "waiting list is used to read the data, and a count waiting list is used to obtain " +
      "the sequence number, allowing tasks to attest the freshness of the data. A special " +
      "data element, CLR, can be sent with the update signal to clear the buffer.")
      :: "blackboard "::Nil,
    "Alternator" ::
      ("<p><strong>Alternator</strong></p>" +
        "For every pair of values received by two waiting lists, it forwards them to the output. " +
        "It sends the values always in the same order, and stores at most 1 value.") ::
        "dupl*dupl;\nfifo*drain*id;\nmerger"::Nil,
    "Sequencer"
      ::"Outputs a value alternating between 3 outputs"
      ::"""seq3 {
          | seq3 (x!,y!,z!) =
          |   event(a,b) event(c,d) eventFull(e,f)
          |   dupl(b,c,x) dupl(d,e,y) dupl(f,a,z)
          |}""".stripMargin::Nil,
  "RoundRobin tasks"
    ::"Round robin between 2 tasks, sending to an actuator. Tasks are not modelled - only the coordinator."
    :: """s1 * p1 * s2 * p2;
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
      """.stripMargin::Nil,
  "RoundRobin tasks - with components"
    ::"Round robin between 2 tasks, sending to an actuator. Tasks are modelled as components always ready to interact."
    :: """t1 * t2;
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
        """.stripMargin::Nil
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
//    "Alternating Port"::"" ::
//      """// Alternating port
//        |...""".stripMargin::Nil,
//    "Test"::""::
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
//        |""".stripMargin::Nil
  )

}