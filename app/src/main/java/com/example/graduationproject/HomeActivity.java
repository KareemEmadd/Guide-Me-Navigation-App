package com.example.graduationproject;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends Activity {
TextToSpeech tts;
SpeechRecognizer speechRecog;
SpeechRecognizer usernameRecog;
String[] choices = {"login" , "register" , "guest"};
LinearLayout LL;
Integer mode=0;
String userid="";
boolean REGISTRATION_MODE=false;
    public String choice;
ImageButton btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initializeTextToSpeech();
        initializeSpeechRecognizer();

        btn=findViewById(R.id.button);
        LL=findViewById(R.id.linearl);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,1);
                speechRecog.startListening(intent);

            }
        });

        speak("Would you like to register, login, or proceed as a guest?");

    }

    private void speak(String message) {
        if(Build.VERSION.SDK_INT >= 21){
            tts.speak(message, TextToSpeech.QUEUE_FLUSH,null,null);
        } else {
            tts.speak(message, TextToSpeech.QUEUE_FLUSH,null);
        }
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
//                    speak("Hello there, I am ready to start our conversation");
                }
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

//                    headerObject.setText(result_arr.get(0));
                    choice = result_arr.get(0).toLowerCase();
                    if(!REGISTRATION_MODE) {
                        List<String> list = Arrays.asList(choices);
                        if (!list.contains(choice.toLowerCase())) {
                            speak("Please state your choice clearly");
                        }
                        processResult(result_arr.get(0));
                    }else{

                        try {
                            processUsernameResult(result_arr.get(0));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
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

//        speak(result_message);
//        wait(2000);
        speak("You have choosen your username to be " + result_message + " you will be using it to login");
        userid=result_message;

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
            REGISTRATION_MODE=true;
            ProceedToRegistration();
        } else if (result_message.indexOf("guest") != -1) {
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
        mode=1;
        i.putExtra("mode",mode);
        startActivity(i);
    }
    public void ProceedToRegistration(){
        Intent i = new Intent(this, androidCamera.class);
        mode=2;
        i.putExtra("mode",mode);
//        i.putExtra("userid",userid);
        startActivity(i);
    }
    public void ProceedToGuest(){
        Intent i = new Intent(this, DetectorActivity.class);

        startActivity(i);
    }

}
