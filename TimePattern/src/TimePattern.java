import com.sun.org.apache.xerces.internal.util.SymbolHash;
import com.sun.org.apache.xerces.internal.util.SynchronizedSymbolTable;
import sun.rmi.runtime.Log;

import javax.swing.text.html.HTMLDocument;
import java.time.LocalTime;
import java.util.*;

/**
 * Created by vagisha on 2/11/16.
 */
public class TimePattern {

    public static void main(String args[]){


        ArrayList<PlayingHistory> playingHistories = new ArrayList<>();

        playingHistories = init_playing_history();
        for (int i=0; i<playingHistories.size(); i++)
        { System.out.println(playingHistories.get(i).name);
          System.out.println(String.valueOf(playingHistories.get(i).timePlayed.toSecondOfDay()));
        }

        Map<String,Double> probMap1 = new HashMap<>();
        probMap1 = generateWeights(playingHistories);

        Map<String,Double> probMap2 = new HashMap<>();
        probMap2 = generateWeights2(playingHistories);

    }


    public static Map<String,Double> generateWeights(ArrayList<PlayingHistory> playingHistories){

        HashMap<String,Double> weightMap = new HashMap<>();
        HashMap<String,Integer> countMap = new HashMap<>();
        LocalTime currentTime = LocalTime.now();
        System.out.println(String.valueOf(currentTime.toSecondOfDay()));

        for(int i=0; i<playingHistories.size() ; i++){

            String songName = playingHistories.get(i).name;
            int timeDifference = getTimeDifference(playingHistories.get(i).timePlayed,currentTime);
            double weight = 1.0/timeDifference ;

            System.out.println(songName);

            if(weightMap.get(songName) == null){
                weightMap.put(songName,weight);
                countMap.put(songName,1);
                System.out.println(String.valueOf(weight));
            }
            else {
                Double value = weightMap.get(songName);
                Integer count = countMap.get(songName);
                weightMap.put(songName,value+weight/Math.pow(2.0,count));
                countMap.put(songName,count+1);
                System.out.println(String.valueOf(value)+":"+String.valueOf(weight/Math.pow(2.0,count))+":" +String.valueOf(value+weight));
            }
        }

        return generateProbFromWeights(weightMap);

    }

    public static Map<String,Double> generateWeights2(ArrayList<PlayingHistory> playingHistories){
        HashMap<String,Double> weightMap = new HashMap<>();
        HashMap<String,Integer> indexMap = new HashMap<>();
        LocalTime currentTime = LocalTime.now();

        ArrayList<SongAndTimeDiff> songAndTimeDiffs = new ArrayList<>();

        for (int i=0; i<playingHistories.size(); i++){
            String songName = playingHistories.get(i).name;
            int timeDifference = getTimeDifference(playingHistories.get(i).timePlayed,currentTime);

            SongAndTimeDiff obj = new SongAndTimeDiff();
            obj.songName = songName;
            obj.timeDiff = timeDifference;

            songAndTimeDiffs.add(obj);
        }

        Collections.sort(songAndTimeDiffs,new MySongComp());

        int index = 0;
        for(int i=0; i<songAndTimeDiffs.size() ; i++){
            String songName = songAndTimeDiffs.get(i).songName;

            if(indexMap.get(songName) == null){
                indexMap.put(songName,index);
                index++;
            }

        }

        for (int i=0; i<songAndTimeDiffs.size() ; i++){
            String songName = songAndTimeDiffs.get(i).songName;
            int divIndex = indexMap.get(songName);
            int timeDiff = songAndTimeDiffs.get(i).timeDiff;
            Double weight = (1.0/timeDiff)/Math.pow(2.0,divIndex);
            System.out.println(songName + ":"+ String.valueOf(divIndex) + ": " + weight);

            if(weightMap.get(songName) == null){
                weightMap.put(songName,weight);
                System.out.println(String.valueOf(weight));
            }
            else {
                Double value = weightMap.get(songName);
                weightMap.put(songName,value+weight);
                System.out.println(String.valueOf(value)+":"+String.valueOf(weight)+":" +String.valueOf(value+weight));
            }
        }


        return generateProbFromWeights(weightMap);
    }

    public static Map<String,Double> generateProbFromWeights(HashMap<String,Double> weightMap){

        Map<String,Double> probMap = new HashMap<>();

        Iterator it = weightMap.entrySet().iterator();
        Map.Entry thisEntry = (Map.Entry) it.next();

        Double min = (Double) thisEntry.getValue(),max = (Double)thisEntry.getValue();
        //System.out.println(String.valueOf(min));
        for (HashMap.Entry<String, Double> entry : weightMap.entrySet()) {
           // System.out.println("Key : " + entry.getKey() + " Value : " + entry.getValue());
            String songName = entry.getKey();
            Double weight = entry.getValue();

            min = Math.min(min,weight);
            max = Math.max(max,weight);

        }

        System.out.println("PRINTING PROBABILITIES : ");
        for (HashMap.Entry<String, Double> entry : weightMap.entrySet()) {
            // System.out.println("Key : " + entry.getKey() + " Value : " + entry.getValue());
            String songName = entry.getKey();
            Double weight = entry.getValue();

            Double prob = (weight - min)/(max - min) ;

            System.out.println(songName + " : " + String.valueOf(prob));
            probMap.put(songName,prob);

        }

        return probMap;
    }

    public static int getTimeDifference(LocalTime time, LocalTime currentTime){
        return Math.abs(time.toSecondOfDay()-currentTime.toSecondOfDay());
    }

    public static ArrayList<PlayingHistory> init_playing_history(){
        ArrayList<PlayingHistory> playingHistories = new ArrayList<>();

        //playingHistories = init_playing_history();
        PlayingHistory history = new PlayingHistory();
        history.name = "A";
        LocalTime time = LocalTime.parse("00:01:45");
        history.timePlayed = time;
        playingHistories.add(history);
        PlayingHistory history1 = new PlayingHistory();
        history1.name = "A";
        time = LocalTime.parse("00:05:23");
        history1.timePlayed = time;
        playingHistories.add(history1);
        PlayingHistory history2 = new PlayingHistory();
        history2.name = "B";
        time = LocalTime.parse("21:40:23");
        history2.timePlayed = time;
        playingHistories.add(history2);
        PlayingHistory history3 = new PlayingHistory();
        history3.name = "C";
        time = LocalTime.parse("20:05:28");
        history3.timePlayed = time;
        playingHistories.add(history3);

        PlayingHistory history4 = new PlayingHistory();
        history4.name = "D";
        time = LocalTime.parse("23:45:28");
        history4.timePlayed = time;
        playingHistories.add(history4);

        return playingHistories;

    }


}

class MySongComp implements Comparator<SongAndTimeDiff> {

    @Override
    public int compare(SongAndTimeDiff s1, SongAndTimeDiff s2) {
        if(s1.timeDiff > s2.timeDiff){
            return 1;
        } else {
            return -1;
        }
    }


}
