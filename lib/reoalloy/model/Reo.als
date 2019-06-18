open util/ordering[State] as S

sig Node {}

abstract sig Connector {
	conns : set Connector ,
	ports : set Node
}

abstract sig Channel extends Connector {
	e1, e2 : one Node
}
{
	ports = e1 + e2
	conns = none 
}

sig State{	fire : set Node }

sig Sync extends Channel {}{
	all s : State | e1 in s.fire iff e2 in s.fire
}

pred sync[src : one Node, s : one Sync, sink : one Node]{
	src = s.e1
	sink = s.e2
}

sig Drain extends Channel {}{
	all s : State | e1 in s.fire iff e2 in s.fire
}

pred drain[sink1, sink2 : one Node, d : one Drain]{
	sink1 = d.e1
	sink2 = d.e2
}

sig Lossy extends Channel {}{
	all s : State | e2 in s.fire implies e1 in s.fire
}

pred lossy[src : one Node, l : one Lossy, sink : one Node]{
	src = l.e1
	sink = l.e2
}

sig Value{}

sig Fifo extends Channel { buffer : Value lone -> State }{
	all s : State - first | let ant = s.prev,  received = e1 in ant.fire, sent = e2 in ant.fire,
					  emptyBefore = no buffer.ant,  emptyNow = no buffer.s,
					  fullBefore = some buffer.ant,  fullNow = some buffer.s
	 {
		received implies emptyBefore and fullNow and not sent
			
		sent implies fullBefore and emptyNow and not received

		not received and not sent implies buffer.ant = buffer.s

	}
}

pred fifo[src : one Node, f : one Fifo, sink: one Node]{
	src = f.e1
	sink = f.e2

	no f.buffer.first
}

pred fifofull[src : one Node, f : one Fifo, sink: one Node]{
	src = f.e1
	sink = f.e2
	some  f.buffer.first
}

sig Merger extends Connector {
	disj i1, i2, o : one Node
}
{
	ports = i1 + i2 + o
	conns = none
}

pred merger[inp1, inp2 : one Node, m : one Merger, out : one Node]{
	inp1 = m.i1 
	inp2 = m.i2 
	out = m.o

	all s : State | let fire_i1 = inp1 in s.fire, fire_i2 = inp2 in s.fire  {
		fire_i1 implies not fire_i2
		fire_i2 implies not fire_i1

		out in s.fire iff fire_i1 or fire_i2
	}
}

sig Replicator extends Connector {
	disj i, o1, o2 : one Node
}
{
	ports = i + o1 + o2
	conns = none
}

pred replicator[inp : one Node, r : one Replicator, out1, out2 : one Node]{
	inp = r.i
	out1 = r.o1
	out2 = r.o2

	all s : State {
		inp in s.fire iff out1 in s.fire
		inp in s.fire iff out2 in s.fire
	}
}

sig VMerger extends Connector {
	o : one Node,
	i1, i2 : lone Node
}
{
	disj[o, i1, i2]
	ports = i1 + i2 + o
	conns = none
}

pred vmerger[inp1, inp2 : lone Node, m : one VMerger, out : one Node]{
	inp1 = m.i1 
	inp2 = m.i2 
	out = m.o

	all s : State | let fire_i1 = m.i1 in s.fire, fire_i2 = m.i2 in s.fire  {
		some m.i1 and fire_i1 implies not fire_i2
		some m.i2 and fire_i2 implies not fire_i1

		some m.i1 + m.i2 implies out in s.fire iff fire_i1 or fire_i2
	}
}

sig VDupl extends Connector{
	i : one Node,
	o1, o2 : lone Node	
}{
	disj[i, o1, o2]
	ports = i + o1 + o2
	conns = none
}

pred vdupl[inp : one Node, r : one VDupl, out1, out2 : lone Node]{
	inp = r.i
	out1 = r.o1
	out2 = r.o2

	all s : State {
		some out1 implies inp in s.fire iff out1 in s.fire
		some out2 implies inp in s.fire iff out2 in s.fire
	}
}

fact{
	all c : Connector | c not in c.^conns
	all s : State - last | s.fire = s.next.fire and no s.fire implies all n : Node | cantFire[s, n]
}

pred cantFire[s : State, n : Node]{
	(some f : Fifo | n = f.e1 and some f.buffer.s or
			         n = f.e2 and no f.buffer.s) or
	(some m : Merger | n = m.i1 and m.i2 in s.fire or
				      n = m.i2 and m.i1 in s.fire) or
	(some vm : VMerger | n = vm.i1 and some vm.i2 and vm.i2 in s.fire or
					   n = vm.i2 and some vm.i1 and vm.i1 in s.fire)
}

fun FireNodes : State -> Node{
	{ s : State, n : Node | n in s.fire }
}

fun EmptyFifo : State -> Fifo{
	{ s : State, f : Fifo | no f.buffer.s }
}

fun FullFifo : State -> Fifo{
	{ s : State, f : Fifo | some f.buffer.s }
}
