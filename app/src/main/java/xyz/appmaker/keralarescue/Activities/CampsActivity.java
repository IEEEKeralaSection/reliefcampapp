package xyz.appmaker.keralarescue.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import xyz.appmaker.keralarescue.Adapters.CampRecycleViewAdapter;
import xyz.appmaker.keralarescue.AppController;
import xyz.appmaker.keralarescue.Interfaces.RecycleItemClickListener;
import xyz.appmaker.keralarescue.MainActivity;
import xyz.appmaker.keralarescue.Models.District;
import xyz.appmaker.keralarescue.R;
import xyz.appmaker.keralarescue.Room.Camp.CampNames;
import xyz.appmaker.keralarescue.Room.CampDatabase;
import xyz.appmaker.keralarescue.Tools.APIService;
import xyz.appmaker.keralarescue.Tools.Config;
import xyz.appmaker.keralarescue.Tools.PreferensHandler;

public class CampsActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private ProgressBar progressBar;
    private CampRecycleViewAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    List<CampNames> campNames = new ArrayList<>();
    RecycleItemClickListener recycleItemClickListener;
    Spinner districtSpinner;
    PreferensHandler pref;
    Context context;
    private APIService apiService;
    private CampDatabase dbInstance;
    String districtSelectedValue;
    List<CampNames> searchResult = new ArrayList<>();
    EditText edtSearch;
    Button btnRecent, btnSearch;

    ArrayAdapter<District> districtAdapter;
    CardView recentCardview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camps);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        context = getApplicationContext();
        pref = new PreferensHandler(context);
        btnRecent = (Button) findViewById(R.id.btn_recent);

        btnSearch = (Button) findViewById(R.id.search_btn);
        recentCardview = (CardView) findViewById(R.id.recent_card_view);
        progressBar = findViewById(R.id.progressBar);

        districtSpinner = findViewById(R.id.spinner_district);
        edtSearch = (EditText) findViewById(R.id.edt_search_camp);
        dbInstance = CampDatabase.getDatabase(context);
        districtAdapter = new ArrayAdapter<District>(this,
                android.R.layout.simple_spinner_item, Config.getDistricts());
        districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // districtSpinner
        districtSpinner.setAdapter(districtAdapter);
        apiService = AppController.getRetrofitInstance();
        Log.d("INSTANCE", dbInstance.getOpenHelper().getReadableDatabase().getPath());

        districtSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // TO-DO
                // make network call for camp list
                District district = (District) parent.getSelectedItem();
                districtSelectedValue = district.getId();
                updateCamps(districtSelectedValue);
                searchResult.clear();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mRecyclerView = findViewById(R.id.camp_recycler_view);


        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);


        recycleItemClickListener = new RecycleItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                // Log.e("TAG","onItem final call"+position);
                CampNames camp = campNames.get(position);

                if (searchResult.size() > 0) {
                    camp = searchResult.get(position);
                }

                if (pref != null) {
                    pref.setRecentCampID(camp.getId());
                    pref.setRecentCamp(camp.getName());
                }
                Intent fieldIntent = new Intent(CampsActivity.this, CampHomeActivity.class);
                fieldIntent.putExtra("campId", String.valueOf(camp.getId()));
                fieldIntent.putExtra("campName",camp.getName());
                startActivity(fieldIntent);
            }
        };
        mAdapter = new CampRecycleViewAdapter(recycleItemClickListener);
        mRecyclerView.setAdapter(mAdapter);

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtSearch != null && edtSearch.getText() != null) {
                    loadSearchedCamp(edtSearch.getText().toString());
                }
            }
        });

    }


    public void updateCamps(final String district) {
        if (district.equals(""))
            return;
        progressBar.setVisibility(View.VISIBLE);
        Call<List<CampNames>> response = apiService.getCampList(authToken(), district);
        response.enqueue(new Callback<List<CampNames>>() {
            @Override
            public void onResponse(Call<List<CampNames>> call, Response<List<CampNames>> response) {
                Log.e("TAG", "success response ");

                List<CampNames> items = response.body();

                if (items != null && items.size() > 0) {

                    insetCampDb(items);
                }
                progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onFailure(Call<List<CampNames>> call, Throwable t) {
                Log.e("TAG", "fail response ");
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void insetCampDb(final List<CampNames> var) {
        Log.e("TAG", "insetCampDb ");

        new Thread(new Runnable() {
            @Override
            public void run() {
                dbInstance.campDao().insertCapms(var);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadCamps(var.get(0).getDistrict());
                    }
                });

            }
        }).start();
    }

    public void loadCamps(final String district) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                campNames = dbInstance.campDao().getAllCampsByDistrict(district);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //mAdapter.upda(campNames);
                        //  mRecyclerView.setAdapter(mAdapter);
                        mAdapter.updateDataset(campNames);


                    }
                });
            }
        }).start();

    }

    public void loadSearchedCamp(String search) {
        Log.e("TAG", "load search " + search);
        searchResult.clear();
        if (search.equals("")) {
            mAdapter.updateDataset(campNames);
            return;
        }

        for (CampNames names : campNames) {
            if (names.getName().toLowerCase().contains(search.toLowerCase())) {
                searchResult.add(names);
            }
        }

        mAdapter.updateDataset(searchResult);

        // return searchResult;

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("TAG", " onResume ");
        if (pref.getRecentCampID() != -1) {
            btnRecent.setText(pref.getRecentCamp());
            btnRecent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent fieldIntent = new Intent(CampsActivity.this, CampHomeActivity.class);
                    fieldIntent.putExtra("campId", String.valueOf(pref.getRecentCampID()));
                    fieldIntent.putExtra("campName",pref.getRecentCamp());
                    startActivity(fieldIntent);
                }
            });
            recentCardview.setVisibility(View.VISIBLE);
        } else {
            recentCardview.setVisibility(View.GONE);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_camp, menu);
        return true;
    }

    public String authToken() {
        return "JWT " + pref.getUserToken();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

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
            Intent actLogin = new Intent(CampsActivity.this, MainActivity.class);
            startActivity(actLogin);
        }
    }
}
