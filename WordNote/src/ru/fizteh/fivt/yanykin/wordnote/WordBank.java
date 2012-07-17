package ru.fizteh.fivt.yanykin.wordnote;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.content.res.Resources;
import android.util.Pair;

/* Фиктивный класс, содержащий различные слова */
public class WordBank {
	/* список пар "слово" - "перевод" */
	private List<Pair<String, String>> wordList;
	/* номер последнего извлечённого слова */
	private int lastSelectedIndex;
	
	public WordBank(String pathToFile) throws IOException {
		wordList = new ArrayList<Pair<String,String>>();
		lastSelectedIndex = -1;
		//загружаем слова из внешнего источника
		BufferedReader reader = new BufferedReader(new FileReader(pathToFile));
		String str = null;
		while ((str = reader.readLine()) != null) {
			String scannedWords[] = str.split(" ");
			if (scannedWords.length >= 2) {
				wordList.add(new Pair<String, String>(scannedWords[0], scannedWords[1]));
			}
		}
	}
	
	/* Конструктор будет выгружать слова из XML-файла ресурсов */
	public WordBank(Context context) {
		wordList = new ArrayList<Pair<String,String>>();
		lastSelectedIndex = -1;
		
		Resources res = context.getResources();
		/* Используем файл words.xml */
		String scannedWordPairs[] = res.getStringArray(R.array.wordlist);
		for (String wordPair : scannedWordPairs) {
			String scannedWords[] = wordPair.split(" ");
			if (scannedWords.length >= 2) {
				wordList.add(new Pair<String, String>(scannedWords[0], scannedWords[1]));
			}
		}
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
