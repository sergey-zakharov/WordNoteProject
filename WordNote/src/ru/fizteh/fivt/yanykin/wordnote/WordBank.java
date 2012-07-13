package ru.fizteh.fivt.yanykin.wordnote;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.util.Pair;

//фиктивный класс, содержащий различные слова
public class WordBank {
	//список пар "слово" - "перевод"
	private List<Pair<String, String>> wordList;
	//номер последнего извлечённого слова
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
	public Pair<String, String> getRandomPair() {
		int nextItemIndex = -1;
		if (wordList.size() == 0) {
			return null;
		} else if (wordList.size() == 1) {
			return wordList.get(0);
		} else {
			Random r = new Random();
			while (nextItemIndex == lastSelectedIndex) {
				nextItemIndex = r.nextInt(wordList.size());
			}
		}
		return wordList.get(nextItemIndex);
	}
}
