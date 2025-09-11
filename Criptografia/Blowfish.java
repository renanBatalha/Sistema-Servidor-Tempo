package Criptografia;

public class Blowfish {
    
    private static final int numero_de_subchaves = 18;
    private static final int Rodadas = 16;

    // Inicialização dos S-Boxes (clonados para evitar modificar o original)
    public static String[] SBox0 = Util.sBox0.clone();
    public static String[] SBox1 = Util.sBox1.clone();
    public static String[] SBox2 = Util.sBox2.clone();
    public static String[] SBox3 = Util.sBox3.clone();

    public static String operacaoXOR(String hex1, int int2){
        int int1 = Integer.parseUnsignedInt(hex1, 16);
        int resultado = int1 ^ int2;
        return String.format("%08x", resultado);
    }

    public static String byteArrayToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static int operacaoXORComInteiros(int num1, int num2){
        return num1 ^ num2;
    }


    
    public static int funcaoF(int parte_esquerda) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) ((parte_esquerda >> 24) & 0xFF);
        bytes[1] = (byte) ((parte_esquerda >> 16) & 0xFF);
        bytes[2] = (byte) ((parte_esquerda >> 8) & 0xFF);
        bytes[3] = (byte) (parte_esquerda & 0xFF);

        int a = bytes[0] & 0xFF;
        int b = bytes[1] & 0xFF;
        int c = bytes[2] & 0xFF;
        int d = bytes[3] & 0xFF;

        int v0 = Integer.parseUnsignedInt(SBox0[a], 16);
        int v1 = Integer.parseUnsignedInt(SBox1[b], 16);
        int v2 = Integer.parseUnsignedInt(SBox2[c], 16);
        int v3 = Integer.parseUnsignedInt(SBox3[d], 16);

        long tmp = ((long)v0 + (long)v1) & 0xFFFFFFFFL;
        tmp = (tmp ^ (long)v2) & 0xFFFFFFFFL;
        tmp = (tmp + (long)v3) & 0xFFFFFFFFL;

        return (int)tmp;
    }

    // Cifra um único bloco 64 bits com a rede Feistel usando os arrays mutáveis P e S atuais
    public static int[] cifrarBloco(int xl, int xr, String[] Array_P) {
        for (int i = 0; i < Rodadas; i++) {
            int valorP = Integer.parseUnsignedInt(Array_P[i], 16);
            xl = operacaoXORComInteiros(xl, valorP);
            xr = operacaoXORComInteiros(funcaoF(xl), xr);

            // Swap
            int aux = xl;
            xl = xr;
            xr = aux;
        }
        int aux = xl;
        xl = xr;
        xr = aux;

        xr = operacaoXORComInteiros(xr, Integer.parseUnsignedInt(Array_P[Rodadas], 16));
        xl = operacaoXORComInteiros(xl, Integer.parseUnsignedInt(Array_P[Rodadas + 1], 16));

        return new int[] {xl, xr};
    }

    // Descriptografa um único bloco 64 bits - usa subchaves em ordem reversa
    public static int[] descriptografarBloco(int xl, int xr, String[] Array_P) {
        xl = operacaoXORComInteiros(xl, Integer.parseUnsignedInt(Array_P[Rodadas + 1], 16));
        xr = operacaoXORComInteiros(xr, Integer.parseUnsignedInt(Array_P[Rodadas], 16));

        // Swap inicial
        int aux = xl;
        xl = xr;
        xr = aux;

        // Aplica rodadas em ordem reversa (de 15 até 0)
        for (int i = Rodadas - 1; i >= 0; i--) {
            // Swap
            aux = xl;
            xl = xr;
            xr = aux;

            xr = operacaoXORComInteiros(funcaoF(xl), xr);
            int valorP = Integer.parseUnsignedInt(Array_P[i], 16);
            xl = operacaoXORComInteiros(xl, valorP);
        }

        return new int[] {xl, xr};
    }

    // Atualiza os arrays P e S progressivamente aplicando cifragem iterativa do bloco zero
    public static void keySchedule(String[] Array_P, String[] s0, String[] s1, String[] s2, String[] s3, byte[] chaveBytes) {
        // 1. XOR do array P com bytes da chave
        for (int i = 0; i < numero_de_subchaves; i++) {
            int chave32 = 0;
            for (int j = 0; j < 4; j++) {
                int b = chaveBytes[(i * 4 + j) % chaveBytes.length] & 0xFF;
                chave32 = (chave32 << 8) | b;
            }
            Array_P[i] = operacaoXOR(Array_P[i], chave32);
        }

        // 2. Cifrar iterativamente para atualizar P e S
        int xl = 0x00000000;
        int xr = 0x00000000;

        // Para facilitar uso das S-boxes mutáveis no funcaoF, atribui-las temporariamente
        SBox0 = s0;
        SBox1 = s1;
        SBox2 = s2;
        SBox3 = s3;

        for (int i = 0; i < numero_de_subchaves; i += 2) {
            int[] res = cifrarBloco(xl, xr, Array_P);
            xl = res[0];
            xr = res[1];
            Array_P[i] = String.format("%08x", xl);
            Array_P[i+1] = String.format("%08x", xr);
        }

        for (int i = 0; i < 256; i += 2) {
            int[] res = cifrarBloco(xl, xr, Array_P);
            xl = res[0];
            xr = res[1];
            s0[i] = String.format("%08x", xl);
            s0[i + 1] = String.format("%08x", xr);
        }

        for (int i = 0; i < 256; i += 2) {
            int[] res = cifrarBloco(xl, xr, Array_P);
            xl = res[0];
            xr = res[1];
            s1[i] = String.format("%08x", xl);
            s1[i + 1] = String.format("%08x", xr);
        }

        for (int i = 0; i < 256; i += 2) {
            int[] res = cifrarBloco(xl, xr, Array_P);
            xl = res[0];
            xr = res[1];
            s2[i] = String.format("%08x", xl);
            s2[i + 1] = String.format("%08x", xr);
        }

        for (int i = 0; i < 256; i += 2) {
            int[] res = cifrarBloco(xl, xr, Array_P);
            xl = res[0];
            xr = res[1];
            s3[i] = String.format("%08x", xl);
            s3[i + 1] = String.format("%08x", xr);
        }

        // Reatribuir para S-boxes atualizadas após modificação
        SBox0 = s0;
        SBox1 = s1;
        SBox2 = s2;
        SBox3 = s3;
    }

    public static String Encriptar(String texto_plano, String chave) {
        // Clona arrays para evitar modificar os originais
        String[] Array_P = Util.Array_P.clone();
        String[] s0 = Util.sBox0.clone();
        String[] s1 = Util.sBox1.clone();
        String[] s2 = Util.sBox2.clone();
        String[] s3 = Util.sBox3.clone();

        byte[] chaveBytes = hexStringToByteArray(chave);

        // Executa o key schedule para inicializar P e S com a chave
        keySchedule(Array_P, s0, s1, s2, s3, chaveBytes);

        // Prepara bloco do texto plano
        byte[] texto_plano_Bytes = hexStringToByteArray(texto_plano);
        byte[] texto_plano_64Bytes = new byte[8];
        int bytesACopiar = Math.min(8, texto_plano_Bytes.length);
        System.arraycopy(texto_plano_Bytes, 0, texto_plano_64Bytes, 0, bytesACopiar);
        for (int i = bytesACopiar; i < 8; i++) {
            texto_plano_64Bytes[i] = 0;
        }

        int parte_esquerda = ((texto_plano_64Bytes[0] & 0xFF) << 24) | ((texto_plano_64Bytes[1] & 0xFF) << 16) |
                             ((texto_plano_64Bytes[2] & 0xFF) << 8) | (texto_plano_64Bytes[3] & 0xFF);
        int parte_direita = ((texto_plano_64Bytes[4] & 0xFF) << 24) | ((texto_plano_64Bytes[5] & 0xFF) << 16) |
                            ((texto_plano_64Bytes[6] & 0xFF) << 8) | (texto_plano_64Bytes[7] & 0xFF);

        // Cifra o bloco com subchaves atualizadas
        int[] resultado = cifrarBloco(parte_esquerda, parte_direita, Array_P);

        System.out.printf("Texto cifrado 64 bits: %08x%08x%n", resultado[0], resultado[1]);
        return String.format("%08x%08x", resultado[0], resultado[1]);
    }

     public static String Descriptografar(String texto_cifrado, String chave) {
        // Clona arrays para evitar modificar os originais
        String[] Array_P = Util.Array_P.clone();
        String[] s0 = Util.sBox0.clone();
        String[] s1 = Util.sBox1.clone();
        String[] s2 = Util.sBox2.clone();
        String[] s3 = Util.sBox3.clone();

        byte[] chaveBytes = hexStringToByteArray(chave);

        // Executa o key schedule para inicializar P e S com a chave
        keySchedule(Array_P, s0, s1, s2, s3, chaveBytes);

        // Prepara bloco do texto cifrado
        byte[] texto_cifrado_Bytes = hexStringToByteArray(texto_cifrado);
        
        // Garante que temos exatamente 8 bytes (64 bits)
        if (texto_cifrado_Bytes.length < 8) {
            byte[] temp = new byte[8];
            System.arraycopy(texto_cifrado_Bytes, 0, temp, 0, texto_cifrado_Bytes.length);
            texto_cifrado_Bytes = temp;
        }

        int parte_esquerda = ((texto_cifrado_Bytes[0] & 0xFF) << 24) | ((texto_cifrado_Bytes[1] & 0xFF) << 16) |
                             ((texto_cifrado_Bytes[2] & 0xFF) << 8) | (texto_cifrado_Bytes[3] & 0xFF);
        int parte_direita = ((texto_cifrado_Bytes[4] & 0xFF) << 24) | ((texto_cifrado_Bytes[5] & 0xFF) << 16) |
                            ((texto_cifrado_Bytes[6] & 0xFF) << 8) | (texto_cifrado_Bytes[7] & 0xFF);

        // Descriptografa o bloco com subchaves atualizadas
        int[] resultado = descriptografarBloco(parte_esquerda, parte_direita, Array_P);

        // Converte resultado de volta para bytes
        byte[] resultado_bytes = new byte[8];
        resultado_bytes[0] = (byte) ((resultado[0] >> 24) & 0xFF);
        resultado_bytes[1] = (byte) ((resultado[0] >> 16) & 0xFF);
        resultado_bytes[2] = (byte) ((resultado[0] >> 8) & 0xFF);
        resultado_bytes[3] = (byte) (resultado[0] & 0xFF);
        resultado_bytes[4] = (byte) ((resultado[1] >> 24) & 0xFF);
        resultado_bytes[5] = (byte) ((resultado[1] >> 16) & 0xFF);
        resultado_bytes[6] = (byte) ((resultado[1] >> 8) & 0xFF);
        resultado_bytes[7] = (byte) (resultado[1] & 0xFF);

        String textoPlanoHex = byteArrayToHexString(resultado_bytes);
        System.out.printf("Texto descriptografado: %s%n", textoPlanoHex);
        return textoPlanoHex;
    }

    public static void main(String[] args) {
        String chave = "aabb09182736ccdd";
        String textoPlano = "123456abcd132536";
        
        
        System.out.println("=== TESTE DE CRIPTOGRAFIA ===");
        String textoCifrado = Encriptar(textoPlano, chave);
        
        
        System.out.println("\n=== TESTE DE DESCRIPTOGRAFIA ===");
        String textoDescriptografado = Descriptografar(textoCifrado, chave);
        
        
        System.out.println("\n=== VERIFICAÇÃO ===");
        System.out.printf("Texto original:        %s%n", textoPlano);
        System.out.printf("Texto descriptografado: %s%n", textoDescriptografado);
        System.out.printf("Descriptografia correta: %s%n", 
                         textoPlano.equals(textoDescriptografado) ? "SIM" : "NÃO");
    }
}
