package com.sirikyebrian.travelmantics;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class TravelDealListActivity extends AppCompatActivity {
    public static final String TRAVEL_DEALS_REFERENCE = "travel_deals";
    private static final String LOG_TAG = TravelDealListActivity.class.getSimpleName();
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel_deal_list);


    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUtil.openFirebaseReference(TRAVEL_DEALS_REFERENCE,this);
        mRecyclerView = findViewById(R.id.dealsRecyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        final DealsAdapter dealsAdapter = new DealsAdapter();
        mRecyclerView.setAdapter(dealsAdapter);
        FirebaseUtil.attachListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseUtil.detachListener();
    }

    public void showMenu(){
        invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.deal_list_menu, menu);

        MenuItem item = menu.findItem(R.id.action_new_deal);
        if (FirebaseUtil.isAdmin == true){
            item.setVisible(true);
        }else {
            item.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.action_new_deal:
                startActivity(new Intent(this, DealActivity.class));
                return true;
            case R.id.action_logout:
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                // ...
                                Log.d(LOG_TAG,"User Logged Out");
                                FirebaseUtil.attachListener();
                            }
                        });
                FirebaseUtil.detachListener();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
