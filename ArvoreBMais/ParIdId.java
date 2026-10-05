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

public class ParIdId implements aed3.InterfaceArvoreBMais<ParIdId> {

  private int id1;
  private int id2;
  private static final short TAMANHO = 8;

  public ParIdId() {
    this(-1, -1);
  }

  public ParIdId(int n1) {
    this(n1, -1);
  }

  public ParIdId(int n1, int n2) {
    try {
      this.id1 = n1; // ID da entidade agregadora
      this.id2 = n2; // ID da outra entidade
    } catch (Exception ec) {
      ec.printStackTrace();
    }
  }

  @Override
  public ParIdId clone() {
    return new ParIdId(this.id1, this.id2);
  }

  public short size() {
    return this.TAMANHO;
  }

  public int compareTo(ParIdId a) {
    int c = Integer.compare(this.id1, a.id1);
    if (c != 0)
      return c;
    return Integer.compare(this.id2, a.id2);
  }

  public int compareToKey(ParIdId a) {
    return Integer.compare(this.id1, a.id1);
  }

  public int compareToKeyRead(ParIdId a) {
    return compareToKey(a);
  }

  public String toString() {
    return String.format("%3d", this.id1) + ";" + String.format("%-3d", this.id2);
  }

  public byte[] serialize() throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(baos);
    dos.writeInt(this.id1);
    dos.writeInt(this.id2);
    return baos.toByteArray();
  }

  public void deserialize(byte[] ba) throws IOException {
    ByteArrayInputStream bais = new ByteArrayInputStream(ba);
    DataInputStream dis = new DataInputStream(bais);
    this.id1 = dis.readInt();
    this.id2 = dis.readInt();
  }

}