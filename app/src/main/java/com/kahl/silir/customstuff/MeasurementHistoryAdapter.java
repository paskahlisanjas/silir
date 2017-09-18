package com.kahl.silir.customstuff;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.kahl.silir.R;
import com.kahl.silir.databasehandler.ProfileDbHandler;
import com.kahl.silir.entity.MeasurementProfile;
import com.kahl.silir.entity.MeasurementResult;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Paskahlis Anjas Prabowo on 02/08/2017.
 */

public class MeasurementHistoryAdapter extends RecyclerView
        .Adapter<MeasurementHistoryAdapter.MeasurementHistoryHolder> {
    private Context context;
    private MeasurementResult[] results;
    private final Drawable dots;

    public MeasurementHistoryAdapter(Context context, MeasurementResult[] results) {
        this.context = context;
        this.results = results;
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
    public void onBindViewHolder(MeasurementHistoryHolder holder, int position) {
        MeasurementResult result = results[position];

        ProfileDbHandler dbHandler = new ProfileDbHandler(context);
        MeasurementProfile profile = dbHandler.getProfile(result.getProfileId());
        String[] nameArray = profile.getName().split(" ");
        String name = nameArray[0];
        if (nameArray.length >= 2) name += " " + nameArray[1];

        holder.profileNameTextView.setText(name);
        holder.pefTextView.setText("PEF   : " + result.getPef());
        holder.fevTextView.setText("FEV1  : " + result.getFev1());
        holder.fvcTextView.setText("FVC   : " + result.getFvc());
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        holder.dateTextView.setText(dateFormat.format(now));
        holder.timeTextView.setText(timeFormat.format(now));
        holder.menuButton.setImageDrawable(dots);
        holder.menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopUpMenu(v);
            }
        });

    }

    private void showPopUpMenu(View v) {
        PopupMenu menu = new PopupMenu(context, v);
        MenuInflater inflater = menu.getMenuInflater();
        inflater.inflate(R.menu.card_list_menu, menu.getMenu());
        menu.show();
    }

    @Override
    public int getItemCount() {
        return results.length;
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
