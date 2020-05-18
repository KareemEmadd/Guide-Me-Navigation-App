package com.example.graduationproject;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.graduationproject.DetectorActivity;
import com.example.graduationproject.R;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MenuActivity extends Activity {
    TextToSpeech tts;
    SpeechRecognizer speechRecog;
    String choice;
    String choices[]={"custom","general","new object"};
    ImageButton btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        initializeTextToSpeech();
        initializeSpeechRecognizer();
        btn=findViewById(R.id.menuBtn);
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
                speak("Click on the screen and state your choice to proceed to general, " +
                        "personal or new object mode");
            }
        });
    }
    private void speak(String message) {
        if(Build.VERSION.SDK_INT >= 21){
            tts.speak(message, TextToSpeech.QUEUE_FLUSH,null,null);
        } else {
            tts.speak(message, TextToSpeech.QUEUE_FLUSH,null);
        }
    }
    private void processResult(String result_message) {
        result_message = result_message.toLowerCase();
        String myObjects;

        if (result_message.indexOf("personal") != -1) {
//            ProceedToLogin();

        } else if (result_message.indexOf("general") != -1) {
            ProceedToDetection();
//            ProceedToRegistration();
        } else if (result_message.indexOf("add object") != -1) {
//            ProceedToAddObject();
        }

        speechRecog.stopListening();

    }
    public void ProceedToDetection(){
        Intent i = new Intent(this, DetectorActivity.class);

        startActivity(i);
    }

}