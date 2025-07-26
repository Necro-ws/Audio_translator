package com.necrows.audio_translator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private ToggleButton toggleButtonGravar;
    private TextView textViewResultado;
    private ScrollView scrollView;
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toggleButtonGravar = findViewById(R.id.toggleButtonGravar);
        textViewResultado = findViewById(R.id.textViewResultado);
        scrollView = findViewById(R.id.scrollView);

        textViewResultado.setText("");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
        } else {
            setupSpeechRecognizer();
        }

        toggleButtonGravar.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (speechRecognizer == null) {
                Toast.makeText(this, "Permissão de áudio não concedida.", Toast.LENGTH_SHORT).show();
                buttonView.setChecked(false);
                return;
            }

            if (isChecked) {
                textViewResultado.setText("");
                speechRecognizer.startListening(speechRecognizerIntent);
            } else {
                speechRecognizer.stopListening();
            }
        });
    }

    private void setupSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, new Locale("pt", "BR").toString());
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);


        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                toggleButtonGravar.setChecked(true);
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
                toggleButtonGravar.setChecked(false);
            }

            @Override
            public void onError(int error) {
                toggleButtonGravar.setChecked(false);
            }

            @Override
            public void onResults(Bundle results) {
                processResults(results);
                if (toggleButtonGravar.isChecked()) {
                    speechRecognizer.startListening(speechRecognizerIntent);
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                processResults(partialResults);
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
            }

            private void processResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String text = matches.get(0);
                    textViewResultado.setText(text);
                    scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissão de áudio concedida!", Toast.LENGTH_SHORT).show();
                setupSpeechRecognizer();
            } else {
                Toast.makeText(this, "A permissão de áudio é necessária para o app funcionar.", Toast.LENGTH_LONG).show();
                toggleButtonGravar.setEnabled(false);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }
}
