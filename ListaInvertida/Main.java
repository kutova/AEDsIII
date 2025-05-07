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
        System.out.println("3 - Listar");
        System.out.println("4 - Atualizar");
        System.out.println("5 - Excluir");
        System.out.println("6 - Imprimir");
        System.out.println("7 - Incrementar entidades");
        System.out.println("8 - Decrementar entidades");
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
            String termo = console.nextLine();
            System.out.print("ID: ");
            int id = Integer.valueOf(console.nextLine());
            System.out.print("Frequência: ");
            float frequencia = Float.valueOf(console.nextLine());
            lista.create(termo, new ElementoLista(id, frequencia));
            lista.print();
          }
            break;
          case 2: {
            System.out.println("\nBUSCA");
            System.out.print("Termo: ");
            String termo = console.nextLine();
            System.out.print("ID: ");
            int id = Integer.valueOf(console.nextLine());
            ElementoLista elemento = lista.read(termo, id);
            System.out.print("Elemento: "+elemento + " ");
          }
            break;
          case 3: {
            System.out.println("\nLISTA");
            System.out.print("Termo: ");
            String termo = console.nextLine();
            ElementoLista[] elementos = lista.read(termo);
            System.out.print("Elementos: ");
            for (int i = 0; i < elementos.length; i++)
              System.out.print(elementos[i] + " ");
          }
            break;
          case 4: {
            System.out.println("\nATUALIZAÇÃO");
            System.out.print("Termo: ");
            String termo = console.nextLine();
            System.out.print("ID: ");
            int id = Integer.valueOf(console.nextLine());
            System.out.print("Frequência: ");
            float frequencia = Float.valueOf(console.nextLine());
            if( lista.update(termo, new ElementoLista(id, frequencia)) )
              lista.print();
            else
              System.out.println("Termo não atualizado.");
          }
            break;
          case 5: {
            System.out.println("\nEXCLUSÃO");
            System.out.print("Termo: ");
            String termo = console.nextLine();
            System.out.print("ID: ");
            int id = Integer.valueOf(console.nextLine());
            if(lista.delete(termo, id))
              lista.print();
            else
              System.out.println("Termo não excluído.");

          }
            break;
          case 6: {
            System.out.println("ENTIDADES: " + lista.numeroEntidades());
            lista.print();
          }
            break;
          case 7: {
            lista.incrementaEntidades();
            System.out.println("Entidades: " + lista.numeroEntidades());
          }
            break;
          case 8: {
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