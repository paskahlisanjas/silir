package com.kahl.silir.customstuff;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kahl.silir.R;

/**
 * Created by Paskahlis Anjas Prabowo on 26/07/2017.
 */

public class NavigationDrawerListAdapter extends BaseAdapter {
    private Context context;
    private Drawable[] icons;
    private String[] titles;

    public NavigationDrawerListAdapter(Context context, Drawable[] icons, String[] titles) {
        this.context = context;
        this.icons = icons;
        this.titles = titles;
    }

    @Override
    public int getCount() {
        return titles.length;
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
        View rowView = inflater.inflate(R.layout.navigation_drawer_custom_list_item, parent, false);
        ImageView icon = (ImageView) rowView.findViewById(R.id.icon_image_view);
        TextView title = (TextView) rowView.findViewById(R.id.title_text_view);
        icon.setImageDrawable(icons[position]);
        title.setText(titles[position]);
        return rowView;
    }
}
