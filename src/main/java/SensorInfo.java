import java.sql.Time;
import java.sql.Timestamp;

import org.json.simple.JSONObject;

public class SensorInfo {
    private Timestamp timestamp;
    private boolean lightValue;
    private boolean mode;

    @Override
    public String toString(){
        String value="false";
        String modeT="M";
        if (lightValue)
            value="true";
        if (mode)
            modeT = "A";
        return ("\t- Timpestamp = " + timestamp + "\n\t- Light value = " + value + "\n\t- Mode = " + modeT);
    }

    public SensorInfo(JSONObject msg) {
        //System.out.println("Print json msg:\n" + msg);
		this.timestamp = new Timestamp(((Long)msg.get("dt")) * 1000); 
		int lightValue = Integer.valueOf(msg.get("p").toString());
        int modeValue = Integer.valueOf(msg.get("m").toString());
        if (lightValue==1)
            this.lightValue = true;
        else 
            this.lightValue = false;
        if (modeValue==1)
            this.mode = true;
        else 
            this.mode = false;
    }

    public SensorInfo(boolean value, Timestamp time, boolean mode){
        this.timestamp = time;
        this.lightValue=value;
        this.mode = mode;

    }

    public Timestamp getTimeStamp(){
        return timestamp;
    }

    public String getLightValue(){
        if(lightValue)
            return "ON";
        else
            return "OFF";
    }

    public String getModeValue(){
        if(mode)
            return "AUTO";
        else
            return "MANUAL";
    }

    public void setTimestamp(Timestamp time){
        this.timestamp=time;

    }

    public void setLightValue(boolean value){
        this.lightValue = value;
    }
}
