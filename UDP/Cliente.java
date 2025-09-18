package UDP;
import java.net.*;
import java.util.Scanner;

public class Cliente {
    private static final int PORTA_SERVIDOR = 8080;

    public static void main(String[] args) throws Exception {
        DatagramSocket socket = new DatagramSocket();
        InetAddress enderecoServidor = InetAddress.getByName("localhost");

        Thread recebimento = new Thread(() -> {
            byte[] buffer = new byte[1024];
            while (!Thread.currentThread().isInterrupted()) {
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
        recebimento.start();

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

                System.out.println("Atualizações automáticas iniciadas. Digite 'p' para parar.");

                // Espera até o usuário digitar "p" para parar as atualizações
                String input;
                do {
                    input = sc.nextLine();
                    if (!input.trim().equalsIgnoreCase("p")) {
                        System.out.println("Digite 'p' para parar as atualizações automáticas.");
                    }
                } while (!input.trim().equalsIgnoreCase("p"));

                enviarComando(socket, enderecoServidor, "p");

                // Dá um tempo para o servidor responder antes de continuar
                Thread.sleep(500);

                System.out.println("Atualizações automáticas paradas.");
            } else if (opcao.equals("3")) {
                enviarComando(socket, enderecoServidor, "3");
                break;
            } else {
                System.out.println("Opção inválida.");
            }
        }

        recebimento.interrupt();
        socket.close();
        System.out.println("Cliente encerrado.");
        sc.close();
    }

    private static void enviarComando(DatagramSocket socket, InetAddress endereco, String comando) throws Exception {
        byte[] dados = comando.getBytes();
        DatagramPacket pacote = new DatagramPacket(dados, dados.length, endereco, PORTA_SERVIDOR);
        socket.send(pacote);
    }
}
