package D3Lib

import org.singlespaced.d3js.d3
import preo.backend._

object GraphsToJS {
  def apply(graph: Graph): String = {
    val nodes = getNodes(graph);
    val links = getLinks(graph);

    s"""
        var svg = d3.select("svg");
        var width = svg.attr("width");
        var height = svg.attr("height");
        var color = d3.scaleOrdinal(d3.schemeCategory20);

        var graph = {"nodes": $nodes, "links": $links


};

        var simulation = d3.forceSimulation()
            .force("link", d3.forceLink().id(function(d) { return d.id; }))
            .force("charge", d3.forceManyBody())
            .force("center", d3.forceCenter(width / 2, height / 2))



          var link = svg.append("g")
              .attr("class", "links")
            .selectAll("line")
            .data(graph.links)
            .enter().append("line")
              .attr("stroke",linkColour);


          var node = svg.append("g")
              .attr("class", "nodes")
            .selectAll("circle")
            .data(graph.nodes)
            .enter().append("circle")
              .attr("r", 5)
              .attr("fill", function(d) { return color(d.group); })
              .call(d3.drag()
                  .on("start", dragstarted)
                  .on("drag", dragged)
                  .on("end", dragended));

          node.append("title")
              .text(function(d) { return d.id; });

          simulation
          	  .force("forcepos", forcepos)
              .nodes(graph.nodes)
              .on("tick", ticked);


          simulation.force("link")
              .links(graph.links);



        //Vai atribuir a cor ao link conforme o tipo de link que é
        function linkColour(d){
            console.log(d);
            if(d.type == "fifo")
                return "green";
            if(d.type == "dup")
            	return "blue";
            if(d.type=="merger")
            	return "yellow";
            else return "red";
            }

          function ticked() {
            link
                .attr("x1", function(d) { return d.source.x; })
                .attr("y1", function(d) { return d.source.y; })
                .attr("x2", function(d) { return d.target.x; })
                .attr("y2", function(d) { return d.target.y; });
            node
                .attr("cx", function(d) { return d.x; })
                .attr("cy", function(d) { return d.y; });
          }

        //vai forçar a posição no eixo dos xx do nodo
        //pode ser atribuido um valor fixo
        //ou usar uma incrementação em relação ao valor atual.
        function forcepos(){
          for (var i = 0, n = graph.nodes.length; i < n; ++i) {
            curr_node = graph.nodes[i];
             //curr_node.attr("cx", newPos);
            if(curr_node.group == 3){
                //curr_node.x += 0.4;
                curr_node.x = 750;
            } else if(curr_node.group == 1){
                curr_node.x = 10;
                //curr_node.x -= 0.4;
            }
          }
        }


        function dragstarted(d) {
          if (!d3.event.active) simulation.alphaTarget(0.3).restart();
          d.fx = d.x;
          d.fy = d.y;
        }

        function dragged(d) {
          d.fx = d3.event.x;
          d.fy = d3.event.y;
        }

        function dragended(d) {
          if (!d3.event.active) simulation.alphaTarget(0);
          d.fx = null;
          d.fy = null;
        }"""
    }

  private def getNodes(graph: Graph): String = graph match{
    case Graph(_, nodes) => "[" + processNodes(nodes) + "]"
  }

  private def getLinks(graph: Graph): String = graph match{
    case Graph(edges, _) => "[" + processEdges(edges) + "]"
  }

  private def processNodes(nodes: List[ReoNode]): String = nodes match{
    case ReoNode(id, name, nodeType, style) :: Nil => {
      val nodeGroup = typeToGroup(nodeType);
      s"""{"id": "$id", "group": $nodeGroup }"""
    }
    case ReoNode(id, name, nodeType, style) :: y :: rest => {
      val nodeGroup = typeToGroup(nodeType);
      s"""{"id": "$id", "group": $nodeGroup },""" + processNodes(y::rest)
    }
    case Nil => ""
  }

  private def typeToGroup(nodeType: NodeType):String = nodeType match{
    case Source => "1"
    case Sink => "3"
    case Mixed => "2"
  }

  private def processEdges(channels: List[ReoChannel]): String = channels match{
    case ReoChannel(src,trg, srcType, trgType, name, style) :: Nil => s"""{"source": "$src", "target": "$trg", "type":"$name"}"""
    case ReoChannel(src,trg, srcType, trgType, name, style) :: y :: rest  => s"""{"source": "$src", "target": "$trg", "type":"$name"},""" + processEdges(y::rest)
    case Nil => ""
  }
}
