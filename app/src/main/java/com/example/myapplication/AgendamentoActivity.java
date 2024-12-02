package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

// novas importações
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;


public class AgendamentoActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agendamento);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        TextView especialidadeText = findViewById(R.id.especialidadeText);
        EditText dateInput = findViewById(R.id.dateInput);
        EditText timeInput = findViewById(R.id.timeInput);
        Button bookButton = findViewById(R.id.bookButton);
        EditText nameInput = findViewById(R.id.nameInput);
        EditText symptomsInput = findViewById(R.id.symptomsInput);

        String especialidade = getIntent().getStringExtra("especialidade");
        especialidadeText.setText("Especialidade: " + especialidade);

        // Verificar se há um ID de consulta para edição
        String consultaId = getIntent().getStringExtra("consultaId");
        if (consultaId != null) {
            // Recuperar dados da consulta do Firebase
            db.collection("Hospitais").document("Consultas")
                    .collection("Agendamentos").document(consultaId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            dateInput.setText(documentSnapshot.getString("Data"));
                            timeInput.setText(documentSnapshot.getString("Hora"));
                            nameInput.setText(documentSnapshot.getString("Nome"));
                            symptomsInput.setText(documentSnapshot.getString("Sintomas"));
                        }
                    });
        }

        // Atualizar ou criar nova consulta ao clicar no botão
        bookButton.setOnClickListener(v -> {
            String date = dateInput.getText().toString().trim();
            String time = timeInput.getText().toString().trim();
            String name = nameInput.getText().toString().trim();
            String symptoms = symptomsInput.getText().toString().trim();
            String userId = mAuth.getCurrentUser().getUid();

            if (date.isEmpty() || time.isEmpty() || name.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validação de formato e data futura
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            try {
                Date selectedDate = sdf.parse(date);
                if (selectedDate.before(new Date())) {
                    Toast.makeText(this, "A data deve ser no futuro!", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (ParseException e) {
                Toast.makeText(this, "Formato de data inválido! Use dd/MM/yyyy.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Criar ou atualizar consulta
            Map<String, Object> consulta = new HashMap<>();
            consulta.put("Especialidade", especialidadeText.getText().toString());
            consulta.put("Data", date);
            consulta.put("Hora", time);
            consulta.put("Nome", name);
            consulta.put("PacienteId", userId);

            if (!symptoms.isEmpty()) {
                consulta.put("Sintomas", symptoms);
            }

            if (consultaId != null) {
                // Atualizar consulta existente
                db.collection("Hospitais").document("Consultas")
                        .collection("Agendamentos").document(consultaId)
                        .set(consulta)
                        .addOnSuccessListener(aVoid -> Toast.makeText(this, "Consulta atualizada com sucesso!", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(this, "Erro ao atualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
                // Criar nova consulta
                db.collection("Hospitais").document("Consultas")
                        .collection("Agendamentos")
                        .add(consulta)
                        .addOnSuccessListener(documentReference -> Toast.makeText(this, "Consulta agendada com sucesso!", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(this, "Erro ao agendar: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

}
