package Criptografia;

public class Blowfish {
    
    private static final int numero_de_subchaves = 18;
    private static final int Rodadas = 16;

    public static String operacaoXOR(String hex1, byte byte2){
        // Converte a string hexadecimal em um valor inteiro
        int int1 = Integer.parseUnsignedInt(hex1, 16);
        int int2 = Byte.toUnsignedInt(byte2);

        // Realiza a operação XOR
        int resultado = int1 ^ int2;

        // Converte o resultado de volta para uma string hexadecimal
        String hexResultado = Integer.toHexString(resultado);

        // Garante que a string hexadecimal tenha 8 caracteres (32 bits)
        while (hexResultado.length() < 8) {
            hexResultado = "0" + hexResultado;
        }

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
            for (int j = 0; j < 4; j++){
                // Realiza a operacao XOR entre a subchave e o byte da chave
                // A chave e repetida caso seja menor que o numero de subchaves
                chave32Bytes = chave32Bytes << 8 | chaveBytes[(i * 4 + j) % chaveBytes.length];
            }
            Array_P[i] = operacaoXOR(Array_P[i], (byte) chave32Bytes);
        }


        return null;
    }
}
