package com.example.firebase;

import com.google.firebase.database.FirebaseDatabase;

public class MyFirebaseApp extends android.app.Application{

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        //para usar esta clase con prioridades tenemos que ir a manifest/androidmanifest y declararla asi
        //<application
        //android:name=".MyFirebaseApp" <-------
        //android:allowBackup="true"
        //android:icon="@mipmap/ic_launcher"
        //android:label="@string/app_name"
        //android:roundIcon="@mipmap/ic_launcher_round"
        //android:supportsRtl="true"
        //android:theme="@style/Theme.Firebase">
    }
}
