package com.example.myapplication;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.airbnb.lottie.LottieAnimationView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class ChatLoading extends Thread {

    public interface ApiCallback{
        void onResponseReceived(String response);
    }

    private ApiCallback apiCallback;
    private String apiUrl;
    private String ChatFileName;

    Context context;


    public ChatLoading(ApiCallback apiCallback, String apiUrl, String chatFileName, Context context) {
        this.apiCallback = apiCallback;
        this.apiUrl = apiUrl;
        this.ChatFileName = chatFileName;
        this.context = context;

    }

    String responseBody;
    @Override
    public void run() {
        super.run();

        Log.d("Gaurav","Inside chatLoading Task");
        Log.d("Gaurav","MY url : "+apiUrl);

        Log.d("Gaurav","file name in chatloading : "+ChatFileName);
        File chatFile = new File(context.getFilesDir(),ChatFileName);
        Log.d("Gaurav","Got chat file");

        StringBuilder stringBuilder = new StringBuilder();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(chatFile));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("Gaurav","contents in chatLoading  : "+stringBuilder);

        // defining okhttp client
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(150, TimeUnit.SECONDS)
                .readTimeout(150, TimeUnit.SECONDS)
                .writeTimeout(150, TimeUnit.SECONDS)
                .build();

        // Create an instance of the MultipartBody.Builder class to build the request body

        MultipartBody.Builder requestBodyBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", ChatFileName, RequestBody.create(MediaType.parse("text/plain"), chatFile));

// Create a Request object with the request body and URL
        Request request = new Request.Builder()
                .url(apiUrl)
                .post(requestBodyBuilder.build())
                .build();

        Log.d("Gaurav","Ready to call request");
        Response response = null;

        // request call
        client.newCall(request).enqueue(new Callback() {
            //String responseBody;
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                int statusCode = response.code();
                responseBody = response.body().string();

                if (response.isSuccessful()) {
                    // Handle successful response from the API
                    Log.d("Gaurav","Response : "+responseBody);
                    if (apiCallback != null) {
                        apiCallback.onResponseReceived(responseBody);
                    }

                } else {
                    // Handle error response from the APi
                    Log.d("Gaurav","Response is not successful "+responseBody);
                }
                if (statusCode == 200) {
                    // Handle successful response from the API
                    //apiCallback.responseNotReceived();

                    Log.d("Gaurav","status code : 200");
                } else {
                    // Handle error response from the API

                    Log.d("Gaurav","Status code : 400");
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                // Handle failure to connect to the API
                Log.d("Gaurav","error message : "+e.getMessage());
                Log.d("Gaurav","Response Failure");
            }
        });

        }



}