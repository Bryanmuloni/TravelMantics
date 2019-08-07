package com.sirikyebrian.travelmantics;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


/**
 * Created by Sirikye Brian on 8/2/2019.
 * bryanmuloni@gmail.com
 */
public class DealsAdapter extends RecyclerView.Adapter<DealsAdapter.DealsViewHolder> {
    private static final String LOG_TAG = DealsAdapter.class.getSimpleName();
    private ArrayList<TravelDeal> travelDeals;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildEventListener;
    ImageView imageView;

    public DealsAdapter() {
        mFirebaseDatabase = FirebaseUtil.mFirebaseDatabase;
        mDatabaseReference = FirebaseUtil.mDatabaseReference;

        this.travelDeals = FirebaseUtil.mDeals;
        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                TravelDeal travelDeal = dataSnapshot.getValue(TravelDeal.class);
                Log.d(LOG_TAG, "Deal:" + travelDeal.getTitle());
                travelDeal.setId(dataSnapshot.getKey());
                travelDeals.add(travelDeal);
                notifyItemInserted(travelDeals.size() - 1);


            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mDatabaseReference.addChildEventListener(mChildEventListener);
    }

    @NonNull
    @Override
    public DealsAdapter.DealsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.deal_item_row,
                parent, false);
        return new DealsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DealsAdapter.DealsViewHolder holder, int position) {

        TravelDeal travelDeal = travelDeals.get(position);
//        String title = travelDeal.getTitle();
//        holder.titleText.setText(title);
        holder.bind(travelDeal);

    }

    @Override
    public int getItemCount() {
        return travelDeals.size();
    }

    public class DealsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView titleText;
        private TextView descriptionText;
        private TextView priceText;


        public DealsViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.textDealTitle);
            descriptionText = itemView.findViewById(R.id.textDealDescription);
            priceText = itemView.findViewById(R.id.textDealPrice);
            imageView = itemView.findViewById(R.id.dealImage);
            itemView.setOnClickListener(this);
        }

        public void bind(TravelDeal deal) {
            titleText.setText(deal.getTitle());
            descriptionText.setText(deal.getDescription());
            priceText.setText(deal.getPrice());
            showImage(deal.getImageUrl());

        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            Log.d(LOG_TAG, "Clicked position: " + position);
            TravelDeal dealSelected = travelDeals.get(position);
            Intent intent = new Intent(v.getContext(), DealActivity.class);
            intent.putExtra(DealActivity.EXTRA_DEAL, dealSelected);
            v.getContext().startActivity(intent);
        }

        private void showImage(String url) {
            if (url != null && !url.isEmpty()) {
                Picasso.get().load(url).resize(160, 160)
                        .centerCrop().into(imageView);
            }
        }
    }


}
