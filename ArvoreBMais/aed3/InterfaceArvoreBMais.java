/*
REGISTRO ÁRVORE B+

Esta interface apresenta os métodos que os objetos
a serem incluídos na árvore B+ devem 
conter.

Implementado pelo Prof. Marcos Kutova
v1.0 - 2021
*/
package aed3;

import java.io.IOException;

public interface InterfaceArvoreBMais<T> {

  public short size(); // tamanho FIXO do registro

  public byte[] serialize() throws IOException; // representação do elemento em um vetor de bytes

  public void deserialize(byte[] ba) throws IOException; // vetor de bytes a ser usado na construção do elemento

  public int compareTo(T obj); // compara dois elementos

  public T clone(); // clonagem de objetos

}
