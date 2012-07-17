package ru.fizteh.fivt.yanykin.wordnote;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;

public class MainMenuActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
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
