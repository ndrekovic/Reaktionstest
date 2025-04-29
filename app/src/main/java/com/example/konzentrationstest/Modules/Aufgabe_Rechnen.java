package com.example.konzentrationstest.Modules;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Html;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.konzentrationstest.MainActivity;
import com.example.konzentrationstest.PopUpFenster;
import com.example.konzentrationstest.R;
import com.example.konzentrationstest.TopScore;
import com.example.konzentrationstest.Zeit;

import java.util.Arrays;

// vielleicht besser, die Zahlen rot zu markieren oder mit anderen Farben, sodass besser sichtbar und nicht nur grau.

public class Aufgabe_Rechnen extends AppCompatActivity {

    private final int [] quadratzahlen = {1, 4, 9, 16, 25, 36, 49, 64, 81, 100};
    private final int [] summand1 = new int[10000 + quadratzahlen.length];     // vielleicht besser new int[100 + quadratzahlen.length] und dann irgendwie verteilen, sind viel zu viele Variablen
    private final int [] summand2 = new int[summand1.length];
    private final int [] summen = new int[summand1.length];

    // Punkte sind 0 sobald Modul geoeffnet wird
    private final int punkte = 0;
    private int nth_activity;
    private final boolean neuerHighScore = false;

    //String[] operator = {"+", "-", "root"};         // hier weitermachen
    String operator = "+";
    String[] stufen = {"Hard", "Moderate", "Easy"};

    private TextView textFeld;

    // Pop-Up-Fenster und dazugehoerige Variablen
    private SharedPreferences preferences;
    private SharedPreferences.Editor preferencesEditor;
    private final String KEY = "speicherPreferences_Rechnen";
    private Dialog epicDialog;

    private ProgressBar timer;
    private Zeit z;

    private PopUpFenster pop;
    public static ImageButton down, up;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide(); // hide the title bar

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aufgabe_rechnen);

        textFeld = findViewById(R.id.textFarbe);
        timer = findViewById(R.id.timer_Rechnen);
        timer.setProgressTintList(ColorStateList.valueOf(Color.rgb(0,0, 139)));

        down = findViewById(R.id.unwahr);
        up = findViewById(R.id.wahr);

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

        // simple Implementierungen fuer die Bestimmung der Summanden
            int [] temp_shuffle = new int[quadratzahlen.length];      // dient als Index nur fuer die Wurzel-Zahlen

            // nur fuer alle Wurzeln
            for (int j = 0; j < quadratzahlen.length; j++) {
                temp_shuffle[j] = (int) (Math.random() * 110);  // random-Index fuer die Wurzel-Zahlen, auf Array summand1 verteilen
                summand1[temp_shuffle[j]] = quadratzahlen[j];
                summand2[temp_shuffle[j]] = 0;        // Merkmal einer Wurzelzahl einfach, dass 2.Summand eine 0 ist (um sie von einfachen Zahlen zu unterscheiden), siehe check Methode
                summen[temp_shuffle[j]] = generiereErgebnis_Wurzel(quadratzahlen[j]);    // bzw. selbst oben einfach eintragen
            }

            // zufällige Werte zuweisen nur für alle Summen
            for (int i = 0; i < summand1.length; i++) {
                if (summand1[i] == 0) {         // besser das hier als summand2[i], da in Java bei einem leeren Int-Array alle Werte per default gleich 0 sind.
                    summand1[i] = (int) (Math.random() * 20) + 1;
                    summand2[i] = (int) (Math.random() * 20) + 1;
                    summen[i] = generiereErgebnis(summand1[i], summand2[i]);
                }
            }

            // Setzen der Werte fuer die erste Seite (wahrscheinlich überflüssig)
            int a = 13 + (int) (Math.random() * 12);            // [13, 24]
            int b = 5 + (int) (Math.random() * 28);             // [5, 32]
            int c = (a + b - 3) + (int) (Math.random() * 3);    // [a+b-3; a+b+2]  -> spaeter nach belieben Aendern
            summand1[0] = a;
            summand2[0] = b;
            summen[0] = c;
            String s = a + " " + operator + " " + b + " = " + c;
            textFeld.setText(s);    // default für den ersten Wert

            // sehr wichtig, da man ins Menue zurueckgehen kann und die Punkte sonst nicht zurzeckgesetzt werden
            nth_activity = 0;

            // erzeuge Pop-Up-Fenster, das bei falscher Antwort aufgerufen wird
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

    // Index der Stufe * 2 + 1 (= 2n+1), um die Anzahl an Sekunden für den jeweiligen Schwierigkeitsgrad zu ermitteln. Reicht als Anfangsalgorithmus
    public int level_in_sekunden(String level_index) {
        return 2 * (Arrays.asList(stufen).indexOf(level_index)+1) + 2;  // +1 sehr wichtig, da für Hard ansonsten Wert von 0 angenommen wird
    }

    // vielleicht nicht nur Plus-, sondern auch Mal Aufgaben oder Wurzel-Aufgaben
    public static int generiereErgebnis(int wert1, int wert2) {
        int genauigkeitsWert = 2;
        return (wert1 + wert2) + (int) (Math.random() * genauigkeitsWert);     // Intervall [wert1+wert2; wert1+wert2+(3-1)], z.B. 8 + 13 -> [21, 23]. Je höher der genauigkeitswert, umso weiter entfernt ist das Ergebnis (spaeter selbst festlegem)
    }

    // Algorithmus fuer Wurzelzahl-Auswahl
    public static int generiereErgebnis_Wurzel(int wert) {
        // gewichtung gilt fuer ersten Parameterwert
        return zufallsGenerator((int) (Math.sqrt(wert)), (int) (Math.sqrt(wert)) + (int) (Math.random() * 2), 60);     // Intervall [wurzel(wert); wurzel(wert)+1]     // spaeter verfeinern, sodass die Wahrscheinlichkeit hoeher ist, dass das Ergebnis korrekt ist, zB mit neuer Methode und Modulo
    }

    // Wahrscheinlichkeit, dass wert1 ausgewaehlt wird, soll gewichtung % sein, und fuer wert2 dann 1 - gewichtung %
    public static int zufallsGenerator(int wert1, int wert2, int gewichtung) {
        int random = (int) (Math.random() * 100);
        if (random < gewichtung) {
            return wert1;
        }
        return wert2;
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

        // alter Zaehler wird gestoppt
        z.running = false;

        // Wieder zuruecksetzen, da neuer Highscore nun normaler Highscore ist
        pop.setNeuerHighScore(false);

        // Variable um Richtigkeit der Antwort zu ueberpruefen
        boolean antwortIstKorrekt;

        // Folgender Kommentar ergänzt die Minus-Aufgaben, das ist nur der Anfang. Erst ganz zum Schluss machen, wenn alles andere wichtige erledigt ist
        /*
        String currentOperator = operator[(int) (Math.random() * operator.length)];
        if (currentOperator.equals("-")) {
            ergebnisIstRichtig = summand1[nth_activity] - summand2[nth_activity] == summen[nth_activity];
        }*/

        // Prueft ob Antwort korrekt ist
        if (summand2[nth_activity] == 0) {  // Quadrat
            antwortIstKorrekt = Math.sqrt(summand1[nth_activity]) == summen[nth_activity];
        } else {        // Summe
            antwortIstKorrekt = summand1[nth_activity] + summand2[nth_activity] == summen[nth_activity];
        }

        if (((view.getId() == R.id.unwahr) && antwortIstKorrekt) || (((view.getId() == R.id.wahr) && !antwortIstKorrekt))) {        // falsche Antwort wurde eingetippt
            // Setzen des neuen Highscores
            TopScore.highscore_rechnen = pop.getPunkte();

            if (preferences.getInt(KEY, 0) < TopScore.highscore_rechnen) {
                preferencesEditor.putInt(KEY, TopScore.highscore_rechnen);
                pop.setNeuerHighScore(true);
            }
            preferencesEditor.putInt("key", TopScore.highscore_rechnen);
            preferencesEditor.commit();

            pop.showPopUpWindow();

        } else {
            // erhoehe Punktestand um 1
            pop.increaseScore();
            z = new Zeit(timer, pop.getPunkte());     // neues Objekt fuer naechste Seite
            // neuen Zaehler starten
            z.laufen(pop);

            ++nth_activity;

            String displayedText = "";
            if (summand2[nth_activity] != 0) {  // gibt Summe aus
                displayedText = summand1[nth_activity] + " " + operator + " " + summand2[nth_activity] + " = " + summen[nth_activity];
                textFeld.setText(displayedText);
            } else if (summand2[nth_activity] == 0) {   // gibt Text aus
                //textFeld.setText(getResources().getString(R.string.sqr_root));
                displayedText = "&#x221a;" + summand1[nth_activity] + " = " + summen[nth_activity];
                textFeld.setText(Html.fromHtml(displayedText));
            }
        }

    }

}