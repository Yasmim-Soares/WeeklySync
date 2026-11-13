package com.example.weeklysync;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "weeklysync.db";
    private static final int DATABASE_VERSION = 2; // Subimos para 2 para recriar as tabelas com nomePessoa!

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE usuarios (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nome TEXT, " +
                "email TEXT UNIQUE, " +
                "senha TEXT)");

        db.execSQL("CREATE TABLE tarefas (" +
                "tarPk INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nomeTarefa TEXT, " +
                "diaSemana TEXT, " +
                "concluida INTEGER, " +
                "nomePessoa TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS usuarios");
        db.execSQL("DROP TABLE IF EXISTS tarefas");
        onCreate(db);
    }

    private String gerarHashSenha(String senha) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(senha.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar hash", e);
        }
    }

    public boolean cadastrarUsuario(String nome, String email, String senha) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM usuarios WHERE email = ?", new String[]{email});
        if (cursor.moveToFirst()) {
            cursor.close();
            return false;
        }
        cursor.close();

        ContentValues values = new ContentValues();
        values.put("nome", nome);
        values.put("email", email);
        values.put("senha", gerarHashSenha(senha));
        return db.insert("usuarios", null, values) != -1;
    }

    public String autenticarUsuario(String email, String senha) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT nome FROM usuarios WHERE email = ? AND senha = ?",
                new String[]{email, gerarHashSenha(senha)});

        if (cursor.moveToFirst()) {
            String nome = cursor.getString(0);
            cursor.close();
            return nome;
        }
        cursor.close();
        return null;
    }

    public void inserirTarefa(Tarefa tarefa) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nomeTarefa", tarefa.getNomeTarefa());
        values.put("diaSemana", tarefa.getDiaSemana());
        values.put("concluida", tarefa.isConcluida() ? 1 : 0);
        values.put("nomePessoa", tarefa.getNomePessoa());
        db.insert("tarefas", null, values);
    }

    public void atualizarStatusTarefa(int tarPk, boolean concluida) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("concluida", concluida ? 1 : 0);
        db.update("tarefas", values, "tarPk = ?", new String[]{String.valueOf(tarPk)});
    }

    public void excluirTarefa(int tarPk) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("tarefas", "tarPk = ?", new String[]{String.valueOf(tarPk)});
    }

    public List<Tarefa> listarTarefasPorPessoa(String nomePessoa) {
        List<Tarefa> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT tarPk, nomeTarefa, diaSemana, concluida, nomePessoa FROM tarefas WHERE nomePessoa = ? ORDER BY tarPk DESC",
                new String[]{nomePessoa});

        if (cursor.moveToFirst()) {
            do {
                lista.add(new Tarefa(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getInt(3) == 1,
                        cursor.getString(4)
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }
}