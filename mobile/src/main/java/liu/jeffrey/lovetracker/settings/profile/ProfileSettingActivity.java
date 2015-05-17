package liu.jeffrey.lovetracker.settings.profile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.transition.Transition;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import liu.jeffrey.lovetracker.CommonUtils;
import liu.jeffrey.lovetracker.Constants;
import liu.jeffrey.lovetracker.R;
import liu.jeffrey.lovetracker.adapter.TransitionAdapter;
import liu.jeffrey.lovetracker.crop.CropOption;
import liu.jeffrey.lovetracker.crop.CropOptionAdapter;

public class ProfileSettingActivity extends Activity {

    private static final int PICK_FROM_CAMERA = 1;
    private static final int CROP_FROM_CAMERA = 2;
    private static final int PICK_FROM_FILE = 3;

    ImageView profileImage;
//    CheckBox prefCheckBox;
//    TextView prefEditText;
    ImageButton buttonEditName;
    TextView nameTextView;

    private Uri mImageCaptureUri;
    private Uri mImageToBeCroppedUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setting);

        // enable home button to go back
        getActionBar().setDisplayHomeAsUpEnabled(true);

        profileImage = (ImageView) findViewById(R.id.settingProfileImageView);
        buttonEditName = (ImageButton) findViewById(R.id.display_name_button);
        nameTextView = (TextView) findViewById(R.id.display_name_text);
        profileImage.setImageBitmap(CommonUtils.loadProfileImage(this,Boolean.TRUE));

//        prefCheckBox = (CheckBox) findViewById(R.id.prefCheckBox);
//        prefEditText = (TextView) findViewById(R.id.prefEditText);



        //===
        final String[] items = new String[]{"Take from camera", "Select from gallery"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, items);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Select Image");
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) { //pick from camera
                if (item == 0) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    mImageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(),
                            "tmp_avatar_" + String.valueOf(System.currentTimeMillis()) + ".jpg"));//TODO: delete photos after taken

                    intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);

                    try {
                        intent.putExtra("return-data", true);

                        startActivityForResult(intent, PICK_FROM_CAMERA);
                    } catch (ActivityNotFoundException e) {
                        e.printStackTrace();
                    }
                } else { //pick from file
                    Intent intent = new Intent();

                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);

                    startActivityForResult(Intent.createChooser(intent, "Complete action using"), PICK_FROM_FILE);
                }
            }
        });

        final AlertDialog dialog = builder.create();

        ImageButton buttonEditPic = (ImageButton) findViewById(R.id.edit_profile_photo);

        buttonEditPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });

        getWindow().getEnterTransition().addListener(new TransitionAdapter() {
            @Override
            public void onTransitionEnd(Transition transition) {
                getWindow().getEnterTransition().removeListener(this);
            }
        });
    }

    public void openNameSetting(View view) {
        Intent intent = new Intent();
        intent.setClass(this, SetDisplayNamePreferenceActivity.class);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(Constants.TAG,"onResume");
        loadPref();
    }

    private void loadPref() {
        SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        String my_edittext_preference = mySharedPreferences.getString("display_name_preference", "Random Person");

        //boolean my_checkbox_preference = mySharedPreferences.getBoolean("display_name_checkbox_preference", false);

        if(my_edittext_preference.equals("")){ //todo: fix this, the displayNamePrefsFragment should handle this
            mySharedPreferences.edit().putString("display_name_preference", "Random Person").apply();
            my_edittext_preference = mySharedPreferences.getString("display_name_preference", "Random Person");
        }

        nameTextView.setText(my_edittext_preference);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finishAfterTransition();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //===
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        loadPref();
        if (resultCode != RESULT_OK)
            return;

        switch (requestCode) {
            case PICK_FROM_CAMERA:
                doCrop();
                break;
            case PICK_FROM_FILE:
                mImageCaptureUri = data.getData();
                doCrop();
                break;
            case CROP_FROM_CAMERA:
                Bundle extras = data.getExtras();

                if (extras != null) {
                    Log.d(Constants.TAG, "extras: " + extras.toString());
                    Bitmap photo = extras.getParcelable("data");
                    if (!SaveImageToExternalStorage(photo)) {
                        Log.d(Constants.TAG, "Profile pic NOT SAVED!");
                    }
                    profileImage.setImageBitmap(CommonUtils.getCircleBitmap(photo));

                    String croppedImageId = mImageToBeCroppedUri.getPathSegments().get(3);
                    Log.d(Constants.TAG, "imageId: " + croppedImageId);

                    int mRowsDeleted = deleteImageFromMediaStore(croppedImageId);
                    Log.d(Constants.TAG, "mRowsDeleted = " + mRowsDeleted);

                    String croppedImageID = getIdOfLastInsertedImage(getApplicationContext());
                    Log.d(Constants.TAG, "croppedImageID = " + croppedImageID);

                    mRowsDeleted = deleteImageFromMediaStore(croppedImageID);
                    Log.d(Constants.TAG, "mRowsDeleted = " + mRowsDeleted);
                }
                break;
        }
    }

    private void doCrop() {
        final ArrayList<CropOption> cropOptions = new ArrayList<CropOption>();

        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setType("image/*");

        List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);

        int size = list.size();

        if (size == 0) {
            Toast.makeText(this, "Can not find image crop app", Toast.LENGTH_SHORT).show();

            return;
        } else {

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mImageCaptureUri);
                mImageToBeCroppedUri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "profilePic", "Created by Love Tracker"));

            } catch (IOException ex) {

            }

            intent.setData(mImageToBeCroppedUri);
            Log.d(Constants.TAG, "mImageCaptureUri = " + mImageToBeCroppedUri.toString());

            intent.putExtra("outputX", 256);
            intent.putExtra("outputY", 256);
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("scale", true);
            intent.putExtra("return-data", true);

            if (size == 1) {
                Intent i = new Intent(intent);
                ResolveInfo res = list.get(0);
                i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
                startActivityForResult(i, CROP_FROM_CAMERA);
            } else {
                for (ResolveInfo res : list) {
                    final CropOption co = new CropOption();

                    co.title = getPackageManager().getApplicationLabel(res.activityInfo.applicationInfo);
                    co.icon = getPackageManager().getApplicationIcon(res.activityInfo.applicationInfo);
                    co.appIntent = new Intent(intent);

                    co.appIntent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));

                    cropOptions.add(co);
                }

                CropOptionAdapter adapter = new CropOptionAdapter(getApplicationContext(), cropOptions);

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Choose Crop App");
                builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        startActivityForResult(cropOptions.get(item).appIntent, CROP_FROM_CAMERA);
                    }
                });

                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {

                        if (mImageCaptureUri != null) {
                            getContentResolver().delete(mImageCaptureUri, null, null);
                            mImageCaptureUri = null;
                        }
                    }
                });

                AlertDialog alert = builder.create();

                alert.show();
            }
        }
    }

    private Boolean SaveImageToExternalStorage(Bitmap finalBitmap) {
        File file = new File(getExternalFilesDir(null), "profile.jpg");

        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return file.exists();
    }


    private String getIdOfLastInsertedImage(Context context) {
        String id;
        String[] proj = {MediaStore.Images.Media._ID};

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, proj, null, null, MediaStore.MediaColumns.DATE_ADDED + " DESC");
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(proj[0]);
        id = cursor.getString(columnIndex);
        cursor.close();
        return id;
    }


    private int deleteImageFromMediaStore(String id) {
        // Defines selection criteria for the rows you want to delete
        String mSelectionClause = BaseColumns._ID + "=?";
        String[] mSelectionArgs = {id};

        // Defines a variable to contain the number of rows deleted
        int mRowsDeleted = getContentResolver().delete(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,    // the user dictionary content URI
                mSelectionClause,                                // the column to select on
                mSelectionArgs                                   // the value to compare to
        );
        return mRowsDeleted;
    }
}
