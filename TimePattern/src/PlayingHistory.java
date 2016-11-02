import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Date;

/**
 * Created by vagisha on 2/11/16.
 */
public class PlayingHistory {

    public String name ;
    public LocalTime timePlayed;


    public static double getTimeDifference(Date date, Date currentDate){
        return currentDate.getTime()-date.getTime();
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

}
