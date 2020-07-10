package com.example.helloworld;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.example.helloworld.camera.CameraActivity;
import com.example.helloworld.kakaomap.KakaoMap;
import com.example.helloworld.traffic.TrafficActivity;

import net.daum.mf.map.api.MapView;
v
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getHashKey();
    }

    public void onClickKakaoMap(View view){
        Intent intent = new Intent(this, KakaoMap.class);
        startActivity( intent );
    }
    public void onClickCameraActivity(View view){
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity( intent );
    }
    public void onClickTrafficActivity(View view){
        Intent intent = new Intent(this, TrafficActivity.class);
        startActivity( intent );
    }

    private void getHashKey(){
        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageInfo == null)
            Log.e("KeyHash", "KeyHash:null");

        for (Signature signature : packageInfo.signatures)
            try {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            } catch (NoSuchAlgorithmException e) {
                Log.e("KeyHash", "Unable to get MessageDigest. signature=" + signature, e);
            }
    }

}