import java.io.BufferedReader;
import java.io.IOException;
import java.net.*;
import java.util.Scanner;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class Cliente {
    private static final String SERVER_HOST = "localhost"; // Endereco do servidor
    private static final int SERVER_PORT = 8080;
    private static volatile boolean atualizacaoAtiva = true;
    private static volatile long tempoAtrasoServidor = 0;

    public static String ajustarHora(String horaRecebida, long atraso) {
        String[] partes = horaRecebida.split(":");
        int horas = Integer.parseInt(partes[0]);
        int minutos = Integer.parseInt(partes[1]);
        int segundos = Integer.parseInt(partes[2]);

        long atrasoEmSegundos = atraso / 1000;
        segundos += atrasoEmSegundos;

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
        System.out.println("Conectando ao servidor em " + SERVER_HOST + " na Porta: " + SERVER_PORT);
        try (
            Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter saida = new PrintWriter(socket.getOutputStream(), true);
            Scanner teclado = new Scanner(System.in);
        ) {

            System.out.println("=== CLIENTE CONECTADO AO SERVIDOR DE TEMPO ===");

            String linha;
            while ((linha = entrada.readLine()) != null) {
                if (linha.startsWith("Tempo Atraso Servidor:")) {
                    String[] partes = linha.split(":", 3);
                    tempoAtrasoServidor = Long.parseLong(partes[1].trim());
                    continue;
                }

                if (linha.contains("Tempo Anterior:")) {
                    tempoAtrasoServidor = Long.parseLong(linha.split(":")[1].trim());
                } else {
                    System.out.println(linha);
                }

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

                                if (horaRecebida.startsWith("Tempo Atraso Servidor:")) {
                                    String dados = horaRecebida.substring("Tempo Atraso Servidor:".length());
                                    String[] partes = dados.split(":");
                                    if (partes.length >= 4) {
                                        try {
                                            tempoAtrasoServidor = Long.parseLong(partes[0].trim());
                                            String horaRecebidaFormatada = String.format("%s:%s:%s", partes[1], partes[2], partes[3]);
                                            
                                            long tempoAtrasoServidor = Long.parseLong(partes[0].trim());
                                            

                                            long tempoAtualCliente = System.currentTimeMillis();

                                            long atraso = tempoAtualCliente - tempoAtrasoServidor;

                                            String horaCorrigida = Cliente.ajustarHora(horaRecebidaFormatada, atraso);

                                            System.out.println("Hora Automatica: " + horaCorrigida);
                                        } catch (NumberFormatException e) {
                                            System.err.println("Erro ao converter tempo de atraso: " + e.getMessage());
                                        }
                                    } else {
                                        System.err.println("Formato invalido da hora recebida.");
                                    }
                                } else {
                                    System.out.println("Hora Automatica: " + horaRecebida);
                                }
                            }
                        } catch (IOException e) {
                            System.err.println("Erro na atualizacao automatica: " + e.getMessage());
                        }
                    });

                    threadAtualizacao.start();

                    System.out.println("Digite \"p\" para parar a atualizacao automatica...");
                    String comandoParar = teclado.nextLine();
                    while (!comandoParar.trim().equalsIgnoreCase("p")) {
                        System.out.println("Digite \"p\" para parar a atualizacao automatica...");
                        comandoParar = teclado.nextLine();
                    }
                    atualizacaoAtiva = false;
                    threadAtualizacao.join();
                    saida.println("p");
                }
            }

        } catch (Exception e) {
            System.err.println("Erro ao conectar ao servidor: " + e.getMessage());
        }
    }
}
