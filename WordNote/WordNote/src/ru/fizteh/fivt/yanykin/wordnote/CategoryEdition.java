package ru.fizteh.fivt.yanykin.wordnote;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class CategoryEdition extends Activity implements OnClickListener{

	final int DIALOG_ADD = 1;
	Dialog createDialog;
	View dialogView;
	EditText dialogEditTextEng;
	EditText dialogEditTextRus;
	int categoryId;
	String categoryName;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edition_mode);

		Bundle bundle = this.getIntent().getExtras();
		categoryId = bundle.getInt("curCategoryId");// его надо как-то узнать
		categoryName = bundle.getString("curCategoryName");
		
        TextView mainTextView = (TextView) findViewById(R.id.edition_main_tv);
        
        Button addWordButton = (Button) findViewById(R.id.add_cat_button);
        
        addWordButton.setText("Добавить новое слово");
        addWordButton.setOnClickListener(this);
        
        mainTextView.setText(categoryName);
        
        // идет запрос в базу, заполняется список
     
        
        //updateWordList();
        updateWordList(categoryName);
        
	}



	private void updateWordList(String catName) {
		
		ListView lvMain = (ListView) findViewById(R.id.edition_cat_list);
		ArrayList<Pair<String, String>> list = wordPairList(catName);
        
        TwinListAdapter adapter = new TwinListAdapter(this, 0, list);
    	lvMain.setAdapter(adapter);
	}
	
	

	private ArrayList<Pair<String, String>> wordPairList(String catName) {
		ArrayList<Pair<String, String>> list = new ArrayList<Pair<String, String>>();
		final String main_table_syn = "MT";
 		final String cat_table_syn = "CT";
    
 		final String tableRequest = WBDBHelper.MAIN_TABLE_NAME + " as " + main_table_syn
			+ " inner join " + WBDBHelper.CAT_TABLE_NAME + " as " + cat_table_syn
			+ " on " + main_table_syn + ".category_id=" + cat_table_syn
			+ "._id";
 		
 		final String[] columnsToReturn = { "eng_word", "rus_transl"};
        
 		final String whereQuery = "name = \'" + catName + "\'";
        
 		WBDBHelper dbHelper = new WBDBHelper(this);
		SQLiteDatabase db = dbHelper.getReadableDatabase();
 		
		Cursor c = db.query(tableRequest, columnsToReturn, whereQuery, null,
				null, null, null);

		if (c.moveToFirst()) {

			// читаем английские и русские слова
			int engColIndex = c.getColumnIndex("eng_word");
			int rusColIndex = c.getColumnIndex("rus_transl");
			
			do{
				list.add(new Pair<String, String>(c.getString(engColIndex), c.getString(rusColIndex)));
			} while(c.moveToNext());
		}
        db.close();
		return list;
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
		    adb.setTitle("Добавление нового слова");
		    // создаем view из dialog.xml
		    dialogView = (LinearLayout) getLayoutInflater()
		        .inflate(R.layout.dialog_add_word, null);
		    // устанавливаем ее, как содержимое тела диалога
		    adb.setView(dialogView);
		    // находим TexView для отображения кол-ва
		    dialogEditTextEng = (EditText) dialogView.findViewById(R.id.dialod_add_word_edit_text_eng);
		    dialogEditTextRus = (EditText) dialogView.findViewById(R.id.dialod_add_word_edit_text_rus);
		    
			adb.setPositiveButton("OK", null);
			adb.setNegativeButton("Отмена", null);
		    
			createDialog = adb.create();
			
			createDialog.setOnDismissListener( new OnDismissListener() {
				
				@Override
				public void onDismiss(DialogInterface dialog) {
					// TODO производится запись в две таблицы новой категории
					Log.d("myLog", "making entry to the database");
					
					WBDBHelper dbHelper = new WBDBHelper(CategoryEdition.this);
					SQLiteDatabase db = dbHelper.getWritableDatabase();
					
					ContentValues cvCat = new ContentValues();
					String engWord = dialogEditTextEng.getText().toString();
					String rusWord = dialogEditTextRus.getText().toString();
					//TODO проверка
					
					// Заполняем таблицу 1 словами
					cvCat.put(WBDBHelper.ENG_WORD_COLUMN_NAME, engWord);
					cvCat.put(WBDBHelper.RUS_WORD_COLUMN_NAME, rusWord);// выбрана по умолчанию
					cvCat.put(WBDBHelper.CATEGORY_ID_COLUMN_NAME, categoryId);
					long catRowID = db.insert(WBDBHelper.MAIN_TABLE_NAME, null, cvCat);
					
					// обновить список
					updateWordList(categoryName);
					
				}
			});
			createDialog.setOnCancelListener(new OnCancelListener() {
				
				@Override
				public void onCancel(DialogInterface dialog) {
					// ничего не делаем
				}
			});
			
			
		    return createDialog;
		}
		
		return super.onCreateDialog(id);
		
	}
	

	class TwinListAdapter extends ArrayAdapter<Pair<String, String>>{
		//TODO улучшить производительность http://habrahabr.ru/post/133575/
		
		private final Context _context;
		private final List<Pair<String, String>> _objects;
		private final int _layoutId;
		
		public TwinListAdapter(Context context, int textViewResourceId,
				List<Pair<String, String>> objects) {
			super(context, textViewResourceId, objects);
			_context = context;
			_objects = objects;
			_layoutId = R.layout.list_item_cat_edition;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent){
			LayoutInflater inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			View rowView = inflater.inflate(_layoutId, parent, false);
			
		 	TextView eng_word = (TextView) rowView.findViewById(R.id.list_item_cat_edition_first_col);
    		TextView rus_trans = (TextView) rowView.findViewById(R.id.list_item_cat_edition_second_col);
    		
    		eng_word.setText(_objects.get(position).first);
    		rus_trans.setText(_objects.get(position).second);
        	
			return rowView;
		}
		
	}


	
	
}
