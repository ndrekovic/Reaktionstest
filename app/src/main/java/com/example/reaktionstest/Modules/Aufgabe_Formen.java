
package com.example.reaktionstest.Modules;


import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.reaktionstest.MainActivity;
import com.example.reaktionstest.PopUpFenster;
import com.example.reaktionstest.R;
import com.example.reaktionstest.TopScore;
import com.example.reaktionstest.Zeit;

import java.util.Arrays;

// 3 Möglichkeiten, dies zu gestalten.
// 1.) Formen wie Rechteck, Kreis etc. hinzeichnen und in die Form das Wort schreiben
// 2.) Identisches Spiel wie Schlag den Raab. 2 Muster sind gegeben, 5 Antwortmöglichkeiten: 2 zeigen die Form und Farbe an, dabei bleibt eins übrig und das muss angeklickt werden
// 3.) vermutlich beste und einfachste Idee:

public class Aufgabe_Formen extends AppCompatActivity {

    // wichtig: formenText und symbolDateien muessen 1:1 in der gleichen Reihenfolge sein
    private final String [] formenText = {"Kreis", "Quadrat", "Stern", "Herz", "Dreieck"};
    // private final String [] formenText = {"Circle", "Rectangle", "Star", "Heart", "Triangle"};

    private final int []symbolDateien = {R.drawable.kreis, R.drawable.quadrat, R.drawable.stern, R.drawable.herz, R.drawable.dreieck};

    private ImageView form;
    private int randomSymbol;
    int symbol;

    private int punkte = 0;
    private final boolean neuerHighScore = false;
    int temp = 0;

    private ProgressBar timer;
    private PopUpFenster pop;
    private Zeit z;
    private TextView textView;

    // Pop-Up-Hilfsmittel
    private SharedPreferences preferences;
    private SharedPreferences.Editor preferencesEditor;
    private Dialog epicDialog;
    private final String KEY = "speicherPreferences_Formen";

    public static ImageButton down, up;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide(); // hide the title bar
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aufgabe_formen);

        textView = findViewById(R.id.formText);
        form = findViewById(R.id.formSymbol);
        timer = findViewById(R.id.timer_Formen);
        timer.setProgressTintList(ColorStateList.valueOf(Color.rgb(0,0, 139)));

        down = findViewById(R.id.unwahr3);
        up = findViewById(R.id.wahr3);

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

        // fuer die erste Seite
        randomSymbol = (int) (Math.random() * symbolDateien.length);
        temp = randomSymbol;        // sehr wichtig fuer erste If-Anweisung in Methode check (nur fuer den Uebergang vom ersten zum zweiten Bild)
        symbol = symbolDateien[randomSymbol];

        // Erstellen des Starttextes und der Startform
        System.out.println("Textview get text: -------" +  textView.getText());
        form.setImageResource(symbolDateien[randomSymbol]);
        form.setScaleX(1.5f); // 150% Breite
        form.setScaleY(1.5f); // 150% Höhe

        textView.setText(formenText[(int) (Math.random() * formenText.length)]);
        punkte = 0;       // sehr wichtig, da man ins Menue zurueckgehen und das Spiel wieder oeffnen kann

        pop = new PopUpFenster(this, punkte, preferences.getInt(KEY, 0), neuerHighScore, epicDialog, preferences, preferencesEditor, KEY);

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

    public void check (View view) {
        // Zeitdifferenz, um zu verhindern, dass 2 Buttons auf einmal geklickt werden
        int difference = 150;
        // Preventing multiple clicks, using threshold of 1 second
        if (SystemClock.elapsedRealtime() - mLastClickTime < difference) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();

        String lastText = textView.getText().toString();
        int lastSymbol = symbol;
        //int currentSymbol = symbolDateien[randomSymbol];

        // alter Zaehler wird gestoppt
        z.running = false;

        // Wieder zuruecksetzen, da neuer Highscore nun normaler Highscore ist
        pop.setNeuerHighScore(false);

        int index = Arrays.asList(formenText).indexOf(lastText);
        boolean antwortIstKorrekt = false;

        // Ueberpruefung ob Index des Textes mit dem der Form uebereinstimmt
        if (index == temp) {
            antwortIstKorrekt = true;
        }

        // Antwort ist nicht korrekt
        if (((view.getId() == R.id.unwahr3) && antwortIstKorrekt) || ((view.getId() == R.id.wahr3) && !antwortIstKorrekt)) {
            // Setzen des neuen Highscores
            TopScore.highscore_formen = pop.getPunkte();

            if (preferences.getInt(KEY, 0) < TopScore.highscore_formen) {
                preferencesEditor.putInt(KEY, TopScore.highscore_formen);
                pop.setNeuerHighScore(true);
            }
            preferencesEditor.putInt("key", TopScore.highscore_formen);
            preferencesEditor.commit();

            pop.showPopUpWindow();

        } else { // Antwort ist korrekt
            pop.increaseScore();
            z = new Zeit(timer, pop.getPunkte());     // neues Objekt fuer naechste Seite
            z.laufen(pop);     // neuen Zaehler starten

            // Symbol und Text werden nicht 2 Mal hintereinander gleich sein
            do {
                randomSymbol = (int) (Math.random() * symbolDateien.length);

                if (randomSymbol == 0) {
                    //int[] random_array = new int[]{symbolDateien.length - 1, 0, 1};
                    int [] random_array = new int[]{0, (int) (Math.random() * symbolDateien.length)};
                    temp = random_array[(int) (Math.random() * random_array.length)];
                } else if (randomSymbol == formenText.length - 1) {
                    //int[] random_array = new int[]{symbolDateien.length - 1, 0, 1};
                    int [] random_array = new int[]{symbolDateien.length - 1, (int) (Math.random() * symbolDateien.length)};
                    temp = random_array[(int) (Math.random() * random_array.length)];
                } else {        // Aeußere sind ausgeschlossen
                    temp = (randomSymbol - 1) + (int) (Math.random() * 3);
                }
                symbol = symbolDateien[temp];
            } while (formenText[randomSymbol].equals(lastText) || (lastSymbol == symbol));

            /*
            // fuer Position des Textes im Symbol
            textView.setX(295.0f);
            if (symbol == R.drawable.kreis) {
                textView.setY(720.0f);
            } else if (symbol == R.drawable.quadrat) {
                textView.setY(732.0f);
            } else if (symbol == R.drawable.stern) {
                textView.setY(708.0f);
            } else if (symbol == R.drawable.herz) {
                textView.setY(708.0f);
            } else {    // if symbol == R.drawable.dreieck
                textView.setY(708.0f);
            }*/

            // Setzen des neuen Textes und der neuen Form
            textView.setText(formenText[randomSymbol]);
            form.setImageResource(symbol);

        }

    }

}
