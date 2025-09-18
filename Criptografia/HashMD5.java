package Criptografia;

import java.security.MessageDigest;

public class HashMD5 {

    // Gera hash MD5 de uma string hexadecimal
    public static String gerarHashMD5(String texto) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(texto.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar MD5", e);
        }
    }

    public static void main(String[] args) {
        String chave = "aabb09182736ccdd";

        System.out.println("\n=== TESTE COM STRING LONGA + MD5 ===");
        String dataHora = "11/09/2025 14:30:45";
        String dataHoraHex = Util.stringParaHex(dataHora);
        System.out.printf("Data/Hora original: %s%n", dataHora);
        System.out.printf("Em hex: %s%n", dataHoraHex);

        // 1. Criptografa com Blowfish
        String cifrado = Blowfish.Encriptar(dataHoraHex, chave);
        System.out.printf("Cifrado: %s%n", cifrado);

        // 2. Aplica hash MD5 sobre o texto cifrado
        String hash = gerarHashMD5(cifrado);
        System.out.printf("Hash MD5 do cifrado: %s%n", hash);

        // 3. Descriptografa com Blowfish
        String decifrado = Blowfish.Descriptografar(cifrado, chave);
        String dataHoraFinal = Util.hexParaString(decifrado);
        System.out.printf("Descriptografado (hex): %s%n", decifrado);
        System.out.printf("Data/Hora final: %s%n", dataHoraFinal);

        // 4. Confere integridade da mensagem original
        System.out.printf("String completa preservada: %s%n",
                dataHora.equals(dataHoraFinal) ? "SIM" : "NÃO");

    }
}
