/*
Esta classe representa um objeto para uma entidade
que será armazenado em uma árvore B+

Neste caso em particular, este objeto é representado
por dois números inteiros para que possa conter
relacionamentos entre dois IDs de entidades quaisquer
 
Implementado pelo Prof. Marcos Kutova
v1.0 - 2021
*/

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ParIntInt implements aed3.RegistroArvoreBMais<ParIntInt> {

  private int num1;
  private int num2;
  private short TAMANHO = 8;

  public ParIntInt() {
    this(-1, -1);
  }

  public ParIntInt(int n1) {
    this(n1, -1);
  }

  public ParIntInt(int n1, int n2) {
    try {
      this.num1 = n1; // ID do Usuário
      this.num2 = n2; // ID da Pergunta
    } catch (Exception ec) {
      ec.printStackTrace();
    }
  }

  @Override
  public ParIntInt clone() {
    return new ParIntInt(this.num1, this.num2);
  }

  public short size() {
    return this.TAMANHO;
  }

  public int compareTo(ParIntInt a) {
    if (this.num1 != a.num1)
      return this.num1 - a.num1;
    else
      // Só compara os valores de Num2, se o Num2 da busca for diferente de -1
      // Isso é necessário para que seja possível a busca de lista
      return this.num2 == -1 ? 0 : this.num2 - a.num2;
  }

  public String toString() {
    return String.format("%3d", this.num1) + ";" + String.format("%-3d", this.num2);
  }

  public byte[] toByteArray() throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(baos);
    dos.writeInt(this.num1);
    dos.writeInt(this.num2);
    return baos.toByteArray();
  }

  public void fromByteArray(byte[] ba) throws IOException {
    ByteArrayInputStream bais = new ByteArrayInputStream(ba);
    DataInputStream dis = new DataInputStream(bais);
    this.num1 = dis.readInt();
    this.num2 = dis.readInt();
  }

}