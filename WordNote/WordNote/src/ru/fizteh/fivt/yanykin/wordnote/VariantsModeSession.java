package ru.fizteh.fivt.yanykin.wordnote;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
/**
 * Activity, presenting default mode of selection translation 
 * of several options 
 * 
 * */


public class VariantsModeSession extends Activity {

	/* Текущее слово */
	TextView currentWord;
	/* Кнопка для перехода к следующему слову */
	Button nextWord;
	/* Массив из кнопок, которые  обозначают варианты ответа */
	ColoredButton variantsOfTranslation[];
	/* Пространство, где эти кнопки будут находиться */
	LinearLayout variantsLayout;
	/* Количество вариантов ответа (для упрощения пока захардкожено) */
	private static final int numberOfVariants = 4;
	/* Номер правильного варианта ответа */
	int rightVariant;
	/* Используемая база слов */
	WordBank bank;
	
	/* Метка, обозначающая верное слово */
	private static final String isCorrectTag = "correct";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_variants_mode_session);
        /* Привязка элементов управления к объектам */
        currentWord = (TextView)findViewById(R.id.originalWordTextView);
        nextWord = (Button)findViewById(R.id.nextWordButton);
        variantsLayout = (LinearLayout)findViewById(R.id.variantsLayout);
        /* Читаем из настроек категории*/
        String[] selectedCategories = readCategories();
        
        /* Создаём базу слов */
        
        //создаем, в зависимости от того, какие категории были выбраны в настройках 
        bank = new WordBank(/*Context*/this);// 
        /* Создаём кнопки */
        generateButtons();
        /* Заполняем сразу словами */
        nextWord(null);
    }   
    
    private String[] readCategories() {
    	SharedPreferences sp;
    	String[] categories = {};
    	int counter = 0;
    	int checkBoxQuant = 5; // TODO здесь надо выяснять, сколько чекбоксов
    	
    	sp = PreferenceManager.getDefaultSharedPreferences(this);
    	Boolean checked;
    	
    	for (int i = 0; i < checkBoxQuant; i++) {
			checked = sp.getBoolean("category_" + (i+1), false);
			if (checked) {
				/* TODO здесь добавляем в список категории, которые были выбраны*/
				//categories[counter]/* = */;/*здесь нужно докопаться до списка категорий и сделать соответствие*/ 
			}
		}
		return categories;
	}

	/* Создание нужного количества кнопок */
    private void generateButtons() {
    	/* Для каждой кнопки необходимо указать параметры расположения внутри variantsLayout */
    	LinearLayout.LayoutParams parameters = new LinearLayout.LayoutParams(
    			LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    	parameters.gravity = Gravity.CLIP_HORIZONTAL;
    	
    	/* Создаём массив из нужного количества кнопок */
    	variantsOfTranslation = new ColoredButton[numberOfVariants];
    	
    	/* Осталось сгенерировать каждую кнопку по отдельности */
    	for (int i = 0; i < numberOfVariants; i++) {
    		variantsOfTranslation[i] = new ColoredButton(this);
    		variantsOfTranslation[i].setText("New button " + (i + 1));
    		/* Навешиваем на кнопку обработчик */
    		variantsOfTranslation[i].setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					checkWord(v);
				}
    			
    		});
    		
    		/* Добавляем кнопку */
    		variantsLayout.addView(variantsOfTranslation[i], parameters);
    	}
    }
    
    /* Выборка следующего слова (выполняется при нажатии на соответствующую кнопку) */
    public void nextWord(View v) {  	
    	/* Список из вариантов ответа */
    	ArrayList<Pair<String, String>> variants = bank.getSomeWords(numberOfVariants);
    	/* Если что-то пошло не так*/
    	if (variants == null) {
    		return;
    	}
    	/* Первый элемент полученного списка определит верный ответ */
    	String originalWord = variants.get(0).first;
    	/* Случайным образом определим номер верного варианта ответа */
    	rightVariant = (new Random()).nextInt(numberOfVariants); 
    	/* Поменяем его с первым элементом variants */
    	Collections.swap(variants, 0, rightVariant);
    	/* Заполняем кнопки и надпись */
    	currentWord.setText(originalWord);
    	for (int i = 0; i < numberOfVariants; i++) {
    		variantsOfTranslation[i].setText(variants.get(i).second);
    		/* По умолчанию у слов метки нет */
    		variantsOfTranslation[i].setTag(null);
    	}
    	/* А у верного слова ставим соответствующую метку */
    	variantsOfTranslation[rightVariant].setTag(isCorrectTag);
    	/* Пока не выбрали вариант ответа, мы не можем перейти к следующему слову */
    	nextWord.setEnabled(false);
    	/* Не забудем про цвет кнопок */
    	resetVariantButtons();
    }
    
    /* Проверка слова при нажатии на один из вариантов */
    public void checkWord(View v) {
    	if (!(v instanceof ColoredButton)) {
    		return;
    	}
    	/* Проверяем, имеет ли кнопка метку */
    	if (v.getTag() == isCorrectTag) {
    		((ColoredButton)v).paintInGreen();
    	} else {
    		((ColoredButton)v).paintInRed();
    		/* Красим в зеленый цвет правильный ответ */
    		((ColoredButton)variantsOfTranslation[rightVariant]).paintInGreen();
    	}
    	/* Делаем все кнопки недоступными для нажатия */
    	for (int i = 0; i < numberOfVariants; i++) {
    		variantsOfTranslation[i].setEnabled(false);
    	}
    	/* А вот кнопку для следующего слова, наоборот, доступной */
    	nextWord.setEnabled(true);
    }
    
    /* Сбрасываем все кнопки с вариантами */
    public void resetVariantButtons() {
    	for (int i = 0; i < numberOfVariants; i++) {
    		variantsOfTranslation[i].paintInDefault();
    		variantsOfTranslation[i].setEnabled(true);
    	}
    }
}
