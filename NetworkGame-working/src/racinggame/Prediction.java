/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package racinggame;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Jiri
 */
public class Prediction {
    LinkedHashMap inputs;
    
    
    public Prediction(){
        this.inputs = new LinkedHashMap();
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
        while ((long)find.getKey()< time){ //loop while the given timestamp is larger than found timestamp
            i.remove(); //remove the entry from the set
            find = (Map.Entry)i.next(); //get the next from the set
        }
    } 
}
