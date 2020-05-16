package com.example.graduationproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    ImageView img;
    Bitmap bitmap;
    String encodedImage;
    Button btnCamera;
    String newImage;
    String imageUrl;
    registrationFaceHandler r;
    facialLoginHandler flh;
    registrationHandler rh;
    EditText fname;
    EditText lname;
    String user_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fname=(EditText)findViewById(R.id.editText);
        lname=(EditText)findViewById(R.id.editText2);

        btnCamera=(Button)findViewById(R.id.cameraBtn);
        img=(ImageView)findViewById(R.id.img);
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,0);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        bitmap=(Bitmap)data.getExtras().get("data");
        img.setImageBitmap(bitmap);




    }

    @SuppressLint("StaticFieldLeak")
    public void registerName(View v) throws ExecutionException, InterruptedException {
        String firstname=fname.getText().toString();
        String lastname=lname.getText().toString();
        if(!firstname.isEmpty() && !firstname.equals("First Name") && !lastname.equals("Last Name") && !lastname.isEmpty()) {
             rh = new registrationHandler(){
                protected void onPostExecute(String result) {
                    Toast.makeText(getApplicationContext(),result+"haaaaaaaaaaaaaaa", Toast.LENGTH_SHORT).show();
                    user_id=result;
                }

            };

             rh.execute(firstname,lastname);
//       user_id=rh.execute(firstname,lastname).get();

        }else{
            Toast.makeText(getApplicationContext(),"Please enter username first.", Toast.LENGTH_SHORT).show();

        }
        }
    @SuppressLint("StaticFieldLeak")
    public void registerFace(View v){

        if(bitmap !=null) {
            r = new registrationFaceHandler();

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();

// Create a reference to "mountains.jpg"
            StorageReference mountainsRef = storageRef.child("mountains.jpg");

// Create a reference to 'images/mountains.jpg'
            StorageReference mountainImagesRef = storageRef.child("images/mountains.jpg");


            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            UploadTask uploadTask = mountainsRef.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
//                Uri downloadUrl = taskSnapshot.getMetadata().getReference().getDownloadUrl().toString();

                    if (taskSnapshot.getMetadata() != null) {
                        if (taskSnapshot.getMetadata().getReference() != null) {
                            Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                            result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    imageUrl = uri.toString();
                                    //createNewPost(imageUrl);
                                    System.out.println(user_id);
                                    r.execute(encode(imageUrl), user_id);
                                }
                            });
                        }
                    }


                }
            });


        }else{
            Toast.makeText(getApplicationContext(),"Please capture image first using open camera button.", Toast.LENGTH_SHORT).show();

        }





    }

    @SuppressLint("StaticFieldLeak")
    public void login(View v){
        if(bitmap!=null) {
            flh = new facialLoginHandler() {
                protected void onPostExecute(String result) {
                    Toast.makeText(getApplicationContext(), "Welcome back, " + result, Toast.LENGTH_SHORT).show();

                }

            };

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();

// Create a reference to "mountains.jpg"
            StorageReference mountainsRef = storageRef.child("z.jpg");


            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            UploadTask uploadTask = mountainsRef.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    if (taskSnapshot.getMetadata() != null) {
                        if (taskSnapshot.getMetadata().getReference() != null) {
                            Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                            result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    imageUrl = uri.toString();
                                    //createNewPost(imageUrl);
                                    System.out.println(imageUrl);
                                    flh.execute(encode(imageUrl));
                                }
                            });
                        }
                    }


                }
            });


        }else{
            Toast.makeText(getApplicationContext(),"Please capture image first using open camera button.", Toast.LENGTH_SHORT).show();

        }
    }

    public static String encode(String url)
    {

        try {

            String encodeURL= URLEncoder.encode( url, "UTF-8" );

            return encodeURL;

        } catch (UnsupportedEncodingException e) {

            return "Issue while encoding" +e.getMessage();

        }

    }


}
