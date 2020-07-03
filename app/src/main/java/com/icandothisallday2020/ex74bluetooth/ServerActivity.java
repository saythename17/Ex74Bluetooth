package com.icandothisallday2020.ex74bluetooth;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.UUID;
import java.util.zip.DeflaterInputStream;

public class ServerActivity extends AppCompatActivity {
    TextView tv;
    BluetoothAdapter adapter;
    BluetoothServerSocket serverSocket;
    BluetoothSocket socket;
    ServerThread serverThread;
    //데어터를 주고받기 위한 스트림(자료형 단위로 보낼 수 있는 stream)
    DataInputStream dis;
    DataOutputStream dos;

    //블루투스 하드웨어 장치에 대한 식별자 UUID
    static final UUID BT_UUID= UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        //제목줄 변경
        getSupportActionBar().setTitle("SERVER");
        tv=findViewById(R.id.tv);

        //블루투스 관리자 객체 소환
        adapter =BluetoothAdapter.getDefaultAdapter();//class's static method
        if(adapter ==null) {//블루투스가 없는 기기- null
            Toast.makeText(this, "Bluetooth가 없는 기기", Toast.LENGTH_SHORT).show();
            finish();//바로 액티비티 종료되는 것이 아닌 작업을 중지키는 것
            return;//액티비티 종료
        }
        //블루투스 장치가 있다면 켜져있는지 체크 및 장치를 켜도록(ON) 요청
        if (adapter.isEnabled()) {
            createServerSocket();//서버 소켓 생성 작업 실행
        }else {
            //블루투스 장치를 ON 하도록 선택하는 액티비티로 전환
            Intent intent=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent,100);
        }

    }//onCreate...

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 100:
                if(resultCode==RESULT_CANCELED){
                    Toast.makeText(this, "블루투스 허용 거부\n앱 종료", Toast.LENGTH_SHORT).show();
                    finish();
                }else{
                    //블루투스가 켜져있다면 서버 소켓 생성 작업
                    createServerSocket();
                }
                break;
            case 200:
                if(resultCode==RESULT_CANCELED){
                    Toast.makeText(this, "블루투스 탐색 거부\n이미 페어링된 기기외 다른 장치에서 이 장치를 찾을 수 없음", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    //서버소켓(소켓을 연결하는 HUB-플러그 꽂는바) 생성 메소드
    void createServerSocket(){
        //통신 작업은 별도의 Thread 가 대신 해야함.
        serverThread=new ServerThread();
        serverThread.start();

        //이 기기를 다른 장치에서 검색할 수 있도록 허용하는 액티비티 실행
        Intent intent=new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,300);//(최대)300초간 검색 허용
        startActivityForResult(intent,200);
    }

    //서버소켓 작업 및 통신을 하는 별도 Thread inner class
    class ServerThread extends Thread{
        @Override
        public void run() {//서버소켓 생성
            try {
                serverSocket=adapter.listenUsingInsecureRfcommWithServiceRecord("SERVER",BT_UUID);
                //parameter- name: 라벨(별명),
                // UUID: universally unique identifier :하드웨어안에서 블루투스를 식별하는 블루투스 장치 식별번호

                setUI("서버소켓이 생성됨\n");//별도스레드가 UI를 제어할때-runOnUiThread()-Runnable() 사용

                //클라이언트의 접속을 기다리기
                socket=serverSocket.accept();//커서가 여기서 대기
                setUI("클라이언트 접속\n");

                //접속된 Socket 을 통해 통신하기위해 무지개로드(Stream) 만들기
                dis=new DataInputStream(socket.getInputStream());
                dos=new DataOutputStream(socket.getOutputStream());
                //Stream 을 통해 원하는 데이터를 전송하거나 받기
//                String msg=dis.readUTF();//보내준 data 순서대로 받기
//                int num=dis.readInt();//보내준 data 자료형별로 전달됨
//                setUI("Client : "+msg+"~~"+num);
/////////////////////////////////////////////////////////////////////////////////////////////////////
                boolean aa= true;

                //스트림을 통해 원하는 데이터 전송하거나 받기
                while(aa){
                    String msg= dis.readUTF();
                    int num= dis.readInt();

                    setUI("클라이언트 : " + msg +" ~~ " + num+"\n");
                }
/////////////////////////////////////////////////////////////////////////////////////////////////////
                dis.close();
            } catch (IOException e) {e.printStackTrace();}
        }//run

        //UI에 Thread 로 메세지 출력하는 기능
        void setUI(final String msg){//final : 익명클래스안에서는 전역 변수(파라미터도 전역변수임)를 쓸 수 없다
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv.append(msg);
                }
            });
        }
    }//ServerThread

}
