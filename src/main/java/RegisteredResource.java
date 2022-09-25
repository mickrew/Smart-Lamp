import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapObserveRelation;

import org.eclipse.californium.core.coap.MediaTypeRegistry;

public class RegisteredResource extends CoapClient {
    private String name;
    private String title;
    private String path;
    private String addr;
    private boolean observable;
    private String uri;
    private String type;

    private String nickname;
    private String lightValue;
    private boolean mode;

    CoapObserveRelation obsRelation;
	ObsResource obsRes;

    public RegisteredResource(String title, String path, String addr, boolean observable){
        super();
        this.title=title;
        this.path=path;
        this.addr=addr;
        this.observable = observable;
        this.nickname = null;
        
        this.lightValue=null;
        this.mode =true;

        
        String[] node =addr.split(":");
        //System.out.println("DEBUG   Costruttore Registered resource: " + node);
        
        this.name = title + " " + node[node.length -1];
        this.uri = "coap://[" + this.addr + "]" + this.path;
        this.setURI(this.uri);
        this.observable = observable;

        if (path.contains("sensor")){
            this.type = "sensor";
        } else{
            this.type="actuator";
        }

        if(observable){
            //System.out.println("IS OBSERVABLE " + addr + "    " + path);
            this.obsRes = new ObsResource(name);
			this.obsRelation = this.observe(obsRes, MediaTypeRegistry.APPLICATION_JSON);
        }

        System.out.println("Node info: \t " + name + "\t" +  path + "\t registered");

    }

    public RegisteredResource(){

    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getAddr() {
        return addr;
    }

    public String getType() {
        return type;
    }

    public String getNickname(){
        return nickname;
    }

    public Boolean getMode(){
        return mode;
    }

    public void setMode(Boolean bool){
        this.mode = bool;
    }

    public void setNickname(String nickname){
        this.nickname = nickname;
    }

    public String getResLightValue(){
        return lightValue; 
    }

    public void setLightValue(Boolean value){
        if (value)
            lightValue="ON";
        else
            lightValue="OFF";
    }

    public void printResource(){
        System.out.println("NODE INFO");
        System.out.println("\tName: " + name);
        if (nickname!=null)
            System.out.println("\tNickname: " + nickname);
        System.out.println("\tTitle: " + title);
        System.out.println("\tAddress: " + addr);
        System.out.println("\tPath: " + path);
        System.out.println("\tType: " + type);
        System.out.println("\tObservable:" + (observable==true?"yes":"no"));
        
        System.out.println();
    }

}
