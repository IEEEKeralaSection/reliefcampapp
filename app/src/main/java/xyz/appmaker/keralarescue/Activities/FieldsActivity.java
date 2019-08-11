package xyz.appmaker.keralarescue.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import xyz.appmaker.keralarescue.AppController;
import xyz.appmaker.keralarescue.MainActivity;
import xyz.appmaker.keralarescue.Models.Gender;
import xyz.appmaker.keralarescue.Models.PersonsResponse;
import xyz.appmaker.keralarescue.Models.District;
import xyz.appmaker.keralarescue.R;
import xyz.appmaker.keralarescue.Room.Camp.CampNames;
import xyz.appmaker.keralarescue.Room.CampDatabase;
import xyz.appmaker.keralarescue.Room.PersonData.PersonDataDao;
import xyz.appmaker.keralarescue.Room.PersonData.PersonDataEntity;
import xyz.appmaker.keralarescue.Tools.APIService;
import xyz.appmaker.keralarescue.Tools.Config;
import xyz.appmaker.keralarescue.Tools.PreferensHandler;

public class FieldsActivity extends AppCompatActivity {


    EditText nameEdt, ageEdt, addressEdt, mobileEdt, notesEdt;
    TextView syncDetailsTextView;
    Spinner campNameSpn, genderSpn, districtSpn;
    HashMap<String, String> distMap = new HashMap<>();
    PreferensHandler pref;
    Button submitBtn;
    Context context;
    private PersonDataDao personDao;
    CampDatabase dbInstance;

    ArrayList<Gender> genderList = new ArrayList<>();
    ArrayList<CampNames> campList = new ArrayList<>();

    String districtSelectedValue = null;
    String genderSelectedValue = null;
    String campSelectedValue = "0";
    APIService apiService;
    TextView titleText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fields);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        context = getApplicationContext();
        pref = new PreferensHandler(context);
        syncDetailsTextView = findViewById(R.id.syncDetails);
        dbInstance = CampDatabase.getDatabase(context);
        apiService = AppController.getRetrofitInstance();
        campSelectedValue = getIntent().getStringExtra("campId");
        titleText = (TextView) findViewById(R.id.title);
        if (pref.getRecentCampID() != -1) {
            titleText.setText(pref.getRecentCamp());
        }
        // Gender spinner
        genderList.add(new Gender("", "-"));
        genderList.add(new Gender("0", "Male"));
        genderList.add(new Gender("1", "Female"));
        genderList.add(new Gender("2", "Others"));

        ArrayAdapter<Gender> genderAdapter = new ArrayAdapter<Gender>(this,
                android.R.layout.simple_spinner_item, genderList);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpn = (Spinner) findViewById(R.id.gender);
        genderSpn.setAdapter(genderAdapter);


        genderSpn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //stateSelectedValue
                Gender gender = (Gender) parent.getSelectedItem();
                genderSelectedValue = gender.getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



        ArrayAdapter<District> districtAdapter = new ArrayAdapter<District>(this,
                android.R.layout.simple_spinner_item, Config.getDistricts());
        districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        districtSpn = (Spinner) findViewById(R.id.district);

        districtSpn.setAdapter(districtAdapter);

        if (pref != null)
            districtSpn.setSelection(pref.getDistrictDef());

        districtSpn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (pref != null)
                    pref.setDistrictDef(position);
                District district = (District) parent.getSelectedItem();
                districtSelectedValue = district.getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        nameEdt = (EditText) findViewById(R.id.name);
        ageEdt = (EditText) findViewById(R.id.age);
        addressEdt = (EditText) findViewById(R.id.address);
        mobileEdt = (EditText) findViewById(R.id.mobile);
        notesEdt = (EditText) findViewById(R.id.note);
        submitBtn = (Button) findViewById(R.id.submit);

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("Tag", "state code, gender code " + districtSelectedValue + "  - " + genderSelectedValue);

                if (validateData()) {

                    PersonDataEntity personDataModel = new PersonDataEntity(
                            nameEdt.getText().toString(),
                            campSelectedValue,
                            ageEdt.getText().toString(),
                            (!genderSelectedValue.equals("")) ? genderSelectedValue : null,
                            addressEdt.getText().toString(),
                            districtSelectedValue,
                            mobileEdt.getText().toString(),
                            notesEdt.getText().toString(),
                            "new",
                            "0");
                    insetPersonDb(personDataModel);
                }
            }
        });
        //updateCamps();
        //loadCamps();

        syncDB();
        updateSynced();
    }

    public void updateSynced() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Integer suyncedCount = dbInstance.personDataDao().statusCount("1");
                final Integer unsyncedCount = dbInstance.personDataDao().statusCount("0");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        syncDetailsTextView.setText(unsyncedCount + " Pending - " + suyncedCount + " Synced");
                    }
                });
            }
        }).start();

    }


    public void syncDB() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<PersonDataEntity> personDataUnsynced = dbInstance.personDataDao().getUnSyncedPersons();
                apiService.addPersons(authToken(), personDataUnsynced).enqueue(new Callback<PersonsResponse>() {
                    @Override
                    public void onResponse(Call<PersonsResponse> call, Response<PersonsResponse> response) {

                        if (response.isSuccessful()) {
                            final int[] updateIds = new int[200];
                            int index = 0;
                            for (PersonDataEntity personDataEntity : personDataUnsynced) {
                                updateIds[index++] = personDataEntity.id;
                            }
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    dbInstance.personDataDao().updateStatus(updateIds, "1");
                                    updateSynced();
                                }
                            }).start();
                        } else {
                            Toast.makeText(getApplicationContext(), "Some error while saving data, Please contact admin", Toast.LENGTH_LONG).show();
                            updateSynced();
                        }
                    }

                    @Override
                    public void onFailure(Call<PersonsResponse> call, Throwable t) {
                        Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_LONG).show();
                        updateSynced();
                    }
                });

            }
        }).start();
    }


    public boolean validateData() {
        if (nameEdt.getText().toString().equals("")) {
//            || ageEdt.getText().toString().equals("") || addressEdt.getText().toString().equals("") ||
//                    mobileEdt.getText().toString().equals("") || notesEdt.getText().toString().equals("")
            Toast.makeText(context, "Name is required",
                    Toast.LENGTH_LONG).show();
            return false;
        } else {
            return true;
        }
    }

    public void insetPersonDb(final PersonDataEntity var) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                dbInstance.personDataDao().insert(var);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        personAdded();
                        syncDB();
                    }
                });


            }
        }).start();
    }

    private void personAdded() {
        Toast.makeText(context, "Person added successfully",
                Toast.LENGTH_LONG).show();
        nameEdt.setText("");
        ageEdt.setText("");
        addressEdt.setText("");
        mobileEdt.setText("");
        notesEdt.setText("");
        nameEdt.requestFocus();
//        nameEdt.setText("");

    }

    public String authToken() {
        return "JWT " + pref.getUserToken();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_sync) {
            Toast.makeText(this, "Syncing", Toast.LENGTH_SHORT).show();
            syncDB();

            return true;
        }
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
            Intent actLogin = new Intent(FieldsActivity.this, MainActivity.class);
            startActivity(actLogin);
        }
    }
}
