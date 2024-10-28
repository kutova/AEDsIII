import java.util.Scanner;
import java.io.File;
import aed3.ListaInvertida;
import aed3.ElementoLista;

public class Main {

  // Método principal apenas para testes
  public static void main(String[] args) {

    ListaInvertida lista;
    Scanner console = new Scanner(System.in);

    try {
      File d = new File("dados");
      if (!d.exists())
        d.mkdir();
      lista = new ListaInvertida(4, "dados/dicionario.listainv.db", "dados/blocos.listainv.db");

      int opcao;
      do {
        System.out.println("\n\n-------------------------------");
        System.out.println("              MENU");
        System.out.println("-------------------------------");
        System.out.println("1 - Inserir");
        System.out.println("2 - Buscar");
        System.out.println("3 - Excluir");
        System.out.println("4 - Imprimir");
        System.out.println("5 - Incrementar entidades");
        System.out.println("6 - Decrementar entidades");
        System.out.println("0 - Sair");
        try {
          opcao = Integer.valueOf(console.nextLine());
        } catch (NumberFormatException e) {
          opcao = -1;
        }

        switch (opcao) {
          case 1: {
            System.out.println("\nINCLUSÃO");
            System.out.print("Termo: ");
            String chave = console.nextLine();
            System.out.print("ID: ");
            int id = Integer.valueOf(console.nextLine());
            System.out.print("Frequência: ");
            float frequencia = Float.valueOf(console.nextLine());
            lista.create(chave, new ElementoLista(id, frequencia));
            lista.print();
          }
            break;
          case 2: {
            System.out.println("\nBUSCA");
            System.out.print("Chave: ");
            String chave = console.nextLine();
            ElementoLista[] elementos = lista.read(chave);
            System.out.print("Elementos: ");
            for (int i = 0; i < elementos.length; i++)
              System.out.print(elementos[i] + " ");
          }
            break;
          case 3: {
            System.out.println("\nEXCLUSÃO");
            System.out.print("Chave: ");
            String chave = console.nextLine();
            System.out.print("ID: ");
            int id = Integer.valueOf(console.nextLine());
            lista.delete(chave, id);
            lista.print();
          }
            break;
          case 4: {
            lista.print();
          }
            break;
          case 5: {
            lista.incrementaEntidades();
            System.out.println("Entidades: " + lista.numeroEntidades());
          }
            break;
          case 6: {
            lista.decrementaEntidades();
            System.out.println("Entidades: " + lista.numeroEntidades());
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