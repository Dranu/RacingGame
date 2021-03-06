package racinggameclient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Prediction {
    
    LinkedHashMap inputs;
    ArrayList<Long> times;
    
    public Prediction(){
        this.inputs = new LinkedHashMap();
        this.times = new ArrayList<Long>();
    }
    
    //Method to add a prediction input: timestamp + angle,x,y,steer,throttle
    public void addHashInput(long time, String data){
        inputs.put(time,data);
    }    
    
    //Iterate through the hashmap to find if the timestamp exists (it should but to be sure)
    public boolean iterate(long time){
        if(inputs.containsKey(time))
            return true;
        else 
            return false;  
    }
    
    //Get the specific input data with the specific timestamp
    public String getInput(long time){  
        return (String)inputs.get(time);
    }
    
    //Get the next timestamp from the hashmap to find the next data
    public long getNextTime(long time){
        Set set = inputs.entrySet(); //make a set from the hashmap
        Iterator i = set.iterator(); //make an iterator for the set
        while (i.hasNext()){ //while there is a next entry
            Map.Entry find = (Map.Entry)i.next(); //get the next entry
            if(find.getKey().equals(time)){ //if timestamps are equal
                if(i.hasNext() == true){
                    find = (Map.Entry)i.next();
                    return (long)find.getKey(); //return the next timestamp
                }
                else
                    return time;
            } 
        }
        return time;
    }
 
    //Remove data from the hashmap up to the given timestamp
    public void removeTime(long time){
        Set set = inputs.entrySet(); //make a set from the hashmap
        Iterator i = set.iterator(); //make an iterator for the set
        Map.Entry find = (Map.Entry)i.next(); //find the entry from the set
        long lowtime = (long)find.getKey();
        int j = 0;
        while(lowtime <time){ //loop while the given timestamp is larger than found timestamp
            times.add(lowtime);
            find = (Map.Entry)i.next(); //get the next from the set
            lowtime = (long)find.getKey();
        }
        
        for(j = 0; j<times.size();j++){
            inputs.remove(times.get(j));

        }
        times.clear();
        
    } 
}