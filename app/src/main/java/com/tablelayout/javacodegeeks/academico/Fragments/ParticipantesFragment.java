package com.tablelayout.javacodegeeks.academico.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.tablelayout.javacodegeeks.academico.Adapters.ParticipantesAdapter;
import com.tablelayout.javacodegeeks.academico.Domain.Aluno;
import com.tablelayout.javacodegeeks.academico.R;
import cz.msebera.android.httpclient.Header;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import org.json.JSONArray;
import org.json.JSONException;


public class ParticipantesFragment extends Fragment {

  private RecyclerView recyclerView;
  private ParticipantesAdapter participantesAdapter;

  CircularProgressView mProgressBar;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View inflate = inflater.inflate(R.layout.second_participantes_fragment, container, false);

    recyclerView = inflate.findViewById(R.id.fragment_second_list_view);
    mProgressBar = inflate.findViewById(R.id.progress_bar);

    LinearLayoutManager llm = new LinearLayoutManager(getContext());
    llm.setOrientation(LinearLayoutManager.VERTICAL);
    recyclerView.setLayoutManager(llm);

    return inflate;
  }

  private void getParticipantes() {
    final ArrayList<Aluno> participantes = new ArrayList();
    AsyncHttpClient client = new AsyncHttpClient();
    client.get(ConnectionUtils.getParticipantes(), new JsonHttpResponseHandler() {
      @Override
      public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
        super.onSuccess(statusCode, headers, response);
        try { for (int i = 0; i < response.length(); i++) {
          Aluno aluno = new Aluno();
          aluno.setQtdePresenca(response.getJSONObject(i).getString("qtdePresenca"));
          aluno.setNomeAluno(response.getJSONObject(i).getString("nomeAluno"));
          participantes.add(aluno);
        }

          if (!participantes.isEmpty()) {
            Collections.sort(participantes, new Comparator<Aluno>() {
              @Override
              public int compare(Aluno aluno, Aluno t1) {
                return - aluno.getQtdePresenca().compareTo(t1.getQtdePresenca());
              }
            });

            showParticipantes(participantes);
          }
        } catch (JSONException e) {
          mProgressBar.setVisibility(View.GONE);
          Toast.makeText(getContext(),
              "ERRO AO CONVERTER OBJETO", Toast.LENGTH_SHORT)
              .show();
          e.printStackTrace();
        }
      }

      @Override
      public void onFailure(int statusCode, Header[] headers, String responseString,
          Throwable throwable) {
        super.onFailure(statusCode, headers, responseString, throwable);
        mProgressBar.setVisibility(View.GONE);
        Toast.makeText(getContext(),
            "ERRO NA COMUNICAÇÃO COM O SERVIDOR", Toast.LENGTH_SHORT)
            .show();
      }
    });
  }

  private void showParticipantes(
      ArrayList<Aluno> participantes) {
    participantesAdapter = new ParticipantesAdapter(participantes);
    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    recyclerView.setAdapter(participantesAdapter);

    mProgressBar.setVisibility(View.GONE);
  }

  @Override
  public void setMenuVisibility(boolean menuVisible) {
    super.setMenuVisibility(menuVisible);
    if (menuVisible) {
      mProgressBar.setVisibility(View.VISIBLE);
      getParticipantes();
    }
  }

}
