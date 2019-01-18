package common.frontend

import ifta.backend.Show
import ifta.{Edge, IFTA}

/**
  * Created by guille on 12/12/2018
  */


object IFTAToJS {

  def apply(ifta:IFTA): String = {
    val edgesIndex = ifta.edges.map(e => (e,(e.act,e.fe).hashCode()))//ifta.edges.zipWithIndex
    generateJS(getNodes(ifta.init, edgesIndex), getLinks(edgesIndex))
  }

  private def generateJS(nodes: String, edges: String): String = {

    s"""
        buildAut();
        function buildAut() {
          var svgAut = d3.select("#iftaAutomata");
          var vboxAut = svgAut.attr('viewBox').split(" ")
          var widthAut = vboxAut[2]; //svgAut.attr("widthAut");
          var heightAut = vboxAut[3];  //svgAut.attr("heightAut");
          var radiusAut = 4.75;

          var graphAut = {"nodesautomata": ${nodes}, "linksautomata": ${edges}};

          var simulationAut = d3.forceSimulation(graphAut.nodesautomata)
            .force('charge', d3.forceManyBody().strength(-200))
            .force('center', d3.forceCenter(widthAut / 2, heightAut / 2))
            .force('y', d3.forceY().y(function(d) { return 0;}))
            .force('x', d3.forceX().x(function(d) {
        //              if (d.group == "1"){
        //                return widthAut;
        //              }
        //              if (d.group == "0"){
        //                return -widthAut;
        //              }
              return 0;
            }))
            .force('collision', d3.forceCollide().radius(function(d) {
                         return d.radius}))
            .force("link", d3.forceLink().links(graphAut.linksautomata).id(function(d) { return d.id; }).distance(5))
            //.force("forcepos", forcepos)
            .on('tick', tickedAut);

          initAut(graphAut.nodesautomata, graphAut.linksautomata);

          function initAut(nodesAut, linksAut){
              //add nodes (nodes "circle" with group 0..2)
              var node = d3.select(".nodesiftaAutomata")
                  .selectAll("circle")
                  .data(nodesAut);
        //              var nodeG = nodeout
        //                  .enter();
        //                  .append("g")
        //                  .attr("class","node");
        //              nodeG.append("text")
        //                  .attr("dx", 12)
        //                  .attr("dy", ".35em")
        //                  .text(function(d) {
        //                     if (d.group == 0 || d.group == 1 || d.group == 2) {
        //                       return "";
        //                     }
        //                     else return d.group;
        //                   });
        //              nodeG.append("circle")
                node.enter().append("circle")
                  .merge(node)
                  .attr("r", function(d){
                    if(d.group == 0 || d.group == 1){
                      return radiusAut + 0.75;
                    }
                    else{
                      return 0;
                    }
                  })
                  .attr("id", function (d) {return d.id;})
                  .call(d3.drag()
                  .on("start", dragstartedAut)
                  .on("drag", draggedAut)
                  .on("end", dragendedAut))
                  .style("stroke-opacity" , "1")
                  .style("stroke-widthAut", function(d){
                    if(d.group == 0 || d.group == 1) {
                      return "1px";
                    } else {
                      return "0px";
                    }
                  })
                  .style("stroke", "black")
                  .style("fill", function(d){
                    if(d.group == 0) {
                      return "white";
                    }
                    else if (d.group==1) {
                      return "black";
                    }
                    else{
                      return "green";
                    }
                  })
                  ;

              node.exit().remove();


               //add links
               var link = d3.select(".linksiftaAutomata")
                  .selectAll("polyline")
                  .data(linksAut);
               link.enter().append("polyline")
                  .style("stroke", "black")
                  .merge(link)
                  .attr('id', function(d) {return d.id})
                  .attr('marker-end', function(d){
                    return 'url(#' + d.end + ')'
                  })
                  .attr('marker-start', function(d){
                    return 'url(#' + d.start + ')'
                  })
                  .style('stroke-dasharray',"1, 0");


              link.append("title")
                  .text(function (d) {return d.type;});
              link.exit().remove();

              //add labels to graphAut
              var edgepaths = svgAut.select(".pathsiftaAutomata")
                  .selectAll(".edgepath")
                  .data(linksAut);
              edgepaths.enter()
                  .append('path')
                  .attr('class', 'edgepath')
                  .attr('fill-opacity', 0)
                  .attr('stroke-opacity', 0)
                  .attr('id', function (d, i) {return 'iftaedgepath' + i})
                  .style("pointer-events", "none");
              edgepaths.exit().remove();

              var edgelabels = svgAut.select(".labelsiftaAutomata")       // for all labels in data
                  .selectAll(".edgelabel")
                  .data(linksAut);
              edgelabels.enter()
                  .append('text')
                  .style("pointer-events", "none")
                  .attr('class', 'edgelabel')
                  .attr('id', function (d, i) {return 'iftaedgelabel' + i})
                  .attr('font-size', 10)
                  .attr('fill', 'black');
              edgelabels.exit().remove();

              d3.select(".labelsiftaAutomata")
                  .selectAll("textPath").remove();

              var textpath = d3.select(".labelsiftaAutomata")
                  .selectAll(".edgelabel")
                  .append('textPath')
                  .attr('xlink:href', function (d, i) {return '#iftaedgepath' + i})
                  .style("text-anchor", "middle")
                  .style("pointer-events", "none")
                  .attr("startOffset", "50%")
                  .text(function (d) {
                    return d.type;
                  });

          }

          function tickedAut() {
              var node = d3.select(".nodesiftaAutomata")
                  .selectAll("circle")
                  .attr('cx', function(d) {return d.x = Math.max(radiusAut, Math.min(widthAut - radiusAut, d.x)); })
                  .attr('cy', function(d) {return d.y = Math.max(radiusAut, Math.min(heightAut - radiusAut, d.y)); });

              var link = d3.select(".linksiftaAutomata")
                  .selectAll("polyline")
                  .attr("points", function(d) {
                      return d.source.x + "," + d.source.y + " " +
                      (d.source.x + d.target.x)/2 + "," + (d.source.y + d.target.y)/2 + " " +
                      d.target.x + "," + d.target.y; });
        //                .attr("x1", function(d) { return d.source.x; })
        //                .attr("y1", function(d) { return d.source.y; })
        //                .attr("x2", function(d) { return d.target.x; })
        //                .attr("y2", function(d) { return d.target.y; });
              d3.select(".pathsiftaAutomata").selectAll(".edgepath").attr('d', function (d) {
                  m = (d.target.y - d.source.y)/(d.target.x - d.source.x);
                  b = d.target.y - m*d.target.x;
                  new_source_x = d.source.x - 2000;
                  new_target_x = d.target.x + 2000;
                  new_source_y = new_source_x * m  +b;
                  new_target_y = new_target_x * m +b;
                  return 'M ' + new_source_x +' '+ new_source_y  +' L '+ new_target_x +' '+ new_target_y;
              });
              d3.select(".labelsiftaAutomata").selectAll(".edgelabel").attr('transform', function (d) {
                new_source_x = d.source.x - 2000;
                new_target_x = d.target.x + 2000;
                  if (new_target_x < new_source_x) {
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


          function dragstartedAut(d) {
            if (!d3.event.active) simulationAut.alphaTarget(0.3).restart();
            d.fx = d.x;
            d.fy = d.y;
          }
          function draggedAut(d) {
            d.fx = d3.event.x;
            d.fy = d3.event.y;
          }
          function dragendedAut(d) {
            if (!d3.event.active) simulationAut.alphaTarget(0);
            if (d.group == 3 || d.group == 1){
              d.fx = null;
              d.fy = null;
            }
          }
        }
      """
  }

  private def getNodes(init:Int, edges:Set[(Edge,Int)]): String =
    edges.flatMap(e => processNode(init,e._1,e._2)).mkString("[",",","]")
//    ifta.edges.flatMap(processNode(ifta.init, _)).mkString("[",",","]")

  private def getLinks(edges:Set[(Edge,Int)]): String =
    edges.flatMap(e => processEdge(e._1,e._2)).mkString("[",",","]")
//    ifta.edges.flatMap(processEdge).mkString("[",",","]")


  private def processNode(init:Int,edge:Edge, id:Int): Set[String] = edge match{
    case Edge(from,cCons,acts,cReset,fe,to) =>
      val (gfrom,gto,gp1,gp2) = nodeGroups(init,from,to)
      Set(s"""{"id": "$from", "group": $gfrom }""",
        s"""{"id": "$to", "group": $gto }""",
        s"""{"id": "$from-1-$to-${id}", "group": "$gp1"}""",
        s"""{"id": "$to-2-$from-${id}", "group": "$gp2" }""")
  }

  //  private def processNode(initAut:Int,trans:(Int,(Int,Set[Int],Set[Edge]))): Set[String] = trans match{
  //    case (from,(to,fire,es)) =>
  //      val (gfrom,gto,gp1,gp2) = nodeGroups(initAut,from,to)
  //      Set(s"""{"id": "$from", "group": $gfrom }""",
  //          s"""{"id": "$to", "group": $gto }""",
  //          s"""{"id": "$from-1-$to-${fire.mkString(".")}", "group": $gp1}""",
  //          s"""{"id": "$to-2-$from-${fire.mkString(".")}", "group": $gp2 }""")
  //  }

  /**
    * Select the right group:
    *  - 0: initial state
    *  - 1: normal state
    *  - otherwise: connection dot
    */
  private def nodeGroups(initAut:Int,from:Int,to:Int):(String,String,String,String) =
    (   if(from==initAut) "0" else "1"
      , if(to==initAut) "0" else "1"
      , "2" , "2"
    )

  private def processEdge(edge:Edge, id:Int): Set[String] = edge match {
    case Edge(from,cCons,acts,cReset,fe,to) =>
      Set(s"""{"id": "${id}" , "source": "$from", "target": "$from-1-$to-${id}", "type":"", "start":"start", "end": "end"}""",
        s"""{"id": "${id}" , "source": "$from-1-$to-${id}", "target": "$to-2-$from-${id}", "type":"${acts.mkString(".")}", "start":"start", "end": "end"}""",
        s"""{"id": "${id}" , "source": "$to-2-$from-${id}", "target": "$to", "type":"", "start":"start", "end": "endarrowoutiftaAutomata"}""")
  }
  //  private def processEdge(trans:(Int,(Int,Set[Int],Set[Edge]))): Set[String] = trans match {
  //    case (from, (to, fire, es)) => {
  //      Set(s"""{"source": "$from", "target": "$from-1-$to-${fire.mkString(".")}", "type":"", "start":"start", "end": "end"}""",
  //          s"""{"source": "$from-1-$to-${fire.mkString(".")}", "target": "$to-2-$from-${fire.mkString(".")}", "type":"${fire.mkString(".")}", "start":"start", "end": "end"}""",
  //          s"""{"source": "$to-2-$from-${fire.mkString(".")}", "target": "$to", "type":"", "start":"start", "end": "endarrowoutautomata"}""")
  //    }
  //  }
}
