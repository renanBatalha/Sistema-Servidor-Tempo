package TCP_RMI;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// criptografia
import Criptografia.Blowfish;
import Criptografia.Util;
import Criptografia.HashMD5;

public class ServidorTempo implements Tempo{
    @Override
    public String obterTempoAtual() throws RemoteException {
        LocalDateTime tempoAtual = LocalDateTime.now();
        String resposta = tempoAtual.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        logs.add("[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
                + "] Cliente solicitou hora atual: " + resposta);
        // Convertendo a string para hexadecimal
        String respostaHex = Util.stringParaHex(resposta);
        String chave_criptografada = Util.stringParaHex(chave);
        String respostaCriptografada = Blowfish.Encriptar(respostaHex, chave_criptografada);
        return respostaCriptografada;
    }
    
    @Override
    public String obterTempoAtualComHash() throws RemoteException {
        LocalDateTime tempoAtual = LocalDateTime.now();
        String resposta = tempoAtual.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        logs.add("[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
                + "] Cliente solicitou hora atual (com hash): " + resposta);
        // Convertendo a string para hexadecimal
        String respostaHex = Util.stringParaHex(resposta);
        String chave_criptografada = Util.stringParaHex(chave);
        String respostaCriptografada = Blowfish.Encriptar(respostaHex, chave_criptografada);
        // Gerar hash MD5 da mensagem criptografada
        String hashMd5 = HashMD5.gerarHashMD5(respostaCriptografada);
        // Retorna no formato mensagemCriptografada:hashMd5
        return respostaCriptografada + ":" + hashMd5;
    }

    private final List<String> logs = new ArrayList<>();
    private static final String chave = "minhaChaveSecreta";

    public static void Menu(){
        System.out.println("=== SERVIDOR DE TEMPO ===");
        System.out.println("Comandos disponiveis:");
        System.out.println("1.  - Mostrar log de solicitações");
        System.out.println("2.  - Encerrar Servidor");
        System.out.println("Digite um Comando: ");
    }
    
    @Override
    public synchronized List<String> obterLog() throws RemoteException {
        return new ArrayList<>(logs); 
    }


    public static void main(String[] args) {
            try {
                ServidorTempo obj = new ServidorTempo();
                Tempo stub = (Tempo) UnicastRemoteObject.exportObject(obj, 0);

                System.setProperty("java.rmi.server.hostname", "localhost");

                // Cria o registry na porta 2000
                Registry registry = LocateRegistry.createRegistry(2000);
                registry.bind("ServidorTempo", stub);

                System.out.println("Servidor pronto e rodando na porta 2000");

                int opcao;
                Scanner entrada = new Scanner(System.in);
                
                do {
                    Menu();
                    opcao = entrada.nextInt();
                    switch (opcao) {
                        case 1:
                            // Mostrar log de solicitações
                            System.out.println("Log de solicitações:");
                            List<String> log = obj.obterLog();
                            for (String registro : log) {
                                System.out.println(registro);
                            }
                            break;
                        case 2:
                            System.out.println("Encerrando servidor...");
                            System.exit(0);
                            break;
                        default:
                            System.out.println("Opção inválida!");
                            break;
                    }
                } while (opcao != 2);
                entrada.close();
                
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
}