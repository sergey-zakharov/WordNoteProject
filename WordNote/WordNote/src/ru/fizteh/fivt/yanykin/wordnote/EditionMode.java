package ru.fizteh.fivt.yanykin.wordnote;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class EditionMode extends Activity implements OnItemClickListener, OnClickListener {

	final int DIALOG_ADD = 1;
	Dialog createDialog;
	View dialogView;
	EditText dialogEditText;
	HashMap<String, Integer> id_categoryMap;
	
	public EditionMode(){
		super();
		id_categoryMap = new HashMap<String, Integer>();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edition_mode);

        Button createCategoryBut = (Button) findViewById(R.id.add_cat_button);
        createCategoryBut.setOnClickListener(this);
        
        updateCategoriesList();
    	
	}

	private void updateCategoriesList() {
		id_categoryMap.clear();
		
        ListView lvMain = (ListView) findViewById(R.id.edition_cat_list);
		ArrayList<String> categories = getCategories();
        
    	ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.my_list_item, categories);
    	lvMain.setAdapter(adapter);
    	lvMain.setOnItemClickListener(this);
	}

	//выбрана одна из категорий
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Bundle bundle = new Bundle();
		TextView tv = null;
		
		try{
			tv = (TextView) view;
		}catch (ClassCastException e){
			e.printStackTrace();
			System.exit(1);
		}
		//TODO выясняем id категории
		// id категории лучше запоминать при запросе из базы при создании activity и маппить
		Integer catId = id_categoryMap.get(tv.getText().toString());
		bundle.putInt("curCategoryId", catId);
		bundle.putString("curCategoryName", tv.getText().toString());
		
		Intent intent = new Intent(this, CategoryEdition.class);
		intent.putExtras(bundle);
    	startActivity(intent);
		
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.add_cat_button){
			showDialog(DIALOG_ADD);
		}
		
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		
		if(id == DIALOG_ADD){
			AlertDialog.Builder adb = new AlertDialog.Builder(this);
		    adb.setTitle("Создание новой категории");
		    // создаем view из dialog.xml
		    dialogView = (LinearLayout) getLayoutInflater()
		        .inflate(R.layout.dialog_add_category, null);
		    // устанавливаем ее, как содержимое тела диалога
		    adb.setView(dialogView);
		    // находим TexView для отображения кол-ва
		    dialogEditText = (EditText) dialogView.findViewById(R.id.dialod_add_cat_edit_text);
			
		    // Устанавливаем кнопки
		    adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// производится запись в одну таблицу
					Log.d("myLog", "making entry to the database");
					WBDBHelper dbHelper = new WBDBHelper(EditionMode.this);
					SQLiteDatabase db = dbHelper.getWritableDatabase();
					
					ContentValues cvCat = new ContentValues();
					String categoryName = dialogEditText.getText().toString();
					dialogEditText.setText("");
					//проверка на пустую строку
					
					if(!categoryName.isEmpty()){//иначе просто закрывается
						
						//обработка строки
						categoryName.toLowerCase();
						String firstChar = categoryName.substring(0, 1);
						categoryName = firstChar.toUpperCase() + categoryName.substring(1, categoryName.length());
						//проверка строки
						Log.d("myLogs", "New category: " + categoryName);
						
						//TODO возможно создание одноименных категорий с разными id
						cvCat.put("name", categoryName);
						cvCat.put("selected", "y");// выбрана по умолчанию
						long catRowID = db.insert(WBDBHelper.CAT_TABLE_NAME, null, cvCat);
						
						// обновить список
						updateCategoriesList();
					}
				}
				
			});
			adb.setNegativeButton("Отмена", null);
		    	
			createDialog = adb.create();
			return createDialog;
		}
		
		return super.onCreateDialog(id);
		
	}
	
	
	
	private ArrayList<String> getCategories() {
    	WBDBHelper dbHelper = new WBDBHelper(EditionMode.this);
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		
		ArrayList<String> list = new ArrayList<String>();
		String[] columns = {"_id", "name"};
		
		Log.d("myLogs", "Getting categories");
		Cursor c = null;
		
		try{//проверка на существование базы
			c = db.query(WBDBHelper.CAT_TABLE_NAME, columns, null, null, null, null, "name asc");
			
		} catch(SQLiteException e){
			//запустим создание базы данных
			dbHelper.onCreate(db);
			Log.d("myLog", "Emergency creating tables");
			c = db.query(WBDBHelper.CAT_TABLE_NAME, columns, null, null, null, null, "name asc");
		} finally{
			createListElementsAndMapId_CatName(list, c);
		}
		
		
		db.close();
		return list;
	}
	
	/**
	 * Заполняет данный список элементами, проходясь данным курсором по данным, вытянутым из таблицы
	 * 
	 * */
		private void createListElementsAndMapId_CatName(ArrayList<String> list,
				Cursor c) {
			if (c.moveToFirst()){
				int nameColIndex = c.getColumnIndex("name");
				int idColIndex = c.getColumnIndex("_id");
				do{
						list.add(c.getString(nameColIndex));
						id_categoryMap.put(c.getString(nameColIndex), c.getInt(idColIndex));
				}while(c.moveToNext());
			}
		}
}
