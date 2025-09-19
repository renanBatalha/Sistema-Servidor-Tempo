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
}
