package Criptografia;

public class Blowfish {
    
    private static final int numero_de_subchaves = 18;
    private static final int Rodadas = 16;
    private static final int TAMANHO_BLOCO = 8; // 64 bits = 8 bytes

    // Inicialização dos S-Boxes (clonados para evitar modificar o original)
    public static String[] SBox0 = Util.sBox0.clone();
    public static String[] SBox1 = Util.sBox1.clone();
    public static String[] SBox2 = Util.sBox2.clone();
    public static String[] SBox3 = Util.sBox3.clone();

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
            xl = Util.operacaoXORComInteiros(xl, valorP);
            xr = Util.operacaoXORComInteiros(funcaoF(xl), xr);

            // Swap
            int aux = xl;
            xl = xr;
            xr = aux;
        }
        int aux = xl;
        xl = xr;
        xr = aux;

        // Verificação de segurança para evitar IndexOutOfBounds
        if (Array_P.length > Rodadas) {
            xr = Util.operacaoXORComInteiros(xr, Integer.parseUnsignedInt(Array_P[Rodadas], 16));
        }
        if (Array_P.length > Rodadas + 1) {
            xl = Util.operacaoXORComInteiros(xl, Integer.parseUnsignedInt(Array_P[Rodadas + 1], 16));
        } else if (Array_P.length > Rodadas) {
            xl = Util.operacaoXORComInteiros(xl, Integer.parseUnsignedInt(Array_P[Rodadas], 16));
        }

        return new int[] {xl, xr};
    }

    
    // Atualiza os arrays P e S progressivamente aplicando cifragem iterativa do bloco zero
    public static void keySchedule(String[] Array_P, String[] s0, String[] s1, String[] s2, String[] s3, byte[] chaveBytes) {
        // 1. XOR do array P com bytes da chave
        int maxIndex = Math.min(numero_de_subchaves, Array_P.length);
        for (int i = 0; i < maxIndex; i++) {
            int chave32 = 0;
            for (int j = 0; j < 4; j++) {
                int b = chaveBytes[(i * 4 + j) % chaveBytes.length] & 0xFF;
                chave32 = (chave32 << 8) | b;
            }
            Array_P[i] = Util.operacaoXOR(Array_P[i], chave32);
        }
        
        // 2. Cifrar iterativamente para atualizar P e S
        int xl = 0x00000000;
        int xr = 0x00000000;
        
        // Para facilitar uso das S-boxes mutáveis no funcaoF, atribui-las temporariamente
        SBox0 = s0;
        SBox1 = s1;
        SBox2 = s2;
        SBox3 = s3;
        
        for (int i = 0; i < maxIndex && i + 1 < Array_P.length; i += 2) {
            int[] res = cifrarBloco(xl, xr, Array_P);
            xl = res[0];
            xr = res[1];
            Array_P[i] = String.format("%08x", xl);
            if (i + 1 < Array_P.length) {
                Array_P[i+1] = String.format("%08x", xr);
            }
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
        
        byte[] chaveBytes = Util.hexStringToByteArray(chave);
        
        // Executa o key schedule para inicializar P e S com a chave
        keySchedule(Array_P, s0, s1, s2, s3, chaveBytes);
        
        // Prepara dados com padding
        byte[] texto_plano_Bytes = Util.hexStringToByteArray(texto_plano);
        byte[] dadosComPadding = Util.aplicarPadding(texto_plano_Bytes);
        
        StringBuilder resultado = new StringBuilder();
        
        // Processa cada bloco de 64 bits
        for (int offset = 0; offset < dadosComPadding.length; offset += TAMANHO_BLOCO) {
            int parte_esquerda = ((dadosComPadding[offset] & 0xFF) << 24) | 
            ((dadosComPadding[offset + 1] & 0xFF) << 16) |
            ((dadosComPadding[offset + 2] & 0xFF) << 8) | 
            (dadosComPadding[offset + 3] & 0xFF);
            
            int parte_direita = ((dadosComPadding[offset + 4] & 0xFF) << 24) | 
            ((dadosComPadding[offset + 5] & 0xFF) << 16) |
            ((dadosComPadding[offset + 6] & 0xFF) << 8) | 
                               (dadosComPadding[offset + 7] & 0xFF);
                               
            // Cifra o bloco
            int[] blocoResultado = cifrarBloco(parte_esquerda, parte_direita, Array_P);
            resultado.append(String.format("%08x%08x", blocoResultado[0], blocoResultado[1]));
        }
        
        return resultado.toString();
    }
    
    // Descriptografa um único bloco 64 bits - usa subchaves em ordem reversa
    public static int[] descriptografarBloco(int xl, int xr, String[] Array_P) {
        // Verificação de segurança para evitar IndexOutOfBounds
        if (Array_P.length > Rodadas + 1) {
            xl = Util.operacaoXORComInteiros(xl, Integer.parseUnsignedInt(Array_P[Rodadas + 1], 16));
        } else if (Array_P.length > Rodadas) {
            xl = Util.operacaoXORComInteiros(xl, Integer.parseUnsignedInt(Array_P[Rodadas], 16));
        }
        
        if (Array_P.length > Rodadas) {
            xr = Util.operacaoXORComInteiros(xr, Integer.parseUnsignedInt(Array_P[Rodadas], 16));
        }

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

            xr = Util.operacaoXORComInteiros(funcaoF(xl), xr);
            int valorP = Integer.parseUnsignedInt(Array_P[i], 16);
            xl = Util.operacaoXORComInteiros(xl, valorP);
        }

        return new int[] {xl, xr};
    }

    public static String Descriptografar(String texto_cifrado, String chave) {
        // Clona arrays para evitar modificar os originais
        String[] Array_P = Util.Array_P.clone();
        String[] s0 = Util.sBox0.clone();
        String[] s1 = Util.sBox1.clone();
        String[] s2 = Util.sBox2.clone();
        String[] s3 = Util.sBox3.clone();

        byte[] chaveBytes = Util.hexStringToByteArray(chave);

        // Executa o key schedule para inicializar P e S com a chave
        keySchedule(Array_P, s0, s1, s2, s3, chaveBytes);

        // Converte texto cifrado para bytes
        byte[] texto_cifrado_Bytes = Util.hexStringToByteArray(texto_cifrado);
        
        if (texto_cifrado_Bytes.length % TAMANHO_BLOCO != 0) {
            throw new IllegalArgumentException("Texto cifrado deve ter tamanho múltiplo de 8 bytes");
        }

        byte[] resultado_bytes = new byte[texto_cifrado_Bytes.length];
        int pos = 0;

        // Descriptografa cada bloco de 64 bits
        for (int offset = 0; offset < texto_cifrado_Bytes.length; offset += TAMANHO_BLOCO) {
            int parte_esquerda = ((texto_cifrado_Bytes[offset] & 0xFF) << 24) | 
                                ((texto_cifrado_Bytes[offset + 1] & 0xFF) << 16) |
                                ((texto_cifrado_Bytes[offset + 2] & 0xFF) << 8) | 
                                (texto_cifrado_Bytes[offset + 3] & 0xFF);
            
            int parte_direita = ((texto_cifrado_Bytes[offset + 4] & 0xFF) << 24) | 
                               ((texto_cifrado_Bytes[offset + 5] & 0xFF) << 16) |
                               ((texto_cifrado_Bytes[offset + 6] & 0xFF) << 8) | 
                               (texto_cifrado_Bytes[offset + 7] & 0xFF);

            // Descriptografa o bloco
            int[] blocoResultado = descriptografarBloco(parte_esquerda, parte_direita, Array_P);
            
            resultado_bytes[pos++] = (byte) ((blocoResultado[0] >> 24) & 0xFF);
            resultado_bytes[pos++] = (byte) ((blocoResultado[0] >> 16) & 0xFF);
            resultado_bytes[pos++] = (byte) ((blocoResultado[0] >> 8) & 0xFF);
            resultado_bytes[pos++] = (byte) (blocoResultado[0] & 0xFF);
            resultado_bytes[pos++] = (byte) ((blocoResultado[1] >> 24) & 0xFF);
            resultado_bytes[pos++] = (byte) ((blocoResultado[1] >> 16) & 0xFF);
            resultado_bytes[pos++] = (byte) ((blocoResultado[1] >> 8) & 0xFF);
            resultado_bytes[pos++] = (byte) (blocoResultado[1] & 0xFF);
        }

        // Remove o padding
        byte[] dadosSemPadding = Util.removerPadding(resultado_bytes);
        
        return Util.byteArrayToHexString(dadosSemPadding);
    }

    public static void main(String[] args) {
        String chave = "aabb09182736ccdd";
        String textoPlano = "123456abcd132536";
        
        // Teste com string longa como data/hora
        System.out.println("\n=== TESTE COM STRING LONGA ===");
        String dataHora = "11/09/2025 14:30:45";
        String dataHoraHex = Util.stringParaHex(dataHora);
        System.out.printf("Data/Hora original: %s%n", dataHora);
        System.out.printf("Em hex: %s%n", dataHoraHex);
        
        String cifrado = Encriptar(dataHoraHex, chave);
        System.out.printf("Cifrado: %s%n", cifrado);
        
        String decifrado = Descriptografar(cifrado, chave);
        String dataHoraFinal = Util.hexParaString(decifrado);
        
        System.out.printf("Descriptografado (hex): %s%n", decifrado);
        System.out.printf("Data/Hora final: %s%n", dataHoraFinal);
        System.out.printf("String completa preservada: %s%n", 
                         dataHora.equals(dataHoraFinal) ? "SIM" : "NÃO");
    }
}