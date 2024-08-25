/*
TESTE DE TABELA HASH EXTENSÍVEL

Este programa principal serve para demonstrar o uso
da tabela hash extensível como um índice indireto.
Aqui, cada elemento do índice será composto pelo par
(email, ID) representado por meio de um objeto da classe
ParEmailID.

Para funcionamento como índice direto, precisaríamos de 
mais uma classe que contivesse o par (ID, endereço).
Mas isso fica por sua conta ;)

Implementado pelo Prof. Marcos Kutova
v1.1 - 2021
*/

import java.util.Scanner;
import java.io.File;
import aed3.HashExtensivel;

public class Main {

  // Método principal apenas para testes
  public static void main(String[] args) {

    HashExtensivel<ParEmailID> he;
    Scanner console = new Scanner(System.in);
    String nomeArquivo = "pessoas";

    try {
      File d = new File("dados");
      if (!d.exists())
        d.mkdir();
      he = new HashExtensivel<>(ParEmailID.class.getConstructor(), 4, "dados/" + nomeArquivo + ".hash_d.db",
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

            System.out.print("E-mail: ");
            String email = console.nextLine();
            System.out.print("ID: ");
            int id = Integer.valueOf(console.nextLine());
            he.create(new ParEmailID(email, id));
            he.print();
          }
            break;
          case 2: {
            System.out.println("\nBUSCA");

            System.out.print("E-mail: ");
            String email = console.nextLine();
            System.out.print("Dados: " + he.read(ParEmailID.hash(email)));
          }
            break;
          case 3: {
            System.out.println("\nEXCLUSÃO");

            System.out.print("E-mail: ");
            String email = console.nextLine();
            he.delete(email.hashCode());
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