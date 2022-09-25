import java.text.ParseException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Queue;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;

public class ObsResource implements CoapHandler {

    Deque<SensorInfo> sensor_data = new LinkedList<SensorInfo>();
    private String name;
    private String nameActuator;
    private int queue_size = 20;

    public ObsResource (String name){
        super();
        this.name = name;
    }

    @Override
    public void onLoad(CoapResponse response){
        try{

            JSONObject msg = (JSONObject)JSONValue.parseWithException(response.getResponseText().toString());
            
            //System.out.println("Nome: "+ name + " " +response.toString());
            SensorInfo new_data = new SensorInfo(msg);

            insert(new_data);
            
                        
        } catch(org.json.simple.parser.ParseException e){
            e.printStackTrace();
        }
    }

    @Override
	public String toString() {
		String values = "";
		for (SensorInfo s : sensor_data) {
			values += s + "\n";
		}
		return values;
	}
    public String getLastValue(){
        /**/

        //showSensorData();

        if (sensor_data.size()==0)
            return "OFF";
        SensorInfo tmp = sensor_data.getFirst();
        if (tmp.toString().contains("true"))
            return "ON";
        else
            return "OFF";

        
    }

    public void onError() {
        // TODO Auto-generated method stub
        System.out.println("Error in observing mode");
        
    }

    public void insert(SensorInfo data){
        if (sensor_data.size()<queue_size){
            sensor_data.addFirst(data);   
        } else{
            sensor_data.removeLast();
            sensor_data.addFirst(data);
        }
    }

    public void showSensorData(){
        for(SensorInfo info : sensor_data){
            System.out.println(info);
        }
    }


    
}
