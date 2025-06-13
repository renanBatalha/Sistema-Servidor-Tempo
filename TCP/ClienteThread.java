import java.io.*;
import java.net.*;
import java.util.List;

public class ClienteThread implements Runnable{
    private Socket conexao; 
    private String clienteConectado;
    private BufferedReader entrada;
    private PrintWriter saida;
    private boolean atualizacaoAtiva = true;

    ClienteThread(Socket conexao) {
        this.conexao = conexao;
        this.clienteConectado = conexao.getRemoteSocketAddress().toString();
    }

    public void enviarTempoPorIntervalo(int intervalo, PrintWriter saida, BufferedReader entrada) {
        this.atualizacaoAtiva = true;
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss");
                while(atualizacaoAtiva){
                    try {
                        saida.println("Tempo Atraso Servidor:" + System.currentTimeMillis() + ":" + java.time.LocalTime.now().format(formatter));
                        saida.flush();
                        Thread.sleep(intervalo);
                    } catch (InterruptedException e) {
                        System.err.println("Erro ao enviar hora automaticamente: " + e.getMessage());
                        break;
                    }
                }
            }
        });
        t.start();
    }

    @Override
    public void run(){
        try{
            entrada = new BufferedReader(new InputStreamReader(conexao.getInputStream()));
            saida = new PrintWriter(conexao.getOutputStream(), true);

            ServidorDeTempo.adicionarCliente(clienteConectado, saida, conexao);
            ServidorDeTempo.registrarAcao("Cliente conectado: " + clienteConectado);
            String comando;

            do{
                // Exibe o menu e aguarda o comando do cliente
                Menu();
                comando = entrada.readLine();
                tratarComando(Integer.parseInt(comando));
            }while(comando != null && !comando.equals("4"));
            
            ServidorDeTempo.registrarAcao("Cliente desconectado: " + clienteConectado);

        } catch(IOException e){
            System.err.println("Erro ao iniciar a conexao: " + e.getMessage());
        }
    }

    private void Menu() throws IOException{
        saida.println("=== SERVIDOR DE HORA ===");
        saida.println("Comandos disponiveis:");
        saida.println("1.  - Mostrar a Hora Atual");
        saida.println("2.  - Enviar Hora Automaticamente a Cada Intervalo");
        saida.println("3.  - Encerrar Conexao");
        saida.println("Digite um Comando: ");
    }
    
    private void tratarComando(Integer comando) throws IOException {
        switch (comando) {
            case 1:
                enviarHoraAtual();
                ServidorDeTempo.registrarAcao("Cliente " + clienteConectado + " solicitou a hora atual");
                break;
            case 2:
                Integer tempo;
                saida.println("Digite o intervalo de tempo que deseja receber atualizacoes (milisegundos): ");
                tempo =  Integer.parseInt(entrada.readLine());
                ServidorDeTempo.registrarAcao("Cliente " + clienteConectado + " solicitou atualizacao automatica de tempo: " + tempo + "ms");

                enviarTempoPorIntervalo(tempo, saida, entrada);

                String resposta;
                while ((resposta = entrada.readLine()) != null) {
                    if (resposta.trim().equalsIgnoreCase("p")) {
                        this.atualizacaoAtiva = false;
                        break;
                    }
                }
                break;
            case 3:
                saida.println("Encerrando conexao...");
                ServidorDeTempo.registrarAcao("Cliente " + clienteConectado + " encerrou a conexao");
                try {
                    conexao.close();
                    System.out.println("O cliente " + clienteConectado + " se desconectou.");
                } catch (IOException e) {
                    System.err.println("Erro ao fechar conexao: " + e.getMessage());
                }
                finally {
                    encerrarConexao();
                }
                break;
            default:
                saida.println("Comando invalido. Tente novamente.");
        }
    } 
    
    private void enviarHoraAtual() {
        String horaAtual = ServidorDeTempo.obterTempoAtual();
        saida.println("Hora atual: " + horaAtual);
    }
   
    private void encerrarConexao() {
            try {
                if(entrada != null) {
                    entrada.close();
                }
                if(saida != null) {
                    saida.close();
                }
                if(conexao != null && !conexao.isClosed()) {
                    conexao.close();
                }
            } catch (IOException e) {
                System.err.println("Erro ao encerrar a conexao: " + e.getMessage());
            }
            finally {
                ServidorDeTempo.removerCliente(clienteConectado);
            }
    }
}


