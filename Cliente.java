import java.io.BufferedReader;
import java.io.IOException;
import java.net.*;
import java.util.Scanner;
import java.io.InputStreamReader;
import java.io.PrintWriter;


public class Cliente{
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8080;
    private static volatile boolean atualizacaoAtiva = true;

    public static String ajustarHora(String horaRecebida, long atraso) {
        // Ajusta a hora recebida com base no atraso
        String[] partes = horaRecebida.split(":");
        int horas = Integer.parseInt(partes[0]);
        int minutos = Integer.parseInt(partes[1]);
        int segundos = Integer.parseInt(partes[2]);

        // Converte o atraso de milissegundos para segundos
        long atrasoEmSegundos = atraso / 1000;

        // Ajusta os segundos
        segundos += atrasoEmSegundos;

        // Corrige os minutos e horas se necessario
        if (segundos >= 60) {
            minutos += segundos / 60;
            segundos %= 60;
        }
        if (minutos >= 60) {
            horas += minutos / 60;
            minutos %= 60;
        }
        if (horas >= 24) {
            horas %= 24;
        }

        return String.format("%02d:%02d:%02d", horas, minutos, segundos);
    }


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

                if (linha.contains("Digite um Comando")) {
                    System.out.print("> ");
                    String comando = teclado.nextLine();
                    saida.println(comando);
                }

                if (linha.contains("Digite o intervalo")) {
                    System.out.print("> ");
                    String intervaloStr = teclado.nextLine();
                    int intervalo = Integer.parseInt(intervaloStr);
                    saida.println(intervaloStr);

                    atualizacaoAtiva = true;

                    Thread threadAtualizacao = new Thread(() -> {
                        try {
                            while (atualizacaoAtiva) {
                                String horaRecebida = entrada.readLine();
                                if (horaRecebida == null) break;

                                if (!atualizacaoAtiva) break;

                                if(horaRecebida.startsWith("Hora atual: ")) {
                                    horaRecebida = horaRecebida.substring(12); // Remove o prefixo "Hora atual: "
                                }
                                Integer atraso = intervalo; // ou medir tempo real
                                String horaCorrigida = Cliente.ajustarHora(horaRecebida, atraso);
                                System.out.println("Hora atualizada: " + horaCorrigida);
                            }
                        } catch (IOException e) {
                            System.err.println("Erro na atualizacao automatica: " + e.getMessage());
                        }
                    });
                    threadAtualizacao.start();

                    System.out.println("Digite \"p\" para parar a atualizacao automatica...");
                    String comandoParar = teclado.nextLine(); // Espera "p"
                    
                    while(!comandoParar.trim().equalsIgnoreCase("p")) {
                        System.out.println("Digite \"p\" para parar a atualizacao automatica...");
                        comandoParar = teclado.nextLine(); // Espera "p"
                    }
                    // Espera o usuario digitar algo para parar
                    //System.out.println("Atualizacao automatica encerrada...");
                    atualizacaoAtiva = false;
                    threadAtualizacao.join(); // Espera a thread terminar
                    saida.println("p");
                }
            }            
        
        } catch (Exception e) {
            System.err.println("Erro ao conectar ao servidor: " + e.getMessage());
        }
    }
}