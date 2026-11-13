package com.example.weeklysync;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private String nomePessoaLogada;

    private LinearLayout layoutAbaSemana, layoutHistoricoMeses;
    private ScrollView layoutAbaMes;
    private Button btnAbaSemana, btnAbaMes, btnGerarRotinaIA;

    private CheckBox checkExercicioHoje, checkLeituraHoje;
    private TextView txtStatusMetasDiarias;

    private EditText editNovaTarefa;
    private Spinner spinnerDiaSemana;
    private RecyclerView recyclerTarefas;
    private TarefaAdapter adapter;
    private List<Tarefa> listaTarefas;

    private TextView txtLabelGraficoTarefas, txtLabelGraficoExercicio, txtLabelGraficoLeitura, txtFeedbackMensal;
    private ProgressBar barGraficoTarefas, barGraficoExercicio, barGraficoLeitura;

    private SharedPreferences prefs;
    private String dataHoje;
    private String mesAtual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        nomePessoaLogada = getIntent().getStringExtra("NOME_PESSOA");
        if (nomePessoaLogada == null || nomePessoaLogada.isEmpty()) {
            nomePessoaLogada = "Estudante";
        }

        prefs = getSharedPreferences("HabitosPrefs_" + nomePessoaLogada, MODE_PRIVATE);
        dataHoje = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        mesAtual = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(new Date());

        inicializarComponentes();
        configurarAbas();
        configurarSpinnerDias();
        configurarHabitosDiarios();
        carregarListaTarefas();
    }

    private void inicializarComponentes() {
        layoutAbaSemana = findViewById(R.id.layoutAbaSemana);
        layoutAbaMes = findViewById(R.id.layoutAbaMes);
        btnAbaSemana = findViewById(R.id.btnAbaSemana);
        btnAbaMes = findViewById(R.id.btnAbaMes);
        btnGerarRotinaIA = findViewById(R.id.btnGerarRotinaIA);

        checkExercicioHoje = findViewById(R.id.checkExercicioHoje);
        checkLeituraHoje = findViewById(R.id.checkLeituraHoje);
        txtStatusMetasDiarias = findViewById(R.id.txtStatusMetasDiarias);

        editNovaTarefa = findViewById(R.id.editNovaTarefa);
        spinnerDiaSemana = findViewById(R.id.spinnerDiaSemana);
        Button btnAdicionarTarefa = findViewById(R.id.btnAdicionarTarefa);
        recyclerTarefas = findViewById(R.id.recyclerTarefas);
        recyclerTarefas.setLayoutManager(new LinearLayoutManager(this));

        txtLabelGraficoTarefas = findViewById(R.id.txtLabelGraficoTarefas);
        txtLabelGraficoExercicio = findViewById(R.id.txtLabelGraficoExercicio);
        txtLabelGraficoLeitura = findViewById(R.id.txtLabelGraficoLeitura);
        txtFeedbackMensal = findViewById(R.id.txtFeedbackMensal);
        barGraficoTarefas = findViewById(R.id.barGraficoTarefas);
        barGraficoExercicio = findViewById(R.id.barGraficoExercicio);
        barGraficoLeitura = findViewById(R.id.barGraficoLeitura);
        layoutHistoricoMeses = findViewById(R.id.layoutHistoricoMeses);

        btnAdicionarTarefa.setOnClickListener(v -> adicionarNovaTarefa());
        btnGerarRotinaIA.setOnClickListener(v -> abrirAssistenteIA());
    }

    private void configurarAbas() {
        btnAbaSemana.setOnClickListener(v -> {
            layoutAbaSemana.setVisibility(View.VISIBLE);
            layoutAbaMes.setVisibility(View.GONE);
            btnAbaSemana.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2D6A4F")));
            btnAbaSemana.setTextColor(Color.parseColor("#FFFFFF"));
            btnAbaMes.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#95B8A6")));
            btnAbaMes.setTextColor(Color.parseColor("#1B4332"));
        });

        btnAbaMes.setOnClickListener(v -> {
            layoutAbaSemana.setVisibility(View.GONE);
            layoutAbaMes.setVisibility(View.VISIBLE);
            btnAbaMes.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2D6A4F")));
            btnAbaMes.setTextColor(Color.parseColor("#FFFFFF"));
            btnAbaSemana.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#95B8A6")));
            btnAbaSemana.setTextColor(Color.parseColor("#1B4332"));
            atualizarGraficosDoMes();
        });
    }

    private void configurarSpinnerDias() {
        String[] dias = {"Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado", "Domingo"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, dias);
        spinnerDiaSemana.setAdapter(spinnerAdapter);
    }

    private void configurarHabitosDiarios() {
        boolean exercicioFeitoHoje = prefs.getBoolean("exercicio_" + dataHoje, false);
        boolean leituraFeitaHoje = prefs.getBoolean("leitura_" + dataHoje, false);

        checkExercicioHoje.setChecked(exercicioFeitoHoje);
        checkLeituraHoje.setChecked(leituraFeitaHoje);
        atualizarMensagemIncentivo();

        String chaveExercicioMes = "total_exercicio_" + mesAtual;
        String chaveLeituraMes = "total_leitura_" + mesAtual;

        checkExercicioHoje.setOnCheckedChangeListener((btn, isChecked) -> {
            prefs.edit().putBoolean("exercicio_" + dataHoje, isChecked).apply();
            int totalExercicio = prefs.getInt(chaveExercicioMes, 0);
            prefs.edit().putInt(chaveExercicioMes, Math.max(0, totalExercicio + (isChecked ? 1 : -1))).apply();
            atualizarMensagemIncentivo();
        });

        checkLeituraHoje.setOnCheckedChangeListener((btn, isChecked) -> {
            prefs.edit().putBoolean("leitura_" + dataHoje, isChecked).apply();
            int totalLeitura = prefs.getInt(chaveLeituraMes, 0);
            prefs.edit().putInt(chaveLeituraMes, Math.max(0, totalLeitura + (isChecked ? 1 : -1))).apply();
            atualizarMensagemIncentivo();
        });
    }
    private void atualizarMensagemIncentivo() {
        if (checkExercicioHoje.isChecked() && checkLeituraHoje.isChecked()) {
            txtStatusMetasDiarias.setText("Parabéns, " + nomePessoaLogada + "! Metas de hoje concluídas!");
            txtStatusMetasDiarias.setTextColor(Color.parseColor("#722F37"));
        } else {
            txtStatusMetasDiarias.setText("Metas de Hoje (" + nomePessoaLogada + ")");
            txtStatusMetasDiarias.setTextColor(Color.parseColor("#1B4332"));
        }
    }

    private void adicionarNovaTarefa() {
        String titulo = editNovaTarefa.getText().toString().trim();
        String dia = spinnerDiaSemana.getSelectedItem().toString();

        if (titulo.isEmpty()) {
            Toast.makeText(this, "Digite o nome da tarefa!", Toast.LENGTH_SHORT).show();
            return;
        }

        Tarefa nova = new Tarefa(titulo, dia, false, nomePessoaLogada);
        dbHelper.inserirTarefa(nova);

        editNovaTarefa.setText("");
        carregarListaTarefas();
    }

    private void abrirAssistenteIA() {
        EditText inputFoco = new EditText(this);
        inputFoco.setHint("Ex: Estudar Java, Treinar, Ler livro");
        inputFoco.setPadding(40, 30, 40, 30);

        new AlertDialog.Builder(this)
                .setTitle("✨ Assistente de Rotina IA")
                .setMessage("Digite seus objetivos separados por vírgula (,):")
                .setView(inputFoco)
                .setPositiveButton("Gerar Rotina", (dialog, which) -> {
                    String textoDigitado = inputFoco.getText().toString().trim();
                    if (textoDigitado.isEmpty()) {
                        Toast.makeText(this, "Digite pelo menos um objetivo!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    processarObjetivosComIA(textoDigitado);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void processarObjetivosComIA(String textoObjetivos) {
        final String GEMINI_API_KEY = "";

        Toast.makeText(this, "Consultando IA...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + GEMINI_API_KEY);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);

                String prompt = "O usuário informou estes objetivos separados por vírgula: '" + textoObjetivos + "'. " +
                        "Para CADA objetivo individualmente, crie tarefas práticas distribuídas de Segunda a Quinta e, obrigatoriamente, inclua uma tarefa de '(Revisão)' na Sexta para os estudos. " +
                        "Responda ESTRITAMENTE no formato: Nome da Tarefa|Dia;Nome da Tarefa|Dia (use apenas Segunda, Terça, Quarta, Quinta, Sexta, Sábado ou Domingo. Sem quebras de linha e sem texto extra).";

                JSONObject part = new JSONObject().put("text", prompt);
                JSONObject content = new JSONObject().put("parts", new JSONArray().put(part));
                JSONObject payload = new JSONObject().put("contents", new JSONArray().put(content));

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(payload.toString().getBytes(StandardCharsets.UTF_8));
                }

                StringBuilder resposta = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String linha;
                    while ((linha = br.readLine()) != null) resposta.append(linha);
                }

                JSONObject jsonResposta = new JSONObject(resposta.toString());
                String textoIA = jsonResposta.getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text").trim();

                runOnUiThread(() -> {
                    String[] itens = textoIA.split(";");
                    for (String item : itens) {
                        String[] partes = item.split("\\|");
                        if (partes.length == 2) {
                            dbHelper.inserirTarefa(new Tarefa(partes[0].trim(), partes[1].trim(), false, nomePessoaLogada));
                        }
                    }
                    carregarListaTarefas();
                    Toast.makeText(this, "Rotina inteligente gerada pela IA!", Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                runOnUiThread(() -> gerarRotinaSeparadaPorVirgula(textoObjetivos));
            }
        }).start();
    }

    private void gerarRotinaSeparadaPorVirgula(String textoObjetivos) {
        String[] objetivos = textoObjetivos.split(",");
        String[] diasDaSemana = {"Segunda", "Terça", "Quarta", "Quinta", "Sexta"};

        for (String objBruto : objetivos) {
            String objetivo = objBruto.trim();
            if (objetivo.isEmpty()) continue;

            objetivo = objetivo.substring(0, 1).toUpperCase() + objetivo.substring(1);

            if (objetivo.contains(":")) {
                String[] partes = objetivo.split(":");
                String nome = partes[0].trim();
                String dia = partes.length > 1 ? partes[1].trim() : "Segunda";
                dia = dia.substring(0, 1).toUpperCase() + dia.substring(1);

                dbHelper.inserirTarefa(new Tarefa(nome, dia, false, nomePessoaLogada));
            } else {
                for (String dia : diasDaSemana) {
                    dbHelper.inserirTarefa(new Tarefa(objetivo, dia, false, nomePessoaLogada));
                }
            }
        }

        dbHelper.inserirTarefa(new Tarefa("Revisão (Estudos)", "Sexta", false, nomePessoaLogada));

        carregarListaTarefas();
        Toast.makeText(this, "Rotina replicada para a semana inteira!", Toast.LENGTH_SHORT).show();
    }
    private void carregarListaTarefas() {
        listaTarefas = dbHelper.listarTarefasPorPessoa(nomePessoaLogada);
        adapter = new TarefaAdapter(listaTarefas, new TarefaAdapter.OnTarefaInteractionListener() {
            @Override
            public void onStatusChanged(Tarefa tarefa, boolean isChecked) {
                dbHelper.atualizarStatusTarefa(tarefa.getTarPk(), isChecked);
                carregarListaTarefas();
            }

            @Override
            public void onDeleteClick(Tarefa tarefa) {
                dbHelper.excluirTarefa(tarefa.getTarPk());
                carregarListaTarefas();
            }
        });
        recyclerTarefas.setAdapter(adapter);
    }

    private void atualizarGraficosDoMes() {
        int totalTarefas = listaTarefas.size();
        int concluidas = 0;
        for (Tarefa t : listaTarefas) {
            if (t.isConcluida()) concluidas++;
        }

        int percTarefas = totalTarefas > 0 ? (concluidas * 100) / totalTarefas : 0;
        int diasExercicio = prefs.getInt("total_exercicio_" + mesAtual, 0);
        int diasLeitura = prefs.getInt("total_leitura_" + mesAtual, 0);

        int percExercicio = Math.min(100, (diasExercicio * 100) / 30);
        int percLeitura = Math.min(100, (diasLeitura * 100) / 30);

        prefs.edit().putInt("perc_tarefas_" + mesAtual, percTarefas).apply();
        registrarMesNoHistorico(mesAtual);

        txtLabelGraficoTarefas.setText("[" + mesAtual + "] Tarefas Cumpridas: " + percTarefas + "% (" + concluidas + "/" + totalTarefas + ")");
        barGraficoTarefas.setProgress(percTarefas);

        txtLabelGraficoExercicio.setText("[" + mesAtual + "] Exercícios (30 min): " + diasExercicio + "/30 dias (" + percExercicio + "%)");
        barGraficoExercicio.setProgress(percExercicio);

        txtLabelGraficoLeitura.setText("[" + mesAtual + "] Hábito de Leitura: " + diasLeitura + "/30 dias (" + percLeitura + "%)");
        barGraficoLeitura.setProgress(percLeitura);

        if (percTarefas >= 50 || (diasExercicio >= 1 && diasLeitura >= 1)) {
            txtFeedbackMensal.setText("Excelente ritmo em " + mesAtual + ", " + nomePessoaLogada + "! Você está mantendo consistência.");
        } else {
            txtFeedbackMensal.setText("Continue marcando suas tarefas e hábitos em " + mesAtual + " para subir suas barras!");
        }

        renderizarBoardsAntigos();
    }
    private void registrarMesNoHistorico(String mes) {
        if (!prefs.contains("historico_iniciado")) {
            prefs.edit()
                    .putBoolean("historico_iniciado", true)
                    .putInt("perc_tarefas_2026-10", 85)
                    .putInt("total_exercicio_2026-10", 22)
                    .putInt("total_leitura_2026-10", 19)
                    .putInt("perc_tarefas_2026-09", 70)
                    .putInt("total_exercicio_2026-09", 18)
                    .putInt("total_leitura_2026-09", 15)
                    .putString("lista_meses", "2026-10;2026-09")
                    .apply();
        }

        String listaMeses = prefs.getString("lista_meses", "");
        if (!listaMeses.contains(mes)) {
            listaMeses = listaMeses.isEmpty() ? mes : mes + ";" + listaMeses;
            prefs.edit().putString("lista_meses", listaMeses).apply();
        }
    }

    private void renderizarBoardsAntigos() {
        layoutHistoricoMeses.removeAllViews();
        String listaMeses = prefs.getString("lista_meses", "");
        if (listaMeses.isEmpty()) return;

        String[] meses = listaMeses.split(";");
        for (String mes : meses) {
            if (mes.equals(mesAtual)) continue;

            int pTarefas = prefs.getInt("perc_tarefas_" + mes, 0);
            int dExercicio = prefs.getInt("total_exercicio_" + mes, 0);
            int dLeitura = prefs.getInt("total_leitura_" + mes, 0);
            int pExercicio = Math.min(100, (dExercicio * 100) / 30);
            int pLeitura = Math.min(100, (dLeitura * 100) / 30);

            LinearLayout cardMes = new LinearLayout(this);
            cardMes.setOrientation(LinearLayout.VERTICAL);
            cardMes.setBackgroundColor(Color.parseColor("#E8F3EC"));
            cardMes.setPadding(32, 28, 32, 28);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, 24);
            cardMes.setLayoutParams(params);

            TextView titulo = new TextView(this);
            titulo.setText("📅 Resumo Fechado: " + mes);
            titulo.setTextSize(15f);
            titulo.setTextColor(Color.parseColor("#722F37"));
            titulo.setPadding(0, 0, 0, 12);

            TextView dados = new TextView(this);
            dados.setText("• Tarefas Cumpridas: " + pTarefas + "%\n" +
                    "• Exercícios (30 min): " + dExercicio + "/30 dias (" + pExercicio + "%)\n" +
                    "• Hábito de Leitura: " + dLeitura + "/30 dias (" + pLeitura + "%)");
            dados.setTextSize(14f);
            dados.setTextColor(Color.parseColor("#1B4332"));

            cardMes.addView(titulo);
            cardMes.addView(dados);
            layoutHistoricoMeses.addView(cardMes);
        }
    }
}