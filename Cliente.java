import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Cliente{
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8080;

    public static void main(String[] args) {
        System.out.println("=== CLIENTE SERVIDOR DE TEOMPO ===");
        System.out.println("Conectando ao servidor em " + SERVER_HOST + "na Porta: " + SERVER_PORT);
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT)) {
            System.out.println("Conexão estabelecida com o servidor.");
            
        
        } catch (Exception e) {
            System.err.println("Erro ao conectar ao servidor: " + e.getMessage());
        }
    }
}