import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;

import java.net.InetAddress;
import java.util.ArrayList;

public class Registrant extends CoapResource {


    public Registrant(String name) {
        super(name);
    }

    public void handleGET(CoapExchange exchange) {
        exchange.accept();

        InetAddress addr = exchange.getSourceAddress();
        boolean observable;
        String name;
        String path;

        String uri = "coap://[" + addr.getHostAddress() + "]:5683/.well-known/core";
        CoapClient request = new CoapClient(uri);

        String reply = request.get().getResponseText();
        
        //System.out.println("\nREPLY to well-known/core\n" + reply + "\n");

        String[] resources = reply.split(",");
        
       

        for (int i=1; i<resources.length; i++){
            String[] parameters = resources[i].split(";");

            /*
            DEBUG parameters: 
            </.well-known/core>
            ct=40,</actuator/lightBulb>
            title="Light actuator"
            methods="GET/POST/PUT state=on|off"
            obs
            rt="light"

            */
            
                path = parameters[0];
                name =parameters[1];
                String[] name1 = name.split("=");
                String finalName = name1[1].replace("\"", "");


                if (parameters.length==5)
                    observable=true;
                else
                    observable=false;
            
            String newPath = path.replace("<", "").replace(">", "");
            RegisteredResource newDevice = new RegisteredResource(finalName, newPath, addr.toString().replace("/", ""), observable);
            
            Server.registeredResource.add(newDevice);
                

        }

    }

    public boolean alreadyRegistered(RegisteredResource res){
        for(int i = 0; i< Server.registeredResource.size(); i++){
            if (res.getAddr().equals(Server.registeredResource.get(i).getAddr())){
                return true;
            }

        }
        return false;
    }
}
