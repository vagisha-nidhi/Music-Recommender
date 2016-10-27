package com.example.vagisha.musicrecommender;

import android.util.Log;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.Date;
import java.util.List;

/**
 * Created by vagisha on 27/10/16.
 */
@Table(name = "recentlyPlayedTable")
public class RecentlyPlayedModel extends Model {

    @Column(name = "songName")
    public String songName;

    @Column(name = "lastPlayed")
    public Date lastPlayed;

    @Column(name = "liked")
    public boolean liked;

    @Column(name = "favorFactor")
    public double favorFactor;

    @Column(name = "freshnessFactor")
    public double freshnessFactor;


    public RecentlyPlayedModel(){ super();}

    public RecentlyPlayedModel(String songName, Date lastPlayed, double favorFactor, double freshnessFactor){
        this.songName = songName;
        this.lastPlayed = lastPlayed;
        this.liked = true;
        this.favorFactor = favorFactor;
        this.freshnessFactor = freshnessFactor;
    }

    public static void updateFactors(String songName, double favorFactor, double freshnessFactor){
        RecentlyPlayedModel model = selectSong("songName", songName);
        model.favorFactor = favorFactor;
        model.freshnessFactor = freshnessFactor;
        model.save();
    }

    public static RecentlyPlayedModel selectSong(String fieldName, String fieldValue) {
        return new Select().from(RecentlyPlayedModel.class)
                .where(fieldName + " = ?", fieldValue).executeSingle();
    }

    public static void updateLiked(String songName, boolean liked){
        RecentlyPlayedModel model = selectSong("songName", songName);
        model.liked = liked;
        model.save();
    }

    public static List<RecentlyPlayedModel> getAll(){
        return new Select()
                .from(RecentlyPlayedModel.class)
                .execute();
    }

    public static void LogAllRecentlyPlayed(){
        List<RecentlyPlayedModel> recentSongs = getAll();
        for (int i=0 ; i<recentSongs.size() ; i++){
            Log.i("Recent : ", recentSongs.get(i).songName);
        }
    }
}
