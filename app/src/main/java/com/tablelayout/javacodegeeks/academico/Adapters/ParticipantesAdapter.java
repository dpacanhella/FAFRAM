package com.tablelayout.javacodegeeks.academico.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.tablelayout.javacodegeeks.academico.Domain.Aluno;
import com.tablelayout.javacodegeeks.academico.R;
import java.util.ArrayList;

public class ParticipantesAdapter extends RecyclerView.Adapter<ParticipantesAdapter.ViewHolder> {

  ArrayList<Aluno> alunos;

  private View view;

  public ParticipantesAdapter(ArrayList<Aluno> participantes) {
    this.alunos = participantes;
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_participantes, parent, false);
    ParticipantesAdapter.ViewHolder viewHolder = new ParticipantesAdapter.ViewHolder(view);
    return viewHolder;
  }

  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {
    holder.populate(alunos.get(position));
  }

  @Override
  public int getItemCount() {
    return alunos.size();
  }

  public class ViewHolder extends RecyclerView.ViewHolder {
    public TextView txtNameAluno;
    public TextView txtCodigoAluno;

    public ViewHolder(View view) {
      super(view);
      txtNameAluno  = itemView.findViewById(R.id.item_nome_aluno);
      txtCodigoAluno = itemView.findViewById(R.id.item_codigo_aluno);
    }

    public void populate(Aluno aluno) {
      txtNameAluno.setText(aluno.getNomeAluno());
      txtCodigoAluno.setText(aluno.getQtdePresenca());
    }
  }
}
