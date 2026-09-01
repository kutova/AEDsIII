/*
REGISTRO HASH EXTENSÍVEL

Esta interface apresenta os métodos que os objetos
a serem incluídos na tabela hash extensível devem 
conter.

Implementado pelo Prof. Marcos Kutova
v1.1 - 2021
*/
package aed3;

import java.io.IOException;

public interface InterfaceHashExtensivel {

  public int hashCode(); // chave numérica positiva para ser usada na busca de uma posição no diretório

  public short size(); // tamanho FIXO do objeto a ser armazenado em cada posição do cesto

  public byte[] serialize() throws IOException; // representação do elemento em um vetor de bytes

  public void deserialize(byte[] ba) throws IOException; // vetor de bytes a ser usado na construção do elemento

}
