package Criptografia;

public class Blowfish {
    
    private static final int numero_de_subchaves = 18;
    private static final int Rodadas = 16;

    
    public static String operacaoXOR(String hex1, int int2){
        // Converte a string hexadecimal em um valor inteiro
        int int1 = Integer.parseUnsignedInt(hex1, 16);

        // Realiza a operação XOR
        int resultado = int1 ^ int2;

        String hexResultado = String.format("%08x", resultado);

        //System.out.printf("XOR: (%s ^ %08x) = %s%n", hex1, int2, hexResultado);

        return hexResultado;
    }
    
    public static String Encriptar(String texto_plano, String chave){

        //==============::Passo 1: Gerar subchaves::==============//
        // Inicializacao do array P com valores hexadecimais oriundos do valor de pi
        String[] Array_P = Util.Array_P;

        // Inicializacao dos S-Boxes
        String[] SBox0 = Util.sBox0;
        String[] SBox1 = Util.sBox1;
        String[] SBox2 = Util.sBox2;
        String[] SBox3 = Util.sBox3;

        //==============::Passo 2: Mudar as subchaves com base na chave de entrada (P[0] = P[0] XOR Chave[0]) ::==============//

        byte[] chaveBytes = chave.getBytes();
        int chave32Bytes = 0;

        for(int i = 0; i < numero_de_subchaves; i++){
            chave32Bytes = 0;

            for (int j = 0; j < 4; j++){
                // Realiza a operacao XOR entre a subchave e o byte da chave
                // A chave e repetida caso seja menor que o numero de subchaves
                chave32Bytes = chave32Bytes << 8 | chaveBytes[(i * 4 + j) % chaveBytes.length];
            }

            //System.out.printf("Subchave P[%d] antes: %s, chave32Bytes: %08x%n", i, Array_P[i], chave32Bytes);

            Array_P[i] = operacaoXOR(Array_P[i], chave32Bytes);

            //System.out.printf("Subchave P[%d] depois: %s%n", i, Array_P[i]);
            //System.out.println("--------------------------------------------------");
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

        int esquerda = ((texto_plano_64Bytes[0] & 0xFF) << 24) | ((texto_plano_64Bytes[1] & 0xFF) << 16) | ((texto_plano_64Bytes[2] & 0xFF) << 8) | (texto_plano_64Bytes[3] & 0xFF);
        int direita = ((texto_plano_64Bytes[4] & 0xFF) << 24) | ((texto_plano_64Bytes[5] & 0xFF) << 16) | ((texto_plano_64Bytes[6] & 0xFF) << 8) | (texto_plano_64Bytes[7] & 0xFF);
        
        return null;
    }
    public static void main(String[] args) {
        String chave = "ABCD";
        Encriptar("textoTeste", chave);        
    }
}
