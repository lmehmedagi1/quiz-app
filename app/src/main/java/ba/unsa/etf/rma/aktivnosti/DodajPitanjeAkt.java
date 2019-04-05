package ba.unsa.etf.rma.aktivnosti;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import ba.unsa.etf.rma.R;

public class DodajPitanjeAkt extends AppCompatActivity {

    private EditText pitanjeET;
    private EditText odgovorET;
    private ListView listaOdgovora;
    private Button dodajOdgovorButton;
    private Button dodajTacanButton;
    private Button dodajPitanjeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dodaj_pitanje_akt);

        pitanjeET = (EditText)findViewById(R.id.etNaziv);
        odgovorET = (EditText)findViewById(R.id.etOdgovor);
        listaOdgovora = (ListView)findViewById(R.id.lvOdgovori);
        dodajOdgovorButton = (Button)findViewById(R.id.btnDodajOdgovor);
        dodajTacanButton = (Button)findViewById(R.id.btnDodajTacan);
        dodajPitanjeButton = (Button)findViewById(R.id.btnDodajPitanje);
    }
}
