package Criptografia;

public class Blowfish {
    
    private static final int numero_de_subchaves = 18;
    private static final int Rodadas = 16;

    // Inicializacao dos S-Boxes
        public static String[] SBox0 = Util.sBox0;
        public static String[] SBox1 = Util.sBox1;
        public static String[] SBox2 = Util.sBox2;
        public static String[] SBox3 = Util.sBox3;
    
    public static String operacaoXOR(String hex1, int int2){
        // Converte a string hexadecimal em um valor inteiro
        int int1 = Integer.parseUnsignedInt(hex1, 16);

        // Realiza a operação XOR
        int resultado = int1 ^ int2;

        String hexResultado = String.format("%08x", resultado);

        System.out.printf("XOR: (%s ^ %08x) = %s%n", hex1, int2, hexResultado);

        return hexResultado;
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
        int resultado = num1 ^ num2;
        return resultado;
    }

    public static int funcaoF(int parte_esquerda){

        // divide a parte esquerda por byte
        byte[] parte_esquerda_bytes = new byte[4];
        parte_esquerda_bytes[0] = (byte) ((parte_esquerda >> 24) & 0xFF);
        parte_esquerda_bytes[1] = (byte) ((parte_esquerda >> 16) & 0xFF);
        parte_esquerda_bytes[2] = (byte) ((parte_esquerda >> 8) & 0xFF);
        parte_esquerda_bytes[3] = (byte) (parte_esquerda & 0xFF);

        // cada inteiro representa um índice para os S-Boxes
        int a = parte_esquerda_bytes[0] & 0xFF;
        int b = parte_esquerda_bytes[1] & 0xFF;
        int c = parte_esquerda_bytes[2] & 0xFF;
        int d = parte_esquerda_bytes[3] & 0xFF;

        //F(R) = ((SBox0[a] + SBox1[b]) XOR SBox2[c]) + SBox3[d]
        int valorSBox0 = Integer.parseUnsignedInt(SBox0[a], 16);
        int valorSBox1 = Integer.parseUnsignedInt(SBox1[b], 16);    
        int valorSBox2 = Integer.parseUnsignedInt(SBox2[c], 16);
        int valorSBox3 = Integer.parseUnsignedInt(SBox3[d], 16);
        int resultado = (((valorSBox0 + valorSBox1)& 0xFFFFFFFF) ^ valorSBox2) + valorSBox3;
        resultado = resultado & 0xFFFFFFFF; // Mantém apenas os 32 bits menos significativos

        return resultado;
    }
    
    public static String Encriptar(String texto_plano, String chave){

        //==============::Passo 1: Gerar subchaves::==============//
        // Inicializacao do array P com valores hexadecimais oriundos do valor de pi
        String[] Array_P = Util.Array_P.clone();

        //==============::Passo 2: Mudar as subchaves com base na chave de entrada (P[0] = P[0] XOR Chave[0]) ::==============//

        // byte[] chaveBytes = chave.getBytes();
        byte[] chaveBytes = hexStringToByteArray(chave);
        int chave32Bytes = 0;

        for(int i = 0; i < numero_de_subchaves; i++){
            chave32Bytes = 0;

            for (int j = 0; j < 4; j++){
                // Realiza a operacao XOR entre a subchave e o byte da chave
                // A chave e repetida caso seja menor que o numero de subchaves
                int b = Byte.toUnsignedInt(chaveBytes[(i * 4 + j) % chaveBytes.length]);
                chave32Bytes = chave32Bytes << 8 | b;
            }

            //System.out.printf("Subchave P[%d] antes: %s, chave32Bytes: %08x%n", i, Array_P[i], chave32Bytes);

            Array_P[i] = operacaoXOR(Array_P[i], chave32Bytes);

            System.out.printf("Subchave P[%d] depois: %s%n", i, Array_P[i]);
            System.out.println("--------------------------------------------------");
        }

        //==============::Passo 3: Encriptar a mensagem com as 16 rodads de Feistel ::==============//
        byte [] texto_plano_Bytes= texto_plano.getBytes();
        byte [] texto_plano_64Bytes = new byte[8]; // Inicializa o array com 8 bytes (64 bits)
        
        // Copia até 8 bytes ou até o final da mensagem
        int bytesACopiar = Math.min(8, texto_plano_Bytes.length);
        for (int i = 0; i < bytesACopiar; i++) {
            texto_plano_64Bytes[i] = texto_plano_Bytes[i];
        }
        
        // Preenche o restante com zeros se necessário (padding)
        for (int i = bytesACopiar; i < 8; i++) {
            texto_plano_64Bytes[i] = 0;
        }
        
        // Divide a mensagem em duas partes de 32 bits

        int parte_esquerda = ((texto_plano_64Bytes[0] & 0xFF) << 24) | ((texto_plano_64Bytes[1] & 0xFF) << 16) | ((texto_plano_64Bytes[2] & 0xFF) << 8) | (texto_plano_64Bytes[3] & 0xFF);
        int parte_direita = ((texto_plano_64Bytes[4] & 0xFF) << 24) | ((texto_plano_64Bytes[5] & 0xFF) << 16) | ((texto_plano_64Bytes[6] & 0xFF) << 8) | (texto_plano_64Bytes[7] & 0xFF);
        
        // System.out.printf("Texto plano 64 bits: %08x%08x%n", esquerda, direita);

        for (int i = 0; i < Rodadas; i++){
            int valorP = Integer.parseUnsignedInt(Array_P[i], 16);
            parte_esquerda = operacaoXORComInteiros(parte_esquerda, valorP);

            parte_direita = operacaoXORComInteiros(funcaoF(parte_esquerda), parte_direita);

            // Troca as partes esquerda e direita (SWAP)
            int aux = parte_esquerda;
            parte_esquerda = parte_direita;
            parte_direita = aux;            
        }
        
        // Troca final (SWAP)
        // Desfaz o último swap do loop
        int aux = parte_esquerda;
        parte_esquerda = parte_direita;
        parte_direita = aux;

        // Agora aplica os XORs finais
        parte_direita = operacaoXORComInteiros(parte_direita, Integer.parseUnsignedInt(Array_P[Rodadas], 16));   // P[16]
        parte_esquerda = operacaoXORComInteiros(parte_esquerda, Integer.parseUnsignedInt(Array_P[Rodadas + 1], 16)); // P[17]
        

        System.out.printf("Texto cifrado 64 bits: %08x%08x%n", parte_esquerda, parte_direita);
        return String.format("%08x%08x", parte_esquerda, parte_direita);

    }
    public static void main(String[] args) {
        String chave = "aabb09182736ccdd";
        Encriptar("123456abcd132536", chave);        
    }
}
