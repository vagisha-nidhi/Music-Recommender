package com.example.vagisha.musicrecommender;

import android.content.Context;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.ActiveAndroid;
import com.facebook.stetho.Stetho;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ListView musiclist;
    MediaPlayer mMediaPlayer;
    boolean isPlaying = false;
    String DB_NAME = "SongInfo.db";
    String DB_PATH = "";
    String currentlyPlayingSong = "";
    FactorCalculator calculator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Stetho.newInitializerBuilder(this)
                .enableDumpapp(
                        Stetho.defaultDumperPluginsProvider(this))
                .enableWebKitInspector(
                        Stetho.defaultInspectorModulesProvider(this))
                .build();
        setContentView(R.layout.activity_main);
        ActiveAndroid.initialize(this);
        init_phone_music_grid();

        calculator = new FactorCalculator();
        calculator.initializeWeights();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.fresh:
                FactorCalculator.freshnessProb();
                return true;
            case R.id.favor:
               // Log.i("Calc", String.valueOf((Double.MAX_VALUE)));
                //Log.i("Calc", String.valueOf((1.0/Math.exp(-(22*60*60*100)/12169.0))/Double.MAX_VALUE));
                getTopSongs();
            case R.id.recent :
                RecentlyPlayedModel.LogAllRecentlyPlayed();
            case R.id.adjust_weights :
                calculator.adjustWeightsByRecentPlayings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void getTopSongs(){
        List<SongModel> allSongs = SongModel.getAll();
        double weightFavor = calculator.factorWeights[0];
        double weightFreshness = calculator.factorWeights[1];
        ArrayList<SongProbabilities> songProbabilities = new ArrayList<>();

        for(int i=0; i<allSongs.size() ; i++){
            double favorProb = FactorCalculator.getFavorProb(SongModel.returnTimesPlayed(allSongs.get(i).songName),SongModel.returnMeanPercentagePlayed(allSongs.get(i).songName));
            double freshnessProb = FactorCalculator.getFreshnessProb(SongModel.returnTimesPlayed(allSongs.get(i).songName),SongModel.returnLastPlayed(allSongs.get(i).songName));
            double finalProb = weightFavor*favorProb + weightFreshness*freshnessProb;
            SongProbabilities songProbabilities1 = new SongProbabilities();
            songProbabilities1.name = allSongs.get(i).songName;
            songProbabilities1.prob = finalProb;
            songProbabilities.add(songProbabilities1);

            Log.i("Prob", songProbabilities1.name + ": "+ String.valueOf(freshnessProb)+ ":"+String.valueOf(favorProb)+":"+String.valueOf(songProbabilities1.prob));

        }

        Collections.sort(songProbabilities,new MySongComp());
        for (int i=0; i<10 ; i++){
            Log.i("Top 10 : ",songProbabilities.get(i).name);
        }


    }


    private void init_phone_music_grid() {

        musiclist = (ListView) findViewById(R.id.PhoneMusicList);

        ArrayList<Song> songArrayList = new ArrayList<>();
        songArrayList = ListAllSongs();

        musiclist.setAdapter(new MusicAdapter(getApplicationContext(),songArrayList));
        mMediaPlayer = new MediaPlayer();

        init_database(songArrayList);

        List<SongModel> allSongs = SongModel.getAll();

        for (int i=0 ; i<allSongs.size(); i++){
            Log.i("Song - db",allSongs.get(i).songName);
        }

    }

    private void init_database(ArrayList<Song> songArrayList){

        String initialDateTime = "2015-01-15 00:00:00";
       for(int i=0 ; i<songArrayList.size() ; i++){
           String name = songArrayList.get(i).songname;
           SongModel songModel = new SongModel(i+1,name,0,initialDateTime);
           songModel.save();
       }

    }



    private String[] STAR = { "*" };
    public ArrayList<Song> ListAllSongs()
    {
        Cursor cursor;
        Uri allsongsuri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        ArrayList<Song> songArrayList = new ArrayList<>();

        if (isSdPresent()) {
            cursor = getContentResolver().query(allsongsuri, STAR, selection, null, null);

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        String songname = cursor
                                .getString(cursor
                                        .getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                        int song_id = cursor.getInt(cursor
                                .getColumnIndex(MediaStore.Audio.Media._ID));

                        String fullpath = cursor.getString(cursor
                                .getColumnIndex(MediaStore.Audio.Media.DATA));

                        String albumname = cursor.getString(cursor
                                .getColumnIndex(MediaStore.Audio.Media.ALBUM));

                        //Log.i("Song",fullpath);
                        Song song = new Song();
                        song.albumname = albumname;
                        song.fullpath = fullpath;
                        song.song_id = song_id;
                        song.songname = songname;

                        songArrayList.add(song);

                    } while (cursor.moveToNext());
                }
                cursor.close();
            }
        }
        return songArrayList;
    }


    public static boolean isSdPresent()
    {
        return android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
    }



    public class MusicAdapter extends ArrayAdapter<Song> {
        Context context;
        ArrayList<Song> songList;


        public MusicAdapter(Context context, ArrayList<Song> objects){
            super(context, 0, objects);
            this.context = context;
            this.songList = objects;
        }
        class ViewHolder {
            TextView aName;
            Button playButton;
            Button pauseButton;

        }

        private void stopPlaying() {
            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
                isPlaying = false;
            }
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = View.inflate(context, R.layout.list_item, null);
                ViewHolder vh = new ViewHolder();
                vh.aName = (TextView) convertView.findViewById(R.id.songName);
                vh.playButton = (Button) convertView.findViewById((R.id.play_button)) ;
                vh.pauseButton = (Button) convertView.findViewById(R.id.pause_button);

                convertView.setTag(vh);


            }

            ViewHolder vh = (ViewHolder) convertView.getTag();
            vh.aName.setText(songList.get(position).songname);
            vh.playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                        try {
                            try {
                                if(isPlaying) {
                                    Integer durationPlayed = mMediaPlayer.getCurrentPosition();
                                    //Toast.makeText(MainActivity.this,String.valueOf(durationPlayed),Toast.LENGTH_SHORT).show();
                                    Integer totalDuration = mMediaPlayer.getDuration();
                                    double percentagePlayed = durationPlayed.doubleValue() / totalDuration.doubleValue();
                                    SongModel.updateFavorScores(currentlyPlayingSong,percentagePlayed);
                                    SongModel.incrementTimesPlayed(currentlyPlayingSong);




                                    Toast.makeText(MainActivity.this, "Percentage Played : " + String.valueOf(percentagePlayed), Toast.LENGTH_SHORT).show();
                                }

                            }catch (Exception e){
                                e.printStackTrace();
                            }

                            stopPlaying();
                            currentlyPlayingSong = songList.get(position).songname;
                            String path = songList.get(position).fullpath;
                            mMediaPlayer = new MediaPlayer();
                            mMediaPlayer.setDataSource(path);
                            mMediaPlayer.prepare();
                            mMediaPlayer.start();

                            double favorProb = FactorCalculator.getFavorProb(SongModel.returnTimesPlayed(currentlyPlayingSong),SongModel.returnMeanPercentagePlayed(currentlyPlayingSong));
                            double freshnessProb = FactorCalculator.getFreshnessProb(SongModel.returnTimesPlayed(currentlyPlayingSong),SongModel.returnLastPlayed(currentlyPlayingSong));
                            Log.i("Consi",currentlyPlayingSong +" : "+String.valueOf(SongModel.returnTimesPlayed(currentlyPlayingSong))+ ": "+ SongModel.getStringFromDate(SongModel.returnLastPlayed(currentlyPlayingSong))+" ; "+String.valueOf(freshnessProb));
                            String currentDateandTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                            SongModel.updateTimeOfLastPlayed(songList.get(position).songname,currentDateandTime);


                            RecentlyPlayedModel recentlyPlayedModel = new RecentlyPlayedModel(songList.get(position).songname,new Date(),favorProb,freshnessProb);
                            recentlyPlayedModel.save();

                            Toast.makeText(MainActivity.this,"\n"+currentDateandTime,Toast.LENGTH_SHORT).show();
                            Log.i("Song","Playing :"+songList.get(position).songname);

                            Log.i("Song", "Playing - Date and time Saved "+ SongModel.getDate(songList.get(position).songname));


                            copyAppDbToDownloadFolder();

                            isPlaying = true;



                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                }
            });

            vh.pauseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    try {
                        if(isPlaying) {
                            Integer durationPlayed = mMediaPlayer.getCurrentPosition();
                            //Toast.makeText(MainActivity.this, String.valueOf(durationPlayed), Toast.LENGTH_SHORT).show();
                            Integer totalDuration = mMediaPlayer.getDuration();
                            double percentagePlayed = durationPlayed.doubleValue() / totalDuration.doubleValue();
                            Toast.makeText(MainActivity.this, "Percentage Played : " + String.valueOf(percentagePlayed), Toast.LENGTH_SHORT).show();
                            SongModel.updateFavorScores(currentlyPlayingSong,percentagePlayed);
                            SongModel.incrementTimesPlayed(currentlyPlayingSong);
                        }


                        Log.i("Song","Pausing");
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                        stopPlaying();
                        isPlaying = false;
                        currentlyPlayingSong = "";

                }
            });
            return convertView;

        }


        public void copyAppDbToDownloadFolder() throws IOException {
            File backupDB = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "backupSongInfo"); // for example "my_data_backup.db"
            File currentDB = getApplicationContext().getDatabasePath(DB_NAME); //databaseName=your current application database name, for example "my_data.db"
            if (currentDB.exists()) {
                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
            }
        }


    }

    class MySongComp implements Comparator<SongProbabilities> {

        @Override
        public int compare(SongProbabilities s1, SongProbabilities s2) {
            if(s1.prob < s2.prob){
                return 1;
            } else {
                return -1;
            }
        }
    }
}
