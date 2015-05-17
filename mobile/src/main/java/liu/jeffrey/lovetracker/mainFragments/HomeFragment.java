package liu.jeffrey.lovetracker.mainFragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import liu.jeffrey.lovetracker.BuzzService;
import liu.jeffrey.lovetracker.Constants;
import liu.jeffrey.lovetracker.MainMapActivity;
import liu.jeffrey.lovetracker.R;
import liu.jeffrey.lovetracker.adapter.Contact;
import liu.jeffrey.lovetracker.adapter.ContactAdapter;
import liu.jeffrey.lovetracker.adapter.DividerItemDecoration;
import liu.jeffrey.lovetracker.adapter.animators.ScaleInLeftAnimator;
import liu.jeffrey.lovetracker.db.DbHelper;

public class HomeFragment extends Fragment {

    private RecyclerView contactRecycler;
    private ContactAdapter contactAdapter;

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        contactRecycler = (RecyclerView) rootView.findViewById(R.id.friendRecyclerView);
        //contactRecycler.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        contactRecycler.setLayoutManager(layoutManager);
        contactAdapter = new ContactAdapter();
        contactRecycler.setAdapter(contactAdapter);

        RecyclerView.ItemDecoration itemDecoration =
                new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST);

        contactRecycler.addItemDecoration(itemDecoration);

        contactRecycler.setItemAnimator(new ScaleInLeftAnimator());

        contactAdapter.SetOnItemClickListener(new ContactAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                TextView _id = (TextView) v.findViewById(R.id._idTextView);
                Log.d(Constants.TAG, "onItemClick " + position + " " + v.getId() + " " + v.getTag() + _id.getText().toString());

                Intent intent = new Intent();
                intent.putExtra(Constants.USER_ID, Integer.parseInt(_id.getText().toString()));
                intent.setComponent(new ComponentName(getActivity(), MainMapActivity.class));
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(View v, int position) {
                TextView _id = (TextView) v.findViewById(R.id._idTextView);
                Log.d(Constants.TAG, "onItemLongClick " + position + " " + v.getId() + " " + v.getTag() + _id.getText().toString());

                final int pos = position;
                new AlertDialog.Builder(getActivity())
                        .setTitle("Delete Contact")
                        .setMessage("Are you sure you want to delete?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                contactAdapter.removeItem(pos);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_menu_delete)
                        .show();
            }
        });

        reloadList();

        return rootView;
    }

    public void reloadList() {
        // Load xml data in a non-ui thread
        new LoadContactTask().execute();
    }

    private class LoadContactTask extends AsyncTask<String, Void, Cursor> {
        @Override
        protected Cursor doInBackground(String... args) {
            // query the database and return a cursor of friend.
            DbHelper friendDatabase = new DbHelper(getActivity());
            return friendDatabase.getAllContactCursor();
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            contactAdapter.removeAll(); // add this just in case NFC add friend and there is 2 copies
            if (cursor != null && cursor.getCount() > 0) {
                for (int count = 0; count < cursor.getCount(); count++) {
                    contactAdapter.addItem(0, new Contact(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getBlob(3)));
                    if (cursor.moveToNext()) {
//                        Log.d(Constants.TAG, "count: " + cursor.getCount());
//                        Log.d(Constants.TAG, "0 " + cursor.getString(0));
//                        Log.d(Constants.TAG, "1 " + cursor.getString(1));
                    }
                }
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_friend_list, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings_add_friend) {
            final String[] items = new String[]{"NFC", "Registration ID", "testing"};
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.select_dialog_item, items);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle("Add a contact using");
            builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) { //pick from camera
                    if (item == 0) {
                        Toast.makeText(getActivity(), "Just make sure NFC is on and bump the devices!", Toast.LENGTH_SHORT).show();
                        NfcAdapter nfcAdpt = NfcAdapter.getDefaultAdapter(getActivity());
                        if (nfcAdpt != null) {
                            if (nfcAdpt.isEnabled()) {
                                //Nfc settings are enabled
                            } else {
                                //Nfc Settings are not enabled
                                if (android.os.Build.VERSION.SDK_INT >= 16) {
                                    startActivity(new Intent(android.provider.Settings.ACTION_NFC_SETTINGS));
                                } else {
                                    startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                                }
                            }
                        }
                    } else if (item == 1) { //pick from file
                        //Using regid
                        createBoxRegidBox();
                    } else {
                        //testing, fake
                        contactAdapter.addItem(0, new Contact(-1, "fake", "fake regid", null));
                    }

                }
            });
            AlertDialog alert = builder.create();
            alert.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Called when the user clicks the Send/edit msg button
     */
    public void createBoxRegidBox() {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

        alert.setTitle("Add a contact");
        alert.setMessage("Paste registration ID here:");

        // Set an EditText view to get user input
        final EditText input = new EditText(getActivity());
        alert.setView(input);

        alert.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Intent service = new Intent();
                service.putExtra(Constants.TARGET_REGISTRATION_ID, input.getText().toString());
                service.putExtra(Constants.DO_ADD_NEW_USER_WITH_REGID, Boolean.TRUE);
                service.setComponent(new ComponentName(getActivity(), BuzzService.class));
                getActivity().startService(service);
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();

        //auto focus keyboard
        input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    //auto paste whatever was in clipboard
                    ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);

                    if (clipboard.hasPrimaryClip()) {
                        ClipData clip = clipboard.getPrimaryClip();

                        // if you need text data only, use:
                        if (clip.getDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                            // WARNING: The item could contain URI that points to the text data.
                            // In this case the getText() returns null and this code fails!
                            input.setText(clip.getItemAt(0).getText().toString());
                        }
                        // or you may coerce the data to the text representation:
                        //textToPaste = clip.getItemAt(0).coerceToText(getActivity()).toString();
                    }
                }
            }
        });
    }
}
