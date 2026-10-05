/*
TESTE DE ÁRVORE B+

Este programa principal serve para demonstrar o uso
da Árvore B+ como um índice indireto de uma entidade qualquer.

Aqui, cada elemento será composto por uma string e um número inteiro.

Implementado pelo Prof. Marcos Kutova
v1.2 - 2026
*/

import aed3.ArvoreBMais;
import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class Main1_NomeID {

  public static void main(String[] args) {

    ArvoreBMais<ParNomeId> arvore;
    Scanner console = new Scanner(System.in);

    try {
      File d = new File("dados");
      if (!d.exists())
        d.mkdir();
      arvore = new ArvoreBMais<>(ParNomeId.class.getConstructor(), 5, "dados/arvore.db");

      int opcao;
      do {
        System.out.println("\n\n-------------------------------");
        System.out.println("              MENU");
        System.out.println("-------------------------------");
        System.out.println("1 - Inserir");
        System.out.println("2 - Buscar por nome");
        System.out.println("3 - Excluir");
        System.out.println("4 - Listar todas");
        System.out.println("5 - Imprimir");
        System.out.println("6 - Buscar por nome (paginado)");
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
              System.out.print("Nome: ");
              String nome = console.nextLine();
              System.out.print("ID: ");
              int id = Integer.valueOf(console.nextLine());
              arvore.create(new ParNomeId(nome, id));
              arvore.print();
            } catch (Exception e) {
              System.out.println("Dados inválidos!");
            }
          }
            break;

          case 2: {
            System.out.println("\nBUSCA");
            System.out.print("Nome: ");
            String nome = console.nextLine();
            ArrayList<ParNomeId> lista = arvore.readAll(new ParNomeId(nome));
            System.out.print("Resposta: ");
            for (ParNomeId par : lista)
              System.out.print(par + " ");
            System.out.println();
          }
            break;

          case 3: {
            System.out.println("\nEXCLUSÃO");
            try {
              System.out.print("Nome: ");
              String nome = console.nextLine();
              System.out.print("ID: ");
              int id = Integer.valueOf(console.nextLine());
              arvore.delete(new ParNomeId(nome, id));
              arvore.print();
            } catch (Exception e) {
              System.out.println("Dados inválidos!");
            }
          }
            break;

          case 4: {
            System.out.println("\nLISTA COMPLETA");
            ArrayList<ParNomeId> lista = arvore.readAll();
            for (ParNomeId par : lista)
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
            System.out.print("Nome: ");
            String nome = console.nextLine();
            System.out.print("Quantidade de resultados por página: ");
            int quantidade = Integer.valueOf(console.nextLine());
            ParNomeId chave = new ParNomeId(nome);
            ParNomeId cursor = null;
            boolean continuar = true;

            while (continuar) {
              ArrayList<ParNomeId> pagina = arvore.readPage(chave, cursor, quantidade);

              if (pagina.isEmpty()) {
                if (cursor == null)
                  System.out.println("Nenhum resultado encontrado.");
                else
                  System.out.println("Fim dos resultados.");
                break;
              }

              System.out.println();
              for (ParNomeId par : pagina)
                System.out.println(par);

              cursor = pagina.get(pagina.size() - 1);

              if (pagina.size() < quantidade) {
                System.out.println("\nFim dos resultados.");
                break;
              }

              System.out.print("\nENTER para a próxima página ou Q para sair: ");
              continuar = !console.nextLine().equalsIgnoreCase("q");
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
