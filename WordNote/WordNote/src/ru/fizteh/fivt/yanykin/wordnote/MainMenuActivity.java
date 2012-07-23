package ru.fizteh.fivt.yanykin.wordnote;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainMenuActivity extends Activity implements OnClickListener{
	
	Button startVariantsModeButton, editWordListButton, mainConfButton, aboutButton, exitButton; 
	final String LOG_TAG = "myLogs"; 
	
	public MainMenuActivity(){
		super();
		Log.d(LOG_TAG, "MainMenuActivity constructed");
		
	}
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        
        startVariantsModeButton = (Button) findViewById(R.id.runSessionButton);
        editWordListButton = (Button) findViewById(R.id.editWordListButton);
        mainConfButton = (Button) findViewById(R.id.configurationsButton);
        aboutButton = (Button) findViewById(R.id.aboutButton);
        exitButton = (Button) findViewById(R.id.exitButton);
        
        startVariantsModeButton.setOnClickListener(this);
        editWordListButton.setOnClickListener(this);
        mainConfButton.setOnClickListener(this);
        aboutButton.setOnClickListener(this);
        exitButton.setOnClickListener(this);
        
    }

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		
			case R.id.runSessionButton:
				startVariantsModeSession(v);
				break;
			case R.id.editWordListButton:
				//TODO startEditionMode(v);
				break;
			case R.id.configurationsButton:
				openPreferencesList(v);
				break;
			case R.id.aboutButton:
				//TODO showAboutActivity(v);
				break;
			case R.id.exitButton:
				closeApplication(v);
				break;
		
		}
		
	}


    //открывает окно с пролистыванием слов
    public void startSession(View v) {
    	//создаём интент (запрос), чтобы запустить новую деятельность
    	Intent intent = new Intent(this, SessionActivity.class);
    	//запускаем деятельность
    	startActivity(intent);
    }
    
    /* Запускает режим с вариантами ответа */
    public void startVariantsModeSession(View v) {
    	Intent intent = new Intent(this, VariantsModeSession.class);
    	startActivity(intent);
    }
    
    /* Открывает окно настроек */
    public void openPreferencesList(View v) {
    	Intent intent = new Intent(this, Preferences.class);
    	startActivity(intent);
    }
    
    //завершает работу приложения
    public void closeApplication(View v) {
    	//создаём диалоговое окно
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle("Вы уверены, что хотите выйти из программы?");
    	builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				MainMenuActivity.this.finish();
			}
    		
    	});
    	
    	builder.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
    	AlertDialog alert = builder.create();
    	alert.show();
    }

	
	
	
}
