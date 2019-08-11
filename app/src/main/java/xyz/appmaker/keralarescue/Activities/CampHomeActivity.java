package xyz.appmaker.keralarescue.Activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import xyz.appmaker.keralarescue.MainActivity;
import xyz.appmaker.keralarescue.R;
import xyz.appmaker.keralarescue.Tools.Config;
import xyz.appmaker.keralarescue.Tools.PreferensHandler;

public class CampHomeActivity extends AppCompatActivity {

    PreferensHandler pref;
    Context context;
    Button updateReq,addInmates,updateCamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camp_home);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        context = getApplicationContext();
        pref = new PreferensHandler(context);
        updateReq = findViewById(R.id.btn_req);
        updateReq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent reqAct = new Intent(CampHomeActivity.this, RequirementActivity.class);
                reqAct.putExtra("campId", getIntent().getStringExtra("campId"));
                startActivity(reqAct);
            }
        });

        addInmates = findViewById(R.id.btn_inmates);
        addInmates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent fieldIntent = new Intent(CampHomeActivity.this, FieldsActivity.class);
                fieldIntent.putExtra("campId", getIntent().getStringExtra("campId"));
                startActivity(fieldIntent);
            }
        });
        updateCamp = findViewById(R.id.btn_update_camp);
        updateCamp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUrl(Config.BASE_URL+"/camp/"+getIntent().getStringExtra("campId")+"/details/");
            }
        });

        setTitle(getIntent().getStringExtra("campName")) ;
    }

    private void openUrl(String url){
        Intent openUrlIntent = new Intent(Intent.ACTION_VIEW);
        openUrlIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        openUrlIntent.setData(Uri.parse(url));
        try {
            startActivity(openUrlIntent);
        }
        catch (Exception e){
            Crashlytics.logException(e);
            Toast.makeText(this,
                    "Couldn't open camp details page, Please make sure you've a web browser installed",
                    Toast.LENGTH_LONG).show();
        }
    }

    public String authToken() {
        return "JWT " + pref.getUserToken();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_camp_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            Toast.makeText(this, "Logging out", Toast.LENGTH_SHORT).show();
            logoutUser();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void logoutUser() {
        if (pref != null) {
            pref.setUserToken("");
            Intent actLogin = new Intent(CampHomeActivity.this, MainActivity.class);
            startActivity(actLogin);
        }
    }
}
