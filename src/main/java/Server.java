import org.eclipse.californium.core.CaliforniumLogger;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.server.resources.CoapExchange;

import java.net.InetAddress;
import java.util.ArrayList;

public class Server extends CoapServer {

    static {
        CaliforniumLogger.disableLogging();
    }

    static public ArrayList<RegisteredResource> registeredResource = new ArrayList<RegisteredResource>();
    private static Registrant reg;

    public Server(int port) {
        super(port);
        //System.out.println("new server");
        reg = new Registrant("registrant");
        this.add(reg);
    }

    public static RegisteredResource findResource(String name){
        for (RegisteredResource res : registeredResource){
            if (res.getName().trim().equals(name.trim())){
                return res;
            }
        }
        return null;
    }




}
