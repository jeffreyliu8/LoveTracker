package liu.jeffrey.lovetracker.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import liu.jeffrey.lovetracker.CommonUtils;
import liu.jeffrey.lovetracker.R;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    public OnItemClickListener mItemClickListener;
    List<Contact> contactDataSet;

    public ContactAdapter() {
        contactDataSet = new ArrayList<Contact>();
    }

    @Override
    public ContactViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.contact_item, viewGroup, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ContactViewHolder itemViewHolder, int i) {
        //Log.d(Constants.TAG, "onBindViewHolder");
        Contact contact = contactDataSet.get(i);
        itemViewHolder.nameTextView.setText(contact.getName());
        itemViewHolder.textTextView.setText(contact.getRid());
        itemViewHolder.timeTextView.setText(Integer.toString(contact.getID()));
        itemViewHolder._idTextView.setText(Integer.toString(contact.getID()));

        byte[] bitmapdata = contact.getPicture();
        //Log.d(Constants.TAG,bytesToHex(bitmapdata));
        if (bitmapdata==null) {
            itemViewHolder.picImageView.setImageResource(R.drawable.ic_launcher);
        } else {
            Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapdata, 0, bitmapdata.length);
            itemViewHolder.picImageView.setImageBitmap(CommonUtils.getCircleBitmap(bitmap));
        }
    }

//    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
//    public static String bytesToHex(byte[] bytes) {
//        if(bytes==null)
//            return "";
//        char[] hexChars = new char[bytes.length * 2];
//        for ( int j = 0; j < bytes.length; j++ ) {
//            int v = bytes[j] & 0xFF;
//            hexChars[j * 2] = hexArray[v >>> 4];
//            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
//        }
//        return new String(hexChars);
//    }

    @Override
    public int getItemCount() {
        return contactDataSet.size();
    }

    public void addItem(int position, Contact data) {
        contactDataSet.add(position, data);
        notifyItemInserted(position);
    }

    public void removeItem(int position) {
        contactDataSet.remove(position);
        notifyItemRemoved(position);
    }

    public void removeAll() {
        contactDataSet.clear();
        notifyDataSetChanged();
    }


    public class ContactViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        protected TextView nameTextView;
        protected TextView textTextView;
        protected TextView timeTextView;
        protected TextView _idTextView;
        protected ImageView picImageView;

        public ContactViewHolder(View itemView) {
            super(itemView);
            picImageView = (ImageView) itemView.findViewById(R.id.profilePictureListImageView);
            nameTextView = (TextView) itemView.findViewById(R.id.nameTextView);
            textTextView = (TextView) itemView.findViewById(R.id.textTextView);
            timeTextView = (TextView) itemView.findViewById(R.id.timeTextView);
            _idTextView = (TextView) itemView.findViewById(R.id._idTextView);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(view, getPosition());
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemLongClick(view, getPosition());
                return true;
            }
            return false;
        }
    }

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
        public void onItemLongClick(View view, int position);
    }

    // for both short and long click
    public void SetOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }
}