/*
TESTE DE ÁRVORE B+

Este programa principal serve para demonstrar o uso
da Árvore B+ como um relacionamento entre duas entidades.

Aqui, cada elemento será composto por dois inteiros que 
representam IDs de entidades quaisquer.

Implementado pelo Prof. Marcos Kutova
v1.1 - 2021
*/

import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;
import aed3.ArvoreBMais;

public class Main {

  // Método principal apenas para testes
  public static void main(String[] args) {

    ArvoreBMais<ParIntInt> arvore;
    Scanner console = new Scanner(System.in);

    try {
      File d = new File("dados");
      if (!d.exists())
        d.mkdir();
      arvore = new ArvoreBMais<>(ParIntInt.class.getConstructor(), 5, "dados/arvore.db");

      int opcao;
      do {
        System.out.println("\n\n-------------------------------");
        System.out.println("              MENU");
        System.out.println("-------------------------------");
        System.out.println("1 - Inserir");
        System.out.println("2 - Buscar");
        System.out.println("3 - Excluir");
        System.out.println("4 - Listar todas");
        System.out.println("5 - Imprimir");
        System.out.println("0 - Sair");
        try {
          opcao = Integer.valueOf(console.nextLine());
        } catch (NumberFormatException e) {
          opcao = -1;
        }

        switch (opcao) {
          case 1: {
            System.out.println("\nINCLUSÃO");
            int n1 = 0, n2 = 0;
            try {
              System.out.print("Num1: ");
              n1 = Integer.valueOf(console.nextLine());
              System.out.print("Num2: ");
              n2 = Integer.valueOf(console.nextLine());
            } catch (Exception e) {
              System.out.println("Não é um número!");
              break;
            }
            arvore.create(new ParIntInt(n1, n2));
            arvore.print();
          }
            break;
          case 2: {
            System.out.println("\nBUSCA");
            System.out.print("Num1: ");
            int n1 = Integer.valueOf(console.nextLine());
            // Ao passar o segundo valor como -1, ele funciona como um coringa
            // de acordo com a implementação do método compareTo na classe
            // ParIntInt
            ArrayList<ParIntInt> lista = arvore.read(new ParIntInt(n1, -1));

            // System.out.print("Num2: ");
            // int n2 = Integer.valueOf(console.nextLine());
            // ArrayList<ParIntInt> lista = arvore.read(new ParIntInt(n1, n2));
            System.out.print("Resposta: ");
            for (int i = 0; i < lista.size(); i++)
              System.out.print(lista.get(i) + " ");
          }
            break;
          case 3: {
            System.out.println("\nEXCLUSÃO");
            System.out.print("Num1: ");
            int n1 = Integer.valueOf(console.nextLine());
            System.out.print("Num2: ");
            int n2 = Integer.valueOf(console.nextLine());
            arvore.delete(new ParIntInt(n1, n2));
            arvore.print();
          }
            break;
          case 4: {
            System.out.println("\nLISTA COMPLETA");
            ArrayList<ParIntInt> lista = arvore.read(null);
            for (int i = 0; i < lista.size(); i++)
              System.out.print(lista.get(i) + " ");
          }
            break;
          case 5: {
            arvore.print();
          }
            break;
          case 0:
            break;
          default:
            System.out.println("Opção inválida");
        }
      } while (opcao != 0);
      console.close();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}