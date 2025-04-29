package com.example.konzentrationstest;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    static Button easy, moderate, hard;
    private static Button[] btns;

    // speichert zuletzt ausgewaehlten Button, wenn man durch Pop-Up-Fenster ins StartMenu gelangt
    static String lastdisabledButton = "Easy";

    private ImageView iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide(); // hide the title bar
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        easy = findViewById(R.id.easy);
        moderate = findViewById(R.id.moderate);
        hard = findViewById(R.id.hard);
        btns = new Button[] {easy, moderate, hard};

        iv = findViewById(R.id.brainIcon);
        iv.setImageResource(R.drawable.braining_finalicon);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.difficulty_items, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // speichert Button ab wenn man durch Verlieren ueber das PopUp-Fenster wieder zurueck ins StartMenu gelangt
        if (lastdisabledButton.equals("Easy")) {
            easy.setBackgroundResource(android.R.drawable.btn_default);
            easy.setEnabled(false);
        } else if (lastdisabledButton.equals("Moderate")) {
            moderate.setBackgroundResource(android.R.drawable.btn_default);
            moderate.setEnabled(false);
        } else if (lastdisabledButton.equals("Hard")) {
            hard.setBackgroundResource(android.R.drawable.btn_default);
            hard.setEnabled(false);
        }

    }

    // gibt ausgewaehlten Schwierigkeitsgrad zurueck
    public static String[] getCurrentDifficultyText() {
        String[] difficultyParts = new String[2];
        for (Button difficulty: btns) {
            if (!difficulty.isEnabled()) {
                String[] temp = difficulty.getText().toString().split("\\(");
                difficultyParts[0] = temp[0].trim();       // Schwierigkeitsgrad
                difficultyParts[1] = temp[1].trim().split("s")[0].trim();       // das letzte Trim fuer den Fall, dass zwischen Sekunden und "s" noch eine Leerzeile vorhanden ist
            }
        }
        return difficultyParts;
    }

    // variable to track event time
    private static long mLastClickTime = 0;

    // setzt angeklickten Schwierigkeitsgrad auf Zustand "enabled", alle anderen auf "disabled"
    public static void terminateDfficulty (View view) {
        // Zeitdifferenz, um zu verhindern, dass 2 Buttons auf einmal geklickt werden
        int difference = 100;
        // Preventing multiple clicks, using threshold of 1 second
        if (SystemClock.elapsedRealtime() - mLastClickTime < difference) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();

        easy.setEnabled(true);
        easy.setBackgroundResource(android.R.drawable.btn_default);
        moderate.setEnabled(true);
        moderate.setBackgroundResource(android.R.drawable.btn_default);
        hard.setEnabled(true);
        hard.setBackgroundResource(android.R.drawable.btn_default);

        for (Button btn: btns) {
            if (view.getId() == btn.getId()) {
                btn.setEnabled(false);
                break;
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String text = parent.getItemAtPosition(position).toString();
        Toast.makeText(parent.getContext(), text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    // oeffnet das ModuleMenu
    public void goToModuleMenu(View view) {
        Intent myIntent = new Intent(MainActivity.this, ModuleMenu.class);
        MainActivity.this.startActivity(myIntent);
    }

}