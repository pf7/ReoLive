package D3Lib

import org.singlespaced.d3js.d3
import preo.backend._

object GraphsToJS {
  def apply(graph: Graph): String = {
    println(graph)
    val nodes = getNodes(graph);
    val links = getLinks(graph);
    s"""
        var svg = d3.select("svg");
        var vbox = svg.attr('viewBox').split(" ")
        var width = vbox[2]; //svg.attr("width");
        var height = vbox[3];  //svg.attr("height");
        var radius = 7.75;
        var rectangle_width = 40;
        var rectangle_height = 20;

        var graph = {"nodes": $nodes, "links": $links};

        var simulation = d3.forceSimulation(graph.nodes)
          .force('charge', d3.forceManyBody().strength(-700))
          .force('center', d3.forceCenter(width / 2, height / 2))
          .force('y', d3.forceY().y(function(d) { return 0;}))
          .force('x', d3.forceX().x(function(d) {
            if (d.group >= 3){
              return width/2;
            }
            if (d.group <=1){
              return -width/2;
            }
            return 0;
          }))
          .force('collision', d3.forceCollide().radius(function(d) {
            return d.radius}))
          .force("link", d3.forceLink().links(graph.links).id(function(d) { return d.id; }).distance(45))
          //.force("forcepos", forcepos)
          .on('tick', ticked);

        init(graph.nodes, graph.links);

        function init(nodes, links){
            //add nodes
            var node = d3.select(".nodes")
                .selectAll("circle")
                .data(nodes.filter(function(d){return d.group >0 && d.group < 4}));
            node.enter()
                .append("circle")
                .merge(node)
                .attr("r", radius - 0.75)
                .attr("id", function (d) {return d.id;})
                .call(d3.drag()
                .on("start", dragstarted)
                .on("drag", dragged)
                .on("end", dragended))
                .style("stroke-opacity" , "1")
                .style("stroke-width", "2px")
                .style("stroke", function(d){
                   if(d.group == 1 || d.group == 3){
                     return "black";
                   }
                   else{
                     return "white";
                   }
                   }
                )
                .style("fill", function(d){
                  if(d.group == 1 || d.group == 3){
                    return "white";
                  }
                  else{
                    return "black";
                  }
                });
            node.exit().remove();

            var rects = d3.select(".nodes")
              .selectAll("rect")
              .data(nodes.filter(function(d){
                return d.group == 0 || d.group == 4
              }));

            rects.enter()
                .append("rect")
                .merge(rects)
                 .attr('width', rectangle_width)
                 .attr('height', rectangle_height)
                 .attr("id", function (d) {return d.id;})
                 .call(d3.drag()
                   .on("start", dragstarted)
                   .on("drag", dragged)
                   .on("end", dragended))
                 .style("stroke", "black")
                 .attr("fill", "#ffd896");

            rects.exit().remove();


             //add links
             var link = d3.select(".links")
                .selectAll("line")
                .data(links);
             link.enter().append("line")
                .style("stroke", "black")
                .merge(link)
                .attr('marker-end', function(d){
                  return 'url(#' + d.end + ')'
                })
                .attr('marker-start', function(d){
                  return 'url(#' + d.start + ')'
                })
                .style('stroke-dasharray', function(d){
                  if(d.type === "lossy"){
                    return ("3, 3");
                  }
                  else {
                    return ("1, 0");
                  }}) ;


            link.append("title")
                .text(function (d) {return d.type;});
            link.exit().remove();

            //add labels to graph
            var edgepaths = svg.select(".paths").selectAll(".edgepath")
                .data(links);
            edgepaths.enter()
                .append('path')
                .attr('class', 'edgepath')
                .attr('fill-opacity', 0)
                .attr('stroke-opacity', 0)
                .attr('id', function (d, i) {return 'edgepath' + i})
                .style("pointer-events", "none");
            edgepaths.exit().remove();

            var edgelabels = svg.select(".labels").selectAll(".edgelabel")
                .data(links);
            edgelabels.enter()
                .append('text')
                .style("pointer-events", "none")
                .attr('class', 'edgelabel')
                .attr('id', function (d, i) {return 'edgelabel' + i})
                .attr('font-size', 14)
                .attr('fill', 'black');
            edgelabels.exit().remove();

            d3.select(".labels").selectAll("textPath").remove();

            var textpath = d3.select(".labels").selectAll(".edgelabel").append('textPath')
                .attr('xlink:href', function (d, i) {return '#edgepath' + i})
                .style("text-anchor", "middle")
                .style("pointer-events", "none")
                .attr("startOffset", "50%")
                .text(function (d) {
                  if(d.type === "drain" || d.type === "lossy" || d.type === "merger" || d.type === "sync"){
                    return "";
                  }
                  else{
                    return d.type;
                  }
                });
        }

        function ticked() {
            var node = d3.select(".nodes")
                .selectAll("circle")
                .attr('cx', function(d) {return d.x = Math.max(radius, Math.min(width - radius, d.x)); })
                .attr('cy', function(d) {return d.y = Math.max(radius, Math.min(height - radius, d.y)); });

            var rect = d3.select(".nodes")
               .selectAll("rect")
               .attr('x', function(d) {
                  if(d.group == 0){
                    return d.x - rectangle_width;
                  }
                  else{
                    return d.x -rectangle_width /10;
                  }
               })
               .attr('y', function(d) {return d.y- rectangle_height/2;});

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
      val nodeGroup = typeToGroup(nodeType, style);
      s"""{"id": "$id", "group": $nodeGroup }"""
    }
    case ReoNode(id, name, nodeType, style) :: y :: rest => {
      val nodeGroup = typeToGroup(nodeType, style);
      s"""{"id": "$id", "group": $nodeGroup },""" + processNodes(y::rest)
    }
    case Nil => ""
  }

  private def typeToGroup(nodeType: NodeType, style: Option[String]):String = (nodeType, style) match{
    case (Source, Some(s)) => if(s.contains("component")) "0" else "1"
    case (Source, None) => "1"
    case (Sink, None) => "3"
    case (Sink, Some(s)) => if(s.contains("component")) "4" else "3"
    case (Mixed, _) => "2"
  }

  private def processEdges(channels: List[ReoChannel]): String = channels match{
    case ReoChannel(src,trg, srcType, trgType, name, style) :: Nil => {
      var start = arrowToString(srcType);
      var end = arrowToString(trgType);
      s"""{"source": "$src", "target": "$trg", "type":"$name", "start":"start$start", "end": "end$end"}"""
    }
    case ReoChannel(src,trg, srcType, trgType, name, style) :: y :: rest  => {
      var start = arrowToString(srcType);
      var end = arrowToString(trgType);
      s"""{"source": "$src", "target": "$trg", "type":"$name", "start":"start$start", "end": "end$end"},""" + processEdges(y::rest)
    }
    case Nil => ""
  }

  private def arrowToString(endType: EndType): String = endType match{
    case ArrowIn => "arrowin"
    case ArrowOut => "arrowout"
    case _ => ""
  }
}


