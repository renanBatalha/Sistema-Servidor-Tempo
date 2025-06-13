package UDP;

import java.net.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.concurrent.*;

public class ServidorDeTempo {
    private static final int PORTA = 8080;
    private static final ExecutorService pool = Executors.newCachedThreadPool();
    private static final String ARQUIVO_LOG = "log_clientes.txt";

    public static void main(String[] args) throws Exception {
        DatagramSocket socket = new DatagramSocket(PORTA);
        System.out.println("Servidor UDP rodando na porta " + PORTA + "...");

        byte[] buffer = new byte[1024];

        while (true) {
            DatagramPacket pacoteRecebido = new DatagramPacket(buffer, buffer.length);
            socket.receive(pacoteRecebido);

            String comando = new String(pacoteRecebido.getData(), 0, pacoteRecebido.getLength()).trim();
            InetAddress enderecoCliente = pacoteRecebido.getAddress();
            int portaCliente = pacoteRecebido.getPort();

            String resposta = "";

            if (comando.startsWith("1")) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                String horaAtual = LocalTime.now().format(formatter);
                resposta = "Hora atual: " + horaAtual;
                enviarMensagem(resposta, socket, enderecoCliente, portaCliente);

            } else if (comando.startsWith("2")) {
                int intervalo = Integer.parseInt(comando.split(" ")[1]);
                resposta = "Iniciando envio automatico a cada " + intervalo + " ms";
                enviarMensagem(resposta, socket, enderecoCliente, portaCliente);

                String logMsg = gerarLog(enderecoCliente, portaCliente, comando, resposta);
                salvarLog(logMsg);

                pool.execute(() -> {
                    try {
                        while (true) {
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                            String hora = LocalTime.now().format(formatter);
                            enviarMensagem("Atualizacao automatica: " + hora, socket, enderecoCliente, portaCliente);
                            Thread.sleep(intervalo);
                        }
                    } catch (Exception e) {
                        System.out.println("Thread de envio encerrada.");
                    }
                });
                continue;

            } else if (comando.equals("3")) {
                resposta = "Encerrando conexao (cliente decide)";
                enviarMensagem(resposta, socket, enderecoCliente, portaCliente);
            } else {
                resposta = "Comando invalido";
                enviarMensagem(resposta, socket, enderecoCliente, portaCliente);
            }

            // Gera e salva o log
            String logMsg = gerarLog(enderecoCliente, portaCliente, comando, resposta);
            salvarLog(logMsg);
        }
    }

    private static void enviarMensagem(String mensagem, DatagramSocket socket, InetAddress endereco, int porta) {
        try {
            byte[] dados = mensagem.getBytes();
            DatagramPacket pacote = new DatagramPacket(dados, dados.length, endereco, porta);
            socket.send(pacote);
        } catch (Exception e) {
            System.err.println("Erro ao enviar resposta: " + e.getMessage());
        }
    }

    private static String gerarLog(InetAddress endereco, int porta, String comando, String resposta) {
        return String.format("[%s] Cliente %s:%d - Comando: '%s' - Resposta: '%s'",
                LocalDateTime.now(), endereco.getHostAddress(), porta, comando, resposta);
    }

    private static void salvarLog(String log) {
        System.out.println(log); // Também mostra no console
        try (PrintWriter writer = new PrintWriter(new FileWriter(ARQUIVO_LOG, true))) {
            writer.println(log);
        } catch (IOException e) {
            System.err.println("Erro ao salvar log: " + e.getMessage());
        }
    }
}
