package com.example.weeklysync;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

public class LoginActivity extends AppCompatActivity {

    private EditText editEmail, editSenha;
    private Button buttonEntrar, buttonCadastrar;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_login);

        dbHelper = new DatabaseHelper(this);

        editEmail = findViewById(R.id.editEmail);
        editSenha = findViewById(R.id.editSenha);
        buttonEntrar = findViewById(R.id.btnEntrar);
        buttonCadastrar = findViewById(R.id.btnCadastrar);

        buttonEntrar.setOnClickListener(v -> realizarLogin());
        buttonCadastrar.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, CadastroActivity.class);
            startActivity(intent);
        });
    }

    private void realizarLogin() {
        String email = editEmail.getText().toString().trim();
        String senha = editSenha.getText().toString().trim();

        if (email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha o e-mail e a senha!", Toast.LENGTH_SHORT).show();
            return;
        }

        String nomePessoa = dbHelper.autenticarUsuario(email, senha);
        if (nomePessoa != null) {
            Toast.makeText(this, "Bem-vinda, " + nomePessoa + "!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("NOME_PESSOA", nomePessoa);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "E-mail ou senha incorretos!", Toast.LENGTH_SHORT).show();
        }
    }
}