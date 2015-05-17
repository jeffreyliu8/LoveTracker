package liu.jeffrey.lovetracker.mainFragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import liu.jeffrey.lovetracker.MapActivity;
import liu.jeffrey.lovetracker.R;

public class PagesFragment extends Fragment implements View.OnClickListener {
    private View rootView;
    public PagesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_pages, container, false);

        Button locationButton = (Button) rootView.findViewById(R.id.location);

        locationButton.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onClick(View view) {
        if (view == rootView.findViewById(R.id.location)) {
            Intent openMapIntent = new Intent(getActivity(), MapActivity.class);
            startActivity(openMapIntent);
        }
    }
}
