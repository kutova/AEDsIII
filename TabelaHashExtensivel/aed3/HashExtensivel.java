/*
TABELA HASH EXTENSÍVEL

Os nomes dos métodos foram mantidos em inglês
apenas para manter a coerência com o resto da
disciplina:
- boolean create(T elemento)
- long read(int hashcode)
- boolean update(T novoElemento)   //  a chave (hashcode) deve ser a mesma
- boolean delete(int hashcode)

Implementado pelo Prof. Marcos Kutova
v1.1 - 2021
*/
package aed3;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;
import java.util.ArrayList;

public class HashExtensivel<T extends InterfaceHashExtensivel> {

  String nomeArquivoDiretorio;
  String nomeArquivoCestos;
  RandomAccessFile arqDiretorio;
  RandomAccessFile arqCestos;
  int quantidadeDadosPorCesto;
  Diretorio diretorio;
  Constructor<T> construtor;

  public class Cesto {

    Constructor<T> construtor;
    short quantidadeMaxima; // quantidade máxima de elementos que o cesto pode conter
    short bytesPorElemento; // tamanho fixo de cada elemento em bytes
    short bytesPorCesto; // tamanho fixo do cesto em bytes

    byte profundidadeLocal; // profundidade local do cesto
    short quantidade; // quantidade de elementos presentes no cesto
    ArrayList<T> elementos; // sequência de elementos armazenados

    public Cesto(Constructor<T> ct, int qtdmax) throws Exception {
      this(ct, qtdmax, 0);
    }

    public Cesto(Constructor<T> ct, int qtdmax, int pl) throws Exception {
      construtor = ct;
      if (qtdmax > 32767)   // assegura que a qtd pode ser do tipo short
        throw new Exception("Quantidade máxima de 32.767 elementos");
      if (pl > 20)          // assegura que o diretório não tenha mais que 8 MB (1 milhão de endereços)
        throw new Exception("Profundidade local máxima de 20 bits");
      profundidadeLocal = (byte) pl;
      quantidade = 0;
      quantidadeMaxima = (short) qtdmax;
      elementos = new ArrayList<>(quantidadeMaxima);
      bytesPorElemento = ct.newInstance().size();
      bytesPorCesto = (short) (bytesPorElemento * quantidadeMaxima + 3);
    }

    public byte[] serialize() throws Exception {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      DataOutputStream dos = new DataOutputStream(baos);
      dos.writeByte(profundidadeLocal);
      dos.writeShort(quantidade);
      int i = 0;
      while (i < quantidade) {
        dos.write(elementos.get(i).serialize());
        i++;
      }
      byte[] vazio = new byte[bytesPorElemento];
      while (i < quantidadeMaxima) {
        dos.write(vazio);
        i++;
      }
      return baos.toByteArray();
    }

    public void deserialize(byte[] ba) throws Exception {
      ByteArrayInputStream bais = new ByteArrayInputStream(ba);
      DataInputStream dis = new DataInputStream(bais);
      profundidadeLocal = dis.readByte();
      quantidade = dis.readShort();
      int i = 0;
      elementos = new ArrayList<>(quantidadeMaxima);
      byte[] dados = new byte[bytesPorElemento];
      T elem;
      while (i < quantidadeMaxima) {
        dis.read(dados);
        elem = construtor.newInstance();
        elem.deserialize(dados);
        elementos.add(elem);
        i++;
      }
    }

    // Inserir elementos no cesto
    public boolean create(T elem) {
      if (full())
        return false;
      int i = quantidade - 1; // posição do último elemento no cesto
      while (i >= 0 && elem.hashCode() < elementos.get(i).hashCode())
        i--;
      elementos.add(i + 1, elem);
      quantidade++;
      return true;
    }

    // Buscar um elemento no cesto
    public T read(int hashElem) {
      if (empty())
        return null;
      int i = 0;
      while (i < quantidade && hashElem > elementos.get(i).hashCode())
        i++;
      if (i < quantidade && hashElem == elementos.get(i).hashCode())
        return elementos.get(i);
      else
        return null;
    }

    // atualizar um elemento do cesto
    public boolean update(T elem) {
      if (empty())
        return false;
      int i = 0;
      while (i < quantidade && elem.hashCode() > elementos.get(i).hashCode())
        i++;
      if (i < quantidade && elem.hashCode() == elementos.get(i).hashCode()) {
        elementos.set(i, elem);
        return true;
      } else
        return false;
    }

    // pagar um elemento do cesto
    public boolean delete(int hashElem) {
      if (empty())
        return false;
      int i = 0;
      while (i < quantidade && hashElem > elementos.get(i).hashCode())
        i++;
      if (hashElem == elementos.get(i).hashCode()) {
        elementos.remove(i);
        quantidade--;
        return true;
      } else
        return false;
    }

    public boolean empty() {
      return quantidade == 0;
    }

    public boolean full() {
      return quantidade == quantidadeMaxima;
    }

    public String toString() {
      String s = "Profundidade Local: " + profundidadeLocal + "\nQuantidade: " + quantidade + "\n| ";
      int i = 0;
      while (i < quantidade) {
        s += elementos.get(i).toString() + " | ";
        i++;
      }
      while (i < quantidadeMaxima) {
        s += "- | ";
        i++;
      }
      return s;
    }

    public int size() {
      return bytesPorCesto;
    }

  }

  protected class Diretorio {

    byte profundidadeGlobal;
    long[] enderecos;

    public Diretorio() {
      profundidadeGlobal = 0;
      enderecos = new long[1];
      enderecos[0] = 0;
    }

    public byte[] serialize() throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      DataOutputStream dos = new DataOutputStream(baos);
      dos.writeByte(profundidadeGlobal);
      int quantidade = (int) Math.pow(2, profundidadeGlobal);
      int i = 0;
      while (i < quantidade) {
        dos.writeLong(enderecos[i]);
        i++;
      }
      return baos.toByteArray();
    }

    public void deserialize(byte[] ba) throws IOException {
      ByteArrayInputStream bais = new ByteArrayInputStream(ba);
      DataInputStream dis = new DataInputStream(bais);
      profundidadeGlobal = dis.readByte();
      int quantidade = (int) Math.pow(2, profundidadeGlobal);
      enderecos = new long[quantidade];
      int i = 0;
      while (i < quantidade) {
        enderecos[i] = dis.readLong();
        i++;
      }
    }

    public String toString() {
      String s = "Profundidade global: " + profundidadeGlobal;
      int i = 0;
      int quantidade = (int) Math.pow(2, profundidadeGlobal);
      while (i < quantidade) {
        s += "\n" + i + ": " + enderecos[i];
        i++;
      }
      return s;
    }

    // Para efeito de determinar o cesto em que o elemento deve ser inserido,
    // só serão considerados valores absolutos da chave (hashCode).
    protected int posicaoNoDiretorio(int hashCode) {
      return Math.abs(hashCode) % (int) Math.pow(2, profundidadeGlobal);
    }

    // Descobre o endereço do cesto indicado pela função hash() acima
    protected long endereçoDoCesto(int p) {
      if (p<0 || p >= Math.pow(2, profundidadeGlobal))
        return -1;
      return enderecos[p];
    }

    protected boolean duplica() {
      if (profundidadeGlobal == 20)  // limite máximo de 1.048.576 endereços
        return false;
      profundidadeGlobal++;
      int q1 = (int) Math.pow(2, profundidadeGlobal - 1);
      int q2 = (int) Math.pow(2, profundidadeGlobal);
      long[] novosEnderecos = new long[q2];
      int i = 0;
      while (i < q1) { // copia o vetor anterior para as duas metades do novo vetor
        novosEnderecos[i] = enderecos[i];
        novosEnderecos[i+q1] = enderecos[i];
        i++;
      }
      enderecos = novosEnderecos;
      return true;
    }

    public boolean atualizaEndereco(int p, long e) {
      if (p<0 || p >= Math.pow(2, profundidadeGlobal))
        return false;
      enderecos[p] = e;
      return true;
    }

    // Método auxiliar para atualizar endereço ao duplicar o diretório
    protected int primeiraPosicao(int hashCode, int profundidadeLocal) { // cálculo do hash para uma dada profundidade local
      return Math.abs(hashCode) % (int) Math.pow(2, profundidadeLocal);
    }

  }

  public HashExtensivel(Constructor<T> ct, int n, String nd, String nc) throws Exception {
    construtor = ct;
    quantidadeDadosPorCesto = n;
    nomeArquivoDiretorio = nd;
    nomeArquivoCestos = nc;

    arqDiretorio = new RandomAccessFile(nomeArquivoDiretorio, "rw");
    arqCestos = new RandomAccessFile(nomeArquivoCestos, "rw");

    // Se o diretório ou os cestos estiverem vazios, cria um novo diretório e lista
    // de cestos
    if (arqDiretorio.length() == 0 || arqCestos.length() == 0) {

      // Cria um novo diretório, com profundidade de 0 bits (1 único elemento)
      diretorio = new Diretorio();
      byte[] bd = diretorio.serialize();
      arqDiretorio.write(bd);

      // Cria um cesto vazio, já apontado pelo único elemento do diretório
      Cesto c = new Cesto(construtor, quantidadeDadosPorCesto);
      bd = c.serialize();
      arqCestos.seek(0);
      arqCestos.write(bd);
    }
  }

  public boolean create(T elem) throws Exception {

    // Carrega TODO o diretório para a memória
    byte[] bd = new byte[(int) arqDiretorio.length()];
    arqDiretorio.seek(0);
    arqDiretorio.read(bd);
    diretorio = new Diretorio();
    diretorio.deserialize(bd);

    // Identifica o endereço do cesto no diretório a partir do hashCode,
    int i = diretorio.posicaoNoDiretorio(elem.hashCode());
    long enderecoCesto = diretorio.endereçoDoCesto(i);

    // Recupera o cesto
    Cesto c = new Cesto(construtor, quantidadeDadosPorCesto);
    byte[] ba = new byte[c.size()];
    arqCestos.seek(enderecoCesto);
    arqCestos.read(ba);
    c.deserialize(ba);

    // Testa se a chave já existe no cesto
    if (c.read(elem.hashCode()) != null)
      throw new Exception("Elemento já existe");

    // Testa se o cesto já não está cheio
    // Se não estiver, create o par de chave e dado
    if (!c.full()) {
      // Insere a chave no cesto e o atualiza
      c.create(elem);
      arqCestos.seek(enderecoCesto);
      arqCestos.write(c.serialize());
      return true;
    }

    // Cria os novos cestos, com os seus dados no arquivo de cestos
    byte pl = c.profundidadeLocal;
    Cesto c1 = new Cesto(construtor, quantidadeDadosPorCesto, pl + 1);
    arqCestos.seek(enderecoCesto);
    arqCestos.write(c1.serialize());

    Cesto c2 = new Cesto(construtor, quantidadeDadosPorCesto, pl + 1);
    long novoEndereco = arqCestos.length();
    arqCestos.seek(novoEndereco);
    arqCestos.write(c2.serialize());

    // Duplica o diretório
    if (pl >= diretorio.profundidadeGlobal)
      diretorio.duplica();
    byte pg = diretorio.profundidadeGlobal;

    // Atualiza os endereços no diretório
    int inicio = diretorio.primeiraPosicao(elem.hashCode(), pl);
    int deslocamento = (int) Math.pow(2, pl);
    int max = (int) Math.pow(2, pg);
    boolean troca = false;
    for (int j = inicio; j < max; j += deslocamento) {
      if (troca)
        diretorio.atualizaEndereco(j, novoEndereco);
      troca = !troca;
    }

    // Atualiza o arquivo do diretório
    bd = diretorio.serialize();
    arqDiretorio.seek(0);
    arqDiretorio.write(bd);

    // Reinsere as chaves do cesto antigo
    for (int j = 0; j < c.quantidade; j++) {
      create(c.elementos.get(j));
    }
    create(elem); // insere o nome elemento
    return true;

  }

  public T read(int chave) throws Exception {

    // Carrega o diretório
    byte[] bd = new byte[(int) arqDiretorio.length()];
    arqDiretorio.seek(0);
    arqDiretorio.read(bd);
    diretorio = new Diretorio();
    diretorio.deserialize(bd);

    // Identifica o endereço do cesto no diretório a partir do hashCode,
    int i = diretorio.posicaoNoDiretorio(chave);
    long enderecoCesto = diretorio.endereçoDoCesto(i);

    // Recupera o cesto
    Cesto c = new Cesto(construtor, quantidadeDadosPorCesto);
    byte[] ba = new byte[c.size()];
    arqCestos.seek(enderecoCesto);
    arqCestos.read(ba);
    c.deserialize(ba);

    return c.read(chave);
  }

  public boolean update(T elem) throws Exception {

    // Carrega o diretório
    byte[] bd = new byte[(int) arqDiretorio.length()];
    arqDiretorio.seek(0);
    arqDiretorio.read(bd);
    diretorio = new Diretorio();
    diretorio.deserialize(bd);

    // Identifica o endereço do cesto no diretório a partir do hashCode,
    int i = diretorio.posicaoNoDiretorio(elem.hashCode());
    long enderecoCesto = diretorio.endereçoDoCesto(i);

    // Recupera o cesto
    Cesto c = new Cesto(construtor, quantidadeDadosPorCesto);
    byte[] ba = new byte[c.size()];
    arqCestos.seek(enderecoCesto);
    arqCestos.read(ba);
    c.deserialize(ba);

    // atualiza o dado
    if (!c.update(elem))
      return false;

    // Atualiza o cesto
    arqCestos.seek(enderecoCesto);
    arqCestos.write(c.serialize());
    return true;

  }

  public boolean delete(int chave) throws Exception {

    // Carrega o diretório
    byte[] bd = new byte[(int) arqDiretorio.length()];
    arqDiretorio.seek(0);
    arqDiretorio.read(bd);
    diretorio = new Diretorio();
    diretorio.deserialize(bd);

    // Identifica o endereço do cesto no diretório a partir do hashCode,
    int i = diretorio.posicaoNoDiretorio(chave);
    long enderecoCesto = diretorio.endereçoDoCesto(i);

    // Recupera o cesto
    Cesto c = new Cesto(construtor, quantidadeDadosPorCesto);
    byte[] ba = new byte[c.size()];
    arqCestos.seek(enderecoCesto);
    arqCestos.read(ba);
    c.deserialize(ba);

    // delete a chave
    if (!c.delete(chave))
      return false;

    // Atualiza o cesto
    arqCestos.seek(enderecoCesto);
    arqCestos.write(c.serialize());
    return true;
  }

  public void print() {
    try {
      byte[] bd = new byte[(int) arqDiretorio.length()];
      arqDiretorio.seek(0);
      arqDiretorio.read(bd);
      diretorio = new Diretorio();
      diretorio.deserialize(bd);
      System.out.println("\nDIRETÓRIO ------------------");
      System.out.println(diretorio);

      System.out.println("\nCESTOS ---------------------");
      arqCestos.seek(0);
      while (arqCestos.getFilePointer() != arqCestos.length()) {
        System.out.println("Endereço: " + arqCestos.getFilePointer());
        Cesto c = new Cesto(construtor, quantidadeDadosPorCesto);
        byte[] ba = new byte[c.size()];
        arqCestos.read(ba);
        c.deserialize(ba);
        System.out.println(c + "\n");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void close() throws IOException {
    arqDiretorio.close();
    arqCestos.close();
  }
}
