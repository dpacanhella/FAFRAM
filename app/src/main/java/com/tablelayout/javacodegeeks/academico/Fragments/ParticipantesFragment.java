package com.tablelayout.javacodegeeks.academico.Fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.os.StrictMode.VmPolicy.Builder;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.ListItem;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfString;
import com.itextpdf.text.pdf.PdfWriter;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.pepperonas.materialdialog.MaterialDialog;
import com.tablelayout.javacodegeeks.academico.Adapters.ParticipantesAdapter;
import com.tablelayout.javacodegeeks.academico.Domain.Aluno;
import com.tablelayout.javacodegeeks.academico.R;
import cz.msebera.android.httpclient.Header;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;


public class ParticipantesFragment extends Fragment implements View.OnClickListener{

  private RecyclerView recyclerView;
  private ParticipantesAdapter participantesAdapter;
  private EditText qtdeAprovados;
  private ProgressDialog progressDialog;

  CircularProgressView mProgressBar;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View inflate = inflater.inflate(R.layout.second_participantes_fragment, container, false);

    recyclerView = inflate.findViewById(R.id.fragment_second_list_view);
    mProgressBar = inflate.findViewById(R.id.progress_bar);
    qtdeAprovados = inflate.findViewById(R.id.editText_enviarAprovados);

    LinearLayoutManager llm = new LinearLayoutManager(getContext());
    llm.setOrientation(LinearLayoutManager.VERTICAL);
    recyclerView.setLayoutManager(llm);

    return inflate;
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    getView().findViewById(R.id.send_list_aprovados).setOnClickListener(this);
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
            showParticipantes(participantes);
          }

          mProgressBar.setVisibility(View.GONE);
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

  @Override
  public void onClick(View view) {
    if (qtdeAprovados.getText().length() > 0) {
      new MaterialDialog.Builder(getContext())
          .title("Aprovados")
          .message("Deseja enviar a lista de participantes que tiveram acima de " + qtdeAprovados.getText() + " presenças?")
          .positiveText("ENVIAR")
          .negativeText("CANCELAR")
          .positiveColor(R.color.green_700)
          .negativeColor(R.color.red_500)
          .buttonCallback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog dialog) {
              super.onPositive(dialog);
              progressDialog = ProgressDialog
                  .show(getContext(), "AGUARDE", "Convertendo para enviar lista...");

              final RequestParams params = new RequestParams();
              params.add("qtdePresenca", qtdeAprovados.getText().toString());

              final ArrayList<Aluno> participantes = new ArrayList();
              AsyncHttpClient client = new AsyncHttpClient();
              client.get(ConnectionUtils.getAprovados(), params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                  super.onSuccess(statusCode, headers, response);
                  try { for (int i = 0; i < response.length(); i++) {
                    Aluno aluno = new Aluno();
                    aluno.setQtdePresenca(response.getJSONObject(i).getString("qtdePresenca"));
                    aluno.setNomeAluno(response.getJSONObject(i).getString("nomeAluno"));
                    aluno.setCodigoAluno(response.getJSONObject(i).getString("codigoAluno"));
                    participantes.add(aluno);
                  }

                    if (!participantes.isEmpty()) {
//                      showParticipantes(participantes);
                      try {
                        enviarAprovados(participantes, qtdeAprovados);
                      } catch (DocumentException e) {
                        mProgressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(),
                            "ERRO AO GERAR PDF", Toast.LENGTH_SHORT)
                            .show();
                        e.printStackTrace();
                      } catch (IOException e) {
                        e.printStackTrace();
                      }
                    }

                    mProgressBar.setVisibility(View.GONE);
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


            @Override
            public void onNegative(MaterialDialog dialog) {
              super.onNegative(dialog);
              Toast.makeText(getContext(), "Erro na comunicação com o servidor!", Toast.LENGTH_SHORT)
                  .show();
                 }
          }).show();

    } else {
      Toast.makeText(getContext(),
          "Insira a quantidade de presenças dos participantes", Toast.LENGTH_SHORT)
          .show();
    }

  }

  private void enviarAprovados(ArrayList<Aluno> aprovados, EditText qtdeAprovados)
      throws DocumentException, IOException {

    File file = File.createTempFile("aprovados", ".pdf", getContext().getExternalCacheDir());
    file.getParentFile().mkdirs();

    try {
      createPdf(file.getAbsolutePath(), qtdeAprovados, aprovados);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

    StrictMode.VmPolicy.Builder builder = new Builder();
    StrictMode.setVmPolicy(builder.build());

    Uri uri = Uri.fromFile(file);
    Intent share = new Intent();
    share.setAction(Intent.ACTION_SEND);
    share.setType("application/pdf");
    share.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    share.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    share.putExtra(Intent.EXTRA_STREAM, uri);

    startActivity(Intent.createChooser(share, "Enviar arquivo"));

    progressDialog.dismiss();
    Toast.makeText(getContext(),
        "AGUARDE...", Toast.LENGTH_SHORT)
        .show();
  }

  private void createPdf(String dest, EditText qtdeAprovados,
      ArrayList<Aluno> aprovados) throws FileNotFoundException, DocumentException {
    Document document = new Document();
    PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(dest));
    writer.setPdfVersion(PdfWriter.VERSION_1_7);
    writer.setTagged();
    writer.setViewerPreferences(PdfWriter.DisplayDocTitle);
    document.addLanguage("en-US");
    document.addTitle("Aprovados");
    writer.createXmpMetadata();
    document.open();

    Paragraph p = new Paragraph();
    p.add("Aprovados que tiveram igual/acima de " + qtdeAprovados.getText() + " presenças");
    p.setAlignment(Element.ALIGN_CENTER);
    Font font = new Font();
    font.setSize(30);
    font.setStyle(3);
    p.setFont(font);
    document.add(p);

    for (Aluno al: aprovados) {
    Paragraph p2 = new Paragraph();
    p2.add(al.getCodigoAluno() + "   -  " + al.getNomeAluno());
    document.add(p2);
    }

    document.close();
  }
}