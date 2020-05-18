package com.example.graduationproject;




import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Face;
import android.hardware.Camera.FaceDetectionListener;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.media.AudioManager;
import android.media.MediaActionSound;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class androidCamera extends Activity implements SurfaceHolder.Callback{
    int faceFrameNum=0;
    String path=null;
    Camera camera;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    boolean previewing = false;
    LayoutInflater controlInflater = null;
    Bitmap bitmap;
    Button buttonTakePicture;
    TextView prompt;
    String imageUrl;
    registrationFaceHandler r;
    facialLoginHandler flh;
    registrationHandler rh;
    final int RESULT_SAVEIMAGE = 0;
    BitmapFactory.Options option ;
    String user_id="0";
    Integer mode;
    String facialRecID;
    MediaActionSound sound;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mode =getIntent().getIntExtra("mode",0);
        if(mode==2){

            try {
                registerName();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
//        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        sound = new MediaActionSound();
        setContentView(R.layout.activity_authentication);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().setFormat(PixelFormat.UNKNOWN);
        surfaceView = (SurfaceView)findViewById(R.id.camerapreview);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        option=new BitmapFactory.Options();
        controlInflater = LayoutInflater.from(getBaseContext());
        option.inJustDecodeBounds=false;
        LayoutParams layoutParamsControl
                = new LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.FILL_PARENT);

    }

    FaceDetectionListener faceDetectionListener
            = new FaceDetectionListener(){

        @Override
        public void onFaceDetection(Face[] faces, Camera camera) {

            if (faces.length == 0){

            }else{
                faceFrameNum++;
                if(faceFrameNum==15) {
                    camera.takePicture(null,
                            null, myPictureCallback_JPG);

                }


            }

        }};

    AutoFocusCallback myAutoFocusCallback = new AutoFocusCallback(){

        @Override
        public void onAutoFocus(boolean arg0, Camera arg1) {
            // TODO Auto-generated method stub
            buttonTakePicture.setEnabled(true);
        }};

    ShutterCallback myShutterCallback = new ShutterCallback(){

        @Override
        public void onShutter() {
            // TODO Auto-generated method stub
            sound.play(MediaActionSound.SHUTTER_CLICK);
        }};

    PictureCallback myPictureCallback_RAW = new PictureCallback(){

        @Override
        public void onPictureTaken(byte[] arg0, Camera arg1) {
            // TODO Auto-generated method stub
            bitmap = BitmapFactory.decodeByteArray(arg0, 0, arg0.length,option);
            Toast.makeText(getApplicationContext(),"No bitmap1", Toast.LENGTH_SHORT).show();

        }};

    PictureCallback myPictureCallback_JPG = new PictureCallback(){

        @Override
        public void onPictureTaken(byte[] arg0, Camera arg1) {
            // TODO Auto-generated method stub
            bitmap = BitmapFactory.decodeByteArray(arg0, 0, arg0.length,option);
            Log.d("xx",bitmap.toString());
            if(bitmap==null){
                Toast.makeText(getApplicationContext(),"No bitmap", Toast.LENGTH_SHORT).show();

            }


            camera.stopPreview();
            if(mode==1){
                login();
            }else if(mode==2){

                registerFace();
            }

        }};

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        // TODO Auto-generated method stub
        if(previewing){
            camera.stopFaceDetection();
            camera.stopPreview();
            previewing = false;
        }

        if (camera != null){
            try {
                camera.setPreviewDisplay(surfaceHolder);
                camera.startPreview();
                camera.startFaceDetection();
                previewing = true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        camera = Camera.open(1);
        camera.setDisplayOrientation(90);

        camera.setFaceDetectionListener(faceDetectionListener);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        camera.stopFaceDetection();
        camera.stopPreview();
        camera.release();
        camera = null;
        previewing = false;
    }
    @SuppressLint("StaticFieldLeak")
    public void login(){
        if(bitmap!=null) {
            flh = new facialLoginHandler() {
                protected void onPostExecute(String result) {
                    Toast.makeText(getApplicationContext(), "Welcome back, " + result, Toast.LENGTH_SHORT).show();
                    System.out.println(result);

                }

            };

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();

            StorageReference LoginPicReference = storageRef.child("LoginImage.jpg");


            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            Matrix matrix = new Matrix();
            matrix.postRotate(-90);
            bitmap= Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

            byte[] data = baos.toByteArray();

            UploadTask uploadTask = LoginPicReference.putBytes(data);
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
    @SuppressLint("StaticFieldLeak")
    public void registerFace(){

        if(bitmap !=null) {
            r = new registrationFaceHandler();

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();

            StorageReference RegistrationPicReference = storageRef.child("RegistrationImage.jpg");



            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Matrix matrix = new Matrix();
            matrix.postRotate(-90);
            bitmap= Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            UploadTask uploadTask = RegistrationPicReference.putBytes(data);
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
                                    System.out.println(user_id);
                                    r.execute(encode(imageUrl), "31727");
                                    ProceedToDetection();
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
    public void registerName() throws Exception {
        String firstname="user";
        String lastname=user_id;
        if(!firstname.isEmpty() && !firstname.equals("First Name") && !lastname.equals("Last Name") && !lastname.isEmpty()) {
            rh = new registrationHandler(){
                protected void onPostExecute(String result) {
                    Toast.makeText(getApplicationContext(),result, Toast.LENGTH_SHORT).show();
                    facialRecID=result;
                    addNewUser(facialRecID);
                }

            };

            rh.execute(firstname,lastname);
//       user_id=rh.execute(firstname,lastname).get();

        }else{
            Toast.makeText(getApplicationContext(),"Please enter username first.", Toast.LENGTH_SHORT).show();

        }
    }
    public void ProceedToDetection(){
        Intent i = new Intent(this, DetectorActivity.class);

        startActivity(i);
    }
    void addNewUser(String result){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();
        user_id=myRef.push().getKey();
        myRef.push().setValue(result);
    }
}
    