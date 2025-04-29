package com.example.konzentrationstest;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.konzentrationstest.Modules.Aufgabe_Farben;
import com.example.konzentrationstest.Modules.Aufgabe_Formen;
import com.example.konzentrationstest.Modules.Aufgabe_Rechnen;
import com.example.konzentrationstest.Modules.Aufgabe_waehleUnpassendeFarbe;

public class PopUpFenster extends AppCompatActivity {

    private Object obj;

    private Button leave, stay;
    private Dialog epicDialog;

    private TextView text, text2;
    private SharedPreferences preferences;
    private SharedPreferences.Editor preferencesEditor;
    private String KEY;      // fuer jede Klasse anderen Key fuer jeweils einen anderen Highscore

    private int punkte;
    private int highscore;
    private boolean neuerHighScore;

    public PopUpFenster() {

    }

    public PopUpFenster(Object obj, int punkte, int highscore, boolean neuerHighScore, Dialog epicDialog, SharedPreferences preferences, SharedPreferences.Editor preferencesEditor, String key) {
        this.obj = obj;
        this.punkte = punkte;
        this.highscore = highscore;
        this.neuerHighScore = neuerHighScore;

        this.epicDialog = epicDialog;
        this.preferences = preferences;
        this.preferencesEditor = preferencesEditor;

        KEY = key;
    }

    public void setNeuerHighScore(boolean neuerHighScore) {
        this.neuerHighScore = neuerHighScore;
    }

    public SharedPreferences getPreferences() {
        return this.preferences;
    }

    public SharedPreferences.Editor getPreferencesEditor() {
        return this.preferencesEditor;
    }

    public String getKEY() {
        return this.KEY;
    }

    public int getPunkte() { return this.punkte; }

    public void increaseScore() { this.punkte += 1; }

    public void showPopUpWindow() {
        epicDialog.setContentView(R.layout.activity_popupfenster);

        leave = epicDialog.findViewById(R.id.verlassen);
        stay = epicDialog.findViewById(R.id.weiter);

        text = epicDialog.findViewById(R.id.anzeigeScore);
        text2 = epicDialog.findViewById(R.id.anzeigeHighscore);

        String punkteText = "\n\tScore: " + this.punkte;
        text.setText(punkteText);

        // Text fuer Highscore
        String displayedText = "\t\t\t\t\tHighscore: " + preferences.getInt(KEY, 0);
        if (neuerHighScore) {
            displayedText = "New Highscore: " + preferences.getInt(KEY, 0);
        }
        text2.setText(displayedText);

        leave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                String difficulty = MainActivity.getCurrentDifficultyText()[0];
                if (difficulty.equals("Easy")) {
                    MainActivity.lastdisabledButton = "Easy";
                } else if (difficulty.equals("Moderate")) {
                    MainActivity.lastdisabledButton = "Moderate";
                } else { // if (difficulty.equals("Hard")) {
                    MainActivity.lastdisabledButton = "Hard";
                }

                Intent myIntent = new Intent( (AppCompatActivity) obj, MainActivity.class);
                ((AppCompatActivity) obj).startActivity(myIntent);
            }
        });

        stay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                switch (KEY) {
                    case "speicherPreferences_Rechnen":
                        // macht Buttons wieder frei sobald das Pop-Up-Fenster verlassen wurde
                        Aufgabe_Rechnen.down.setEnabled(true);
                        Aufgabe_Rechnen.up.setEnabled(true);
                        break;
                    case "speicherPreferences_Farben":
                        Aufgabe_Farben.down.setEnabled(true);
                        Aufgabe_Farben.up.setEnabled(true);
                        break;
                    case "speicherPreferences_Formen":
                        Aufgabe_Formen.down.setEnabled(true);
                        Aufgabe_Formen.up.setEnabled(true);
                        break;
                    case "speicherPreferences_waehleUnpassendeFarbe":
                        // enablen der Buttons
                        for (ImageButton ib: Aufgabe_waehleUnpassendeFarbe.getButtons()) {
                            ib.setEnabled(true);
                        }
                        break;
                }
                epicDialog.dismiss();
                // aktiviert Back-Button wieder sobald auf "Weiter" geklickt wird
                Zeit.active = true;
                // Punkte wieder zuruecksetzen
                punkte = 0;
            }
        });

        // sorgt dafuer dass Aktivität beendet wird sobald man beim Laufen der Aktivität ins Hauptmenu zurueckmoechte
        epicDialog.setCancelable(false);
        epicDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        epicDialog.show();

    }

}