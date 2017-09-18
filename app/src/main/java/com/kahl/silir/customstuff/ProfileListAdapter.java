package com.kahl.silir.customstuff;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.kahl.silir.R;
import com.kahl.silir.entity.MeasurementProfile;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;
import net.steamcrafted.materialiconlib.MaterialIconView;

/**
 * Created by Paskahlis Anjas Prabowo on 30/07/2017.
 */

public class ProfileListAdapter extends BaseAdapter {
    private Context context;
    private MeasurementProfile[] profiles;
    private String[] keys;

    public ProfileListAdapter(Context context, MeasurementProfile[] profiles, String[] keys) {
        this.context = context;
        this.profiles = profiles;
        this.keys = keys;
    }

    @Override
    public int getCount() {
        return profiles.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View row = inflater.inflate(R.layout.profile_list_item, parent, false);

        final Drawable male = MaterialDrawableBuilder.with(context)
                .setColor(context.getResources().getColor(R.color.colorPrimary))
                .setIcon(MaterialDrawableBuilder.IconValue.GENDER_MALE).build();
        final Drawable female = MaterialDrawableBuilder.with(context)
                .setColor(context.getResources().getColor(R.color.colorPrimary))
                .setIcon(MaterialDrawableBuilder.IconValue.GENDER_FEMALE).build();

        TextView name = (TextView) row.findViewById(R.id.profile_name);
        TextView weight = (TextView) row.findViewById(R.id.weight_value);
        TextView height = (TextView) row.findViewById(R.id.height_value);
        TextView age = (TextView) row.findViewById(R.id.age);
        MaterialIconView gender = (MaterialIconView) row.findViewById(R.id.gender_icon);

        name.setText(profiles[position].getName());
        height.setText(String.format(context.getString(R.string.height_profile_label), profiles[position].getHeight()));
        weight.setText(String.format(context.getString(R.string.weight_profile_label), profiles[position].getWeight()));
        age.setText(String.format("%d", profiles[position].getAge()));
        gender.setImageDrawable(profiles[position].getGender().equals("Male")?male:female);

        return row;
    }

    public String getKeys(int position) {
        return keys[position];
    }

    public MeasurementProfile getProfile (int position) {
        return profiles[position];
    }
}
