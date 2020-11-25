import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class CustomDateFormatter {

    private static final String datePattern = "yyyy-MM-dd HH:mm:ss.SSS";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat(datePattern);
    private static final TimeZone timeZone = TimeZone.getTimeZone("Europe/Budapest");

    public static final long ONE_DAY = 60 * 60 * 24; //One day in seconds
    public static final long ONE_YEAR = ONE_DAY * 365; //One year in seconds

    public static long getTimeStampFromDateInSeconds(String dateString) {
        dateFormat.setTimeZone(timeZone);
        Date parsedDate;
        try {
            parsedDate = dateFormat.parse(dateString);
            return parsedDate.getTime() / 1000; //To get seconds from milliseconds
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
