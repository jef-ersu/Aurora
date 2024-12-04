package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ConsultasActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ListView consultasListView;
    private ArrayList<String> consultaIds; // Declarar aqui

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consultas);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        consultasListView = findViewById(R.id.consultasListView);

        consultaIds = new ArrayList<>(); // Inicializar aqui

        String userId = mAuth.getCurrentUser().getUid();

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
                            consultasList.add("Paciente: " + nome + "\n" + especialidade + " - " + data + " às " + hora);

                            consultaIds.add(document.getId()); // Adiciona os IDs das consultas
                        });

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
                .addOnFailureListener(e -> Toast.makeText(this, "Erro ao carregar consultas: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        consultasListView.setOnItemLongClickListener((parent, view, position, id) -> {
            String consultaId = consultaIds.get(position); // Pega o ID correspondente à posição

            new AlertDialog.Builder(this)
                    .setTitle("Opções da Consulta")
                    .setItems(new String[]{"Editar", "Excluir"}, (dialog, which) -> {
                        if (which == 0) {
                            editarConsulta(consultaId);
                        } else if (which == 1) {
                            excluirConsulta(consultaId);
                        }
                    })
                    .show();

            return true;
        });
    }

    private void editarConsulta(String consultaId) {
        db.collection("Hospitais").document("Consultas")
                .collection("Agendamentos").document(consultaId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Escolha a especialidade novamente!", Toast.LENGTH_SHORT).show();

                    // redireciona para a tela de criação
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao remover consulta: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }


    private void excluirConsulta(String consultaId) {
        db.collection("Hospitais").document("Consultas")
                .collection("Agendamentos").document(consultaId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Consulta removida com sucesso!", Toast.LENGTH_SHORT).show();
                    // Atualize a lista após exclusão
                    recreate();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}

