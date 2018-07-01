package ifixcar.crismon.com.ifixcar;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.regex.Pattern;

import dmax.dialog.SpotsDialog;
import ifixcar.crismon.com.ifixcar.Model.User;
import ifixcar.crismon.com.ifixcar.Utils.Utils;
import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {

    private Button signIn;
    private Button signUp;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference drivers;
    private MaterialEditText emailEdit,nameEdit,phoneEdit,passEdit;
    private RelativeLayout rootLayout;


    @Override protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("fonts/raleWay.ttf")
                        .setFontAttrId(R.attr.fontPath)
                        .build());

        // init local db
        Paper.init(this);

        setContentView(R.layout.activity_main);

        //init Firebase
         firebaseAuth=FirebaseAuth.getInstance();
         drivers= FirebaseDatabase.getInstance().getReference(Utils.DRIVERS);

        rootLayout=(RelativeLayout)findViewById(R.id.root_layout);
        signIn=(Button)findViewById(R.id.sign_in_btn);
        signUp=(Button)findViewById(R.id.sign_up_btn);
        signUp.setOnClickListener(view->signUpAlert());
        signIn.setOnClickListener(view -> signInAlert());

        String email=Paper.book().read(Utils.EMAIL);
        String password=Paper.book().read(Utils.PASSWORD);
        if(email!=null && password!=null)
        {
            if(!email.isEmpty()&&!password.isEmpty())

                autoAuthenticateMe(email,password);
        }
    }

    private void autoAuthenticateMe(String email, String password)
    {
        if(Utils.isConnected(MainActivity.this))
        {

                SpotsDialog spotsDialog = new SpotsDialog(MainActivity.this,R.style.CustomAlert);
                spotsDialog.show();
                firebaseAuth.signInWithEmailAndPassword(email,password)
                        .addOnSuccessListener(authResult ->
                        {
                            spotsDialog.dismiss();
                            drivers.child(firebaseAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Utils.currentUser=dataSnapshot.getValue(User.class);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    databaseError.toException().printStackTrace();
                                }
                            });
                            startActivity(new Intent(MainActivity.this,DashboardActivity.class));
                            finish();
                        })
                        .addOnFailureListener( e ->
                        {
                            spotsDialog.dismiss();
                            Snackbar.make(rootLayout,e.getMessage(),Snackbar.LENGTH_LONG)
                                    .setActionTextColor(getResources().getColor(R.color.blueSky))
                                    .setAction("Ok",null).show();
                            Log.d("Debug", "autoAuthenticateMe: "+e.getMessage());
                        });

        }
        else
        {
            Snackbar.make(rootLayout,"Please check your internet!",Snackbar.LENGTH_LONG)
                    .setActionTextColor(getResources().getColor(R.color.blueSky))
                    .setAction("Ok",null).show();
        }
    }

    private void signInAlert()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("SIGN IN");
        builder.setMessage("Please fill  the fields:");

        View signInLayout=getLayoutInflater().inflate(R.layout.sign_in_layout,null);
        MaterialEditText editEmail=(MaterialEditText)signInLayout.findViewById(R.id.emailedit);
        MaterialEditText editPass=(MaterialEditText)signInLayout.findViewById(R.id.passedit);
        builder.setView(signInLayout);
        builder.setNegativeButton("Cancel", (dialog, i) -> dialog.dismiss());
        builder.setPositiveButton("Sign IN",(dialog,i)->signInProcess(editEmail,editPass));
        builder.show();

    }

    private void signInProcess(MaterialEditText emailEdit,MaterialEditText passEdit)
    {
        if(Utils.isConnected(MainActivity.this))
        {
            if(!isEmailAndPassAreValid(emailEdit,passEdit))
            {
                return;
            }
            else
            {
                SpotsDialog spotsDialog = new SpotsDialog(MainActivity.this,R.style.CustomAlert);
                spotsDialog.show();
                firebaseAuth.signInWithEmailAndPassword(emailEdit.getText().toString(),passEdit.getText().toString())
                        .addOnSuccessListener(authResult ->
                        {
                            Paper.book().write(Utils.EMAIL,emailEdit.getText().toString());
                            Paper.book().write(Utils.PASSWORD,passEdit.getText().toString());
                            spotsDialog.dismiss();
                            drivers.child(firebaseAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Utils.currentUser=dataSnapshot.getValue(User.class);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                            startActivity(new Intent(MainActivity.this,DashboardActivity.class));
                            finish();
                        })
                        .addOnFailureListener( e ->
                        {
                            spotsDialog.dismiss();
                            Snackbar.make(rootLayout,e.getMessage(),Snackbar.LENGTH_LONG)
                                    .setActionTextColor(getResources().getColor(R.color.blueSky))
                                    .setAction("Ok",null).show();
                        });
            }
        }
        else
        {
            Snackbar.make(rootLayout,"Please check your internet!",Snackbar.LENGTH_LONG)
                    .setActionTextColor(getResources().getColor(R.color.blueSky))
                    .setAction("Ok",null).show();
        }
    }

    private void signUpAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("SIGN UP");
        builder.setMessage("Please fill the fields:");

        View signUpLayout = getLayoutInflater().inflate(R.layout.sign_up_layout, null);
        emailEdit = (MaterialEditText) signUpLayout.findViewById(R.id.email_edit);
        nameEdit = (MaterialEditText) signUpLayout.findViewById(R.id.edit_name);
        phoneEdit = (MaterialEditText) signUpLayout.findViewById(R.id.phone_edit);
        passEdit = (MaterialEditText) signUpLayout.findViewById(R.id.pass_edit);

        builder.setView(signUpLayout);
        builder.setNegativeButton("Cancel", (dialog, i) -> dialog.dismiss());
        builder.setPositiveButton("Sign UP",(dialog,i)->signUpProcess());
        builder.show();
    }

    private void signUpProcess()
    {
        if(Utils.isConnected(MainActivity.this))
        {
             if(!isAllValid())
             {
                 Snackbar.make(rootLayout,"Sign up failed !",Snackbar.LENGTH_SHORT)
                         .setActionTextColor(getResources().getColor(R.color.blueSky))
                         .setAction("Ok",null).show();
                 return;
             }
             else
             {
                 SpotsDialog spotsDialog = new SpotsDialog(MainActivity.this,R.style.CustomAlert);
                 spotsDialog.show();
                 firebaseAuth.createUserWithEmailAndPassword(emailEdit.getText().toString(),passEdit.getText().toString())
                         .addOnSuccessListener(authResult ->
                         {
                             startActivity(new Intent(MainActivity.this,DashboardActivity.class));
                             finish();
                             User user= new User();
                             user.setEmail(emailEdit.getText().toString());
                             user.setName(nameEdit.getText().toString());
                             user.setPhone(phoneEdit.getText().toString());
                             user.setPassword(passEdit.getText().toString());
                             String uid=FirebaseAuth.getInstance().getCurrentUser().getUid();
                             drivers.child(uid).setValue(user).addOnSuccessListener(aVoid ->
                             {
                                 spotsDialog.dismiss();
                             }).addOnFailureListener(e ->
                             {
                                 spotsDialog.dismiss();
                                 Snackbar.make(rootLayout,e.getMessage(),Snackbar.LENGTH_SHORT)
                                         .setActionTextColor(getResources().getColor(R.color.blueSky))
                                         .setAction("Ok",null).show();
                             });

                         })
                         .addOnFailureListener(e->
                         {
                             spotsDialog.dismiss();
                             Snackbar.make(rootLayout,e.getMessage(),Snackbar.LENGTH_SHORT)
                                     .setActionTextColor(getResources().getColor(R.color.blueSky))
                                     .setAction("Ok",null).show();
                         });
             }
        }
        else
        {
            Snackbar.make(rootLayout,"Please check your internet!",Snackbar.LENGTH_LONG)
                    .setActionTextColor(getResources().getColor(R.color.blueSky))
                    .setAction("Ok",null).show();
        }
    }

    private  boolean isAllValid()
    {
        boolean isValid=true;
        String email=emailEdit.getText().toString();
        String name=nameEdit.getText().toString();
        String phone=phoneEdit.getText().toString();
        String pass=passEdit.getText().toString();

        if(email.isEmpty()|| !Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            emailEdit.setError("Invalid email !");
            isValid=false;
        }
        else
        {
            emailEdit.setError(null);
        }
        if(name.isEmpty()|| name.length()<6)
        {
            nameEdit.setError("At least 6 chars !");
            isValid=false;
        }
        else
        {
            nameEdit.setError(null);
        }
        if(phone.isEmpty()|| phone.length()!=10)
        {
            phoneEdit.setError("Invalid phone number !");
            isValid=false;
        }
        else
        {
            phoneEdit.setError(null);
        }
        if(pass.isEmpty()|| phone.length()<6)
        {
            passEdit.setError("At least 6 chars !");
            isValid=false;
        }
        else
        {
            passEdit.setError(null);
        }
        return  isValid;
    }

    private boolean isEmailAndPassAreValid(MaterialEditText emailEdit,MaterialEditText passEdit)
    {
        boolean isValid=true;
        if(emailEdit.getText().toString().isEmpty()|| !Patterns.EMAIL_ADDRESS.matcher(emailEdit.getText().toString()).matches())
        {
            emailEdit.setError("Invalid email !");
            isValid=false;
        }
        else
        {
            emailEdit.setError(null);
        }

        if(passEdit.getText().toString().isEmpty()|| passEdit.getText().toString().length()<6)
        {
            passEdit.setError("At least 6 chars !");
            isValid=false;
        }
        else
        {
            passEdit.setError(null);
        }
        return isValid;

    }
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(base));
    }
}


