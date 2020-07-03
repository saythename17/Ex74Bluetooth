package com.icandothisallday2020.ex74bluetooth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String permission=Manifest.permission.ACCESS_FINE_LOCATION;
        //Location Permission 에 대한 동적퍼미션 작업
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if(checkSelfPermission(permission)== PackageManager.PERMISSION_DENIED){
                requestPermissions(new String[]{permission},17);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 17:
                if(grantResults[0]== PackageManager.PERMISSION_DENIED){
                    Toast.makeText(this, "Client 로서 새로운 장치를 검색하는 기능 제한\n기존 페어링된 장치만 접속 가능", Toast.LENGTH_LONG).show();
                    //페어링 : 접속X 블루투스간 장치를 서로 인식하는 것
                }
                break;
        }
    }

    public void clickServer(View view) {
        Intent intent=new Intent(this,ServerActivity.class);
        startActivity(intent);

    }

    public void clickClient(View view) {
        Intent intent=new Intent(this,ClientActivity.class);
        startActivity(intent);
    }
}
