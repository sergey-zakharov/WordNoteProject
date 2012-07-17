package ru.fizteh.fivt.yanykin.wordnote;

import android.content.Context;
import android.graphics.Color;
import android.widget.Button;

/* Расширенная версия кнопки, которая получила два дополнительных метода */
public class ColoredButton extends Button {

	public ColoredButton(Context context) {
		super(context);
		paintInDefault();
	}

	private final static int defaultColor = Color.DKGRAY;
	
	/* Покрасить кнопку в зеленый цвет (верный ответ) */
	public void paintInGreen() {
		super.setBackgroundColor(Color.GREEN);
	}
	
	/* Покрасить кнопку в красный цвет (неверный ответ) */
	public void paintInRed() {
		super.setBackgroundColor(Color.RED);
	}
	
	/* Покрасить кнопку в серый цвет (по умолчанию, ответ не выбран) */
	public void paintInDefault() {
		super.setBackgroundColor(defaultColor);
	}
}
