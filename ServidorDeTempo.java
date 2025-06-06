import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.OutputStream;
import java.io.PrintWriter;
public class ServidorDeTempo {
    private static final int PORTA = 8080;

    public static String obterTempoFormatado(){

            // le o tempo atual da maquina servidora
            LocalDateTime tempoAtual = LocalDateTime.now();

            // formata a hora em 0-23: 0-59: 0-59
            String tempo = tempoAtual.format(DateTimeFormatter.ofPattern("HH:mm:ss"));

            return tempo;
    }
    
    public static void main(String[] args) {


        try (ServerSocket serverSocket = new ServerSocket(PORTA)) {
            System.out.println("Servidor iniciado na porta " + PORTA);
            while (true) {

                // Recebe a conexao do cliente
                // O servidor de tempo so avanca apos conexao
                Socket clienteSocket = serverSocket.accept();

                // Exibe o endereco do cliente conectado
                System.out.println("Cliente conectado: " + clienteSocket.getInetAddress());

                String tempoAtual = ServidorDeTempo.obterTempoFormatado();

                OutputStream output = clienteSocket.getOutputStream();

                // autoFlush = true permite o envio imediato da mensagem
                // ao cliente
                PrintWriter resposta = new PrintWriter(output, true);

                // Reposta para o cliente
                resposta.println(tempoAtual);                            
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            System.out.println("Servidor encerrado.");
        }
    }
}
