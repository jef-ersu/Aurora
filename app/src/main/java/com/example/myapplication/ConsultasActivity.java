package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ConsultasActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ListView consultasListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consultas);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        consultasListView = findViewById(R.id.consultasListView);

        String userId = mAuth.getCurrentUser().getUid();



        // Recuperar consultas do Firebase
        db.collection("Hospitais").document("Consultas").collection("Agendamentos")
                .whereEqualTo("PacienteId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        ArrayList<String> consultasList = new ArrayList<>();
                        queryDocumentSnapshots.forEach(document -> {
                            String especialidade = document.getString("Especialidade");
                            String data = document.getString("Data");
                            String hora = document.getString("Hora");
                            String nome = document.getString("Nome");
                            consultasList.add("Paciente: " + nome + "\n" +especialidade + " - " + data + " às " + hora);
                        });

                        // Exibir na ListView
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                ConsultasActivity.this,
                                android.R.layout.simple_list_item_1,
                                consultasList
                        );
                        consultasListView.setAdapter(adapter);
                    } else {
                        Toast.makeText(this, "Você não tem consultas marcadas.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao carregar consultas: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
