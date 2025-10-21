
package com.example.reaktionstest.Modules;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.reaktionstest.MainActivity;
import com.example.reaktionstest.PopUpFenster;
import com.example.reaktionstest.R;
import com.example.reaktionstest.TopScore;
import com.example.reaktionstest.Zeit;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Aufgabe_waehleUnpassendeFarbe extends AppCompatActivity {

    private Zeit z;
    private ProgressBar timer;
    int punkte;

    SharedPreferences preferences;
    SharedPreferences.Editor preferencesEditor;
    String KEY = "speicherPreferences_waehleUnpassendeFarbe";

    private TextView farbText;

    private final String[] farben = {"Grün", "Gelb", "Blau", "Rot", "Orange", "Pink", "Schwarz"};
    //private final String[] farben = {"Green", "Yellow", "Blue", "Red", "Orange", "Pink", "Black"};

    private final int[] farbCodes = new int[farben.length];

    static ImageButton btn1, btn2, btn3;
    public static ImageButton[] btns;

    private Dialog epicDialog;

    boolean neuerHighScore = false;
    PopUpFenster pop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide(); // hide the title bar
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aufgabe_waehleunpassendefarbe);

        timer = findViewById(R.id.timer_waehleUnpassendeFarbe);
        timer.setProgressTintList(ColorStateList.valueOf(Color.rgb(0,0, 139)));
        farbText = findViewById(R.id.colorText);

        btn1 = findViewById(R.id.farbe1);
        btn2 = findViewById(R.id.farbe2);
        btn3 = findViewById(R.id.farbe3);

        // initialisiere das button-array das alle farb-buttons beinhaltet, die geshufflet werden
        btns = new ImageButton[] {btn1, btn2, btn3};

        // durchsucht alle Farben in colors.xml (und weitere) und filtert alle Farben heraus, die im Array "farben" enthalten sind
        try {
            Field[] fields = Class.forName("com.example.reaktionstest" + ".R$color").getDeclaredFields();
            //Field[] fields = Class.forName();
            String colorName;
            for (Field farbe : fields) {
                colorName = farbe.getName();
                if (Arrays.asList(farben).contains(colorName)) {
                    int colorId = farbe.getInt(null);
                    int color = getResources().getColor(colorId);
                    farbCodes[Arrays.asList(farben).indexOf(colorName)] = color;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Setze erste Farbe aus im Array farben angegebenen Farben
        int random1 = (int) (Math.random() * farben.length);
        farbText.setText(farben[random1]);

        // Setze zweite Farbe, die unterschiedlich ist von erster Farbe
        int random2;
        do {
            random2 = (int) (Math.random() * farbCodes.length);
        } while (random1 == random2);
        farbText.setTextColor(farbCodes[random2]);

        int startColor_Btn1 = farbCodes[Arrays.asList(farben).indexOf(farbText.getText().toString())];
        int startColor_Btn2 = farbText.getCurrentTextColor();

        btn1.setBackgroundColor(startColor_Btn1);
        btn2.setBackgroundColor(startColor_Btn2);

        // Dritte Farbe unterscheidet sich von den ersten beiden Farben und ist die korrekte Antwort auf die Frage
        int newColor;
        do {
            newColor = (int) (Math.random() * farbCodes.length);
        } while ((startColor_Btn1 == farbCodes[newColor]) || (farbCodes[newColor] == startColor_Btn2));

        btn3.setBackgroundColor(farbCodes[newColor]);

        // PopUp-Fenster
        epicDialog = new Dialog(this);

        // Setzen der max. Sekundenzahl durch ausgewaehlten Schwierigkeitsgrad
        String[] diff = MainActivity.getCurrentDifficultyText();
        int milliSec = Integer.parseInt(String.valueOf(Double.parseDouble(diff[1]) * 1000).split("\\.")[0]);

        // Das Maximum fuer die Zeitleiste setzen
        timer.setMax((milliSec*9) / ((milliSec / 100) / 5));

        // Die erste Timeline sollte aufgefuellt sein
        timer.setProgress(timer.getMax());
        z = new Zeit(timer, punkte);


        //setting preferences
        this.preferences = this.getSharedPreferences("myPrefsKey", Context.MODE_PRIVATE);
        preferencesEditor = preferences.edit();
        preferencesEditor.apply();

        pop = new PopUpFenster(this, punkte, preferences.getInt(KEY, 0), neuerHighScore, epicDialog, preferences, preferencesEditor, KEY);

        // Punktestand wird jedes Mal zurueckgesetzt, wenn die Seite neu betreten wird (besonders wenn auf "Verlassen" geklickt wird)
        punkte = 0;
    }

    public static ImageButton[] getButtons() {
        return btns;
    }

    public void setButtonsDisabled() {
        for (ImageButton ib: btns) {
            ib.setEnabled(false);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)  && (!Zeit.active)) {
            //preventing default implementation previous to android.os.Build.VERSION_CODES.ECLAIR
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    // variable to track event time
    private long mLastClickTime = 0;

    public void check(View view) {
        // Zeitdifferenz, um zu verhindern, dass 2 Buttons auf einmal geklickt werden
        int difference = 150;
        // Preventing multiple clicks, using threshold of 1 second
        if (SystemClock.elapsedRealtime() - mLastClickTime < difference) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();

        String lastText = farbText.getText().toString();
        int lastColor = farbText.getCurrentTextColor();

        // Prueft ob die angegebene Loesung korrekt ist
        boolean ergebnisIstRichtig = true;

        // alter Zaehler wird gestoppt
        z.running = false;

        // Wieder zuruecksetzen, da neuer Highscore nun normaler Highscore ist
        pop.setNeuerHighScore(false);

        ImageButton clickedButton;
        int id = view.getId();

        if (id == R.id.farbe1) {
            clickedButton = btn1;
        } else if (id == R.id.farbe2) {
            clickedButton = btn2;
        } else { //if (id == R.id.farbe3) {
            clickedButton = btn3;
        }

        int clickedButtonColor = ((ColorDrawable) clickedButton.getBackground()).getColor();

        if ((lastColor == clickedButtonColor) || (farbCodes[Arrays.asList(farben).indexOf(lastText)] == clickedButtonColor)) {     // wenn die Antwort also falsch ist
            ergebnisIstRichtig = false;
        }

        if (!ergebnisIstRichtig) {
            // Setzen des neuen Highscores
            TopScore.highscore_waehleUnpassendeFarbe = pop.getPunkte();

            if (preferences.getInt(KEY, 0) < TopScore.highscore_waehleUnpassendeFarbe) {
                preferencesEditor.putInt(KEY, TopScore.highscore_waehleUnpassendeFarbe);
                pop.setNeuerHighScore(true);
            }
            preferencesEditor.putInt("key", TopScore.highscore_waehleUnpassendeFarbe);
            preferencesEditor.commit();

            pop.showPopUpWindow();

        } else {
            // Score um 1 erhoehen
            pop.increaseScore();
            // neues Objekt fuer naechste Seite
            z = new Zeit(timer, pop.getPunkte());
            // neuen Zaehler srtarten
            z.laufen(pop);

            int randomNumber, randomFarbe;

                // Implementierung, sodass Text und Farbe jedes Mal unterschiedlich zur vorherigen Activity sind + Farbe niemals dem Text entspricht
                do {
                    randomNumber = (int) (Math.random() * farben.length);
                    randomFarbe = (int) (Math.random() * farbCodes.length);
                } while (farben[randomNumber].equals(lastText) || (farbCodes[randomFarbe] == lastColor) || (randomNumber == randomFarbe));

                farbText.setText(farben[randomNumber]);
                farbText.setTextColor(farbCodes[randomFarbe]);

            // nach jedem Klick die Buttons shufflen, damit richtige Antwort nicht immer an der dritten Stelle ist
            List<ImageButton> intList = Arrays.asList(btns);
            Collections.shuffle(intList);
            intList.toArray(btns);

            // Setzen der Farben
            // anhand der Textfarbe
            btns[0].setBackgroundColor(farbText.getCurrentTextColor());
            // anhand des Textes
            btns[1].setBackgroundColor(farbCodes[Arrays.asList(farben).indexOf(farbText.getText().toString())]);

            // die letzte Farbe unterscheidet sich von den anderen beiden und ist richtig
            int newColor;
            do {
                newColor = (int) (Math.random() * farbCodes.length);
            } while ((farbCodes[Arrays.asList(farben).indexOf(farbText.getText().toString())] == farbCodes[newColor]) || (farbCodes[newColor] == farbText.getCurrentTextColor()));

            btns[2].setBackgroundColor(farbCodes[newColor]);
        }

    }
}
