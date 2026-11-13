package com.example.weeklysync;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.net.InetAddress;

public class CadastroActivity extends AppCompatActivity {

    private EditText editNome, editEmail, editSenha;
    private Button btnFinalizarCadastro, btnVoltarLogin;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        dbHelper = new DatabaseHelper(this);

        editNome = findViewById(R.id.editNomeCadastro);
        editEmail = findViewById(R.id.editEmailCadastro);
        editSenha = findViewById(R.id.editSenhaCadastro);
        btnFinalizarCadastro = findViewById(R.id.btnFinalizarCadastro);
        btnVoltarLogin = findViewById(R.id.btnVoltarLogin);

        btnFinalizarCadastro.setOnClickListener(v -> processarCadastro());
        btnVoltarLogin.setOnClickListener(v -> finish()); // Volta para a LoginActivity
    }

    private boolean senhaEhValida(String senha) {
        if (senha.length() < 8) return false;
        return senha.matches(".*[0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");
    }

    private void processarCadastro() {
        String nome = editNome.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String senha = editSenha.getText().toString().trim();

        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Formato de e-mail inválido!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!senhaEhValida(senha)) {
            Toast.makeText(this, "A senha precisa de 8+ caracteres e pelo menos 1 número ou símbolo!", Toast.LENGTH_LONG).show();
            return;
        }

        btnFinalizarCadastro.setEnabled(false);
        btnFinalizarCadastro.setText("Verificando e-mail...");

        new Thread(() -> {
            boolean emailExiste = verificarDominioExiste(email);

            runOnUiThread(() -> {
                btnFinalizarCadastro.setEnabled(true);
                btnFinalizarCadastro.setText("Cadastrar");

                if (!emailExiste) {
                    Toast.makeText(this, "Este domínio de e-mail não existe! Digite um e-mail real.", Toast.LENGTH_LONG).show();
                    return;
                }

                boolean sucesso = dbHelper.cadastrarUsuario(nome, email, senha);
                if (sucesso) {
                    Toast.makeText(this, "Conta criada com sucesso!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CadastroActivity.this, MainActivity.class);
                    intent.putExtra("NOME_PESSOA", nome);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Este e-mail já está cadastrado!", Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }

    private boolean verificarDominioExiste(String email) {
        try {
            String dominio = email.substring(email.indexOf("@") + 1);
            InetAddress.getByName(dominio);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}