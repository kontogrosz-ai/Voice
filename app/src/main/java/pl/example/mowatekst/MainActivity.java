package pl.example.mowatekst;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {

    private static final int REQUEST_RECORD_AUDIO = 100;

    private SpeechRecognizer speechRecognizer;
    private Intent speechIntent;

    private TextView textView;
    private Button microphoneButton;
    private Button clearButton;

    private boolean listening = false;

    private String finalText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createInterface();

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(
                    this,
                    "Rozpoznawanie mowy nie jest dostępne na tym urządzeniu.",
                    Toast.LENGTH_LONG
            ).show();
            microphoneButton.setEnabled(false);
            return;
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        speechIntent = new Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH
        );

        speechIntent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );

        speechIntent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                "pl-PL"
        );

        speechIntent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                "pl-PL"
        );

        speechIntent.putExtra(
                RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                true
        );

        speechIntent.putExtra(
                RecognizerIntent.EXTRA_MAX_RESULTS,
                1
        );

        speechRecognizer.setRecognitionListener(
                new RecognitionListener() {

                    @Override
                    public void onReadyForSpeech(Bundle params) {
                        microphoneButton.setText("⏹ Zatrzymaj");
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

                        if (!listening) {
                            return;
                        }

                        // Po krótkiej przerwie w mówieniu
                        // uruchamiamy rozpoznawanie ponownie.
                        startListeningAgain();
                    }

                    @Override
                    public void onResults(Bundle results) {

                        ArrayList<String> matches =
                                results.getStringArrayList(
                                        SpeechRecognizer.RESULTS_RECOGNITION
                                );

                        if (matches != null && !matches.isEmpty()) {

                            String result = matches.get(0);

                            if (!result.isEmpty()) {

                                if (!finalText.isEmpty()) {
                                    finalText += " ";
                                }

                                finalText += result;

                                textView.setText(finalText);
                            }
                        }

                        if (listening) {
                            startListeningAgain();
                        }
                    }

                    @Override
                    public void onPartialResults(Bundle partialResults) {

                        ArrayList<String> matches =
                                partialResults.getStringArrayList(
                                        SpeechRecognizer.RESULTS_RECOGNITION
                                );

                        if (matches != null && !matches.isEmpty()) {

                            String partialText = matches.get(0);

                            String displayText = finalText;

                            if (!displayText.isEmpty()) {
                                displayText += " ";
                            }

                            displayText += partialText;

                            textView.setText(displayText);
                        }
                    }

                    @Override
                    public void onEvent(int eventType, Bundle params) {
                    }
                }
        );
    }

    private void createInterface() {

        LinearLayout root = new LinearLayout(this);

        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 24, 24, 24);
        root.setBackgroundColor(0xFFF5F5F5);

        TextView title = new TextView(this);

        title.setText("Mowa na tekst");
        title.setTextSize(28);
        title.setTextColor(0xFF111111);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 20, 0, 20);

        root.addView(
                title,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        ScrollView scrollView = new ScrollView(this);

        textView = new TextView(this);

        textView.setText(
                "Naciśnij przycisk mikrofonu i zacznij mówić..."
        );

        textView.setTextSize(22);
        textView.setTextColor(0xFF222222);
        textView.setPadding(20, 20, 20, 20);

        scrollView.addView(textView);

        LinearLayout.LayoutParams scrollParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0,
                        1
                );

        scrollParams.setMargins(0, 10, 0, 10);

        root.addView(scrollView, scrollParams);

        microphoneButton = new Button(this);

        microphoneButton.setText("🎤 Mów");
        microphoneButton.setTextSize(18);

        microphoneButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        toggleListening();
                    }
                }
        );

        root.addView(
                microphoneButton,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        clearButton = new Button(this);

        clearButton.setText("Wyczyść");
        clearButton.setTextSize(18);

        clearButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finalText = "";
                        textView.setText("");
                    }
                }
        );

        root.addView(
                clearButton,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        setContentView(root);
    }

    private void toggleListening() {

        if (listening) {

            stopListening();

        } else {

            if (checkSelfPermission(
                    Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                        new String[]{
                                Manifest.permission.RECORD_AUDIO
                        },
                        REQUEST_RECORD_AUDIO
                );

                return;
            }

            listening = true;

            finalText = textView.getText().toString();

            if (finalText.equals(
                    "Naciśnij przycisk mikrofonu i zacznij mówić..."
            )) {
                finalText = "";
                textView.setText("");
            }

            startListeningAgain();
        }
    }

    private void startListeningAgain() {

        if (!listening || speechRecognizer == null) {
            return;
        }

        try {
            speechRecognizer.cancel();

            speechRecognizer.startListening(speechIntent);

            microphoneButton.setText("⏹ Zatrzymaj");

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Nie można uruchomić mikrofonu.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void stopListening() {

        listening = false;

        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
            speechRecognizer.cancel();
        }

        microphoneButton.setText("🎤 Mów");
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults
    ) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode == REQUEST_RECORD_AUDIO) {

            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                listening = true;
                startListeningAgain();

            } else {

                Toast.makeText(
                        this,
                        "Aplikacja potrzebuje dostępu do mikrofonu.",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }

    @Override
    protected void onDestroy() {

        listening = false;

        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            speechRecognizer = null;
        }

        super.onDestroy();
    }
}
