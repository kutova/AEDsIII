/*
TESTE DE ÁRVORE B+

Este programa principal serve para demonstrar o uso
da Árvore B+ para representar um relacionamento N:N.

Aqui, cada elemento será composto por dois números inteiros.

Implementado pelo Prof. Marcos Kutova
v1.2 - 2026
*/

import aed3.ArvoreBMais;
import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class Main2_Relacionamento {

  public static void main(String[] args) {

    ArvoreBMais<ParIdId> arvore;
    Scanner console = new Scanner(System.in);

    try {
      File d = new File("dados");
      if (!d.exists())
        d.mkdir();
      arvore = new ArvoreBMais<>(ParIdId.class.getConstructor(), 5, "dados/arvore_relacionamento.db");

      int opcao;
      do {
        System.out.println("\n\n-------------------------------");
        System.out.println("              MENU");
        System.out.println("-------------------------------");
        System.out.println("1 - Inserir");
        System.out.println("2 - Buscar por ID1");
        System.out.println("3 - Excluir");
        System.out.println("4 - Listar todas");
        System.out.println("5 - Imprimir");
        System.out.println("6 - Buscar por ID1 (paginado)");
        System.out.println("0 - Sair");
        try {
          opcao = Integer.valueOf(console.nextLine());
        } catch (NumberFormatException e) {
          opcao = -1;
        }

        switch (opcao) {
          case 1: {
            System.out.println("\nINCLUSÃO");
            try {
              System.out.print("ID 1: ");
              int id1 = Integer.valueOf(console.nextLine());
              System.out.print("ID 2: ");
              int id2 = Integer.valueOf(console.nextLine());
              arvore.create(new ParIdId(id1, id2));
              arvore.print();
            } catch (Exception e) {
              System.out.println("Dados inválidos!");
            }
          }
            break;

          case 2: {
            System.out.println("\nBUSCA");
            try {
              System.out.print("ID 1: ");
              int id1 = Integer.valueOf(console.nextLine());
              ArrayList<ParIdId> lista = arvore.readAll(new ParIdId(id1));
              System.out.print("Resposta: ");
              for (ParIdId par : lista)
                System.out.print(par + " ");
              System.out.println();
            } catch (Exception e) {
              System.out.println("Dados inválidos!");
            }
          }
            break;

          case 3: {
            System.out.println("\nEXCLUSÃO");
            try {
              System.out.print("ID 1: ");
              int id1 = Integer.valueOf(console.nextLine());
              System.out.print("ID 2: ");
              int id2 = Integer.valueOf(console.nextLine());
              arvore.delete(new ParIdId(id1, id2));
              arvore.print();
            } catch (Exception e) {
              System.out.println("Dados inválidos!");
            }
          }
            break;

          case 4: {
            System.out.println("\nLISTA COMPLETA");
            ArrayList<ParIdId> lista = arvore.readAll();
            for (ParIdId par : lista)
              System.out.print(par + " ");
            System.out.println();
          }
            break;

          case 5: {
            arvore.print();
          }
            break;

          case 6: {
            System.out.println("\nBUSCA PAGINADA");
            try {
              System.out.print("ID 1: ");
              int id1 = Integer.valueOf(console.nextLine());
              System.out.print("Quantidade de resultados por página: ");
              int quantidade = Integer.valueOf(console.nextLine());
              ParIdId chave = new ParIdId(id1);
              ParIdId cursor = null;
              boolean continuar = true;

              while (continuar) {
                ArrayList<ParIdId> pagina = arvore.readPage(chave, cursor, quantidade);

                if (pagina.isEmpty()) {
                  if (cursor == null)
                    System.out.println("Nenhum resultado encontrado.");
                  else
                    System.out.println("Fim dos resultados.");
                  break;
                }

                System.out.println();
                for (ParIdId par : pagina)
                  System.out.println(par);

                cursor = pagina.get(pagina.size() - 1);

                if (pagina.size() < quantidade) {
                  System.out.println("\nFim dos resultados.");
                  break;
                }

                System.out.print("\nENTER para a próxima página ou Q para sair: ");
                continuar = !console.nextLine().equalsIgnoreCase("q");
              }
            } catch (Exception e) {
              System.out.println("Dados inválidos!");
            }
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
