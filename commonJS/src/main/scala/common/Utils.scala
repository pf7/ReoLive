package common

import java.net.{URLDecoder}

object Utils {

  /**
    * Parses the "search" attribute of a uri, e.g., "?q=abc&p=2"
    * @param str text to be parsed
    * @return mapping from parameter names to their values
    */
  def parseSearchUri(str: String): Map[String,String] = {
    var res = Map[String,String]()
    if (str.startsWith("?")) {
      for (pair <- str.tail.split("&"))
        pair.split("=",2) match {
          case Array(name, value) =>
            res += name ->  URLDecoder.decode(value,"UTF8")
          case _ =>
        }
    }
    res
  }

  /**
    * Builds the URL part defining a given set of attributes.
    * E.g., from Map("a"->"b") builds "?a=b"
    */
  def buildSearchUri(keys:Iterable[(String,String)]): String = {
    if (keys.isEmpty) ""
    else "?"+keys.map(p=>s"${p._1}=${p._2
        .replaceAll("\n","%0A")
        .replaceAll(" ","%20")
      }").mkString("&")
  }


}
