package ifixcar.crismon.com.ifixcar.Model;

/**
 * Created by ouardi15 on 05/03/2018.
 */
public class HistoryInfo
{
    private String address;
    private String duration;
    private String isAccepted;
    private String distance;


    public HistoryInfo(String address, String time, String isAccepted, String distance) {
        this.address = address;
        this.duration = time;
        this.isAccepted = isAccepted;
        this.distance = distance;
    }

    public  HistoryInfo(){}

    public String getAddress() {
        return address;
    }


    public void setAddress(String address) {
        this.address = address;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getIsAccepted() {
        return isAccepted;
    }

    public void setIsAccepted(String isAccepted) {
        this.isAccepted = isAccepted;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }
}
