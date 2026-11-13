package com.example.weeklysync;

public class Tarefa {
    private int tarPk;
    private String nomeTarefa;
    private String nomePessoa;
    private String diaSemana;
    private boolean concluida;

    public Tarefa(int tarPk, String nomeTarefa, String diaSemana, boolean concluida, String nomePessoa) {
        this.tarPk = tarPk;
        this.nomePessoa = nomePessoa;
        this.nomeTarefa = nomeTarefa;
        this.diaSemana = diaSemana;
        this.concluida = concluida;
    }

    public Tarefa(String nomeTarefa, String diaSemana, boolean concluida, String nomePessoa) {
        this.nomeTarefa = nomeTarefa;
        this.diaSemana = diaSemana;
        this.concluida = concluida;
        this.nomePessoa = nomePessoa;
    }

    public String getNomePessoa() {
        return nomePessoa;
    }

    public void setNomePessoa(String nomePessoa) {
        this.nomePessoa = nomePessoa;
    }

    public int getTarPk() {
        return tarPk;
    }

    public void setTarPk(int tarPk) {
        this.tarPk = tarPk;
    }

    public boolean isConcluida() {
        return concluida;
    }

    public void setConcluida(boolean concluida) {
        this.concluida = concluida;
    }

    public String getNomeTarefa() {
        return nomeTarefa;
    }

    public void setNomeTarefa(String nomeTarefa) {
        this.nomeTarefa = nomeTarefa;
    }

    public String getDiaSemana() {
        return diaSemana;
    }

    public void setDiaSemana(String diaSemana) {
        this.diaSemana = diaSemana;
    }
}
