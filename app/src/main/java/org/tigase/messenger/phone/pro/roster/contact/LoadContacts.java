package org.tigase.messenger.phone.pro.roster.contact;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

//import androidx.annotation.NonNull;
import android.support.annotation.*;
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AppCompatActivity;
import android.support.v7.app.AppCompatActivity;
//import androidx.core.app.ActivityCompat;
import android.support.v4.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
import android.support.v4.content.ContextCompat;
//import androidx.recyclerview.widget.LinearLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
import android.support.v7.widget.RecyclerView;

import org.tigase.messenger.phone.pro.R;
import org.tigase.messenger.phone.pro.account.ContactModel;
import org.tigase.messenger.phone.pro.db.DatabaseContract;
import org.tigase.messenger.phone.pro.providers.RosterProvider;

import java.util.ArrayList;

public class LoadContacts extends AppCompatActivity {
    RecyclerView recyclerView;
    ArrayList<ContactModel> arrayList = new ArrayList<ContactModel>();
    MainAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_contact);
        recyclerView = findViewById(R.id.recycler_view);
        checkPermission();
    }

    private void checkPermission() {
        if(ContextCompat.checkSelfPermission(LoadContacts.this, Manifest.permission.READ_CONTACTS)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(LoadContacts.this
                    ,new String[]{Manifest.permission.READ_CONTACTS},100);
        } else{
            getContactList();
        }

    }

    private void getContactList() {
        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        //String sort = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+"ASC";
        String sort = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME;
        Cursor cursor = getContentResolver().query(uri, null, null, null, sort);

        String[] columnsToReturn = new String[]{DatabaseContract.RosterItemsCache.FIELD_ID,
                DatabaseContract.RosterItemsCache.FIELD_ACCOUNT,
                DatabaseContract.RosterItemsCache.FIELD_JID,
                DatabaseContract.RosterItemsCache.FIELD_NAME,
                DatabaseContract.RosterItemsCache.FIELD_STATUS};

            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    Uri uriphone = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
                    String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " =?";
                    Cursor phoneCursor = getContentResolver().query(uriphone, null, selection, new String[]{id}, null);

                    if (phoneCursor.moveToFirst()&& phoneCursor!=null) {
                        String number = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            ContactModel model = new ContactModel();
                            model.setName(name);
                            model.setNumber(number);
                            arrayList.add(model);
                        phoneCursor.close();
                    }
                }
                cursor.close();
            }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MainAdapter(this,arrayList);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==100 && grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
            getContactList();
        } else {
            Toast.makeText(LoadContacts.this,"Permission denied",Toast.LENGTH_SHORT).show();
            checkPermission();
        }

    }
}
