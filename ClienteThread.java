import java.io.*;
import java.net.*;
import java.util.List;

public class ClienteThread implements Runnable{
    private Socket conexao; 
    private String clienteConectado;
    private BufferedReader entrada;
    private PrintWriter saida;

    ClienteThread(Socket conexao) {
        this.conexao = conexao;
        this.clienteConectado = conexao.getRemoteSocketAddress().toString();
    }
    @Override
    public void run(){
        try{
            entrada = new BufferedReader(new InputStreamReader(conexao.getInputStream()));
            saida = new PrintWriter(conexao.getOutputStream(), true);

            Menu();
            String comando;
            while((comando = entrada.readLine()) != null){
                tratarComando(Integer.parseInt(comando));
            }
        } catch(IOException e){
            System.err.println("Erro ao iniciar a conexão: " + e.getMessage());
        }
    }

private void Menu() throws IOException{
        saida.println("=== SERVIDOR DE HORA ===");
        saida.println("Comandos disponíveis:");
        saida.println("1. HORA - Mostrar a hora atual");
        saida.println("2. AUTOMATICO <segundos> - Enviar hora automaticamente a cada intervalo");
        saida.println("3. PARAR - Parar atualizações automáticas");
        saida.println("4. HISTORICO - Ver histórico de ações");
        saida.println("5. SAIR - Encerrar conexão");
        saida.println("Digite um comando:");
    }
private void tratarComando(Integer comando) {
        switch (comando) {
            case 1:
                enviarHoraAtual();
                break;
            case 2:
                try {
                    String[] partes = entrada.readLine().split(" ");
                    int segundos = Integer.parseInt(partes[1]);
                    new Thread(() -> {
                        while (true) {
                            try {
                                Thread.sleep(segundos * 1000);
                                enviarHoraAtual();
                            } catch (InterruptedException e) {
                                break; // Interrompe o loop se a thread for interrompida
                            }
                        }
                    }).start();
                } catch (Exception e) {
                    saida.println("Erro ao iniciar envio automático: " + e.getMessage());
                }
                break;
            case 3:
                // Implementar lógica para parar atualizações automáticas
                saida.println("Atualizações automáticas paradas.");
                break;
            case 4:
                // Implementar lógica para mostrar histórico de ações
                saida.println("Histórico de ações não implementado.");
                break;
            case 5:
                saida.println("Encerrando conexão...");
                try {
                    conexao.close();
                } catch (IOException e) {
                    System.err.println("Erro ao fechar conexão: " + e.getMessage());
                }
                break;
            default:
                saida.println("Comando inválido. Tente novamente.");
        }
    } 
    
private void enviarHoraAtual() {
        String horaAtual = java.time.LocalTime.now().toString();
        saida.println("Hora atual: " + horaAtual);
    }
}

