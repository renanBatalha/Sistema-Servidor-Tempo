package TCP_RMI;
import java.util.List;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Tempo extends Remote{

    // Retorna tempo atual do servidor
    String obterTempoAtual() throws RemoteException;

    // Retorna log de acessos
    List<String> obterLog() throws RemoteException;

    // Retorna mensagem criptografada:hashMD5
    String obterTempoAtualComHash() throws RemoteException;
    
}