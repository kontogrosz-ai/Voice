import 'package:flutter/material.dart';
import 'package:flutter/services.dart'; // Wymagane do obsługi schowka (Clipboard)
import 'package:speech_to_text/speech_to_text.dart' as stt;

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Głos na Tekst',
      debugShowCheckedModeBanner: false, // Ukrywa pasek debugowania
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.teal),
        useMaterial3: true,
      ),
      home: const VoiceScreen(),
    );
  }
}

class VoiceScreen extends StatefulWidget {
  const VoiceScreen({super.key});

  @override
  State<VoiceScreen> createState() => _VoiceScreenState();
}

class _VoiceScreenState extends State<VoiceScreen> {
  final stt.SpeechToText _speech = stt.SpeechToText();
  bool _isListening = false;
  String _text = 'Naciśnij przycisk na dole i zacznij mówić...';

  // Metoda obsługująca włączanie i wyłączanie nasłuchiwania
  void _listen() async {
    if (!_isListening) {
      bool available = await _speech.initialize(
        onStatus: (status) {
          if (status == 'notListening') {
            setState(() => _isListening = false);
          }
        },
        onError: (val) => print('Błąd: $val'),
      );
      
      if (available) {
        setState(() => _isListening = true);
        _speech.listen(
          localeId: 'pl_PL', // Wymuszenie języka polskiego
          onResult: (result) => setState(() {
            _text = result.recognizedWords;
          }),
        );
      } else {
        _showSnackBar("Rozpoznawanie mowy nie jest dostępne lub brak uprawnień.");
      }
    } else {
      setState(() => _isListening = false);
      _speech.stop();
    }
  }

  // Funkcja do kopiowania tekstu do pamięci telefonu
  void _copyToClipboard() {
    if (_text.isNotEmpty && _text != 'Naciśnij przycisk na dole i zacznij mówić...') {
      Clipboard.setData(ClipboardData(text: _text));
      _showSnackBar("Tekst skopiowany do schowka!");
    } else {
      _showSnackBar("Nie ma czego skopiować.");
    }
  }

  // Funkcja czyszcząca ekran
  void _clearText() {
    setState(() {
      _text = 'Naciśnij przycisk na dole i zacznij mówić...';
    });
  }

  // Pomocnicze wyświetlanie dymków z komunikatami
  void _showSnackBar(String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text(message), duration: const Duration(seconds: 2)),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Głos na Tekst (Dart)'),
        centerTitle: true,
        backgroundColor: Theme.of(context).colorScheme.primaryContainer,
        actions: [
          // Przycisk kopiowania w pasku u góry
          IconButton(
            icon: const Icon(Icons.copy),
            tooltip: 'Kopiuj tekst',
            onPressed: _copyToClipboard,
          ),
          // Przycisk resetu w pasku u góry
          IconButton(
            icon: const Icon(Icons.delete_outline),
            tooltip: 'Wyczyść',
            onPressed: _clearText,
          ),
        ],
      ),
      body: Padding(
        padding: const EdgeInsets.all(20.0),
        child: Column(
          children: [
            Expanded(
              child: Container(
                width: double.infinity,
                padding: const EdgeInsets.all(16.0),
                decoration: BoxDecoration(
                  color: Colors.grey[100],
                  borderRadius: BorderRadius.circular(12),
                  border: Border.all(color: Colors.grey[300]!),
                ),
                child: SingleChildScrollView(
                  child: Text(
                    _text,
                    style: const TextStyle(fontSize: 18.0, color: Colors.black87, height: 1.4),
                  ),
                ),
              ),
            ),
            const SizedBox(height: 24),
            // Duży, ładny przycisk nagrywania na dole
            SizedBox(
              width: double.infinity,
              height: 64,
              child: ElevatedButton.icon(
                onPressed: _listen,
                icon: Icon(_isListening ? Icons.stop : Icons.mic, size: 28),
                label: Text(
                  _isListening ? 'SŁUCHAM... KLIKNIJ BY ZATRZYMAĆ' : 'ZACZNIJ MÓWIĆ',
                  style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                ),
                style: ElevatedButton.styleFrom(
                  backgroundColor: _isListening ? Colors.redAccent : Colors.teal,
                  foregroundColor: Colors.white,
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(16),
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
