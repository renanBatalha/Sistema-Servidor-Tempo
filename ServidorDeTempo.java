import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

public class ServidorDeTempo {
    private static final int PORTA = 8080;
    private static ServerSocket servidor;
    private static ExecutorService poolConexoes;
    private static final <String> historicoDeAcoes = Collections.synchronizedList(new ArrayList<>());



    public static String obterTempoAtual(){

            // Le o tempo atual da maquina servidora
            LocalDateTime tempoAtual = LocalDateTime.now();

            // Retorna e formata a hora em 0-23: 0-59: 0-59
            return tempoAtual.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }

    public static void definirAtualizacaoAutomatica(Integer intervalo, PrintWriter saida){
        long tempoPassado = System.currentTimeMillis();

        while(true){
            long tempoAtual = System.currentTimeMillis();

            if(intervalo <= tempoAtual - tempoPassado){
                saida.println("Hora atual: " + obterTempoAtual());

                tempoPassado = tempoAtual;
            }
        }
    }    

    public static void registrarAcao(String acao){
        String dataAtual = ServidorDeTempo.obterTempoAtual();
        String entradaLog = "[" + dataAtual + "]" + acao;
        historicoDeAcoes.add(entradaLog);
    }

    public List<String> obterHistorico(){
        return new ArrayList<>(historicoDeAcoes);
    }

    private static void encerrarServidor() {
        try {
            if (servidor != null) servidor.close();
            if (poolConexoes != null) poolConexoes.shutdown();
            if (agendador != null) agendador.shutdown();
        } catch (IOException e) {
            System.err.println("Erro ao encerrar servidor: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        try (servidor = new ServerSocket(PORTA)) {
            System.out.println("Servidor iniciado na porta " + PORTA);
            while (true) {

                // Recebe a conexao do cliente
                // O servidor de tempo so avanca apos conexao
                registrarAcao("Servidor iniciado na porta " + PORTA);
                while(true){

                Socket clienteSocket = servidor.accept();
                poolConexoes.execute(new ClienteThread(clienteSocket));
                
                }
            }
        } catch (IOException e) {
            System.err.println("Erro no servidor: " + e.getMessage());
        }
        finally {
            System.out.println("Servidor encerrado.");
        }
    }
}
