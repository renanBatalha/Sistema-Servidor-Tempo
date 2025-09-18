package UDP;

import java.net.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.concurrent.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServidorDeTempo {
    private static final int PORTA = 8080;
    private static final String ARQUIVO_LOG = "log_clientes.txt";

    // Executor para tarefas de envio automático
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

    // Mapa para armazenar tarefas agendadas por cliente (IP + porta)
    private static final Map<String, ScheduledFuture<?>> tarefasAutomaticas = new ConcurrentHashMap<>();

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
            String clienteKey = enderecoCliente.getHostAddress() + ":" + portaCliente;

            String resposta = "";

            if (comando.equals("1")) {
                // Hora atual única
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                String horaAtual = LocalTime.now().format(formatter);
                resposta = "Hora atual: " + horaAtual;
                enviarMensagem(resposta, socket, enderecoCliente, portaCliente);

                registrarLog(enderecoCliente, portaCliente, comando, resposta);

            } else if (comando.startsWith("2")) {
                // Comando para iniciar envio automático: "2 [intervalo]"
                String[] partes = comando.split(" ");
                if (partes.length < 2) {
                    resposta = "Erro: intervalo não especificado.";
                    enviarMensagem(resposta, socket, enderecoCliente, portaCliente);
                } else {
                    try {
                        int intervalo = Integer.parseInt(partes[1]);
                        resposta = "Iniciando envio automatico a cada " + intervalo + " ms. Envie 'p' para parar.";
                        enviarMensagem(resposta, socket, enderecoCliente, portaCliente);

                        registrarLog(enderecoCliente, portaCliente, comando, resposta);

                        // Se já existir uma tarefa para esse cliente, cancela antes
                        ScheduledFuture<?> tarefaAnterior = tarefasAutomaticas.get(clienteKey);
                        if (tarefaAnterior != null && !tarefaAnterior.isCancelled()) {
                            tarefaAnterior.cancel(true);
                        }

                        // Agenda envio periódico do horário para o cliente
                        ScheduledFuture<?> tarefa = scheduler.scheduleAtFixedRate(() -> {
                            try {
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                                String hora = LocalTime.now().format(formatter);
                                enviarMensagem("Atualizacao automatica: " + hora, socket, enderecoCliente, portaCliente);
                            } catch (Exception e) {
                                System.err.println("Erro ao enviar atualizacao automatica: " + e.getMessage());
                            }
                        }, 0, intervalo, TimeUnit.MILLISECONDS);

                        tarefasAutomaticas.put(clienteKey, tarefa);

                    } catch (NumberFormatException e) {
                        resposta = "Erro: intervalo invalido.";
                        enviarMensagem(resposta, socket, enderecoCliente, portaCliente);
                    }
                }

            } else if (comando.equalsIgnoreCase("p")) {
                // Parar envio automático do cliente
                ScheduledFuture<?> tarefa = tarefasAutomaticas.get(clienteKey);
                if (tarefa != null) {
                    tarefa.cancel(true);
                    tarefasAutomaticas.remove(clienteKey);
                    resposta = "Envio automatico parado.";
                } else {
                    resposta = "Nenhum envio automatico ativo para parar.";
                }
                enviarMensagem(resposta, socket, enderecoCliente, portaCliente);
                registrarLog(enderecoCliente, portaCliente, comando, resposta);

            } else if (comando.equals("3")) {
                // Encerrar conexão - no UDP isso é só um aviso
                resposta = "Encerrando conexao (cliente decide)";
                enviarMensagem(resposta, socket, enderecoCliente, portaCliente);
                registrarLog(enderecoCliente, portaCliente, comando, resposta);

                // Também cancela envio automático caso exista
                ScheduledFuture<?> tarefa = tarefasAutomaticas.get(clienteKey);
                if (tarefa != null) {
                    tarefa.cancel(true);
                    tarefasAutomaticas.remove(clienteKey);
                }

            } else {
                resposta = "Comando invalido";
                enviarMensagem(resposta, socket, enderecoCliente, portaCliente);
                registrarLog(enderecoCliente, portaCliente, comando, resposta);
            }
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

    private static void registrarLog(InetAddress endereco, int porta, String comando, String resposta) {
        String log = String.format("[%s] Cliente %s:%d - Comando: '%s' - Resposta: '%s'",
                LocalDateTime.now(), endereco.getHostAddress(), porta, comando, resposta);
        System.out.println(log);
        try (PrintWriter writer = new PrintWriter(new FileWriter(ARQUIVO_LOG, true))) {
            writer.println(log);
        } catch (IOException e) {
            System.err.println("Erro ao salvar log: " + e.getMessage());
        }
    }
}
