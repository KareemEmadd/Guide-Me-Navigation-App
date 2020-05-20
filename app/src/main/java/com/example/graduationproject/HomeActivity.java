package com.example.graduationproject;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends thread {
    private static final int PERMISSION_REQUEST_CODE_VIBRATE =2 ;
    private static final int PERMISSION_REQUEST_CODE_COARSE_LOCATION = 3;
    private static final int PERMISSION_REQUEST_CODE_RECORD_AUDIO = 4;
    private static final int PERMISSION_REQUEST_CODE_FINE_LOCATION = 5;
    private static final int PERMISSION_REQUEST_SEND_SMS =6 ;
    TextToSpeech tts;
    private static final int PERMISSION_REQUEST_CODE_CAMERA = 1;

    SpeechRecognizer speechRecog;
    String[] choices = {"login" , "register" , "guest"};
    Integer mode=0;
    int REGISTRATION_MODE=0;
    public String choice;
    ImageButton btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initializeTextToSpeech();
        initializeSpeechRecognizer();


            checkAllPermission();



        btn=findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,1);
                speechRecog.startListening(intent);

            }
        });


    }

    private void speak(String message) {
        tts.speak(message, TextToSpeech.QUEUE_FLUSH,null,null);
    }
    private void initializeTextToSpeech() {
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (tts.getEngines().size() == 0 ){
                    Toast.makeText(getBaseContext(), getString(R.string.app_name),Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    tts.setLanguage(Locale.US);
                }
                speak("Click on the screen and state your choice to proceed to register, " +
                        "login or guest mode");
            }

        });
    }
    private void initializeSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecog = SpeechRecognizer.createSpeechRecognizer(this);
            speechRecog.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {


                }

                @Override
                public void onBeginningOfSpeech() {

                }

                @Override
                public void onRmsChanged(float rmsdB) {

                }

                @Override
                public void onBufferReceived(byte[] buffer) {

                }

                @Override
                public void onEndOfSpeech() {

                }

                @Override
                public void onError(int error) {

                }

                @Override
                public void onResults(Bundle results) {
                    List<String> result_arr = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                    choice = result_arr.get(0).toLowerCase();
                        List<String> list = Arrays.asList(choices);
                        if (!list.contains(choice.toLowerCase())) {
                            speak("Please state your choice clearly");
                        }
                        processResult(result_arr.get(0));
                    }


                @Override
                public void onPartialResults(Bundle partialResults) {

                }

                @Override
                public void onEvent(int eventType, Bundle params) {

                }
            });
        }
    }
    private void processUsernameResult(String result_message) throws InterruptedException {
        result_message = result_message.toLowerCase();

        speechRecog.stopListening();
        ProceedToRegistration();
    }
    private void processResult(String result_message) {
        result_message = result_message.toLowerCase();
        String myObjects;

        if (result_message.indexOf("login") != -1) {
            ProceedToLogin();

        } else if (result_message.indexOf("register") != -1) {
//            speak("Choose your new username");
            REGISTRATION_MODE=1;
            ProceedToRegistration();
        } else if (result_message.indexOf("guest") != -1) {
            REGISTRATION_MODE=2;
            ProceedToGuest();
        }

        speechRecog.stopListening();

    }



    public void listen(){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,1);
        speechRecog.startListening(intent);
    }
    public void ProceedToLogin(){
        Intent i = new Intent(this, androidCamera.class);

        i.putExtra("mode",REGISTRATION_MODE);
        startActivity(i);
    }
    public void ProceedToRegistration(){
        Intent i = new Intent(this, androidCamera.class);

        i.putExtra("mode",REGISTRATION_MODE);
//        i.putExtra("userid",userid);
        startActivity(i);
    }
    public void ProceedToGuest(){
        Intent i = new Intent(this, MenuActivity.class);
        i.putExtra("mode",REGISTRATION_MODE);

        startActivity(i);
    }

void checkAllPermission(){
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

        if (checkSelfPermission(Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {


            Log.d("permission", "permission denied to SEND_SMS - requesting it");
            String[] permissions = {Manifest.permission.CAMERA};
            requestPermissions(permissions, PERMISSION_REQUEST_CODE_CAMERA);
        }
        if (checkSelfPermission(Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_DENIED) {


            Log.d("permission", "permission denied to SEND_SMS - requesting it");
            String[] permissions = {Manifest.permission.SEND_SMS};
            requestPermissions(permissions, PERMISSION_REQUEST_SEND_SMS);
        }
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_DENIED) {


            Log.d("permission", "permission denied to SEND_SMS - requesting it");
            String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION};
            requestPermissions(permissions, PERMISSION_REQUEST_CODE_COARSE_LOCATION);
        }
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_DENIED) {


            Log.d("permission", "permission denied to SEND_SMS - requesting it");
            String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
            requestPermissions(permissions, PERMISSION_REQUEST_CODE_FINE_LOCATION);
        }
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_DENIED) {


            Log.d("permission", "permission denied to SEND_SMS - requesting it");
            String[] permissions = {Manifest.permission.RECORD_AUDIO};
            requestPermissions(permissions, PERMISSION_REQUEST_CODE_RECORD_AUDIO);
        }
        if (checkSelfPermission(Manifest.permission.VIBRATE)
                == PackageManager.PERMISSION_DENIED) {


            Log.d("permission", "permission denied to SEND_SMS - requesting it");
            String[] permissions = {Manifest.permission.VIBRATE};
            requestPermissions(permissions, PERMISSION_REQUEST_CODE_VIBRATE);
        }


    }
}
}
