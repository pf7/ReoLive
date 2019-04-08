package common.widgets.Lince

import common.widgets.{ButtonsBox, Setable}

class LinceExamplesBox(reload: => Unit, inputBox: Setable[String], descr: Setable[String])
  extends ButtonsBox(reload, List(inputBox,descr)){

  override protected val buttons: Seq[List[String]] = Seq(
    "Bounce"->
      """// Bouncing ball example
        |v:=5; p:=10; c:=0;
        |while (c<4) {
        |  v=-9.8, p=v & p<0 /\ v<0;
        |  v:=-0.5*v; c:=c+1
        |}""".stripMargin ->
    descr("Bouncing Ball","A ball position and velocity as it bounces in the floor.")
//    ,"Bounce"->
//       """// Bouncing ball example
//         |v:=5; p:=10;
//         |repeat 4 {
//         |  v=-9.8, p=v & p<0 /\ v<0;
//         |  v:=-0.5*v
//         |}""".stripMargin
    ,"Traffic lights"->
      """// Alternate between two constant values.
        |repeat 4 {
        |   l:=0; wait 10 ;
        |   l:=1; wait 10
        |}""".stripMargin ->
      descr("Traffic lights","Alternating between two constant values.")
    ,"Fireflies"->
      """f1 := 8; f2 := 5;
        |repeat 4 {
        |  f1=1, f2=1 & f1 > 10 \/ f2 > 10;
        |  if f1>=10 /\ f2<10
        |    then f1:=0; f2:=f2+1
        |    else if f2>=10 /\ f1<10
        |         then f2:=0;f1 :=f1 +1
        |         else f1:=0;f2 :=0
        |}""".stripMargin ->
      descr("Fireflies","Every firefly has an internal clock which helps " +
        "it to know when to flash: when the clock reaches a threshold the firefly " +
        "flashes and the clock’s value is reset to zero. The flash of a firefly " +
        "increases the internal clock’s value of all other fireflies nearby.")
    ,"Cruise control"->
      """// Cruise control
        |p:=0; v:=2;
        |repeat 15 {
        |  if v<=10
        |  then p=v,v=5  & 1
        |  else p=v,v=-2 & 1
        |}""".stripMargin ->
      descr("Cruise Control","Maintain a velocity of 10, updating every time unit.")
    ,"Dependent variables"->
      """// Solution not naively computed (precise solution involves sin/cos)
        |// Use the online version to use the precise solution.
        |p:=1;v:=1;
        |p=v, v=-p & 8""".stripMargin ->
      "Experiments - when involving mutually dependent variables the naive numerical analysis does not work."
  ).map(x=>List(x._1._1,x._1._2,x._2))

}
