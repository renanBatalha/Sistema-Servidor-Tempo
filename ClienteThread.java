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
        } catch(IOException e){
            System.err.println("Erro ao iniciar a conexão: " + e.getMessage());
        }
    }

public void Menu() throws IOException{
        saida.println("=== SERVIDOR DE HORA ===");
        saida.println("Comandos disponíveis:");
        saida.println("1. HORA - Mostrar a hora atual");
        saida.println("2. AUTOMATICO <segundos> - Enviar hora automaticamente a cada intervalo");
        saida.println("3. PARAR - Parar atualizações automáticas");
        saida.println("4. HISTORICO - Ver histórico de ações");
        saida.println("5. SAIR - Encerrar conexão");
        saida.println("Digite um comando:");
    }
public void tratarComando(String comando) {
        String[] partes = comando.split(" ");
        String acao = partes[0].toUpperCase();

        switch (acao) {
            case "HORA":
                enviarHoraAtual();
                break;

            case "AUTOMATICO":
                if (partes.length > 1) {
                    configurarAtualizacaoAutomatica(partes[1]);
                } else {
                    escritor.println("ERRO: Informe o intervalo em segundos (ex: AUTOMATICO 10)");
                }
                break;

            case "PARAR":
                pararAtualizacao();
                break;

            case "HISTORICO":
                enviarHistorico();
                break;

            case "SAIR":
                encerrarConexao();
                break;

            default:
                escritor.println("Comando inválido.");
        }
    } 
private void enviarHoraAtual() {
        String horaAtual = java.time.LocalTime.now().toString();
        saida.println("Hora atual: " + horaAtual);
    }
}

