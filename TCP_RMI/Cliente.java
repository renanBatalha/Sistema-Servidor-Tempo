package TCP_RMI;

import java.rmi.registry.LocateRegistry;
import Criptografia.Blowfish;
import Criptografia.Util;
import Criptografia.HashMD5;
import java.rmi.registry.Registry;
import java.util.Scanner;
import java.rmi.Naming;

public class Cliente {

    private static volatile boolean flag = true;
    private static String chave = "minhaChaveSecreta";

    public static void Menu() {
        System.out.println("=== SERVIDOR DE TEMPO ===");
        System.out.println("Comandos disponiveis:");
        System.out.println("1.  - Mostrar a Hora Atual");
        System.out.println("2.  - Enviar Hora Automaticamente a Cada Intervalo");
        System.out.println("3.  - Encerrar Programa");
        System.out.println("Digite um Comando: ");
    }

    public static void obterTempoAutomaticamente(int intervalo, Tempo servidor) {
        while (flag) {
            try {
                System.out.println("Hora Atual: ");
                String respostaComHash = servidor.obterTempoAtualComHash();
                String[] partes = respostaComHash.split(":");
                String mensagemCriptografada = partes[0];

                String chave_hexadecimal = Util.stringParaHex(chave);
                String mensagem_descriptografada = Blowfish.Descriptografar(mensagemCriptografada, chave_hexadecimal);
                mensagem_descriptografada = Util.hexParaString(mensagem_descriptografada);
                System.out.println(mensagem_descriptografada);
                Thread.sleep(intervalo * 1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("Observação do tempo automático concluída.");
    }

    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost");
            Tempo servidor = (Tempo) Naming.lookup("rmi://localhost:2000/ServidorTempo");

            Scanner entrada = new Scanner(System.in);
            int opcao;
            do {
                Menu();
                opcao = entrada.nextInt();
                switch (opcao) {
                    case 1:
                        // funcao para obter hora atual
                        System.out.println("Hora atual: ");
                        String respostaComHash = servidor.obterTempoAtualComHash();
                        String[] partes = respostaComHash.split(":");
                        String mensagemCriptografada = partes[0];
                        String hashRecebido = partes[1];

                        // Verificar a integridade da mensagem
                        String hashCalculado = HashMD5.gerarHashMD5(mensagemCriptografada);
                        if (hashCalculado.equals(hashRecebido)) {
                            System.out.println("Integridade da mensagem verificada: OK");
                        } else {
                            throw new SecurityException("Integridade da mensagem comprometida: HASH NÃO CORRESPONDE!");
                        }

                        String chave_hexadecimal = Util.stringParaHex(chave);
                        String mensagem_descriptografada = Blowfish.Descriptografar(mensagemCriptografada,
                                chave_hexadecimal);
                        mensagem_descriptografada = Util.hexParaString(mensagem_descriptografada);
                        System.out.println(mensagem_descriptografada);
                        break;
                    case 2:
                        // funcao para enviar hora automaticamente por intervalo de tempo
                        System.out.println("Digite o intervalo em segundos: ");
                        int intervalo = entrada.nextInt();
                        new Thread(() -> obterTempoAutomaticamente(intervalo, servidor)).start();
                        System.out.println("Pressione Enter para parar a observação automática...");
                        entrada.nextLine();
                        entrada.nextLine(); // Espera o usuario pressionar Enter
                        flag = false;
                        break;
                    case 3:
                        System.out.println("Fechar Programa...");
                        break;
                    default:
                        System.out.println("Opcao invalida!");
                        break;
                }

            } while (opcao != 3);
            entrada.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}