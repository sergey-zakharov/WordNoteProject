package ru.fizteh.fivt.yanykin.wordnote;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

/* Фиктивный класс, содержащий различные слова */
public class WordBank {
	/* список пар "слово" - "перевод" */
	private List<Pair<String, String>> wordList;
	/* номер последнего извлечённого слова */
	private int lastSelectedIndex;
	WBDBHelper dbHelper;
		
	/* Конструктор будет выгружать слова из базы данных */
	public WordBank(Context context, String[] categories) {
		wordList = new ArrayList<Pair<String,String>>();
		lastSelectedIndex = -1;
		
		//открываем соединение с базой данных, если она не создана, создаем и загружаем в нее слова из внешнего источника
		dbHelper = new WBDBHelper(context, wordList);
		
		SQLiteDatabase db = dbHelper.getReadableDatabase();//иногда возникают накладки
		
		dbHelper.readWordsFromDB(db , categories);
		db.close();//не тратим дорогие ресурсы
		
	}
	
	/* Возвращает случайный индекс слова такой, чтобы оно не повторялось с ранее
	 * последним выбранным значением. Если в массиве ровно одно слово, то возвращается всегда 0. Если
	 * слов вообще нет - возвращается -1 */
	private int getRandomIndex() {
		if (wordList.size() == 0) {
			return -1;
		} else if (wordList.size() == 1) {
			return 0;
		} else {
			Random r = new Random();
			int nextItemIndex = lastSelectedIndex;
			while (nextItemIndex == lastSelectedIndex) {
				nextItemIndex = r.nextInt(wordList.size());
			}
			return nextItemIndex;
		}
	}
	
	/* Вернуть случайное слово */
	public Pair<String, String> getRandomPair() {
		int nextItemIndex = getRandomIndex();
		if (nextItemIndex == -1) {
			return null; 
		} else {
			return wordList.get(nextItemIndex);
		}
	}
	
	/* Получить несколько неповторяющихся пар слов (первое из них НЕ совпадает с последним выбранным словом) */
	public ArrayList<Pair<String, String>> getSomeWords(int numberOfVariants) {
		/* Если вариантов неположительное число или же слов в словаре вообще нет */
		if (numberOfVariants <= 0 || wordList.size() <= 0) {
			return null;
		}
		/* Если слов в словаре не хватает */
		if (numberOfVariants > wordList.size()) {
			return null;
		}
		ArrayList<Pair<String, String>> pairsList = new ArrayList<Pair<String,String>>(); 
		/* Получаем номер первой пары, которая не должна совпадать с ранее выбранным словом */
		int index = getRandomIndex();
		/* Добавляем выбранный элемент*/
		pairsList.add(wordList.get(index));
		/* Запоминаем индекс */
		lastSelectedIndex = index;
		/* Создаём список из номеров от 0 до wordList.size() - 1 исключая номер index */
		List<Integer> numbers = new ArrayList<Integer>();
		for (int i = 0; i < wordList.size(); i++) {
			if (i != index) {
				numbers.add(i);
			}
		}
		/* Перемешиваем содержимое списка */
		Collections.shuffle(numbers);
		/* Первые numberOfVariants - 1 элементов и определят оставшиеся элементы */
		for (int i = 0; i < numberOfVariants - 1; i++) {
			pairsList.add(wordList.get(numbers.get(i)));
		}
		/* Возвращаем результат */
		return pairsList;
	}
}


class WBDBHelper extends SQLiteOpenHelper{

	private Context _context;
	private List<Pair<String, String>> _wordList;
	private String _pathToFile;
	final String LOG_TAG = "myLogs";
	final static String TABLE_NAME = "wordnotetable";
	
	
	public WBDBHelper(Context context, List<Pair<String, String>> wordList) {
		super(context, "WBDatabase", null/*Cursor factory*/, 1/*version*/);
		_wordList = wordList;
		_context = context;
		Log.d(LOG_TAG, "WBDBHelper constructor worked");
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		//создается база данных sql запросом
			//столбцы: id, слово на английском, его перевод на русском, категория
		try{
			Log.d(LOG_TAG, "creating database");
			db.execSQL("create table " + TABLE_NAME + " ("
				+ "_id integer primary key autoincrement," + "eng_word text,"
				+ "rus_transl text," + "category text" + ");");
		} catch(SQLException e){
			Toast.makeText(_context, "Database error: not created", Toast.LENGTH_LONG).show();
			return;
		}
		Log.d(LOG_TAG, "database created");
		Resources res = _context.getResources();
		Log.d(LOG_TAG, "got resources");
		
		/* Используем файл words.xml */
		String scannedWordPairs[] = res.getStringArray(R.array.wordlist);// массив строк из файла
		ContentValues cv = new ContentValues();
		String engWord;
		String translation;
		
		// Перегоняем слова из файла XML в базу данных
		for (String wordPair : scannedWordPairs) {
			String scannedWords[] = wordPair.split(" ");// разъединили слова
			if (scannedWords.length >= 2) {
				//_wordList.add(new Pair<String, String>(scannedWords[0], scannedWords[1]));
				// здесь заливаем и готовим к записи в бд
				engWord = scannedWords[0];
				translation = scannedWords[1];

				cv.put("eng_word", engWord);
				cv.put("rus_transl", translation);
				cv.put("category", "no_category");
				long rowID = db.insert(TABLE_NAME, null, cv);
				
				Log.d(LOG_TAG, "word = " + engWord + ", transl = " + translation);
			}
			else{
				Log.e(LOG_TAG, "Error occured: scannedWords.length < 2!");// TODO ошибка
			}
		}
		
	}

	public void readWordsFromDB(SQLiteDatabase db, String[] categories /*TODO здесь еще и условия на категории*/){
		// Теперь надо выбрать те записи, для которых установлены категории по умолчанию 
		
		final String[] columnsToReturn = new String[2];
		columnsToReturn[0] = "eng_word";
		columnsToReturn[1] = "rus_transl";
		
		//составляется условный запрос
		String specCategoryQuery = "category=\'no_category\'";//TODO здесь надо захардкодить категорию по умолчанию 
		
		
		for(int i = 0; i < categories.length; i++){
			specCategoryQuery.concat(" or category=\'");
			specCategoryQuery.concat(categories[i]);
			specCategoryQuery.concat("\'");
		}
		
		Cursor c = db.query(TABLE_NAME, columnsToReturn, specCategoryQuery, null, null, null, null);
		
		if (c.moveToFirst()) {

			// определяем номера столбцов по имени в выборке
			//int idColIndex = c.getColumnIndex("_id");
			int engColIndex = c.getColumnIndex("eng_word");
			int rusColIndex = c.getColumnIndex("rus_transl");

			do {
				_wordList.add(new Pair<String, String>(c.getString(engColIndex), c.getString(rusColIndex) ));
			} while (c.moveToNext());
		} else {
			Toast.makeText(_context, "no words in default category!", Toast.LENGTH_LONG).show();
		}
	}
	
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion < newVersion){
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
			onCreate(db);
		}
		else 
			Toast.makeText(_context, "You have the lastest version of database", Toast.LENGTH_LONG).show();
	}
	
}
