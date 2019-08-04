package com.phan.alc.travelmantics;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import android.os.Handler;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.RecyclerViewHolder> {
    ArrayList<TravelDeals> travelDeals;
    FirebaseDatabase database;
    DatabaseReference myRef;
    ChildEventListener childEventListener;
    Context context;
    ImageView imageView;
    ProgressDialog dialog;
   public RecyclerAdapter(final Context context){
     //  FirebaseUtil.openReference("traveldeals");
       this.context = context;
       database = FirebaseUtil.database;
       myRef = FirebaseUtil.myRef;
       travelDeals = FirebaseUtil.travelDeals;
       travelDeals.clear();
       dialog = new ProgressDialog(context);
       dialog.setMessage("Please wait...");
       dialog.show();
       childEventListener = new ChildEventListener(){
           @Override
           public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
               TravelDeals traveldeals = dataSnapshot.getValue(TravelDeals.class);
               traveldeals.setId(dataSnapshot.getKey());
               travelDeals.add(traveldeals);
               notifyItemInserted(travelDeals.size()-1);
               dialog.hide();
           }

           @Override
           public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
               travelDeals.clear();
               notifyDataSetChanged();
               dialog.hide();

           }

           @Override
           public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
               travelDeals.clear();
               notifyDataSetChanged();
               dialog.hide();
           }

           @Override
           public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

           }

           @Override
           public void onCancelled(@NonNull DatabaseError databaseError) {

           }
       };
       myRef.addChildEventListener(childEventListener);
       myRef.addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               if (!dataSnapshot.exists()){
                   dialog.hide();
                   AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                   builder1.setMessage("No Records Yet.");
                   builder1.setCancelable(true);

                   builder1.setPositiveButton(
                           "OK",
                           new DialogInterface.OnClickListener() {
                               public void onClick(DialogInterface dialog, int id) {
                                   dialog.cancel();
                               }
                           });

                   builder1.setNegativeButton(
                           "Cancel",
                           new DialogInterface.OnClickListener() {
                               public void onClick(DialogInterface dialog, int id) {
                                   dialog.cancel();
                               }
                           });

                   AlertDialog alert11 = builder1.create();
                   alert11.show();
               }
           }

           @Override
           public void onCancelled(@NonNull DatabaseError databaseError) {

           }
       });
       Handler handler = new Handler();
       handler.postDelayed(new Runnable() {
           @Override
           public void run() {
               dialog.hide();
           }
       },3000);
   }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.deal, parent, false);
        return new RecyclerViewHolder(itemView);
   }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {
        TravelDeals deals = travelDeals.get(position);
        holder.bind(deals);
    }

    @Override
    public int getItemCount() {
        return travelDeals.size();
    }



    public class  RecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView title, description, price;
        public RecyclerViewHolder(View itemView){
            super(itemView);
            title = itemView.findViewById(R.id.title);
            description = itemView.findViewById(R.id.description);
            price = itemView.findViewById(R.id.price);
            imageView = itemView.findViewById(R.id.imageView2);
            itemView.setOnClickListener(this);
        }
        public void bind(TravelDeals deal){
            title.setText(deal.getTitle());
            description.setText(deal.getDescription());
            price.setText(deal.getPrice());
            showImage(deal.getImageUrl());
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            TravelDeals travelD = travelDeals.get(position);
            Intent intent = new Intent(v.getContext(), EditActivity.class);
            intent.putExtra("Deals", travelD);
            v.getContext().startActivity(intent);
        }
    }
    private void showImage(String url){
        if (url != null && url.isEmpty() == false){
            Picasso.get()
                    .load(url)
                    .placeholder(R.drawable.place)
                    .into(imageView);
        }
    }
}
