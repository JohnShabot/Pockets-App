package yonatan.shabot.pocketsapp;

import android.content.res.Resources;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class SpinnerAdapter extends ArrayAdapter<String> {

    Context context;
    List<String> strings;
    boolean isTri;
    public SpinnerAdapter(Context context, List<String> strings, boolean isTri){
        super(context, R.layout.list_item, strings);
        this.context=context;
        this.strings=strings;
        this.isTri = isTri;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }
    public View getCustomView(int pos, View convertView, ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.list_item, parent, false);

        TextView textView = row.findViewById(R.id.spinner_text);
        textView.setText(strings.get(pos));
        Resources res = context.getResources();
        int resID = 0;
        if(isTri){
            resID = res.getIdentifier("beat_t"+strings.get(pos).toLowerCase(), "drawable", context.getPackageName());

        }else {
            resID = res.getIdentifier("beat_"+strings.get(pos).toLowerCase(), "drawable", context.getPackageName());

        }

        if(resID != 0x00000000) {
            Drawable dr = res.getDrawable(resID);
            textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, resID, 0);
        }
        else{
            textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.empty, 0);
        }
        return row;
    }

}
