package com.example.reaktionstest.Modules;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
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

public class Aufgabe_Farben extends AppCompatActivity {

    private TextView farbText;

    private final String [] farben = {"Grün", "Gelb", "Blau", "Rot", "Orange", "Weiß", "Pink", "Schwarz"};
    //private final String[] farben = {"Green", "Yellow", "Blue", "Red", "Orange", "White", "Pink", "Black"};

    private final int [] farbCodes = new int[farben.length];

    private SharedPreferences preferences;
    private SharedPreferences.Editor preferencesEditor;
    private Dialog epicDialog;
    private final String KEY = "speicherPreferences_Farben";

    private ProgressBar timer;
    private Zeit z;

    private int punkte;
    private boolean neuerHighScore = false;

    public static ImageButton down, up;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide(); // hide the title bar
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aufgabe_farben);

        down = findViewById(R.id.unwahr2);
        up = findViewById(R.id.wahr2);

        timer = findViewById(R.id.timer_Farben);
        timer.setProgressTintList(ColorStateList.valueOf(Color.rgb(0,0, 139)));

        farbText = findViewById(R.id.textFarbe);

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

        // durchsucht alle Farben in colors.xml (und weitere) und filtert alle Farben heraus, die im Array "farben" enthalten sind
        try {
            Field[] fields = Class.forName("com.example.reaktionstest" + ".R$color").getDeclaredFields();
            String colorName;
            for (Field farbe: fields) {
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

        // Fuer erste Seite
        farbText.setText(farben[(int) (Math.random() * farben.length)]);
        farbText.setTextColor(farbCodes[(int) (Math.random() * farbCodes.length)]);
//        ansicht.setBackgroundColor(farbCodes[(int) (Math.random() * farbCodes.length)]);

        this.punkte = 0;       // sehr wichtig, da man ins Menue zurueckgehen kann

        pop = new PopUpFenster(Aufgabe_Farben.this, punkte, preferences.getInt(KEY, 0), neuerHighScore, epicDialog, preferences, preferencesEditor, KEY);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d("----", "Nein");
        // nur zurueckgehen wenn die Zeit nicht am Laufen ist
        if ((keyCode == KeyEvent.KEYCODE_BACK) && (!Zeit.active)) {
            //preventing default implementation previous to android.os.Build.VERSION_CODES.ECLAIR
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    // variable to track event time
    private long mLastClickTime = 0;

    PopUpFenster pop;
    public void check(View view) {
        // Zeitdifferenz, um zu verhindern, dass 2 Buttons auf einmal geklickt werden
        int difference = 150;
        // Preventing multiple clicks, using threshold of 1 second
        if (SystemClock.elapsedRealtime() - mLastClickTime < difference) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();

        // vorherigen Zaehler stoppen
        z.running = false;

        // Wieder zuruecksetzen, da neuer Highscore nun normaler Highscore ist
        pop.setNeuerHighScore(false);

        String currentText = farbText.getText().toString();
        int currentColor = farbText.getCurrentTextColor();
        //int currentColor = ((ColorDrawable) ansicht.getBackground()).getColor();

        // Jedes Mal den HighScore neu auf falsch setzen, sonst wird jedes Mal angegeben, dass ein neuer HighScore erreicht wurde
        boolean antwortIstKorrekt = false;

        if (farbCodes[Arrays.asList(farben).indexOf(currentText)] == farbText.getCurrentTextColor()) {      // "... == currentColor" fuer Background-Color
            antwortIstKorrekt = true;
        }

       // pop = new PopUpFenster(Aufgabe_Farben.this, punkte, preferences.getInt(KEY, 0), neuerHighScore, epicDialog, preferences, preferencesEditor, KEY);

        if (((view.getId() == R.id.unwahr2) && antwortIstKorrekt) || ((view.getId() == R.id.wahr2) && !antwortIstKorrekt)){   // wenn auf Falsch geklickt wird, das Ergebnis aber richtig ist
            // Setzen des neuen Highscores
            TopScore.highscore_farben = pop.getPunkte();

            if (preferences.getInt(KEY, 0) < TopScore.highscore_farben) {
                preferencesEditor.putInt(KEY, TopScore.highscore_farben);
                pop.setNeuerHighScore(true);
            }
            preferencesEditor.putInt("key", TopScore.highscore_farben);
            preferencesEditor.commit();

            pop.showPopUpWindow();

        } else {    // Ergebnis ist richtig
            pop.increaseScore();
            z = new Zeit(timer, pop.getPunkte());     // neuer Zaehler wird erstellt

            z.laufen(pop);     // neuer Zaehler startet

            int randomNumber;
            int randomFarbe;

            // Implementierung, sodass nur die benachbarten Farben ausgewählt werden können um die Häufigkeit zu erhöhen
            do {
                randomNumber = (int) (Math.random() * farben.length);
                if (randomNumber == 0) {
                    //int[] random_array = new int[]{farben.length - 1, 0, 1};
                    int [] random_array = new int[]{0, (int) (Math.random() * farben.length)};
                    randomFarbe = farbCodes[random_array[(int) (Math.random() * random_array.length)]];
                } else if (randomNumber == farben.length - 1) {
                    //int[] random_array = new int[]{farben.length - 2, farben.length - 1, 0};
                    int [] random_array = new int[]{farben.length-1, (int) (Math.random() * farben.length)};
                    randomFarbe = farbCodes[random_array[(int) (Math.random() * random_array.length)]];
                } else {    // fuer alle Zahlen bis auf die aeußersten des Arrays
                    randomFarbe = farbCodes[(randomNumber - 1) + (int) (Math.random() * 3)];
                }
            } while (farben[randomNumber].equals(currentText) || (randomFarbe == currentColor));      // nach jedem Klick eine andere Farbe und ein anderer Text

            farbText.setText(farben[randomNumber]);
            farbText.setTextColor(randomFarbe);
            //ansicht.setBackgroundColor(randomFarbe);

        }
    }

}