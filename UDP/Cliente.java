package UDP;
import java.net.*;
import java.util.Scanner;

public class Cliente {
    private static final int PORTA_SERVIDOR = 8080;

    public static void main(String[] args) throws Exception {
        DatagramSocket socket = new DatagramSocket();
        InetAddress enderecoServidor = InetAddress.getByName("localhost");

        Thread receptor = new Thread(() -> {
            byte[] buffer = new byte[1024];
            while (true) {
                try {
                    DatagramPacket resposta = new DatagramPacket(buffer, buffer.length);
                    socket.receive(resposta);
                    String msg = new String(resposta.getData(), 0, resposta.getLength());
                    System.out.println("[SERVIDOR] " + msg);
                } catch (Exception e) {
                    break;
                }
            }
        });
        receptor.start();

        Scanner sc = new Scanner(System.in);
        String opcao;

        while (true) {
            System.out.println("\n=== CLIENTE DE TEMPO ===");
            System.out.println("1 - Solicitar Hora Atual");
            System.out.println("2 - Solicitar Atualização Contínua (ms)");
            System.out.println("3 - Encerrar Conexão");
            System.out.print("Escolha: ");
            opcao = sc.nextLine();

            if (opcao.equals("1")) {
                enviarComando(socket, enderecoServidor, "1");
            } else if (opcao.equals("2")) {
                System.out.print("Digite o intervalo em milissegundos: ");
                String intervalo = sc.nextLine();
                enviarComando(socket, enderecoServidor, "2 " + intervalo);
            } else if (opcao.equals("3")) {
                enviarComando(socket, enderecoServidor, "3");
                break;
            } else {
                System.out.println("Opção inválida.");
            }
        }

        socket.close();
        System.out.println("Cliente encerrado.");
    }

    private static void enviarComando(DatagramSocket socket, InetAddress endereco, String comando) throws Exception {
        byte[] dados = comando.getBytes();
        DatagramPacket pacote = new DatagramPacket(dados, dados.length, endereco, PORTA_SERVIDOR);
        socket.send(pacote);
    }
}
