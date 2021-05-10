package org.tigase.messenger.phone.pro.account;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class GetNumberCall {

    // Need to ask user for permission: android.permission.READ_PHONE_STATE
   /* @SuppressLint("MissingPermission")
    private void getPhoneNumbers() {
        try {
            TelephonyManager manager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);

            String phoneNumber1 = manager.getLine1Number();

         //   this.editTextPhoneNumbers.setText(phoneNumber1);

            //
          *//*  Log.i(TAG,"Your Phone Number: " + phoneNumber1);
            Toast.makeText(this,"Your Phone Number: " + phoneNumber1,
                    Toast.LENGTH_LONG).show();*//*

            // Other Informations:
            if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) { // API Level 26.
                String imei = manager.getImei();
                int phoneCount = manager.getPhoneCount();

               *//* Log.i(LOG_TAG,"Phone Count: " + phoneCount);
                Log.i(LOG_TAG,"EMEI: " + imei);*//*
            }
            if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) { // API Level 28.
                SignalStrength signalStrength = manager.getSignalStrength();
                int level = signalStrength.getLevel();

                //Log.i(LOG_TAG,"Signal Strength Level: " + level);
            }

        } catch (Exception ex) {

            ex.printStackTrace();
        }
    }

*/
    }
