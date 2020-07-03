package com.icandothisallday2020.ex74bluetooth;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.zip.DeflaterInputStream;

public class ClientActivity extends AppCompatActivity {
    TextView tv;
    BluetoothAdapter adapter;
    BluetoothSocket socket;
    DataInputStream dis;
    DataOutputStream dos;
    //블루투스 하드웨어 장치에 관한 식별자 UUID
    static final UUID BT_UUID= UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");//Mac 주소: IP 번호/ UUIP : port 번호
    ClientThread clientThread;
    //블루투스의 Mac 주소 필요(컴퓨터의 IP 주소와 같은 개념)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        //제목줄 글씨 변경
        getSupportActionBar().setTitle("CLIENT");
        tv=findViewById(R.id.tv);


        //블루투스 관리자 객체 소환
        adapter=BluetoothAdapter.getDefaultAdapter();
        if(adapter==null){//블루투스가 없을 경우
            Toast.makeText(this, "기기에 블루투스가 없음", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        //블루투스가 켜져있는지 확인
        if(adapter.isEnabled()){
            //서버 블루투스장치를 탐색 및 탐색된 결과를 리스트로 보여주는 액티비티를 만들어 실행
            discoveryBluetoothDevices();
        }else{
            //블루투스 장치 On 화면 실행
            Intent intent=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);//*****묵시적 인텐트
            startActivityForResult(intent,10);
        }
    }//onCreate

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Toast.makeText(this, ""+requestCode, Toast.LENGTH_SHORT).show();
        switch (requestCode){
            case 10:
                if (resultCode==RESULT_CANCELED){
                    Toast.makeText(this, "사용불가", Toast.LENGTH_SHORT).show();
                    finish();
                }else{
                    Toast.makeText(this, "se", Toast.LENGTH_SHORT).show();
                    //서버 블루투스 장치 탐색 및 리스트를 보는 화면 실행
                    discoveryBluetoothDevices();
                }
                break;
            case 20:
                if(resultCode==RESULT_OK){
                    //선택된 블루투스 디바이스의 Mac 주소 얻기
                    String address=data.getStringExtra("Address");
                    //선택된 Mac 주소를 이용하여 Socket 생성
                    //통신작업 별도 스레드가 하도록...
                    clientThread=new ClientThread(address);
                    clientThread.start(); //run method 발동
                }
                break;
        }
    }

    //블루투스 장치 탐색 액티비티 실행 메소드
    void discoveryBluetoothDevices(){
        Intent intent=new Intent(this,BTListActivity.class);//*****명시적 인텐트
        startActivityForResult(intent,20);
    }

    public void clickBtn(View view) {
        new Thread(){
            @Override
            public void run() {
                try {
                    dos.writeUTF("asdfasdf");
                    dos.writeInt(127);
                    dos.flush();
                } catch (IOException e) {e.printStackTrace();}
            }
        }.start();
    }

    class ClientThread extends Thread{
        String address;

        public ClientThread(String address) {
            this.address = address;
        }

        @Override
        public void run() {
            //전달받은 Mac 주소에 해당하는 BluetoothDevice 객체 얻어오기
            BluetoothDevice device=adapter.getRemoteDevice(address);
            //원격 디바이스와 소켓연결작업 수행
            try {
                socket=device.createInsecureRfcommSocketToServiceRecord(BT_UUID);
                socket.connect();//연결 시도
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv.append("서버와 연결됨");
                    }
                });
                Log.i("tag","tag1");
                //접속된 Socket 을 통해 데이터를 주고받는 무지개로드 만들기
                dis=new DataInputStream(socket.getInputStream());
                dos=new DataOutputStream(socket.getOutputStream());
                Log.i("tag","tag2");
                //Stream 을 통해 원하는 데이터 주고받기
                dos.writeUTF("안녕하세요");//UTF:한글도 깨지지않고 전송가능한 문자열 인코딩방식
                dos.writeInt(50);
                dos.flush();
//                dos.close();
                Log.i("tag","tag3");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
