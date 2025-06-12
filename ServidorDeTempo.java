import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.IOException;
import java.io.PrintWriter;

public class ServidorDeTempo {
    private static final int PORTA = 8080;
    private static ServerSocket servidor;
    // Controla a execução do codigo para cada cliente.
    // É um gerenciador de threads. Ele cria e controla um "pool" (conjunto) de threads para lidar com múltiplos clientes ao mesmo tempo
    private static ExecutorService poolConexoes;
    private static final List<String> historicoDeAcoes = Collections.synchronizedList(new ArrayList<>());
    // É um mapa que armazena os clientes conectados.
    // A chave é o ID do cliente (uma String) e o valor é um PrintWriter que permite enviar mensagens para esse cliente.
    private static final Map<String, Socket> socketsClientes = new ConcurrentHashMap<>();

    private static final Map<String, PrintWriter> clientesConectados = new ConcurrentHashMap<>();    

    private static volatile boolean flag = true;

    private static void mostrarHoraAutomaticaServidor(){
        String resposta;
        do{ 
            Thread tempoAtual = new Thread( new Runnable(){
                @Override
                public void run(){
                    while(flag) {
                        System.out.println(obterTempoAtual()); 
                        try{
                            Thread.sleep(1000);
                        }
                        catch(Exception e){
                            e.printStackTrace();
                        }
                    }        
                }
            });
            tempoAtual.start();

            Scanner scanner = new Scanner(System.in);
            System.out.println("Digite 'p' para parar");
            resposta = scanner.nextLine();

        }while(!resposta.equals("p"));
        flag = false;
        
    }

    private static void mostrarLogPorCliente() {
    System.out.println("=== LOG DE ACOES POR CLIENTE ===");

    // Mapa: chave = ID do cliente, valor = lista de ações
    Map<String, List<String>> logPorCliente = new HashMap<>();

    synchronized (historicoDeAcoes) {
        for (String entrada : historicoDeAcoes) {
            // Verifica se a entrada contém "Cliente: [id]"
            int indice = entrada.indexOf("Cliente ");
            if (indice != -1) {
                String parteCliente = entrada.substring(indice);
                String[] partes = parteCliente.split(" ");
                if (partes.length >= 2) {
                    String idCliente = partes[1];

                    logPorCliente.putIfAbsent(idCliente, new ArrayList<>());
                    logPorCliente.get(idCliente).add(entrada);
                }
            }
        }
    }

    if (logPorCliente.isEmpty()) {
        System.out.println("Nenhum log associado a clientes encontrado.");
    } else {
        for (Map.Entry<String, List<String>> entrada : logPorCliente.entrySet()) {
            System.out.println("Cliente com ID: " + entrada.getKey());
            for (String acao : entrada.getValue()) {
                System.out.println("  - " + acao);
            }
            System.out.println("--------------------------------");
        }
    }

    System.out.println("================================");
    System.out.println("Pressione Enter para continuar...");
    Scanner scanner = new Scanner(System.in);
    scanner.nextLine();
}


    private static void mostrarClientesConectados() {
    int numeroClientes = clientesConectados.size();
    
    System.out.println("=== CLIENTES CONECTADOS ===");
    System.out.println("Numero total de clientes conectados: " + numeroClientes);
    
    if (numeroClientes > 0) {
        System.out.println("IDs dos clientes conectados:");
        for (String id : clientesConectados.keySet()) {
            System.out.println("- Cliente: " + id);
        }
    } else {
        System.out.println("Nenhum cliente conectado no momento.");
    }
    
    System.out.println("========================");
    System.out.println("Pressione Enter para continuar...");
    Scanner scanner = new Scanner(System.in);
    scanner.nextLine(); 
    }

    private static void Menu(){
        System.out.println("=== SERVIDOR DE HORA ===");
        System.out.println("01 - Mostrar Hora Atual Automatica");
        System.out.println("02 - Mostrar Numero de Clientes Conectados");
        System.out.println("03 - Mostrar Log de Acoes");
        System.out.println("04 - Encerrar Todas as Conexoes");
        System.out.println("05 - Derrubar Servidor");        
    }

    private static void opcaoServidor(){
        Scanner scanner = new Scanner(System.in);
        int opcao;

        do{
            Menu();
            String resposta = scanner.nextLine();
            
            opcao = Integer.parseInt(resposta);
            

            switch(opcao){
                case 1:
                    mostrarHoraAutomaticaServidor();
                    break;
                case 2:
                    mostrarClientesConectados();
                    break;
                case 3:
                    mostrarLogPorCliente();
                    break;
                case 4:
                    desconectarTodosOsClientes();
                    System.out.println("Todos os clientes foram desconectados...");
                    break;
                case 5:
                    encerrarServidor();
                    break;
            }

        }while(opcao != 5);
    }


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

    public static void adicionarCliente(String id, PrintWriter escritor, Socket sockets) {
        clientesConectados.put(id, escritor);
        socketsClientes.put(id, sockets);
    }

    public static void desconectarTodosOsClientes(){
        PrintWriter saidaCliente;
        Socket socketCliente;

        for (Map.Entry<String, PrintWriter> entry : clientesConectados.entrySet()) {
            saidaCliente = entry.getValue();
            String id = entry.getKey();

            socketCliente = socketsClientes.get(id);

            try{

                if(saidaCliente != null){
                    saidaCliente.println("Voce foi desconectado pelo servidor.");
                    saidaCliente.close();
                }

                if(socketCliente != null && !socketCliente.isClosed()){
                    socketCliente.close();
                    System.out.println("Soquete do cliente " + id + " fechado");
                }

            }catch(IOException e){
                e.getMessage();
            }

        }
    }

    private static void encerrarServidor() {
        try {
            desconectarTodosOsClientes(); 
            if (servidor != null) servidor.close();
            if (poolConexoes != null) poolConexoes.shutdown();
        } catch (IOException e) {
        System.err.println("Erro ao encerrar servidor: " + e.getMessage());
    }
    }
    
    public static void main(String[] args) {
        
        try  {
            servidor = new ServerSocket(PORTA);
            poolConexoes = Executors.newFixedThreadPool(10);

            System.out.println("Servidor iniciado na porta " + PORTA);

            // Recebe a conexao do cliente
            // O servidor de tempo so avanca apos conexao
            registrarAcao("Servidor iniciado na porta " + PORTA);

            Thread menuThread = new Thread( new Runnable(){
                @Override
                public void run(){
                    opcaoServidor();
                }
            });
            menuThread.start();

            while(true){


            Socket clienteSocket = servidor.accept();
            poolConexoes.execute(new ClienteThread(clienteSocket));

            }
        } catch (IOException e) {
            System.err.println("Erro no servidor: " + e.getMessage());
        }
        finally {
            encerrarServidor();
            System.out.println("Servidor encerrado.");
        }
    }

}
