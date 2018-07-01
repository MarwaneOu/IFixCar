package ifixcar.crismon.com.ifixcar.Utils;

import android.view.View;
import android.widget.TextView;

import ifixcar.crismon.com.ifixcar.Model.HistoryInfo;
import ifixcar.crismon.com.ifixcar.R;

/**
 * Created by ouardi15 on 27/03/2018.
 */
public class HistoryDeclinedHolder extends HistoryViewHolder {

    public TextView addressView;
    public TextView timeView;
    public TextView distanceView;
    public TextView statusView;

    public HistoryDeclinedHolder(View itemView) {
        super(itemView);
        addressView = (TextView) itemView.findViewById(R.id.adress_view);
        timeView = (TextView) itemView.findViewById(R.id.time_view);
        distanceView = (TextView) itemView.findViewById(R.id.dis_view);
        statusView = (TextView) itemView.findViewById(R.id.acc_view);

    }


    public void bindData(HistoryInfo item)
    {
        statusView.setText(item.getIsAccepted());
        distanceView.setText(item.getDistance());
        timeView.setText(item.getDuration());
        addressView.setText(item.getAddress());
    }
}
