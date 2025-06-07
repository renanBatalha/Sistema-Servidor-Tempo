import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.OutputStream;
import java.io.PrintWriter;
public class ServidorDeTempo {
    private static final int PORTA = 8080;

    public static String obterTempoAtual(){

            // Le o tempo atual da maquina servidora
            LocalDateTime tempoAtual = LocalDateTime.now();

            // Retorna e formata a hora em 0-23: 0-59: 0-59
            return tempoAtual.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }

    public static void definirAtualizacaoAutomatica(Integer intervalo){
        long tempoPassado = System.currentTimeMillis();

        while(true){
            long tempoAtual = System.currentTimeMillis();

            if(intervalo <= tempoAtual - tempoPassado){
                System.out.println(obterTempoAtual());

                tempoPassado = tempoAtual;
            }
        }
    }

    
    
    public static void main(String[] args) {
        /* 
        try (ServerSocket serverSocket = new ServerSocket(PORTA)) {
            System.out.println("Servidor iniciado na porta " + PORTA);
            while (true) {

                // Recebe a conexao do cliente
                // O servidor de tempo so avanca apos conexao
                Socket clienteSocket = serverSocket.accept();

                // Exibe o endereco do cliente conectado
                System.out.println("Cliente conectado: " + clienteSocket.getInetAddress());

                ClienteThread handler = new ClienteThread(clienteSocket);

                Thread thread = new Thread(handler);
                thread.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            System.out.println("Servidor encerrado.");
        }
        */
        definirAtualizacaoAutomatica(1000);
    }
}
