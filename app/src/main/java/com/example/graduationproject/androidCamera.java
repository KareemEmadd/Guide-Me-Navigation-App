package com.example.graduationproject;




import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.ExecutionException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Face;
import android.hardware.Camera.FaceDetectionListener;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
    String user_id="22";
    Integer mode;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mode =getIntent().getIntExtra("mode",0);
        setContentView(R.layout.activity_authentication);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        user_id=getIntent().getExtras().getString("UID");
        getWindow().setFormat(PixelFormat.UNKNOWN);
        surfaceView = (SurfaceView)findViewById(R.id.camerapreview);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        option=new BitmapFactory.Options();
        controlInflater = LayoutInflater.from(getBaseContext());
        option.inJustDecodeBounds=false;
        //        View viewControl = controlInflater.inflate(R.layout.control, null);
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
                if(faceFrameNum==3) {
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
//            Uri uriTarget = getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, new ContentValues());
//
//            OutputStream imageFileOS;
//            try {
//                imageFileOS = getContentResolver().openOutputStream(uriTarget);
//                imageFileOS.write(arg0);
//                imageFileOS.flush();
//                imageFileOS.close();
//                Log.d("capture","Image saved: " + uriTarget.toString());
////                prompt.setText("Image saved: " + uriTarget.toString());
//                path=uriTarget.toString();
//            } catch (FileNotFoundException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }

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
    @SuppressLint("StaticFieldLeak")
    public void registerFace(){

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
//                    ***            r.execute(encode(imageUrl), user_id);
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
//    public void registerName() throws ExecutionException, InterruptedException {
//        String firstname=fname.getText().toString();
//        String lastname=lname.getText().toString();
//        if(!firstname.isEmpty() && !firstname.equals("First Name") && !lastname.equals("Last Name") && !lastname.isEmpty()) {
//            rh = new registrationHandler(){
//                protected void onPostExecute(String result) {
//                    Toast.makeText(getApplicationContext(),result+"haaaaaaaaaaaaaaa", Toast.LENGTH_SHORT).show();
//                    user_id=result;
//                }
//
//            };
//
//            rh.execute(firstname,lastname);
////       user_id=rh.execute(firstname,lastname).get();
//
//        }else{
//            Toast.makeText(getApplicationContext(),"Please enter username first.", Toast.LENGTH_SHORT).show();
//
//        }
//    }
}
