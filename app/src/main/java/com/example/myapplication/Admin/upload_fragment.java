package com.example.myapplication.Admin;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.UploadProgressListener;
import com.example.myapplication.R;
import com.obsez.android.lib.filechooser.ChooserDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import dmax.dialog.SpotsDialog;

public class upload_fragment extends Fragment {

    private String url = "http://13.235.100.235:8000/api/upload/locations/";
    private String token;
    private static final String TAG = "UploadFragment";
    private String filePath;
    private File csvFile;
    private AlertDialog uploadingDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.upload_fragment, container, false);
        CardView uploadCard = view.findViewById(R.id.card1);
        SharedPreferences prefs = getActivity().getSharedPreferences("tokenFile", Context.MODE_PRIVATE);
        token = prefs.getString("token", "");
        Log.d("url", "onCreateView: " + url);
        uploadCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCsvPicker();

            }
        });


        return view;
    }


    private void openCsvPicker() {
        File file = Environment.getExternalStorageDirectory();
        String start = file.getAbsolutePath();
        new ChooserDialog(getActivity())
                .withStartFile(start)
                .withChosenListener(new ChooserDialog.Result() {
                    @Override
                    public void onChoosePath(String s, File file) {
                        filePath = s;
                        csvFile = file;
                        uploadingDialog = new SpotsDialog.Builder().setContext(getActivity())
                                .setMessage("Uploading Csv...")
                                .setCancelable(false)
                                .build();
                        uploadingDialog.show();
                        uploadCsv();
                    }
                })
                .withOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        dialogInterface.cancel();
                    }
                })
                .build()
                .show();
    }

    private void uploadCsv() {
        AndroidNetworking.upload(url)
                .addHeaders("Authorization", "Token " + token)
                .addMultipartFile("location_csv", csvFile)
                .setTag("Upload Csv")
                .setPriority(Priority.HIGH)
                .build()
                .setUploadProgressListener(new UploadProgressListener() {
                    @Override
                    public void onProgress(long bytesUploaded, long totalBytes) {
                        Log.d(TAG, "onProgress: " + bytesUploaded);
                    }
                })
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "onResponse: " + response);
                        String count;
                        try {
                            JSONObject rootObject = new JSONObject(String.valueOf(response));
                            count = rootObject.getString("count");
                            Toast.makeText(getActivity(), "Successfully Uploaded " + count + " locations", Toast.LENGTH_LONG).show();
                            uploadingDialog.dismiss();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            uploadingDialog.dismiss();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.d(TAG, "onError: " + anError.getErrorDetail() + " " + anError.getErrorBody() +
                                " " + anError.getMessage() + " " + anError.getErrorCode());
                        Toast.makeText(getActivity(), "Sorry something went wrong, please try again!", Toast.LENGTH_LONG).show();
                        uploadingDialog.dismiss();
                    }
                });
    }
}
