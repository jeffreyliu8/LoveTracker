package liu.jeffrey.lovetracker.settings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import liu.jeffrey.lovetracker.CommonUtils;
import liu.jeffrey.lovetracker.R;

public class SettingArrayAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final String[] values;

    public SettingArrayAdapter(Context context, String[] values) {
        super(context, R.layout.setting_list_row, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.setting_list_row, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.settingLabel);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.settingIcon);
        textView.setText(values[position]);
        // Change the icon for Windows and iPhone
        String s = values[position];


        if (s.startsWith("Profile")) {
            imageView.setImageBitmap(CommonUtils.loadProfileImage(getContext(),Boolean.TRUE));
        } else if (s.startsWith("Location")) {
            imageView.setImageResource(android.R.drawable.ic_menu_mylocation);
        } else if (s.startsWith("Notification")) {
            imageView.setImageResource(android.R.drawable.ic_menu_info_details);
        } else if (s.startsWith("Widget")) {
            imageView.setImageResource(android.R.drawable.ic_menu_view);
        }

        return rowView;
    }
}