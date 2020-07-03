package com.icandothisallday2020.ex74bluetooth;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class BTListActivity extends AppCompatActivity {
    ListView listView;
    ArrayList<String> deviceList=new ArrayList<>();
    ArrayAdapter adapter;
    BluetoothAdapter bluetoothAdapter;
    Set<BluetoothDevice> devices;//set dataFramework 의 특징:중복된 값을 허용 하지않고 저장
    DiscoveryResultReceiver discoveryResultReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Toast.makeText(this, "onCreate", Toast.LENGTH_SHORT).show();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btlist);
        listView=findViewById(R.id.listview);
        adapter=new ArrayAdapter(this,android.R.layout.simple_list_item_1,deviceList);
        listView.setAdapter(adapter);
        //블루투스 아답터 소환
        bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        //이미 페어링(인식) 되어있는 디바이스들을 리스트에 추가
        devices=bluetoothAdapter.getBondedDevices();
        for(BluetoothDevice device: devices){
            String name=device.getName();
            String address=device.getAddress();

            deviceList.add(name+"\n"+address);
        }
        //새로운 장치를 찾은 결과를 운영체제에서 Broadcast 함
        //그러므로 이를 듣기 위해서 BroadcastReceiver 필요
        //※블루투스의 장치검색결과는 (자바언어에서 등록한)동적 리시버만 가능
        //              ---Manifest 에 등록하는 것이아니라 자바에서 등록
        discoveryResultReceiver=new DiscoveryResultReceiver();

        //필터1:장치를 찾았다는 방송을 듣는 필터
        IntentFilter filter=new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);//블루투스 장치를 찾았다는 방송을 필터링(선택적 수용)
        registerReceiver(discoveryResultReceiver,filter);//(동적 등록)

        //필터2:탐색이 종료된 것을 듣는 필터
        IntentFilter filter2=new IntentFilter();
        filter2.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(discoveryResultReceiver,filter2);
        bluetoothAdapter.startDiscovery();//탐색 시작

        //다이얼로그 스타일 액티비티-아웃사이드를 터치했을때 cancel 되지 않도록
        setFinishOnTouchOutside(false);

        //리스트뷰에서 원하는 아이템을 클릭했을때
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //아이템 선택시 탐색중지
                bluetoothAdapter.cancelDiscovery();

                String s=deviceList.get(position);
                //s 문자열에 저장된 name 과 Mac 주소 분리(주소만 사용)
                String[] ss=s.split("\n");
                String address=ss[1];
                //얻어온 address 를 액티비티를 실행했던 ClientActivity 에 전달
                //이 액티비티를 실행했던 인텐트(택배기사) 소환
                Intent intent=getIntent();
                //인텐트에게 가지고 돌아갈 데이터 추가
                intent.putExtra("Address",address);
                //이게 액티비티의 결과다!!!!!!!!!!
                setResult(RESULT_OK,intent);//데이터를 가지고 돌아감
                finish();//액티비티 종료
            }
        });
    }

    //inner class 로 Receiver 등록
    class DiscoveryResultReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            if(action.equals(BluetoothDevice.ACTION_FOUND)){

                //장치를 찾았을 때
                BluetoothDevice device=intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //기본자료형을 제외하고 다른 문서에서 입력된 다른 자료형 데이터를 알아들으려면 getParcelableExtra() 사용
//                devices.add(device);//장치들을 중복되지 않게 가지고 있는 Set 객체
//                boolean isAdded=devices.add(device);//중복된 디바이스가 없다면 true/중복된 데이터가 있다면 false 값 저장
//                if(isAdded)//새로운 장치일 경우
                    deviceList.add(device.getName()+"\n"+device.getAddress());//리스트뷰에 보여줄 데이터에 추가
                adapter.notifyDataSetChanged();//아답터에게 알려줘서 리스트에 뜨도록

            }else if(action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED))
                Toast.makeText(context, "블루투스 탐색 완료", Toast.LENGTH_SHORT).show();
        }//onReceive
    }//DRR class

    //액티비티가 화면에서 안보일때 리시버 등록 해제
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(discoveryResultReceiver);
    }
}
