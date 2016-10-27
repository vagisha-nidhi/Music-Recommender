package com.example.vagisha.musicrecommender;

import android.database.Cursor;
import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by vagisha on 24/10/16.
 */
@Table(name = "SongInfoTable")
public class SongModel extends Model {

    static String avgFavorScoreColumnName = "avgFavorScore";

    @Column(name = "_id",unique = true)
    public int id;

    @Column(name = "songName")
    public String songName;

    @Column(name = "timesPlayed")
    public long timesPlayed;

    @Column(name = "lastPlayed")
    public Date timestamp;

    @Column(name = "avgFavorScore")
    public double avgFavorScore;

    @Column(name = "sumFavorScore")
    public double sumFavorScore;

    public void setDateFromString(String date) {
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sf.setLenient(true);
        try {
            this.timestamp = sf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static Date getDateFromString(String date){
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sf.setLenient(true);

        Date date1 = null;
        try {
            date1 = sf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date1;
    }

    public static String getStringFromDate(Date date){
        String DateandTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
        return DateandTime;
    }

    public SongModel(){
        super();
    }

    public SongModel(int id, String songName, long timesPlayed, String lastPlayed){
        this.id = id;
        this.songName = songName;
        this.timesPlayed = timesPlayed ;
        this.avgFavorScore = 0.0;
        this.sumFavorScore = 0.0;
        setDateFromString(lastPlayed);

    }
    public static List<SongModel> getAll() {
        return new Select()
                .from(SongModel.class)
                .execute();
    }

    public static void incrementTimesPlayed(String songName){
        SongModel model = selectSong("songName", songName);
        model.timesPlayed = model.timesPlayed + 1;
        model.save();
    }

    public static SongModel selectSong(String fieldName, String fieldValue) {
        return new Select().from(SongModel.class)
                .where(fieldName + " = ?", fieldValue).executeSingle();
    }

    public static void updateTimeOfLastPlayed(String songName, String date){
        SongModel model = selectSong("songName",songName);
        model.timestamp = getDateFromString(date);
        model.save();

    }

    public static String getDate(String songName){
        List<SongModel> rows = new Select(new String[]{"Id,lastPlayed"}).from(SongModel.class).where("songName = ?", songName)
                .execute();
        Date date  = rows.get(0).timestamp;
        return getStringFromDate(date);
    }

    public static void updateFavorScores(String songName, double percentagePlayed){
        SongModel model = selectSong("songName",songName);
        double avgFavScore = (model.avgFavorScore*model.timesPlayed+percentagePlayed)/(model.timesPlayed+1);
        double sumFavScore = model.sumFavorScore + percentagePlayed ;
        model.avgFavorScore = (model.avgFavorScore*model.timesPlayed+percentagePlayed)/(model.timesPlayed+1);
        model.sumFavorScore = model.sumFavorScore + percentagePlayed ;
        model.save();

        Log.i("Favor Scores :", songName + ": " + String.valueOf(avgFavScore) + ": " + String.valueOf(sumFavScore));
    }

    public static double getSumAvgFavorScore(){

            Cursor c = ActiveAndroid.getDatabase().rawQuery("SELECT AVG("+avgFavorScoreColumnName+") as total FROM SongInfoTable " , null);
            c.moveToFirst();
            double total = c.getDouble(c.getColumnIndex("total"));
            c.close();
            return total;

    }

    public static long returnTimesPlayed(String songName){
        SongModel model = selectSong("songName", songName);
        return model.timesPlayed;
    }
    public static double returnMeanPercentagePlayed(String songName){
        SongModel model = selectSong("songName", songName);
        return model.avgFavorScore;
    }
    public static Date returnLastPlayed(String songName){
        SongModel model = selectSong("songName", songName);
        return model.timestamp;
    }



}
