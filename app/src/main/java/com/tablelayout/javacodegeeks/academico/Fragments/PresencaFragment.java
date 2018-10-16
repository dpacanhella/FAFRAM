package com.tablelayout.javacodegeeks.academico.Fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.pepperonas.materialdialog.MaterialDialog;
import com.tablelayout.javacodegeeks.academico.barcodereader.BarcodeCaptureActivity;
import com.tablelayout.javacodegeeks.academico.R;
import cz.msebera.android.httpclient.Header;

public class PresencaFragment extends Fragment implements View.OnClickListener{

    private static final String TAG = "PresencaFragment";

    private static final int RC_BARCODE_CAPTURE = 9001;
    private ProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.first_presenca_fragment, container, false);
    }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    getView().findViewById(R.id.read_barcode).setOnClickListener(this);
  }

  @Override
    public void onClick(View v) {
        if (v.getId() == R.id.read_barcode) {
//          BarcodeCaptureFragment fragment = new BarcodeCaptureFragment();
//          android.support.v4.app.FragmentManager fragmentManager = getFragmentManager();
//          android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//          fragmentTransaction.replace(R.i)


            Intent intent = new Intent(getContext(), BarcodeCaptureActivity.class);
            startActivityForResult(intent, RC_BARCODE_CAPTURE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);

                    if (!barcode.displayValue.isEmpty()) {

                        String codigoAluno = null;
                        String nomeAluno = null;

                        try {
                            String[] codigoNome = barcode.displayValue.split(" - ");
                            codigoAluno = codigoNome[0];
                            nomeAluno = codigoNome[1];

                            final String finalCodigoAluno = codigoAluno;
                            final String finalNomeAluno = nomeAluno;

                            new MaterialDialog.Builder(getContext())
                                .title("Controle de presença")
                                .message(codigoAluno + " - " + nomeAluno)
                                .positiveText("SALVAR")
                                .negativeText("CANCELAR")
                                .positiveColor(R.color.green_700)
                                .negativeColor(R.color.red_500)
                                .buttonCallback(new MaterialDialog.ButtonCallback() {
                                    @Override
                                    public void onPositive(final MaterialDialog dialog) {
                                        super.onPositive(dialog);

                                        progressDialog = ProgressDialog
                                            .show(getContext(), "AGUARDE", "Salvando aluno...");

                                        RequestParams params = new RequestParams();
                                        params.add("codigoAluno", finalCodigoAluno);
                                        params.add("nomeAluno", finalNomeAluno);

                                        AsyncHttpClient client = new AsyncHttpClient();
                                        client.post(ConnectionUtils.postPresenca(), params,
                                            new AsyncHttpResponseHandler() {
                                                @Override
                                                public void onSuccess(int statusCode, Header[] headers,
                                                    byte[] responseBody) {
                                                    Toast.makeText(getContext(), "PRESENÇA CONFIRMADA", Toast.LENGTH_SHORT)
                                                        .show();
                                                    progressDialog.dismiss();
                                                }

                                                @Override
                                                public void onFailure(int statusCode, Header[] headers,
                                                    byte[] responseBody, Throwable error) {
                                                    Toast.makeText(getContext(),
                                                        "ERRO NA COMUNICAÇÃO COM O SERVIDOR", Toast.LENGTH_SHORT)
                                                        .show();
                                                    progressDialog.dismiss();
                                                }
                                            });

                                    }

                                    @Override
                                    public void onNegative(MaterialDialog dialog) {
                                        super.onNegative(dialog);
                                        Toast.makeText(getContext(), "Aluno não salvo!", Toast.LENGTH_SHORT)
                                            .show();
                                        progressDialog.dismiss();
                                    }

                                }).show();
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Não foi possível ler aluno", Toast.LENGTH_SHORT)
                                .show();
                        }

                    } else {
                        Log.d(TAG, getString(R.string.barcode_failure));
                        progressDialog.dismiss();
                    }
                } else {
                    Toast.makeText(getContext(),
                        "TENTE NOVAMENTE", Toast.LENGTH_SHORT)
                        .show();
                    progressDialog.dismiss();
                }
            }else {
                Log.d(TAG, getString(R.string.barcode_error));
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
