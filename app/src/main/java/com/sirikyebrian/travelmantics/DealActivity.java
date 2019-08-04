package com.sirikyebrian.travelmantics;


import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DealActivity extends AppCompatActivity {
    private static final String TRAVEL_DEALS_REFERENCE = "travel_deals";
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;

    EditText txtTitle;
    EditText txtPrice;
    EditText txtDescription;
    TravelDeal travelDeal;
    private TravelDealListActivity travelDealListActivity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal);
        travelDealListActivity = new TravelDealListActivity();

        FirebaseUtil.openFirebaseReference(TRAVEL_DEALS_REFERENCE, travelDealListActivity);
        mFirebaseDatabase = FirebaseUtil.mFirebaseDatabase;
        mDatabaseReference = FirebaseUtil.mDatabaseReference;

        txtTitle = findViewById(R.id.txtTitle);
        txtPrice = findViewById(R.id.txtPrice);
        txtDescription = findViewById(R.id.txtDescription);

        Intent intent = getIntent();
        TravelDeal deal = (TravelDeal) intent.getSerializableExtra("Deal");
        if (deal == null) {
            deal = new TravelDeal();
        }
        this.travelDeal = deal;
        txtTitle.setText(deal.getTitle());
        txtDescription.setText(deal.getDescription());
        txtPrice.setText(deal.getPrice());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save_menu, menu);
        if (FirebaseUtil.isAdmin) {
            menu.findItem(R.id.action_delete_deal).setVisible(true);
            menu.findItem(R.id.action_save_deal).setVisible(true);
            enableEditTexts(true);
        } else {
            menu.findItem(R.id.action_delete_deal).setVisible(false);
            menu.findItem(R.id.action_save_deal).setVisible(false);
            enableEditTexts(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.action_save_deal:
                saveTravelDeal();
                Toast.makeText(this, "Deal saved!", Toast.LENGTH_SHORT).show();
                clean();
                backToList();
                return true;
            case R.id.action_delete_deal:
                deleteTravelDeal();
                Toast.makeText(this, "Deal Deleted!", Toast.LENGTH_SHORT).show();
                backToList();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }


    }

    private void saveTravelDeal() {
        travelDeal.setTitle(txtTitle.getText().toString());
        travelDeal.setDescription(txtDescription.getText().toString());
        travelDeal.setPrice(txtPrice.getText().toString());

        if (travelDeal.getId() == null) {
            mDatabaseReference.push().setValue(travelDeal);
        } else {
            mDatabaseReference.child(travelDeal.getId()).setValue(travelDeal);
        }

    }

    private void deleteTravelDeal() {
        if (travelDeal == null) {
            Toast.makeText(this, "Please save the travelDeal before deleting", Toast.LENGTH_SHORT).show();
            return;
        }
        mDatabaseReference.child(travelDeal.getId()).removeValue();
    }

    private void backToList() {
        startActivity(new Intent(this, TravelDealListActivity.class));
    }

    private void clean() {
        txtTitle.setText("");
        txtDescription.setText("");
        txtPrice.setText("");
        txtTitle.requestFocus();
    }

    private void enableEditTexts(boolean isEnabled) {
        txtTitle.setEnabled(isEnabled);
        txtDescription.setEnabled(isEnabled);
        txtPrice.setEnabled(isEnabled);
    }
}
