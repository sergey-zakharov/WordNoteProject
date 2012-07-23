package ru.fizteh.fivt.yanykin.wordnote;

import java.util.List;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

/* Класс, реализующий окно с настройками */
public class Preferences extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    /* Заполняет окно настроек шапками с указанным содержимым
     * (в дальнейшем каждой шапке будет сопоставлен отдельный статический класс) */
    public void onBuildHeaders(List<Header> target) {
    	/* Выгружает из указанного файла информацию о хэдерах */
    	loadHeadersFromResource(R.xml.preferences_headers, target);
    }
    
    /* Первый раздел настроек, касающийся настроек списка слов */
    public static class WordListPreferences extends PreferenceFragment {
    	@Override
    	public void onCreate(Bundle savedInstanceState) {		
    		super.onCreate(savedInstanceState);
    		/* Установка настроек по умолчанию*/
    		//TODO: написать XML-ресурс с настройками по умолчанию
    		/* Загружает содержимое раздела */
    		addPreferencesFromResource(R.xml.wordlist_preferences);
    	}
    }
    
    /* Ещё один раздел настроек, который описывает всевозможные настройки интерфейса */
    public static class UIPreferences extends PreferenceFragment {
    	@Override
    	public void onCreate(Bundle savedInstanceState) {
    		super.onCreate(savedInstanceState);
    		/* Загружает содержимое раздела */
    		addPreferencesFromResource(R.xml.ui_preferences);
    	}
    }
}
