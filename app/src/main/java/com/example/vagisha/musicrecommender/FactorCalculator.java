package com.example.vagisha.musicrecommender;

import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by vagisha on 25/10/16.
 */

public class FactorCalculator {


    private static double[] factorWeights = new double[2];

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
    public static double getFreshnessProb(long timesPlayed, Date lastPlayed){
        long periodFromLast = getDifferenceBetweenDates(new Date(),lastPlayed);
        double factor;
        double singleDay = 24*60*60*1000;
        if(timesPlayed != 0) {
            //factor = -(Double.valueOf(periodFromLast) / timesPlayed)/100000;

            Log.i("Consi", ":"+String.valueOf(periodFromLast/singleDay));
           // return (1.0/Math.exp(factor))/Double.MAX_VALUE;
            double prob  = periodFromLast/singleDay;
            if(prob > 1.0)
                return 1.0;
            else
            return prob;

        }
        else {
          //  factor = Long.MIN_VALUE;
            Log.i("Consi - else", String.valueOf(Double.MAX_VALUE));
          //  return (Double.MAX_VALUE)/Double.MAX_VALUE;
            return 1.0;
        }
        //Normalize factor and take reciprocal and then return it;
        //Log.i("Consi", String.valueOf(1.0/Math.exp(factor)));

    }

    public void initializeWeights(){

        factorWeights[0] = 1.0; //Favor
        factorWeights[1] = 1.0; //Freshness
    }

    public static void favourProb(){
        List<SongModel> allSongs = SongModel.getAll();
        ArrayList<Double> favorFactorArrayList = new ArrayList<>();
        double meanOfAllSongs = SongModel.getSumAvgFavorScore();
       // double meanOfAllSongs = sumAvgScore/allSongs.size();
        //m's value to be experimented and checked
        int m = 5;
        Log.i("Fav - mean",String.valueOf(meanOfAllSongs));

        for(int i=0 ; i<allSongs.size() ; i++){
            Long timesPlayed = allSongs.get(i).timesPlayed;
            double meanPercentagePlayed = allSongs.get(i).avgFavorScore;


            double prob = (timesPlayed.doubleValue()/(timesPlayed.doubleValue()+m))*meanPercentagePlayed + (m/(m+timesPlayed.doubleValue()))*meanOfAllSongs;
            favorFactorArrayList.add(prob);
            Log.i("Favor Factors : ", allSongs.get(i).songName + " - " + String.valueOf(prob));
        }

    }

    public static double getFavorProb(Long timesPlayed, double meanPercentagePlayed){
        int m = 5;
        double meanOfAllSongs = SongModel.getSumAvgFavorScore();
        double prob = (timesPlayed.doubleValue()/(timesPlayed.doubleValue()+m))*meanPercentagePlayed + (m/(m+timesPlayed.doubleValue()))*meanOfAllSongs;
        return prob;

    }



    public void adjustWeightsByRecentPlayings(){
        List<RecentlyPlayedModel> allRecentlyPlayedSongs = RecentlyPlayedModel.getAll();
        //Current factor weights is W
        //We change the factor weights by looking at all the recent Playings.
        //For each playing and its next playing, we check what factor has contributed the most and what has contributed least
        //Initializing Contribution of factors
        int Ffreshness = 0;
        int Ffavor = 0;

        for(int i=0 ; i<allRecentlyPlayedSongs.size()-1; i++){
            RecentlyPlayedModel firstSong = allRecentlyPlayedSongs.get(i);
            RecentlyPlayedModel secondSong = allRecentlyPlayedSongs.get(i+1);
            Log.i("Contri",firstSong.songName+":"+secondSong.songName);
            double favorProbFirstSong = firstSong.favorFactor;
            double favorProbSecondSong = secondSong.favorFactor;
            double freshnessProbFirstSong = firstSong.freshnessFactor;
            double freshnessProbSecondSong = secondSong.freshnessFactor;

            double differenceFavor = favorProbSecondSong - favorProbFirstSong;
            double differenceFreshness = freshnessProbSecondSong - freshnessProbFirstSong;
            Log.i("Contri",String.valueOf(differenceFavor)+":"+String.valueOf(differenceFreshness));

            if(differenceFavor>differenceFreshness  && differenceFreshness!=0.0){
                Ffavor = Ffavor+1;
                Ffreshness = Ffreshness-1;
            }
            else{
                if(differenceFreshness == 0.0) {
                    Ffreshness = Ffreshness + 2;
                    Ffavor = Ffavor - 1;
                }
                else if(differenceFreshness>0.0){
                    Ffreshness = Ffreshness + 1;
                    Ffavor = Ffavor - 1;
                }
            }

            Log.i("Contri",String.valueOf(Ffavor)+":"+String.valueOf(Ffreshness));

        }

        Log.i("Contri : ", String.valueOf(Ffavor) + " : " + String.valueOf(Ffreshness));
        double delta = 0.1;
        if(Ffavor>Ffreshness){
            factorWeights[0] = factorWeights[0]+delta;
            factorWeights[1] = factorWeights[1]-delta;
        }
        else{
            factorWeights[1] = factorWeights[1]+delta;
            factorWeights[0] = factorWeights[0]-delta;
        }
        Log.i("Contri : weights ", String.valueOf(factorWeights[0]) + " : " + String.valueOf(factorWeights[1]));

    }

    public static Long getDifferenceBetweenDates(Date currentDate, Date timeOfLastPlayed){
        return currentDate.getTime()-timeOfLastPlayed.getTime();
    }
}
