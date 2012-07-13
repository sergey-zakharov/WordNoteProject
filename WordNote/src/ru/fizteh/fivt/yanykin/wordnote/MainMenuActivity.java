package ru.fizteh.fivt.yanykin.wordnote;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.View;

public class MainMenuActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);
        return true;
    }
    
    //��������� ���� � �������������� ����
    public void startSession(View v) {
    	//������ ������ (������), ����� ��������� ����� ������������
    	Intent intent = new Intent(this, SessionActivity.class);
    	//��������� ������������
    	startActivity(intent);
    }
    
    //��������� ������ ����������
    public void closeApplication(View v) {
    	//������ ���������� ����
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle("�� �������, ��� ������ ����� �� ���������?");
    	builder.setPositiveButton("��", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				MainMenuActivity.this.finish();
			}
    		
    	});
    	
    	builder.setNegativeButton("���", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
    	AlertDialog alert = builder.create();
    	alert.show();
    }
   
}
