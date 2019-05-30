package reoalloy

/**
  * Objeto que representa um conetor de uma rede que representa o circuito Reo.
  * @param id Identificador do conetor de base
  * @param entradas Nodos de entrada
  * @param saidas Nodos de saída
  */
class Nodo(var id : String, var entradas : List[Int], var saidas : List[Int] ) {}
