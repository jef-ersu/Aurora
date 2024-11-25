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

        String especialidade = getIntent().getStringExtra("especialidade");
        especialidadeText.setText("Especialidade: " + especialidade);

        bookButton.setOnClickListener(v -> {
            String date = dateInput.getText().toString().trim(); // Formato esperado: dd/MM/yyyy
            String time = timeInput.getText().toString().trim();
            String userId = mAuth.getCurrentUser().getUid();

            if (date.isEmpty() || time.isEmpty()) {
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

            // Verificar conflito de horário
            db.collection("Hospitais").document("Consultas").collection("Agendamentos")
                    .whereEqualTo("Data", date)
                    .whereEqualTo("Hora", time)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            Toast.makeText(this, "Horário já reservado!", Toast.LENGTH_SHORT).show();
                        } else {
                            // Salvar a consulta com a data completa
                            Map<String, Object> consulta = new HashMap<>();
                            consulta.put("Especialidade", especialidade);
                            consulta.put("Data", date); // Data completa como campo único
                            consulta.put("Hora", time);
                            consulta.put("PacienteId", userId);

                            db.collection("Hospitais").document("Consultas")
                                    .collection("Agendamentos")
                                    .add(consulta)
                                    .addOnSuccessListener(documentReference ->
                                            Toast.makeText(this, "Consulta agendada com sucesso!", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e ->
                                            Toast.makeText(this, "Erro ao agendar: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Erro ao verificar disponibilidade: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

    }
}
