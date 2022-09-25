import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.coap.CoAP.Code;

public class Application {

    static Server server = new Server(5683);

    static Utility utility = new Utility();


    public static void main(String[] args){

        new Thread() {
            public void run() {
                server.start();
            }
        }.start();

        System.out.println("SMART LAMP\n");

        System.out.println("I'm waiting devices ... \n");

        while(Server.registeredResource.isEmpty()){ try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }}

        Scanner sc = new Scanner(System.in);
        ArrayList<String> options = new ArrayList();
        options.add("1"); options.add("2"); options.add("3"); options.add("4"); options.add("5"); options.add("6"); options.add("7");

        while (true){
            
            String command = "";
            
            do {
                Utility.menu();
                command=sc.nextLine();
            }
            while(!Utility.checkOptions(options, command));

            int choose; 
            try{
                choose = Integer.valueOf(command);
            } catch(NumberFormatException e) {
                break;
            }
            
            String typeResource;
            

            if (choose < 8){
                switch(choose){
                    case 2: 
                        typeResource = Utility.chooseType();
                        System.out.println("You choose: " + typeResource);
                        getInfo(typeResource);
                        break;
                    case 3:
                        viewHistory();
                        break;
                    case 4:
                        switchLight();
                        break;
                    case 1:
                        System.out.println();
                        showLights();
                        break;
                    case 5:
                        setNickname();
                        break;
                    case 6:       
                        showDetectors();
                        break;
                    case 7:
                        changeMode();
                        break;
                    default: 
                        System.out.println("Choose a valid option!");        
                }

            }          
        }

    }

    public static void setNickname() {
        String type = Utility.chooseType();
        String name;
        if (type.equals("actuator"))
            name = selectLight();
        else
            name = selectPresence();
        
        String newName = changeName(name);

        for(RegisteredResource res : Server.registeredResource){
            if (res.getName().equals(name)){
                res.setNickname(newName);
            }
        }

    }

    public static String changeName(String oldName){
            String newName="";
            Scanner sc = new Scanner(System.in);
            boolean confirm = false;
            try {
                while(!confirm){
                    System.out.println("Insert the new nickname of " + oldName);
                    newName = sc.nextLine();
                    System.out.println("Are you sure of the new name \"" + newName + "\"");
                    if(Utility.check())
                        confirm = true;

                }
            } catch (Exception e) {
                //TODO: handle exception
            }
            System.out.println();
            return newName;
        }

    

    public static void getAll() {
        for (RegisteredResource res : Server.registeredResource){
            res.printResource();
        }
    }

    public static void getInfo(String typeResource){
        for(RegisteredResource res : Server.registeredResource){
            res.getPath();
            if (res.getPath().contains(typeResource.trim())){
                res.printResource();
            }
        }
    }

    public static void viewHistory() {
		System.out.println();
        for(RegisteredResource res  : Server.registeredResource) {
            if (res.getNickname()==null)
                System.out.println(res.getName());
            else
                System.out.println(res.getNickname());
			System.out.println(res.obsRes.toString());		
		}
	}

    public static void switchLight(){
        String lightName = selectLight();

        System.out.println("You choose: "+ lightName);

        Boolean oldLightValue = false;
        String resourceURI = "";
        for(RegisteredResource res  : Server.registeredResource) {
            if (res.getName().equals(lightName)){
                //System.out.println("DBG: " + res.obsRes.sensor_data.size());
                
                resourceURI = "coap://[" + res.getAddr() + "]" + res.getPath();
                
                Timestamp ts = Timestamp.from(Instant.now());

                CoapClient client = new CoapClient(resourceURI);
		
                String newLightValue = "OFF";

                Request request = new Request(Code.GET);
                request.setURI(resourceURI);
                request.send();
                Response response1 = null;
                try {
                    response1 = request.waitForResponse();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
        
                String responseValueText = response1.getPayloadString();
                //System.out.println("DEBUG ResponseValue: " + responseValueText);
                boolean value = false;
                if(responseValueText.replaceAll("[^\\d]", "").trim().equals("0")){
                    newLightValue="ON";
                    value =true;
                } else{
                    newLightValue="OFF";
                    value=false;
                }
                
                res.setLightValue(value);

                RegisteredResource resCorresponding = findCorrespondingRes(res.getName());

                SensorInfo data = new SensorInfo(value, ts, resCorresponding.getMode());

                res.obsRes.insert(data);
        
                CoapResponse response = client.post("state=" + newLightValue, MediaTypeRegistry.TEXT_PLAIN);
                
                String code = response.getCode().toString();
                
                if (!code.startsWith("2")) {
                    System.out.println("Error: " + code);
                    return;
                }

               
            }
            
        }
    }


    public static String selectLight() {
        System.out.println("Select light:");
        ArrayList<String> lights = printLights();
        Scanner sc = new Scanner(System.in);
        int choose=0;
        try {
            choose = sc.nextInt();
        } catch (Exception e) {
            System.out.println("Out of range !");
        }
        return lights.get(choose-1);
    }

    public static String selectPresence(){
        System.out.println("Select presence detector:");
        ArrayList<String> presence = printPresence();
        Scanner sc = new Scanner(System.in);
        int choose=0;
        try {
            choose = sc.nextInt();
        } catch (Exception e) {
            System.out.println("Out of range !");
        }
        return presence.get(choose-1);
    }

    public static ArrayList<String> printPresence() {
        ArrayList<String> presence = new ArrayList<String>();
        int i=1;
        for(RegisteredResource res : Server.registeredResource){
            if(res.getType().equals("sensor")){
                System.out.println(i++ + ") " + res.getName());
                presence.add(res.getName());
            }
        }
        return presence;
    }    

    public static ArrayList<String> printLights() {
        ArrayList<String> lights = new ArrayList<String>();
        int i=1;
        for(RegisteredResource res : Server.registeredResource){
            if(res.getType().equals("actuator")){
                System.out.println(i++ + ") " + res.getName());
                lights.add(res.getName());
            }
        }
        return lights;
    }

    public static void showLights(){
        for(RegisteredResource res : Server.registeredResource){
            if(res.getType().equals("actuator")){
                String resourceURI = "coap://[" + res.getAddr() + "]" + res.getPath();
                Request request = new Request(Code.GET);
                request.setURI(resourceURI);
                request.send();
                Response response1 = null;
                try {
                    response1 = request.waitForResponse();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                String responseValueText = response1.getPayloadString();
                String value=null;
                //System.out.println("DEBUG ResponseValue: " + responseValueText);
                if(responseValueText.replaceAll("[^\\d]", "").trim().equals("0")){
                    
                    value="OFF";
                } else{
                    
                    value="ON";
                }
                res.setLightValue(value.equals("ON"));

                String name = res.getNickname();

                if(name ==null)
                    name = res.getName();
                
                System.out.println(name + "\t" + value);
            }

        }
        System.out.println();
    }

    public static void showDetectors(){
        for(RegisteredResource res : Server.registeredResource){
            if(res.getType().equals("sensor")){
                String resourceURI = "coap://[" + res.getAddr() + "]" + res.getPath() +"/?mode";
                Request request = new Request(Code.GET);
                request.setPayload("mode");
                request.setURI(resourceURI);
                request.send();
                Response response1 = null;
                try {
                    response1 = request.waitForResponse();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                String responseValueText = response1.getPayloadString();
                String value=null;

                //System.out.println("DEBUG ResponseValue: " + responseValueText);

                if(responseValueText.contains("\"m\":1")){
                    value="AUTO";
                } else{
                    
                    value="MANUAL";
                }
                
                String name = res.getNickname();

                if(name ==null)
                    name = res.getName();
                
                System.out.println(name + "\t" + value);
            }

        }
        System.out.println();
    }

    
    public static void changeMode(){
        
        String presenceName = selectPresence();

        System.out.println("You choose: "+ presenceName);

        String resourceURI = "";
        RegisteredResource res = Server.findResource(presenceName);

        //System.out.println(resourceURI);
        
        resourceURI = "coap://[" + res.getAddr() + "]" + res.getPath();

        CoapClient client = new CoapClient(resourceURI);
		        
        /*
        String responseValueText = responseValue.getResponseText().toString();
        System.out.println("DEBUG ResponseValue: " + responseValueText);
        */ 

        System.out.println("Which mode? \n 1)AUTO \n 2)MANUAL");
        Scanner sc = new Scanner(System.in);
        String choose = sc.nextLine();
        
        if (choose.equals("1"))
            choose="AUTO";
        else
            choose="MANUAL";

		CoapResponse response = client.put("mode=" + choose, MediaTypeRegistry.TEXT_PLAIN);
        
        if (choose.equals("MANUAL"))
            res.setMode(false);
        else 
            res.setMode(true);
		String code = response.getCode().toString();
		
		if (!code.startsWith("2")) {
			System.out.println("Error: " + code);
			return;
		}

    }
    
    static RegisteredResource findCorrespondingRes(String name){
        String index = name.replaceAll( "[^\\d]", "");
        for(RegisteredResource res :  Server.registeredResource){
            if (res.getName().contains(index) && !name.equals(res.getName())){
                //System.out.println("DEBUG findCorrisponding name of " + name + " is " + res.getName());
                return res;
            }
        }
    return null; 
    }
}
