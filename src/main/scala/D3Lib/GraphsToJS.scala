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

        var graph = {"nodes": $nodes, "links": $links};

        var simulation = d3.forceSimulation(graph.nodes)
          .force('charge', d3.forceManyBody().strength(-100))
          .force('center', d3.forceCenter(width / 2, height / 2))
          .force('collision', d3.forceCollide().radius(function(d) {
            return d.radius}))
          .force("link", d3.forceLink().links(graph.links).id(function(d) { return d.id; }))
          .force("forcepos", forcepos)
          .on('tick', ticked);

        init(graph.nodes, graph.links);

        function init(nodes, links){
            //add nodes
            var node = d3.select(".nodes")
                .selectAll("circle")
                .data(nodes);


            node.enter()
                .append("circle")
                .merge(node)
                .attr("r", 5)
                .attr("id", function (d) {return d.id;})
                .call(d3.drag()
                  .on("start", dragstarted)
                  .on("drag", dragged)
                  .on("end", dragended));

            node.exit().remove();

            //add links
            var link = d3.select(".links")
                .selectAll("line")
                .data(links);



            link.enter().append("line")
                .style("stroke", "black")
                .merge(link)
                .attr('marker-end','url(#arrowhead)');

            link.append("title")
                .text(function (d) {return d.type;});


            link.exit().remove();

            //add labels to graph
            var edgepaths = svg.selectAll(".edgepath")
                .data(links)
                .enter()
                .append('path')
                .attr('class', 'edgepath')
                .attr('fill-opacity', 0)
                .attr('stroke-opacity', 0)
                .attr('id', function (d, i) {return 'edgepath' + i})
                .style("pointer-events", "none");

            var edgelabels = svg.selectAll(".edgelabel")
                .data(links)
                .enter()
                .append('text')
                .style("pointer-events", "none")
                .attr('class', 'edgelabel')
                .attr('id', function (d, i) {return 'edgelabel' + i})
                .attr('font-size', 12)
                .attr('fill', 'black');

            d3.selectAll(".edgelabel").append('textPath')
                .attr('xlink:href', function (d, i) {return '#edgepath' + i})
                .style("text-anchor", "middle")
                .style("pointer-events", "none")
                .attr("startOffset", "50%")
                .text(function (d) {return d.type});

        }


        function ticked() {
            var node = d3.select(".nodes")
                .selectAll("circle")
                .attr('cx', function(d) {return d.x})
                .attr('cy', function(d) {return d.y});

            var link = d3.select(".links")
                .selectAll("line")
                .attr("x1", function(d) { return d.source.x; })
                .attr("y1", function(d) { return d.source.y; })
                .attr("x2", function(d) { return d.target.x; })
                .attr("y2", function(d) { return d.target.y; });

            d3.selectAll(".edgepath").attr('d', function (d) {
                return 'M ' + d.source.x + ' ' + d.source.y + ' L ' + d.target.x + ' ' + d.target.y;
            });

            d3.selectAll(".edgelabel").attr('transform', function (d) {
                if (d.target.x < d.source.x) {
                    var bbox = this.getBBox();

                    rx = bbox.x + bbox.width / 2;
                    ry = bbox.y + bbox.height / 2;
                    return 'rotate(180 ' + rx + ' ' + ry + ')';
                }
                else {
                    return 'rotate(0)';
                }
            });
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
          if (d.group == 3 || d.group == 1){
            d.fx = null;
            d.fy = null;
          }
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
