/*
TESTE DE TABELA HASH EXTENSÍVEL

Este programa principal serve para demonstrar o uso
da tabela hash extensível como um índice direto.
Aqui, cada elemento do índice será composto pelo par
(ID, endereco) representado por meio de um objeto da classe
ParIDEndereco.

Implementado pelo Prof. Marcos Kutova
v1.0 - 2025
*/

import java.util.Scanner;
import java.io.File;
import aed3.HashExtensivel;
import aed3.ParIDEndereco;

public class Main {

  // Método principal apenas para testes
  public static void main(String[] args) {

    HashExtensivel<ParIDEndereco> he;
    Scanner console = new Scanner(System.in);
    String nomeArquivo = "livros";

    try {
      File d = new File("dados");
      if (!d.exists())
        d.mkdir();
      he = new HashExtensivel<>(ParIDEndereco.class.getConstructor(), 4, "dados/" + nomeArquivo + ".hash_d.db",
          "dados/" + nomeArquivo + ".hash_c.db");

      int opcao;
      do {
        System.out.println("\n\n-------------------------------");
        System.out.println("              MENU");
        System.out.println("-------------------------------");
        System.out.println("1 - Inserir");
        System.out.println("2 - Buscar");
        System.out.println("3 - Excluir");
        System.out.println("4 - Imprimir");
        System.out.println("0 - Sair");
        try {
          opcao = Integer.valueOf(console.nextLine());
        } catch (NumberFormatException e) {
          opcao = -1;
        }

        switch (opcao) {
          case 1: {
            System.out.println("\nINCLUSÃO");

            System.out.print("ID: ");
            int id = Integer.valueOf(console.nextLine());
            System.out.print("Endereço: ");
            long endereco = Long.valueOf(console.nextLine());
            he.create(new ParIDEndereco(id, endereco));
            he.print();
          }
            break;
          case 2: {
            System.out.println("\nBUSCA");

            System.out.print("ID: ");
            int id = Integer.valueOf(console.nextLine());
            System.out.print("Dados: " + he.read(id));
          }
            break;
          case 3: {
            System.out.println("\nEXCLUSÃO");
            System.out.print("ID: ");
            int id = Integer.valueOf(console.nextLine());
            he.delete(id);
            he.print();
          }
            break;
          case 4: {
            he.print();
          }
            break;
          case 0:
            break;
          default:
            System.out.println("Opção inválida");
        }
      } while (opcao != 0);

    } catch (Exception e) {
      e.printStackTrace();
    }
    console.close();
  }
}