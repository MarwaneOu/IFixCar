package ifixcar.crismon.com.ifixcar;

import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ifixcar.crismon.com.ifixcar.Model.HistoryInfo;
import ifixcar.crismon.com.ifixcar.Utils.HistoryAddapter;
import ifixcar.crismon.com.ifixcar.Utils.HistoryViewHolder;
import ifixcar.crismon.com.ifixcar.Utils.Utils;

public class CallsHistoryActivity extends AppCompatActivity {


    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private DatabaseReference history;

    private List<HistoryInfo> historyInfos;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calls_history);

        historyInfos= new ArrayList<>();

        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        history = FirebaseDatabase.getInstance().getReference(Utils.HISTORY);



        if(Utils.isConnected(this)) {
            loadHistoryFromDb();
        }
        else {
            Snackbar.make(recyclerView,"Something went wrong ,try again !",Snackbar.LENGTH_INDEFINITE).setActionTextColor(getResources().getColor(R.color.blueSky))
                    .setAction("Retry",view -> loadHistoryFromDb()).show();
        }






    }

    private void loadHistoryFromDb()
    {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        history.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                for(DataSnapshot data:dataSnapshot.getChildren())
                    historyInfos.add(data.getValue(HistoryInfo.class));
                recyclerView.setAdapter(new HistoryAddapter(historyInfos,CallsHistoryActivity.this));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }
}
