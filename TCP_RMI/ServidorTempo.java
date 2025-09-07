package TCP_RMI;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ServidorTempo implements Tempo{
    
    @Override
    public String obterTempoAtual(){
        LocalDateTime tempoAtual = LocalDateTime.now();
        return tempoAtual.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }

    @Override
    public List<String> obterLog() throws RemoteException{
        List<String> lista = new ArrayList<>();
        return lista;
    }

    public static void main(String[] args) {
            try {
                ServidorTempo obj = new ServidorTempo();
                Tempo stub = (Tempo) UnicastRemoteObject.exportObject(obj, 0);

                System.setProperty("java.rmi.server.hostname", "localhost");

                // Cria o registry na porta 2000
                Registry registry = LocateRegistry.createRegistry(2000);
                registry.bind("ServidorTempo", stub);

                System.out.println("Server ready on port 2000");
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
}


