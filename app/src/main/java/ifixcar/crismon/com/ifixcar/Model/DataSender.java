package ifixcar.crismon.com.ifixcar.Model;

import java.util.Map;

/**
 * Created by ouardi15 on 12/03/2018.
 */
public class DataSender {
    public String to;
    public Map<String, String> data;

    public DataSender() {
    }

    public DataSender(String to, Map<String, String> data) {
        this.to = to;
        this.data = data;
    }
} 
