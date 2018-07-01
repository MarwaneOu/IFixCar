package ifixcar.crismon.com.ifixcar.Utils;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import ifixcar.crismon.com.ifixcar.Model.HistoryInfo;
import ifixcar.crismon.com.ifixcar.R;

/**
 * Created by ouardi15 on 05/03/2018.
 */
public class HistoryAddapter  extends RecyclerView.Adapter<HistoryViewHolder>
{

    private List<HistoryInfo> historyInfos= new ArrayList<>();
    private Context context;
    private HistoryInfo item;

    public HistoryAddapter(List<HistoryInfo> historyInfos, Context context) {
        this.historyInfos = historyInfos;
        this.context = context;
    }

    @Override
    public HistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        switch (viewType) {
            case 0:
                ViewGroup view = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.history_accepted_layout, parent, false);
                return new HistoryAcceptedHolder(view);

            case 1:
                ViewGroup decView = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.history_declined_layout, parent, false);
                return new HistoryDeclinedHolder(decView);

            default:
                ViewGroup defaultView = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.history_accepted_layout, parent, false);
                return new HistoryAcceptedHolder(defaultView);
        }
    }

    @Override
    public void onBindViewHolder(HistoryViewHolder holder, int position)
    {
        item= historyInfos.get(position);
        switch (getItemViewType(position))
        {
            case 0:
                HistoryAcceptedHolder acceptedHolder=(HistoryAcceptedHolder)holder;
                acceptedHolder.bindData(item);
                break;

            case 1:
                HistoryDeclinedHolder declinedHolder=(HistoryDeclinedHolder)holder;
                declinedHolder.bindData(item);
                break;


        }



    }

    @Override
    public int getItemCount() {
        return historyInfos.size();
    }

    @Override
    public int getItemViewType(int position)
    {
        return (historyInfos.get(position).getIsAccepted().equals("Declined")) ? 1 : 0;
    }
}
