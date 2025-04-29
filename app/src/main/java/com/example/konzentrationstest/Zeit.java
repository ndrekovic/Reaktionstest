package com.example.konzentrationstest;

import android.os.CountDownTimer;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.konzentrationstest.Modules.Aufgabe_Farben;
import com.example.konzentrationstest.Modules.Aufgabe_Formen;
import com.example.konzentrationstest.Modules.Aufgabe_Rechnen;
import com.example.konzentrationstest.Modules.Aufgabe_waehleUnpassendeFarbe;

public class Zeit extends AppCompatActivity {

    private final ProgressBar counter;
    private PopUpFenster pop;

    int punkte = 0;
    int milliSec;

    String[] diff;

    public boolean running = true;
    public static boolean active = true;

    public Zeit (ProgressBar counter, int punkte) {
        this.counter = counter;
        this.punkte = punkte;
    }

    // startet Zeitleiste
    public void laufen(PopUpFenster popUp) {
        pop = popUp;        // loest wohl irgendwie das Problem mit dem Zurückgehen und dem Stoppen der Acitivity
        diff = MainActivity.getCurrentDifficultyText();
        milliSec = Integer.parseInt(String.valueOf(Double.parseDouble(diff[1]) * 1000).split("\\.")[0]);

        // Jedes Mal neu resetten, um bei richtiger Antwort die letzte Anzeige der Zeitleiste zu loeschen und die neue Liste wieder voll zu machen
        this.running = true;

        Zeit.active = false;

        CountDownTimer countDownTimer = new CountDownTimer(milliSec, 10) {
            public void onTick(long millisUntilFinished) {
                Zeit.this.counter.setProgress(((int) millisUntilFinished*9) / ((milliSec / 100) / 5));     // mathematisches Umrechnen, im Kopf etwas schwerer zu machen

                // setzt nach jeder richtigen Antwort die Progressbar zurueck und stoppt den vorheigen Timer, damit diese nicht einfach weiterlaeuft
                if (!Zeit.this.running) {
                    this.cancel();
                    //Zeit.this.running = false;
                    Log.d("","---Bazinga");
                    Zeit.this.counter.setProgress(Zeit.this.counter.getMax());
                    return;
                }
            }

            public void onFinish() {
                // sorgt dafuer dass Aktivität stoppt sobald man beim Laufen der Aktivität ins Hauptmenu zurueckmoechte
                if (!isFinishing()) {
                    Zeit.this.running = false;
                }
                Zeit.this.counter.setProgress(Zeit.this.counter.getMax());

                // nun ist der Backbutton wieder aktiv
                Zeit.active = true;

                // spaeter getter aufrufen oder aehnliches, keine statischen Variablen verwenden
                int hs = 0;
                switch (pop.getKEY()) {
                    case "speicherPreferences_Rechnen":
                        TopScore.highscore_rechnen = pop.getPunkte();
                        hs = TopScore.highscore_rechnen;
                        // verhindert Button-Klick nachdem die Zeitleiste vorbei ist (durch schnelles Klicken)
                        Aufgabe_Rechnen.down.setEnabled(false);
                        Aufgabe_Rechnen.up.setEnabled(false);
                        break;
                    case "speicherPreferences_Farben":
                        TopScore.highscore_farben = pop.getPunkte();
                        hs = TopScore.highscore_farben;
                        Aufgabe_Farben.down.setEnabled(false);
                        Aufgabe_Farben.up.setEnabled(false);
                        break;
                    case "speicherPreferences_Formen":
                        TopScore.highscore_formen = pop.getPunkte();
                        hs = TopScore.highscore_formen;
                        Aufgabe_Formen.down.setEnabled(false);
                        Aufgabe_Formen.up.setEnabled(false);
                        break;
                    case "speicherPreferences_waehleUnpassendeFarbe":
                        TopScore.highscore_waehleUnpassendeFarbe = pop.getPunkte();
                        hs = TopScore.highscore_waehleUnpassendeFarbe;
                        // verhindert Button-Klick nachdem die Zeitleiste vorbei ist (durch schnelles Klicken)
                        for (ImageButton iv: Aufgabe_waehleUnpassendeFarbe.btns) {
                            iv.setEnabled(false);
                        }
                        //Aufgabe_waehleUnpassendeFarbe.btn1.setEnabled(false);
                        //Aufgabe_waehleUnpassendeFarbe.btn2.setEnabled(false);
                        //Aufgabe_waehleUnpassendeFarbe.btn3.setEnabled(false);
                        break;
                }

                if (pop.getPreferences().getInt(pop.getKEY(), 0) < hs) {
                    pop.getPreferencesEditor().putInt(pop.getKEY(), hs);
                    pop.setNeuerHighScore(true);
                }

                pop.getPreferencesEditor().putInt("key", hs);
                pop.getPreferencesEditor().commit();
                pop.showPopUpWindow();
                Zeit.active = true;
            }
        };
        countDownTimer.start();
    }

}
