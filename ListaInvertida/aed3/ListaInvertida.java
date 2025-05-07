/*********
 * LISTA INVERTIDA
 * String termo, int dado
 * 
 * Os nomes dos métodos foram mantidos em inglês
 * apenas para manter a coerência com o resto da
 * disciplina:
 * - boolean create(String termo, int dado)
 * - int[] read(int termo)
 * - boolean delete(String termo, int dado)
 * 
 * Implementado pelo Prof. Marcos Kutova
 * v1.0 - 2020
 */
package aed3;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class ListaInvertida {

  String nomeArquivoDicionario;
  String nomeArquivoBlocos;
  RandomAccessFile arqDicionario;
  RandomAccessFile arqBlocos;
  int quantidadeDadosPorBloco;

  class Bloco {

    short quantidade; // quantidade de dados presentes na lista
    short quantidadeMaxima; // quantidade máxima de dados que a lista pode conter
    ElementoLista[] elementos; // sequência de dados armazenados no bloco
    long proximo; // ponteiro para o bloco sequinte do mesmo termo
    short bytesPorBloco; // size fixo do cesto em bytes

    public Bloco(int qtdmax) throws Exception {
      quantidade = 0;
      quantidadeMaxima = (short) qtdmax;
      elementos = new ElementoLista[quantidadeMaxima];
      proximo = -1;
      bytesPorBloco = (short) (2 + (4+4) * quantidadeMaxima + 8);  // 4 do INT e 4 do FLOAT
    }

    public byte[] toByteArray() throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      DataOutputStream dos = new DataOutputStream(baos);
      dos.writeShort(quantidade);
      int i = 0;
      while (i < quantidade) {
        dos.writeInt(elementos[i].getId());
        dos.writeFloat(elementos[i].getFrequencia());
        i++;
      }
      while (i < quantidadeMaxima) {
        dos.writeInt(-1);
        dos.writeFloat(-1);
        i++;
      }
      dos.writeLong(proximo);
      return baos.toByteArray();
    }

    public void fromByteArray(byte[] ba) throws IOException {
      ByteArrayInputStream bais = new ByteArrayInputStream(ba);
      DataInputStream dis = new DataInputStream(bais);
      quantidade = dis.readShort();
      int i = 0;
      while (i < quantidadeMaxima) {
        elementos[i] = new ElementoLista(dis.readInt(), dis.readFloat());
        i++;
      }
      proximo = dis.readLong();
    }

    // Insere um valor no bloco
    public boolean create(ElementoLista e) {
      if (full())
        return false;
      int i = quantidade - 1;
      while (i >= 0 && e.getId() < elementos[i].getId()) {
        elementos[i + 1] = elementos[i];
        i--;
      }
      i++;
      elementos[i] = e.clone();
      quantidade++;
      return true;
    }

    // Testa se um valor existe no bloco
    public boolean test(int id) {
      if (empty())
        return false;
      int i = 0;
      while (i < quantidade && id > elementos[i].getId())
        i++;
      if (i < quantidade && id == elementos[i].getId())
        return true;
      else
        return false;
    }

    // Lê um valor existente no bloco
    public ElementoLista read(int id) {
      if (empty())
        return null;
      int i = 0;
      while (i < quantidade && id > elementos[i].getId())
        i++;
      if (i < quantidade && id == elementos[i].getId())
        return elementos[i].clone();
      else
        return null;
    }

    // Remove um valor do bloco
    public boolean update(ElementoLista e) {
      if (empty())
        return false;
      int i = 0;
      while (i < quantidade && e.getId() > elementos[i].getId())
        i++;
      if (e.getId() == elementos[i].getId()) {
        elementos[i] = e.clone();
        return true;
      } else
        return false;
    }

    // Remove um valor do bloco
    public boolean delete(int id) {
      if (empty())
        return false;
      int i = 0;
      while (i < quantidade && id > elementos[i].getId())
        i++;
      if (id == elementos[i].getId()) {
        while (i < quantidade - 1) {
          elementos[i] = elementos[i + 1];
          i++;
        }
        quantidade--;
        return true;
      } else
        return false;
    }

    public ElementoLista last() {
      return elementos[quantidade - 1];
    }

    public ElementoLista[] list() {
      ElementoLista[] lista = new ElementoLista[quantidade];
      for (int i = 0; i < quantidade; i++)
        lista[i] = elementos[i].clone();
      return lista;
    }

    public boolean empty() {
      return quantidade == 0;
    }

    public boolean full() {
      return quantidade == quantidadeMaxima;
    }

    public String toString() {
      String s = "\nQuantidade: " + quantidade + "\n| ";
      int i = 0;
      while (i < quantidade) {
        s += elementos[i] + " | ";
        i++;
      }
      while (i < quantidadeMaxima) {
        s += "- | ";
        i++;
      }
      return s;
    }

    public long next() {
      return proximo;
    }

    public void setNext(long p) {
      proximo = p;
    }

    public int size() {
      return bytesPorBloco;
    }

  }

  public ListaInvertida(int n, String nd, String nc) throws Exception {
    quantidadeDadosPorBloco = n;
    nomeArquivoDicionario = nd;
    nomeArquivoBlocos = nc;

    arqDicionario = new RandomAccessFile(nomeArquivoDicionario, "rw");
    if(arqDicionario.length()<4) {    // cabeçalho do arquivo com número de entidades
      arqDicionario.seek(0);
      arqDicionario.writeInt(0);
    }
    arqBlocos = new RandomAccessFile(nomeArquivoBlocos, "rw");
  }

  // Incrementa o número de entidades
  public void incrementaEntidades() throws Exception {
    arqDicionario.seek(0);
    int n = arqDicionario.readInt();
    arqDicionario.seek(0);
    arqDicionario.writeInt(n+1);    
  }

  // Decrementa o número de entidades
  public void decrementaEntidades() throws Exception {
    arqDicionario.seek(0);
    int n = arqDicionario.readInt();
    arqDicionario.seek(0);
    arqDicionario.writeInt(n-1);    
  }

  // Retorna o número de entidades
  public int numeroEntidades() throws Exception {
    arqDicionario.seek(0);
    return arqDicionario.readInt();
  }

  // Insere um dado na lista do termo de forma NÃO ORDENADA
  public boolean create(String c, ElementoLista e) throws Exception {

    // Percorre toda a lista testando se já não existe
    // o dado associado a esse termo
    ElementoLista[] lista = read(c);
    for (int i = 0; i < lista.length; i++)
      if (lista[i].getId() == e.getId())
        return false;

    String termo = "";
    long endereco = -1;

    // localiza o termo no dicionário
    boolean existe = false;
    arqDicionario.seek(4);
    while (arqDicionario.getFilePointer() != arqDicionario.length()) {
      termo = arqDicionario.readUTF();
      endereco = arqDicionario.readLong();
      if (termo.compareTo(c) == 0) {
        existe = true;
        break;
      }
    }

    // Se não encontrou, cria um novo bloco para esse termo
    if (!existe) {
      // Cria um novo bloco
      Bloco b = new Bloco(quantidadeDadosPorBloco);
      endereco = arqBlocos.length();
      arqBlocos.seek(endereco);
      arqBlocos.write(b.toByteArray());

      // Insere o novo termo no dicionário
      arqDicionario.seek(arqDicionario.length());
      arqDicionario.writeUTF(c);
      arqDicionario.writeLong(endereco);
    }

    // Cria um laço para percorrer todos os blocos encadeados nesse endereço
    Bloco b = new Bloco(quantidadeDadosPorBloco);
    byte[] bd;
    while (endereco != -1) {
      long proximo = -1;

      // Carrega o bloco
      arqBlocos.seek(endereco);
      bd = new byte[b.size()];
      arqBlocos.read(bd);
      b.fromByteArray(bd);

      // Testa se o dado cabe nesse bloco
      if (!b.full()) {
        b.create(e);
      } else {
        // Avança para o próximo bloco
        proximo = b.next();
        if (proximo == -1) {
          // Se não existir um novo bloco, cria esse novo bloco
          Bloco b1 = new Bloco(quantidadeDadosPorBloco);
          proximo = arqBlocos.length();
          arqBlocos.seek(proximo);
          arqBlocos.write(b1.toByteArray());

          // Atualiza o ponteiro do bloco anterior
          b.setNext(proximo);
        }
      }

      // Atualiza o bloco atual
      arqBlocos.seek(endereco);
      arqBlocos.write(b.toByteArray());
      endereco = proximo;
    }
    return true;
  }

  // Retorna a lista de entidades completa de um determinado termo
  public ElementoLista[] read(String c) throws Exception {

    ArrayList<ElementoLista> lista = new ArrayList<>();

    String termo = "";
    long endereco = -1;

    // localiza o termo no dicionário
    boolean existe = false;
    arqDicionario.seek(4);
    while (arqDicionario.getFilePointer() != arqDicionario.length()) {
      termo = arqDicionario.readUTF();
      endereco = arqDicionario.readLong();
      if (termo.compareTo(c) == 0) {
        existe = true;
        break;
      }
    }
    if (!existe)
      return new ElementoLista[0];

    // Cria um laço para percorrer todos os blocos encadeados nesse endereço
    Bloco b = new Bloco(quantidadeDadosPorBloco);
    byte[] bd;
    while (endereco != -1) {

      // Carrega o bloco
      arqBlocos.seek(endereco);
      bd = new byte[b.size()];
      arqBlocos.read(bd);
      b.fromByteArray(bd);

      // Acrescenta cada valor à lista
      ElementoLista[] lb = b.list();
      for (int i = 0; i < lb.length; i++)
        lista.add(lb[i]);

      // Avança para o próximo bloco
      endereco = b.next();

    }

    // Constrói o vetor de respostas
    lista.sort(null);
    ElementoLista[] resposta = new ElementoLista[lista.size()];
    for (int j = 0; j < lista.size(); j++)
      resposta[j] = (ElementoLista) lista.get(j);
    return resposta;
  }

  // Retorna a frequência de um determinado termo em um determinado documento
  public ElementoLista read(String c, int id) throws Exception {

    String termo = "";
    long endereco = -1;

    // localiza o termo no dicionário
    boolean existe = false;
    arqDicionario.seek(4);
    while (arqDicionario.getFilePointer() != arqDicionario.length()) {
      termo = arqDicionario.readUTF();
      endereco = arqDicionario.readLong();
      if (termo.compareTo(c) == 0) {
        existe = true;
        break;
      }
    }
    if (!existe)
      return null;

    // Cria um laço para percorrer todos os blocos encadeados nesse endereço
    Bloco b = new Bloco(quantidadeDadosPorBloco);
    byte[] bd;
    while (endereco != -1) {

      // Carrega o bloco
      arqBlocos.seek(endereco);
      bd = new byte[b.size()];
      arqBlocos.read(bd);
      b.fromByteArray(bd);

      // Testa se o valor está neste bloco e sai do laço
      if (b.test(id)) 
        return b.read(id);

      // Avança para o próximo bloco
      endereco = b.next();
    }

    return null;
  }

  // Atualiza o dado de um termo, se ele existir
  public boolean update(String c, ElementoLista e) throws Exception {

    String termo = "";
    long endereco = -1;

    // localiza o termo no dicionário
    boolean existe = false;
    arqDicionario.seek(4);
    while (arqDicionario.getFilePointer() != arqDicionario.length()) {
      termo = arqDicionario.readUTF();
      endereco = arqDicionario.readLong();
      if (termo.compareTo(c) == 0) {
        existe = true;
        break;
      }
    }
    if (!existe)
      return false;

    // Cria um laço para percorrer todos os blocos encadeados nesse endereço
    Bloco b = new Bloco(quantidadeDadosPorBloco);
    byte[] bd;
    while (endereco != -1) {

      // Carrega o bloco
      arqBlocos.seek(endereco);
      bd = new byte[b.size()];
      arqBlocos.read(bd);
      b.fromByteArray(bd);

      // Testa se o valor está neste bloco e sai do laço
      if (b.test(e.getId())) {
        b.update(e);
        arqBlocos.seek(endereco);
        arqBlocos.write(b.toByteArray());
        return true;
      }

      // Avança para o próximo bloco
      endereco = b.next();
    }

    // termo não encontrado
    return false;

  }

  // Remove o dado de um termo (mas não apaga o termo nem apaga blocos)
  public boolean delete(String c, int id) throws Exception {

    String termo = "";
    long endereco = -1;

    // localiza o termo no dicionário
    boolean existe = false;
    arqDicionario.seek(4);
    while (arqDicionario.getFilePointer() != arqDicionario.length()) {
      termo = arqDicionario.readUTF();
      endereco = arqDicionario.readLong();
      if (termo.compareTo(c) == 0) {
        existe = true;
        break;
      }
    }
    if (!existe)
      return false;

    // Cria um laço para percorrer todos os blocos encadeados nesse endereço
    Bloco b = new Bloco(quantidadeDadosPorBloco);
    byte[] bd;
    while (endereco != -1) {

      // Carrega o bloco
      arqBlocos.seek(endereco);
      bd = new byte[b.size()];
      arqBlocos.read(bd);
      b.fromByteArray(bd);

      // Testa se o valor está neste bloco e sai do laço
      if (b.test(id)) {
        b.delete(id);
        arqBlocos.seek(endereco);
        arqBlocos.write(b.toByteArray());
        return true;
      }

      // Avança para o próximo bloco
      endereco = b.next();
    }

    // termo não encontrado
    return false;

  }

  public void print() throws Exception {

    System.out.println("\nLISTAS INVERTIDAS:");

    // Percorre todos os termos
    arqDicionario.seek(4);
    while (arqDicionario.getFilePointer() != arqDicionario.length()) {

      String termo = arqDicionario.readUTF();
      long endereco = arqDicionario.readLong();

      // Percorre a lista deste termo
      ArrayList<ElementoLista> lista = new ArrayList<>();
      Bloco b = new Bloco(quantidadeDadosPorBloco);
      byte[] bd;
      while (endereco != -1) {

        // Carrega o bloco
        arqBlocos.seek(endereco);
        bd = new byte[b.size()];
        arqBlocos.read(bd);
        b.fromByteArray(bd);

        // Acrescenta cada valor à lista
        ElementoLista[] lb = b.list();
        for (int i = 0; i < lb.length; i++)
          lista.add(lb[i]);

        // Avança para o próximo bloco
        endereco = b.next();
      }

      // Imprime o termo e sua lista
      System.out.print(termo + ": ");
      lista.sort(null);
      for (int j = 0; j < lista.size(); j++)
        System.out.print(lista.get(j) + " ");
      System.out.println();
    }
  }
}
