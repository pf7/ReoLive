package common.widgets

class LinceExamplesBox(reload: => Unit, inputBox: Setable[String])
  extends ButtonsBox(reload, inputBox, inputBox){

  override protected val buttons: Seq[((String,String),String)] = Seq(
    "bounce"->""->"""v:=5; p:=10;
                  |repeat 4 {
                  |  v=-9.8, p=v & p<0 /\ v<0;
                  |  v:=-0.5*v
                  |}""".stripMargin,
    "flip"->""->
      """repeat 4 {
        |   l:=0; skip & 10 ;
        |   l:=1; skip &10
        |}""".stripMargin,
    "fireflies"->""->"""f1 := 8; f2 := 5;
                       |repeat 4 {
                       |  f1=1, f2=1 & f1 > 10 \/ f2 > 10;
                       |  if f1>=10 /\ f2<10
                       |    then f1:=0; f2:=f2+1
                       |    else if f2>=10 /\ f1<10
                       |         then f2:=0;f1 :=f1 +1
                       |         else f1:=0;f2 :=0
                       |}""".stripMargin,
    "cruise"->""->
      """p:=0; v:=2;
        |repeat 15 {
        |  if v<=10
        |  then p=v,v=5 & 1
        |  else p=v,v=-2&1
        |}""".stripMargin
  )

}
