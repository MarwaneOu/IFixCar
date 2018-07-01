package ifixcar.crismon.com.ifixcar.Services;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import ifixcar.crismon.com.ifixcar.Model.Token;
import ifixcar.crismon.com.ifixcar.Utils.Utils;

public class AppInstanceId extends FirebaseInstanceIdService {



    @Override public void onTokenRefresh() {
        String _token= FirebaseInstanceId.getInstance().getToken();
        updateToken(_token);
    }

    private void updateToken(String token) {

        DatabaseReference tokens= FirebaseDatabase.getInstance().getReference(Utils.TOKENS);
        Token stringToken= new Token(token);
        if(FirebaseAuth.getInstance().getCurrentUser()!=null)
            tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(stringToken);
    }
}
