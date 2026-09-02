package aed3;
/*

Esta classe representa um PAR CHAVE VALOR (PCV) 
para uma entidade Pessoa. Seu objetivo é representar
uma entrada de índice. 

Esse índice será secundário e indireto, baseado no
email de uma pessoa. Ao fazermos a busca por pessoa,
ele retornará o ID dessa pessoa, para que esse ID
possa ser buscado em um índice direto (que não é
apresentado neste projeto)

Um índice direto de ID precisaria ser criado por meio
de outra classe, cujos dados fossem um int para o ID
e um long para o endereço
 
Implementado pelo Prof. Marcos Kutova
v1.1 - 2026
 
*/

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ParEmailID implements aed3.InterfaceHashExtensivel {

  private String email;  // máximo de 40 bytes
  private int id;
  private short TAMANHO = 44;

  public ParEmailID() {
    this("", -1);
  }

  public ParEmailID(String e, int i) {
    try {
      if (e.getBytes().length + 4 > TAMANHO)
        throw new Exception("Número de caracteres do email maior que o permitido.");
    } catch (Exception ec) {
      System.err.println(ec.getMessage());
    }
    this.email = e;
    this.id = i;
  }

  @Override
  public int hashCode() {
    return Math.abs(this.email.hashCode());
  }

   @Override
   public short size() {
    return this.TAMANHO;
  }

   @Override
   public String toString() {
    return this.email + ";" + this.id;
  }

  @Override
   public byte[] serialize() throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(baos);
    byte[] vb = email.getBytes();
    dos.write(vb);
    for (int i = vb.length; i < TAMANHO-4; i++)
      dos.writeByte(' ');
    dos.writeInt(id);
    return baos.toByteArray();
  }

  @Override
  public void deserialize(byte[] ba) throws IOException {
    ByteArrayInputStream bais = new ByteArrayInputStream(ba);
    DataInputStream dis = new DataInputStream(bais);
    byte[] vb = new byte[40];
    dis.read(vb);
    this.email = (new String(vb)).trim();
    this.id = dis.readInt();
  }

  public static int hash(String email) {
    return Math.abs(email.hashCode());
  }

}