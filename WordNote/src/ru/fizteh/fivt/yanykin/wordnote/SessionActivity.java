package ru.fizteh.fivt.yanykin.wordnote;

import java.io.IOException;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Pair;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SessionActivity extends Activity {
	//надписи, содержащие слово и перевод
	TextView currentWord = null;
    TextView translationWord = null;
	//подгруженный словарь
	WordBank dictionary;
	//текущая пара слово-перевод
	Pair<String, String> currentPair;
	//показан ли сейчас перевод слова?
	boolean isTranslationShowed = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);
        currentWord = (TextView) findViewById(R.id.originalWordView);
        translationWord = (TextView) findViewById(R.id.translationWordView);
        //привязываем к кнопке обработчик
        final Button showAndHideButton = (Button)findViewById(R.id.showHideButton);
        showAndHideButton.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (isTranslationShowed) {
					showAndHideButton.setText(R.string.show_translation);
					hideTranslation();
					isTranslationShowed = false;
				} else {
					showAndHideButton.setText(R.string.hide_translation);
					showTranslation();
					isTranslationShowed = true;
				}
			}
        	
        });
        
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    	//создаём контекст
        try {
			dictionary = new WordBank(getString(R.string.path_to_dictionary));
		} catch (IOException e) {
			//Инициализируем "фабрику" диалоговых окон
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Ошибка при загрузке словаря!");
			builder.setCancelable(false);
			builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int arg1) {
					SessionActivity.this.finish();
				}
			
			});
			AlertDialog alert = builder.create();
			alert.show();
		}
        //если всё хорошо, то выбираем случайным образом пару
        //currentPair = dictionary.getRandomPair();
        nextWord(null);
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_session, menu);
        return true;
    }

    //следующее слово
    public void nextWord(View v) {
    	currentPair = dictionary.getRandomPair();
    	currentWord.setText(currentPair.first);
    	translationWord.setText(getString(R.string.stub));
    }
    
    //показать перевод
    public void showTranslation() {
    	translationWord.setText(currentPair.second);
    }
    
    //спрятать перевод
    public void hideTranslation() {
    	translationWord.setText(getString(R.string.stub));
    }
}
