package com.example.weeklysync;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TarefaAdapter extends RecyclerView.Adapter<TarefaAdapter.TarefaViewHolder> {

    private final List<Tarefa> listaTarefas;
    private final OnTarefaInteractionListener listener;

    public interface OnTarefaInteractionListener {
        void onStatusChanged(Tarefa tarefa, boolean isChecked);
        void onDeleteClick(Tarefa tarefa);
    }

    public TarefaAdapter(List<Tarefa> listaTarefas, OnTarefaInteractionListener listener) {
        this.listaTarefas = listaTarefas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TarefaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tarefa, parent, false);
        return new TarefaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TarefaViewHolder holder, int position) {
        Tarefa tarefa = listaTarefas.get(position);

        holder.txtTitulo.setText(tarefa.getNomeTarefa());
        holder.txtDia.setText(tarefa.getDiaSemana());

        holder.checkTarefa.setOnCheckedChangeListener(null);
        holder.checkTarefa.setChecked(tarefa.isConcluida());

        if (tarefa.isConcluida()) {
            holder.txtTitulo.setPaintFlags(holder.txtTitulo.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.txtTitulo.setPaintFlags(holder.txtTitulo.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        holder.checkTarefa.setOnCheckedChangeListener((buttonView, isChecked) -> {
            tarefa.setConcluida(isChecked);
            listener.onStatusChanged(tarefa, isChecked);
        });

        holder.btnDeletar.setOnClickListener(v -> listener.onDeleteClick(tarefa));
    }

    @Override
    public int getItemCount() {
        return listaTarefas.size();
    }

    static class TarefaViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkTarefa;
        TextView txtTitulo, txtDia;
        ImageButton btnDeletar;

        public TarefaViewHolder(@NonNull View itemView) {
            super(itemView);
            checkTarefa = itemView.findViewById(R.id.checkTarefa);
            txtTitulo = itemView.findViewById(R.id.txtTituloTarefa);
            txtDia = itemView.findViewById(R.id.txtDiaTarefa);
            btnDeletar = itemView.findViewById(R.id.btnDeletarTarefa);
        }
    }
}