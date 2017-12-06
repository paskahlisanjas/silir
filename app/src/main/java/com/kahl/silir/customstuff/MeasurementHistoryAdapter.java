package com.kahl.silir.customstuff;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.icu.util.Measure;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kahl.silir.R;
import com.kahl.silir.databasehandler.ProfileDbHandler;
import com.kahl.silir.databasehandler.ResultDbHandler;
import com.kahl.silir.entity.MeasurementProfile;
import com.kahl.silir.entity.MeasurementResult;
import com.kahl.silir.main.history.MeasurementHitoryFragment;
import com.kahl.silir.main.history.MeasurementResultActivity;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.List;

/**
 * Created by Paskahlis Anjas Prabowo on 02/08/2017.
 */

public class MeasurementHistoryAdapter extends RecyclerView
        .Adapter<MeasurementHistoryAdapter.MeasurementHistoryHolder> {
    private Context context;
    private List<MeasurementResult> results;
    private boolean isCloud;

    private final Drawable dots;

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
            .child("measurements").child(firebaseAuth.getCurrentUser().getUid());

    public MeasurementHistoryAdapter(Context context, List<MeasurementResult> results, boolean isCloud) {
        this.context = context;
        this.results = results;
        this.isCloud = isCloud;

        dots = MaterialDrawableBuilder.with(context)
                .setIcon(MaterialDrawableBuilder.IconValue.DOTS_VERTICAL)
                .setColor(context.getResources().getColor(R.color.colorPrimary)).build();
    }

    @Override
    public MeasurementHistoryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.measurement_result_cardview,
                parent, false);
        return new MeasurementHistoryHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MeasurementHistoryHolder holder, final int position) {
        final MeasurementResult result = results.get(position);

        ProfileDbHandler dbHandler = new ProfileDbHandler(context);
        MeasurementProfile profile = dbHandler.getProfile(result.getProfileId());
        String[] nameArray = profile.getName().split(" ");
        String name = nameArray[0];
        if (nameArray.length >= 2) name += " " + nameArray[1];

        holder.profileNameTextView.setText(name);
        holder.pefTextView.setText("PEF   : " + formatNumber(result.getPef()));
        holder.fevTextView.setText("FEV1  : " + formatNumber(result.getFev1()));
        holder.fvcTextView.setText("FVC   : " + formatNumber(result.getFvc()));
        String[] time = result.getTime().split(" - ");
        String date = time[0];
        String clock = time[1];
        holder.dateTextView.setText(date);
        holder.timeTextView.setText(clock);
        holder.menuButton.setImageDrawable(dots);
        holder.menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopUpMenu(v, result.getTime(), result, position);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MeasurementResultActivity.class);
                intent.putExtra(MeasurementHitoryFragment.FROM_HERE, true);
                intent.putExtra(MeasurementHitoryFragment.CHOSEN_RESULT, result.getTime());
                intent.putExtra(MeasurementHitoryFragment.IDENTIFY_CLOUD, isCloud);
                context.startActivity(intent);
            }
        });
    }

    private String formatNumber(float f) {
        return String.format("%.2f", f);
    }

    private void showPopUpMenu(View v, final String time, final MeasurementResult result, final int position) {
        PopupMenu menu = new PopupMenu(context, v);
        MenuInflater inflater = menu.getMenuInflater();
        inflater.inflate(R.menu.card_list_menu, menu.getMenu());
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.delete_record:
                        if (isCloud) {
                            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for (DataSnapshot child : dataSnapshot.getChildren()){
                                        MeasurementResult result = child.getValue(MeasurementResult.class);
                                        if (result.getTime().equals(time)) {
                                            child.getRef().removeValue();
                                            break;
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        } else {
                            ResultDbHandler dbHandler = new ResultDbHandler(context);
                            dbHandler.deleteResult(result.getTime());
                        }
                        results.remove(position);
                        notifyItemRemoved(position);
                        return true;
                    case R.id.upload_record:
                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yy|hh:mm:ss");
                        if (isCloud)
                            Toast.makeText(context, "Already in cloud", Toast.LENGTH_SHORT).show();
                        else {
                            databaseReference.child(sdf.format(new Date())).setValue(result);
                            Toast.makeText(context, "Record added", Toast.LENGTH_SHORT).show();
                        }
                        return true;
                }

                return false;
            }
        });
        menu.show();
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    public static class MeasurementHistoryHolder extends RecyclerView.ViewHolder {
        protected TextView profileNameTextView;
        protected TextView pefTextView;
        protected TextView fevTextView;
        protected TextView fvcTextView;
        protected TextView dateTextView;
        protected TextView timeTextView;
        protected ImageButton menuButton;

        public MeasurementHistoryHolder(View itemView) {
            super(itemView);
            profileNameTextView = (TextView) itemView.findViewById(R.id.profile_name);
            pefTextView = (TextView) itemView.findViewById(R.id.pef);
            fevTextView = (TextView) itemView.findViewById(R.id.fev);
            fvcTextView = (TextView) itemView.findViewById(R.id.fvc);
            dateTextView = (TextView) itemView.findViewById(R.id.date);
            timeTextView = (TextView) itemView.findViewById(R.id.time);
            menuButton = (ImageButton) itemView.findViewById(R.id.menu_button);
        }
    }
}
