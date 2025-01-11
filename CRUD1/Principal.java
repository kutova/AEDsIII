import java.util.Scanner;


public class Principal {

public static void main(String[] args) {

    Scanner console;

    try {
        console = new Scanner(System.in);
        int opcao;
        do {

            System.out.println("\n\nAEDsIII");
            System.out.println("-------");
            System.out.println("> Início");
            System.out.println("\n1 - Clientes");
            System.out.println("0 - Sair");

            System.out.print("\nOpção: ");
            try {
                opcao = Integer.valueOf(console.nextLine());
            } catch(NumberFormatException e) {
                opcao = -1;
            }

            switch (opcao) {
                case 1:
                    (new MenuClientes()).menu();
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opção inválida!");
                    break;
            }

        } while (opcao != 0);



    } catch(Exception e) {
        e.printStackTrace();
    }

}

}