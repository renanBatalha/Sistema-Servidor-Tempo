import java.io.BufferedReader;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.io.InputStreamReader;
import java.io.PrintWriter;


public class Cliente{
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8080;

    public static void main(String[] args) {
        System.out.println("=== CLIENTE SERVIDOR DE TEOMPO ===");
        System.out.println("Conectando ao servidor em " + SERVER_HOST + "na Porta: " + SERVER_PORT);
        try (
            Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter saida = new PrintWriter(socket.getOutputStream(), true);
            Scanner teclado = new Scanner(System.in);    
        ) {
            System.out.println("=== CLIENTE CONECTADO AO SERVIDOR DE TEMPO ===");

            String linha;
            while ((linha = entrada.readLine()) != null) {
                System.out.println(linha); // Mostra o que o servidor envia (como o menu)

                if (linha.contains("Digite um comando")) {
                    System.out.print("> ");
                    String comando = teclado.nextLine();
                    saida.println(comando);
                }
            }            
        
        } catch (Exception e) {
            System.err.println("Erro ao conectar ao servidor: " + e.getMessage());
        }
    }
}