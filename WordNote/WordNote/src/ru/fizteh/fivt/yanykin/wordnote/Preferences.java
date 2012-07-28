package ru.fizteh.fivt.yanykin.wordnote;

import java.util.ArrayList;
import java.util.Iterator;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.util.Pair;

/* Класс, реализующий окно с настройками */
public class Preferences extends PreferenceActivity {

	CheckBoxListener onCheckBoxPreferenceChangelistener = new CheckBoxListener();
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		// создаем экран
		PreferenceScreen rootScreen = getPreferenceManager()
				.createPreferenceScreen(this);
		// говорим Activity, что rootScreen - корневой
		setPreferenceScreen(rootScreen);
		
		PreferenceScreen categoriesScreen = getPreferenceManager().createPreferenceScreen(this);// экран выбора категорий
		categoriesScreen.setKey("categories_list");
		categoriesScreen.setTitle("Список слов");
		categoriesScreen.setSummary("Настройка выбора категории");// TODO не хардкод
		
		
		//делаем запрос в базу на категории
		ArrayList<Pair<String, Boolean> > categoriesAndMarks = getCategories();
		
		for(Iterator i = categoriesAndMarks.iterator(); i.hasNext(); ){
			Pair<String, Boolean> next = (Pair<String, Boolean>) i.next();
			//сделать пункт с галочкой
			CheckBoxPreference chb = new CheckBoxPreference(this);
			chb.setKey(next.first);// ключ и title совпадают!
			chb.setTitle(next.first);
			chb.setChecked(next.second);
			chb.setOnPreferenceChangeListener(onCheckBoxPreferenceChangelistener);
			
			categoriesScreen.addPreference(chb);
		//	chb.setSummary("Description of checkbox 2");
		}
		rootScreen.addPreference(categoriesScreen);
		
		PreferenceScreen interfaceScreen = getPreferenceManager().createPreferenceScreen(this);
		interfaceScreen.setTitle("Интерфейс");
		interfaceScreen.setSummary("А здесь - настройки самого приложения...");
		
		rootScreen.addPreference(interfaceScreen);
        
    }
    
   
    
    private ArrayList<Pair<String, Boolean> > getCategories() {
    	WBDBHelper dbHelper = new WBDBHelper(Preferences.this);
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		
		ArrayList<Pair<String, Boolean> > list = new ArrayList<Pair<String, Boolean> >();
		String[] columns = {"name", "selected"};
		
		Cursor c = db.query(WBDBHelper.CAT_TABLE_NAME, columns, null, null, null, null, "name asc");
		
		//SharedPreferences sp;
		//sp = PreferenceManager.getDefaultSharedPreferences(this);
		
		
		if (c.moveToFirst()){
			int nameColIndex = c.getColumnIndex("name");
			int selColIndex = c.getColumnIndex("selected");
			do{
					boolean selected;
					
					String charFromBase = c.getString(selColIndex).toString();
					int compRes = charFromBase.compareTo("y");
					if( compRes == 0){ 
						selected = true;
						Log.d("myLogs", "assingning true");
					}
					else {
						selected = false; 
						Log.d("myLogs", "assingning false");
					}
					
					Log.d("myLogs", "selected = " + selected);
					Log.d("myLogs", "char from base: " + charFromBase);
					Log.d("myLogs", "string from base: \'" + c.getString(selColIndex) + "\'");
					
					/*
					 * 	07-28 01:15:11.030: D/myLogs(10113): selected = false
						07-28 01:15:11.030: D/myLogs(10113): string from base: 'y'

					 * 
					 * 
					 * 
					 * */
					
					Pair<String, Boolean> pair = new Pair(c.getString(nameColIndex), selected);
					list.add(pair);
			}while(c.moveToNext());
		}
		return list;
	}

    class CheckBoxListener implements OnPreferenceChangeListener{

    	@Override
    	public boolean onPreferenceChange(Preference preference, Object newValue) {
    		CheckBoxPreference cb = (CheckBoxPreference) preference;
    		//берем ключ, делаем запрос в базу на обновление строки с этим именем напротивоположное значение selected
    		WBDBHelper helper = new WBDBHelper(Preferences.this);
    		SQLiteDatabase db = helper.getWritableDatabase();
    		//делаем запрос на строку с таким именем
    		String[] selectionArgs = {preference.getKey().toString()};
    		Log.d("myLogs", selectionArgs.toString());
    		
    		Cursor c = db.query(WBDBHelper.CAT_TABLE_NAME, null, "name = ?", selectionArgs, null, null, null);
    		int idIndex = c.getColumnIndex("_id");
    		Log.d("myLogs", "prepare to update");
    		
    		if(c.moveToFirst()){
    			String isSelected;
    			
    			if (cb.isChecked()) isSelected = "y";
    			else isSelected = "n";
    			
	    		ContentValues cv = new ContentValues();
	    		cv.put("name", cb.getKey().toString());
	    		cv.put("_id", c.getInt(idIndex));
	    		cv.put("selected", isSelected);
	    		db.update(WBDBHelper.CAT_TABLE_NAME, cv, "_id = " + c.getInt(idIndex), null);
	    		
	    		//самостоятельно переводим в другое состояние
	    		if (cb.isChecked()){
	    			cb.setChecked(false);
	    		}
	    		else cb.setChecked(true);
	    		
	    		Log.d("myLogs", "does cb checked: " + cb.isChecked());
	    		Log.d("myLogs", "update was made");
    		}
    		
    		return false;
    	}
    	
    }
}


