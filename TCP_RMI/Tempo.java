package TCP_RMI;
import java.util.List;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Tempo extends Remote{

    String obterTempoAtual() throws RemoteException;
    List<String> obterLog() throws RemoteException;
    //void registrarAcao(String acao) throws RemoteException;
    //String obterTempoAutomaticamente(int intervalo) throws RemoteException;
}