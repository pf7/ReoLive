package widgets

import org.scalajs.dom.raw.{Event, MessageEvent, WebSocket}

/**
  * Offers reusable functions to use when building widgets that require interaction with a server.
  */
object RemoteBox {

  /**
    * Sends a call using websockets to a `service` with a message `send`,
    * and reacts to the result by invoking a `callback` function.
    * @param service name of the service to be called.
    * @param send message to be sent.
    * @param callback function that is called when receiving the message.
    */
  def remoteCall(service: String, send:String, callback: String=>Unit): Unit = {
    val socket = new WebSocket(s"ws://localhost:9000/$service")

    socket.onmessage = { e: MessageEvent => {callback(e.data.toString); socket.close()}}

    val cleanSend = send.replace("\\","\\\\")
                        .replace("\n","\\n")

    socket.addEventListener("open", (e: Event) => {
        socket.send(cleanSend)
    })
  }
}
