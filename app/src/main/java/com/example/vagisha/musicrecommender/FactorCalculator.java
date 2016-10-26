package com.example.vagisha.musicrecommender;

import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by vagisha on 25/10/16.
 */

public class FactorCalculator {



    public static void freshnessProb(){
        List<SongModel> allSongs = SongModel.getAll();

        ArrayList<Double> retentionFactorArrayList = new ArrayList<>();

        for (int i=0 ; i<allSongs.size() ; i++){

            long timesPlayed = allSongs.get(i).timesPlayed;
            long periodFromLast = getDifferenceBetweenDates(new Date(),allSongs.get(i).timestamp);
            long factor;
            if(timesPlayed != 0) {
                factor = -(periodFromLast / timesPlayed)/100000;
                retentionFactorArrayList.add(Math.exp(factor));
            }
            else {
                factor = Long.MIN_VALUE;
                retentionFactorArrayList.add(Math.exp(factor));
            }

            Log.i("Retention Factors : ", allSongs.get(i).songName + " - " + String.valueOf(Math.exp(factor)));

        }

    }

    public static void favourProb(){
        List<SongModel> allSongs = SongModel.getAll();
        ArrayList<Double> favorFactorArrayList = new ArrayList<>();
        double sumAvgScore = SongModel.getSumAvgFavorScore();
        double meanOfAllSongs = sumAvgScore/allSongs.size();
        //m's value to be experimented and checked
        int m = 5;
        Log.i("Fav - mean",String.valueOf(sumAvgScore));

        for(int i=0 ; i<allSongs.size() ; i++){
            Long timesPlayed = allSongs.get(i).timesPlayed;
            double meanPercentagePlayed = allSongs.get(i).avgFavorScore;


            double prob = (timesPlayed.doubleValue()/(timesPlayed.doubleValue()+m))*meanPercentagePlayed + (m/(m+timesPlayed.doubleValue()))*meanOfAllSongs;
            favorFactorArrayList.add(prob);
            Log.i("Favor Factors : ", allSongs.get(i).songName + " - " + String.valueOf(prob));
        }

    }

    public static void adjustWeights(){

    }

    public static Long getDifferenceBetweenDates(Date currentDate, Date timeOfLastPlayed){
        return currentDate.getTime()-timeOfLastPlayed.getTime();
    }
}
