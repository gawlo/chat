package org.tigase.messenger.phone.pro.roster;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.tigase.messenger.phone.pro.MainActivity;
import org.tigase.messenger.phone.pro.R;
import org.tigase.messenger.phone.pro.roster.contact.LoadContacts;

public class ContactFilterFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
       // return super.onCreateView(inflater, container, savedInstanceState);
        LoadContactFilter();
        return inflater.inflate(R.layout.fragment_contact_liste,container,false);
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static Fragment newInstance(MainActivity.XMPPServiceConnection mServiceConnection) {
        ContactFilterFragment fragment = new ContactFilterFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
       // LoadContactFilter();
        return fragment;
    }

    public void LoadContactFilter(){
        String sortbyname = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME;
        Cursor cursorphone = getContext().getContentResolver().
                query(ContactsContract.Contacts.CONTENT_URI, null, null, null, sortbyname);
        if(cursorphone.getCount()>0){
            Log.i("COUNT","NBR : "+cursorphone.getCount());
            MatrixCursor cursor = new MatrixCursor(new String[] {"id", "name", "phonenumber"});
            while(cursorphone.moveToNext()){

               String idcontactcall= cursorphone.getString(cursorphone.getColumnIndex(ContactsContract.Contacts._ID));
               String name = cursorphone.getString(cursorphone.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
               String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?";
                Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
               Cursor cursorphonenb = getContext().getContentResolver().query(uri,null,selection,new String[]{idcontactcall},null);
               if(cursorphonenb.moveToFirst()) {
                String number = cursorphonenb.getString(cursorphonenb.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                   cursor.newRow()
                           .add("id", idcontactcall)
                           .add("name", name)
                           .add("phonenumber", number);
                   if(cursor.moveToFirst()){
                   String testnumber = cursor.getString(cursor.getColumnIndex("phonenumber"));
                   Log.i("PPP", "NUMBER : " + testnumber);}
               }
              // Cursor cursorphonenumber =
            }
        }

    }
}
