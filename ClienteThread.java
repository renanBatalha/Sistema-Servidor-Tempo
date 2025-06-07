import java.io.*;
import java.net.*;
import java.util.List;

// classe de manipulacao / handler
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
        saida.println("Comandos disponiveis:");
        saida.println("1.  - Mostrar a hora atual");
        saida.println("2.  - Enviar hora automaticamente a cada intervalo");
        saida.println("3.  - Ver historico de acoes");
        saida.println("4.  - Encerrar conexao");
        saida.println("Digite um comando:");
    }
    
private void tratarComando(Integer comando) throws IOException {
        switch (comando) {
            case 1:
                enviarHoraAtual();
                break;
            case 2:
                Integer tempo;
                System.out.println("Digite o intervalo de tempo que deseja receber atualizacoes (milisegundos): ");
                tempo =  Integer.parseInt(entrada.readLine());

                // tempo da requisicao de atualizacao
                long tempoDeEnvio = System.currentTimeMillis();

                ServidorDeTempo.definirAtualizacaoAutomatica(tempo, saida);
                
                String horaRecebida = entrada.readLine();

                long tempoDeRecebimento = System.currentTimeMillis();

                long atraso = tempoDeRecebimento - tempoDeEnvio;

                String horaCorrigida = ajustarHora(horaRecebida, atraso);

                System.out.println("Hora atualizada: " + horaCorrigida);                

                break;
            case 3:
                // Implementar lógica para mostrar histórico de ações
                saida.println("Historico de acoes");
                break;
            case 4:
                saida.println("Encerrando conexão...");
                try {
                    conexao.close();
                } catch (IOException e) {
                    System.err.println("Erro ao fechar conexao: " + e.getMessage());
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
private void AtualizarTempo(Integer tempo){
    try{
        if(tempo < 1){
            saida.println("ERRO: o tempo deve ser maior que zero");
            return;
        }
        
    } catch(NumberFormatException e){
        saida.println("ERRO: numero invalido");
    }
}

public String ajustarHora(String horaRecebida, long atraso) {
        // Ajusta a hora recebida com base no atraso
        String[] partes = horaRecebida.split(":");
        int horas = Integer.parseInt(partes[0]);
        int minutos = Integer.parseInt(partes[1]);
        int segundos = Integer.parseInt(partes[2]);

        // Converte o atraso de milissegundos para segundos
        long atrasoEmSegundos = atraso / 1000;

        // Ajusta os segundos
        segundos += atrasoEmSegundos;

        // Corrige os minutos e horas se necessário
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

}

private void enviarHis
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
            System.err.println("Erro ao encerrar a conexão: " + e.getMessage());
        }
    }
}

