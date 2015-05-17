package liu.jeffrey.lovetracker.mainFragments;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import liu.jeffrey.lovetracker.CommonUtils;
import liu.jeffrey.lovetracker.R;
import liu.jeffrey.lovetracker.settings.profile.ProfileSettingActivity;

public class ProfileFragment extends Fragment implements View.OnClickListener {
    private View rootView;
    private ImageView profileImage;
    private TextView profileName;

    public ProfileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        View cardview = rootView.findViewById(R.id.profile_card_view);
        profileImage = (ImageView) rootView.findViewById(R.id.profileImageView);
        profileName = (TextView) rootView.findViewById(R.id.profileTextView);
        Button infoButton = (Button) rootView.findViewById(R.id.infoRegIdButton);
        Button copyButton = (Button) rootView.findViewById(R.id.copyRegIdButton);

        cardview.setOnClickListener(this);
        infoButton.setOnClickListener(this);
        copyButton.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        profileImage.setImageBitmap(CommonUtils.loadProfileImage(getActivity(),Boolean.TRUE));
        profileName.setText(CommonUtils.getDisplayNamePreference(getActivity()));
    }

    @Override
    public void onClick(View view) {
        if (view == rootView.findViewById(R.id.infoRegIdButton)) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

            alert.setTitle("Registration ID");
            alert.setMessage("The registration ID is for this app to know who you are. Every user has a unique ID. You can share or add a friend using registration ID. Your ID is: " +
                    CommonUtils.getRegistrationId(getActivity()));


            alert.setPositiveButton("Copy", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    copyRegidToClipBoard();
                    Toast.makeText(getActivity(), "Copied", Toast.LENGTH_SHORT).show();
                }
            });

            alert.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Canceled.
                }
            });

            alert.show();
        } else if (view == rootView.findViewById(R.id.copyRegIdButton)) {
            copyRegidToClipBoard();
            Toast.makeText(getActivity(), "Copied", Toast.LENGTH_SHORT).show();
        } else if (view == rootView.findViewById(R.id.profile_card_view)) {
            Intent profileIntent = new Intent();
            profileIntent.setComponent(new ComponentName(getActivity(), ProfileSettingActivity.class));

            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(getActivity(),
                    Pair.create((View)profileImage, "profile_photo_transition"),
                    Pair.create((View)profileName, "profile_name_transition"));

            startActivity(profileIntent, options.toBundle());
        }
    }

    private void copyRegidToClipBoard() {
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("regid", CommonUtils.getRegistrationId(getActivity()));
        clipboard.setPrimaryClip(clip);
    }
}