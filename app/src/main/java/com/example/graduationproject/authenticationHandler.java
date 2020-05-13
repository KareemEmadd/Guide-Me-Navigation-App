package com.example.graduationproject;


import android.os.AsyncTask;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;

class authenticationHandler extends AsyncTask<String, String,String> {
        protected String doInBackground(String... urls) {

            OkHttpClient client = new OkHttpClient();

            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
            RequestBody body = RequestBody.create(mediaType, "photo=https%3A%2F%2Fi.ibb.co%2Frfcj0rC%2Fkpic3.jpg");
            Request request = new Request.Builder()
                    .url("https://luxand-cloud-face-recognition.p.rapidapi.com/photo/search")
                    .post(body)
                    .addHeader("x-rapidapi-host", "luxand-cloud-face-recognition.p.rapidapi.com")
                    .addHeader("x-rapidapi-key", "b8f892acb6msh1fdb5ea8f22729ap18a944jsn01ddb7e55b18")
                    .addHeader("content-type", "application/x-www-form-urlencoded")
                    .build();

            try {
                Response response = client.newCall(request).execute();
                System.out.println(response.body().string());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;

}

    }

