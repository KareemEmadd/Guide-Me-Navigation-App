package com.example.graduationproject;


import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.Toast;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

class facialLoginHandler extends AsyncTask<String, String,String> {
    String id;

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
//        Toast.makeText(,"Please capture image first using open camera button.", Toast.LENGTH_SHORT).show();
            System.out.println(name);
    }

    String name;
    protected String doInBackground(String... names) {
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "photo="+names[0]);
//        Request request = new Request.Builder()
//                .url("https://luxand-cloud-face-recognition.p.rapidapi.com/photo/search")
//                .post(body)
//                .addHeader("x-rapidapi-host", "luxand-cloud-face-recognition.p.rapidapi.com")
//                .addHeader("x-rapidapi-key", "b8f892acb6msh1fdb5ea8f22729ap18a944jsn01ddb7e55b18")
//                .addHeader("content-type", "application/x-www-form-urlencoded")
//                .build();
        Request request = new Request.Builder()
                .url("https://luxand-cloud-face-recognition.p.rapidapi.com/photo/search")
                .post(body)
                .addHeader("x-rapidapi-host", "luxand-cloud-face-recognition.p.rapidapi.com")
                .addHeader("x-rapidapi-key", "cba9f48ac0mshe2dd7bf8140600bp10352fjsn0f9e5daa2cef")
                .addHeader("content-type", "application/x-www-form-urlencoded")
                .build();
        try {
            Response response = client.newCall(request).execute();
//            System.out.println(response.body().toString());
            JSONArray jArr= new JSONArray(response.body().string());
            JSONObject obj = new JSONObject(jArr.getJSONObject(0).toString());
            id = obj.getString("id");
            name=obj.getString("name").toString();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return name;

    }

}

