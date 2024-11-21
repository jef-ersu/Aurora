package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button cardioButton = findViewById(R.id.cardioButton);
        Button neuroButton = findViewById(R.id.neuroButton);

        cardioButton.setOnClickListener(v -> openAgendamento("Cardiologia"));
        neuroButton.setOnClickListener(v -> openAgendamento("Neurologia"));
    }

    private void openAgendamento(String especialidade) {
        Intent intent = new Intent(MainActivity.this, AgendamentoActivity.class);
        intent.putExtra("especialidade", especialidade);
        startActivity(intent);
    }
}
