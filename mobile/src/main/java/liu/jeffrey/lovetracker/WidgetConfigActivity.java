package liu.jeffrey.lovetracker;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import liu.jeffrey.lovetracker.adapter.Contact;
import liu.jeffrey.lovetracker.adapter.ContactAdapter;
import liu.jeffrey.lovetracker.adapter.DividerItemDecoration;
import liu.jeffrey.lovetracker.db.DbHelper;

public class WidgetConfigActivity extends Activity {

    private int mAppWidgetId;

    private RecyclerView mRecyclerView;
    private ContactAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if they press the back button.
        setResult(RESULT_CANCELED);

        // Set the view layout resource to use.
        setContentView(R.layout.activity_widget_config);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If they gave us an intent without the widget id, just bail.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            Log.d(Constants.TAG, "bailing");
            finish();
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.widget_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter
        mAdapter = new ContactAdapter();
        mRecyclerView.setAdapter(mAdapter);

        RecyclerView.ItemDecoration itemDecoration =
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST);
        mRecyclerView.addItemDecoration(itemDecoration);

        // query the database and return a cursor of friend.
        DbHelper friendDatabase = new DbHelper(this);
        Cursor cursor = friendDatabase.getAllContactCursor();
        if (cursor != null && cursor.getCount() > 0) {
            for (int count = 0; count < cursor.getCount(); count++) {
                mAdapter.addItem(0, new Contact(cursor.getInt(0),cursor.getString(1), cursor.getString(2), cursor.getBlob(3)));
                cursor.moveToNext();
            }
            cursor.close();
        }

        mAdapter.SetOnItemClickListener(new ContactAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                TextView regid = (TextView) v.findViewById(R.id.textTextView);

                DbHelper friendDatabase = new DbHelper(getApplicationContext());
                int numberOfRowsUpdated = friendDatabase.saveWidgetID(regid.getText().toString(), mAppWidgetId);
                if (numberOfRowsUpdated == 1) {
                    //one row is updated, so we can let user add this widget

                    // Push widget update to surface with newly set prefix
                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
                    WidgetProvider.updateAppWidget(getApplicationContext(), appWidgetManager, mAppWidgetId);

                    // Make sure we pass back the original appWidgetId
                    Intent resultValue = new Intent();
                    resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                    setResult(RESULT_OK, resultValue);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "This person already existed.",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onItemLongClick(View v, int position) {
                Log.d(Constants.TAG, "onItemLongClick in widget");
            }
        });
    }
}
