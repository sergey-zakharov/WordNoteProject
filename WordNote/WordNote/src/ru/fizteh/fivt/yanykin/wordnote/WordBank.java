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
	public WordBank(Context context) {
		wordList = new ArrayList<Pair<String, String>>();
		lastSelectedIndex = -1;

		// открываем соединение с базой данных, если она не создана, создаем и
		// загружаем в нее слова из внешнего источника
		dbHelper = new WBDBHelper(context, wordList);

		SQLiteDatabase db = dbHelper.getReadableDatabase();// иногда возникают
															// накладки

		dbHelper.readWordsFromDB(db);
		db.close();// не тратим дорогие ресурсы

	}

	/*
	 * Возвращает случайный индекс слова такой, чтобы оно не повторялось с ранее
	 * последним выбранным значением. Если в массиве ровно одно слово, то
	 * возвращается всегда 0. Если слов вообще нет - возвращается -1
	 */
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

	/*
	 * Получить несколько неповторяющихся пар слов (первое из них НЕ совпадает с
	 * последним выбранным словом)
	 */
	public ArrayList<Pair<String, String>> getSomeWords(int numberOfVariants) {
		/* Если вариантов неположительное число или же слов в словаре вообще нет */
		if (numberOfVariants <= 0 || wordList.size() <= 0) {
			return null;
		}
		/* Если слов в словаре не хватает */
		if (numberOfVariants > wordList.size()) {
			return null;
		}
		ArrayList<Pair<String, String>> pairsList = new ArrayList<Pair<String, String>>();
		/*
		 * Получаем номер первой пары, которая не должна совпадать с ранее
		 * выбранным словом
		 */
		int index = getRandomIndex();
		/* Добавляем выбранный элемент */
		pairsList.add(wordList.get(index));
		/* Запоминаем индекс */
		lastSelectedIndex = index;
		/*
		 * Создаём список из номеров от 0 до wordList.size() - 1 исключая номер
		 * index
		 */
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

class WBDBHelper extends SQLiteOpenHelper {

	private Context _context;
	private List<Pair<String, String>> _wordList;
	private String _pathToFile;
	final String LOG_TAG = "myLogs";
	final public static String MAIN_TABLE_NAME = "wordnotetable";
	final public static String CAT_TABLE_NAME = "wordnotecattable";
	final public static String ENG_WORD_COLUMN_NAME = "eng_word";
	final public static String RUS_WORD_COLUMN_NAME = "rus_transl";
	final public static String CATEGORY_ID_COLUMN_NAME = "category_id";
	
	final static int DBVersion = 8;

	public WBDBHelper(Context context) {
		super(context, "WBDatabase", null/* Cursor factory */, DBVersion/* version */);
		_context = context; 
		Log.d(LOG_TAG, "simple WBDBHelper constructor worked");
	}

	public WBDBHelper(Context context, List<Pair<String, String>> wordList) {
		super(context, "WBDatabase", null/* Cursor factory */, DBVersion/* version */);
		_wordList = wordList;
		_context = context;
		Log.d(LOG_TAG, "WBDBHelper constructor worked");
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// создается база данных sql запросом
		// столбцы: id, слово на английском, его перевод на русском, категория
		try {
			Log.d(LOG_TAG, "creating table: " + MAIN_TABLE_NAME);
			db.execSQL("create table " + MAIN_TABLE_NAME + " ("
					+ "_id integer primary key autoincrement,"
					+ "eng_word text," + "rus_transl text,"
					+ "category_id integer" + ");");
		} catch (SQLException e) {
			Toast.makeText(_context,
					"Database error: " + MAIN_TABLE_NAME + "not created",
					Toast.LENGTH_LONG).show();
			return;
		}

		try {
			Log.d(LOG_TAG, "creating table: " + CAT_TABLE_NAME);
			db.execSQL("create table " + CAT_TABLE_NAME + " ("
					+ "_id integer primary key autoincrement," + "name text,"
					+ "selected text" + ");");
		} catch (SQLException e) {
			Toast.makeText(_context,
					"Database error: " + CAT_TABLE_NAME + "not created",
					Toast.LENGTH_LONG).show();
			return;
		}

		Log.d(LOG_TAG, "database created");
		//Log.d(LOG_TAG, _context.toString());
		
		Resources res = _context.getResources();
		Log.d(LOG_TAG, "got resources");

		/* Используем файл words.xml */
		String scannedWordPairs[] = res.getStringArray(R.array.wordlist_other);
		String catName = "Others";
		fillTableWithExtractedWords(db, scannedWordPairs, catName);

		scannedWordPairs = res.getStringArray(R.array.wordlist_food);
		catName = "Food";
		fillTableWithExtractedWords(db, scannedWordPairs, catName);

	}

	private void fillTableWithExtractedWords(SQLiteDatabase db,
			String[] scannedWordPairs, String categoryName) {
		ContentValues cv = new ContentValues();
		ContentValues cvCat = new ContentValues();
		String engWord;
		String translation;

		// заполняем категории
		cvCat.put("name", categoryName);
		cvCat.put("selected", "y");// выбрана по умолчанию
		long catRowID = db.insert(CAT_TABLE_NAME, null, cvCat);

		// Перегоняем слова из файла XML в базу данных
		for (String wordPair : scannedWordPairs) {
			String scannedWords[] = wordPair.split(" ");// разъединили слова
			if (scannedWords.length >= 2) {

				// заполняем основную таблицу
				engWord = scannedWords[0];
				translation = scannedWords[1];

				cv.put("eng_word", engWord);
				cv.put("rus_transl", translation);

				/*
				 * выясняем запросом, какой id у текущей категории в предыдущей
				 * таблице
				 */
				int catID = locateCategory(db, categoryName);

				cv.put("category_id", catID);
				long rowID = db.insert(MAIN_TABLE_NAME, null, cv);

				Log.d(LOG_TAG, "word = " + engWord + ", transl = "
						+ translation);
			} else {
				Log.e(LOG_TAG, "Error occured: scannedWords.length < 2!");// TODO
																			// обработать
																			// ошибку
			}
		}
	}

	private int locateCategory(SQLiteDatabase db, String categoryName) {
		int catID = -1;
		String specCategoryQuery = "name = \'" + categoryName + "\'";//
		String[] columnsToReturn = { "_id" };
		Cursor c = db.query(CAT_TABLE_NAME, columnsToReturn, specCategoryQuery,
				null, null, null, null);

		if (c.moveToFirst()) {
			int idIndex = c.getColumnIndex("_id");
			catID = c.getInt(idIndex);// номер нужной категории
		} else {
		}// TODO если не найдено или найдено несколько
		return catID;
	}

	public void readWordsFromDB(SQLiteDatabase db) {
		// Теперь надо выбрать те записи, для которых установлены категории по
		// умолчанию

		// синонимы
		final String main_table_syn = "MT";
		final String cat_table_syn = "CT";

		final String[] columnsToReturn = { "eng_word", "rus_transl", "name",
				"selected" };

		String tableRequest = MAIN_TABLE_NAME + " as " + main_table_syn
				+ " inner join " + CAT_TABLE_NAME + " as " + cat_table_syn
				+ " on " + main_table_syn + ".category_id=" + cat_table_syn
				+ "._id";
		String whereQuery = "selected=\'y\'";// эта штука выбирает из базы то,
												// что уже помечено

		Cursor c = db.query(tableRequest, columnsToReturn, whereQuery, null,
				null, null, null);

		if (c.moveToFirst()) {

			// читаем английские и русские слова
			int engColIndex = c.getColumnIndex("eng_word");
			int rusColIndex = c.getColumnIndex("rus_transl");

			// TODO? так же можем для каждой пары отображать тематику

			do {
				_wordList.add(new Pair<String, String>(
						c.getString(engColIndex), c.getString(rusColIndex)));
			} while (c.moveToNext());
		} else {
			Toast.makeText(_context, "no words in selected categories!",
					Toast.LENGTH_LONG).show();
		}

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion != newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + MAIN_TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + CAT_TABLE_NAME);
			onCreate(db);
		} else
			Toast.makeText(_context,
					"You have the lastest version of database",
					Toast.LENGTH_LONG).show();
	}

}
