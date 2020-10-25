package ba.unsa.etf.rma.aktivnosti;

import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.baza.GetRequestIntentService;
import ba.unsa.etf.rma.baza.GetRequestResultReceiver;
import ba.unsa.etf.rma.klase.ConnectionStateMonitor;
import ba.unsa.etf.rma.klase.Pitanje;

public class DodajPitanjeAkt extends AppCompatActivity implements GetRequestResultReceiver.Receiver, ConnectionStateMonitor.Network {

    private EditText pitanjeET;
    private EditText odgovorET;
    private ListView listaOdgovora;
    private Button dodajOdgovorButton;
    private Button dodajTacanButton;
    private Button dodajPitanjeButton;

    private ArrayList<String> odgovori = new ArrayList<>();
    private ArrayList<Pitanje> azuriranaPitanja = new ArrayList<>();
    private ArrayList<Pitanje> dodanaPitanja = new ArrayList<>();

    private ArrayAdapter<String> odgovoriAdapter = null;

    private String tacan;
    private String token = "";

    private Pitanje pitanje = null;

    private GetRequestResultReceiver receiver = null;

    private String trenutniNaziv = "";

    ConnectionStateMonitor connectionStateMonitor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dodaj_pitanje_akt);

        connectionStateMonitor = new ConnectionStateMonitor(this, (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE));
        connectionStateMonitor.registerNetworkCallback();

        receiver = new GetRequestResultReceiver(new Handler());
        receiver.setReceiver(this);

        Intent intent = getIntent();
        token = intent.getStringExtra("token");
        dodanaPitanja = (ArrayList<Pitanje>) intent.getSerializableExtra("dodanaPitanja");

        // da tastatura ne pomjeri layout
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        pitanjeET = (EditText)findViewById(R.id.etNaziv);
        odgovorET = (EditText)findViewById(R.id.etOdgovor);
        listaOdgovora = (ListView)findViewById(R.id.lvOdgovori);
        dodajOdgovorButton = (Button)findViewById(R.id.btnDodajOdgovor);
        dodajTacanButton = (Button)findViewById(R.id.btnDodajTacan);
        dodajPitanjeButton = (Button)findViewById(R.id.btnDodajPitanje);


        odgovoriAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, odgovori);
        odgovoriAdapter = (new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, odgovori) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View row = super.getView(position, convertView, parent);

                if (tacan != null && getItem(position).equals(tacan))
                    row.setBackgroundColor(Color.GREEN);
                else
                    row.setBackgroundColor(0);

                return row;
            }
        });

        listaOdgovora.setAdapter(odgovoriAdapter);
        
        dodajListenerNaButtons();
        dodajListenerNaListu();
        dodajListenerNaEditTexts();
    }

    private void dodajListenerNaEditTexts() {
        pitanjeET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ocistiBoje();
            }
        });
        odgovorET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ocistiBoje();
            }
        });
    }
    private void ocistiBoje() {
        odgovorET.setBackgroundColor(0);
        pitanjeET.setBackgroundColor(0);
    }
    private void dodajListenerNaListu() {
        listaOdgovora.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3) {
                String odabraniOdgovor = (String)adapter.getItemAtPosition(position);
                for (int i=0; i<odgovori.size(); i++) {
                    if (odgovori.get(i).equals(odabraniOdgovor)) {

                        // ako je odabran tacan odgovor
                        if (tacan != null && odabraniOdgovor.equals(tacan)) {
                            tacan = null;
                            dodajTacanButton.setEnabled(true);
                        }

                        odgovori.remove(i);
                        odgovoriAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }
    private void dodajListenerNaButtons() {
        dodajOdgovorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validanOdgovor()) return;
                dodajOdgovor();
            }
        });
        dodajTacanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validanOdgovor()) return;
                dodajTacanOdgovor();
            }
        });
        dodajPitanjeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validniPodaci()) return;
                trenutniNaziv = pitanjeET.getText().toString();
                uzmiSvaPitanja();
            }
        });


    }

    private void uzmiSvaPitanja() {
        Intent intent = new Intent(Intent.ACTION_SYNC, null, this, GetRequestIntentService.class);
        intent.putExtra("TOKEN", token);
        intent.putExtra("akcija", GetRequestIntentService.AKCIJA_PITANJA);
        intent.putExtra("receiver", receiver);
        startService(intent);
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        if (resultData != null) {
            if (resultCode == GetRequestIntentService.AKCIJA_PITANJA) {
                azuriranaPitanja.clear();
                azuriranaPitanja.addAll((ArrayList<Pitanje>) resultData.getSerializable("pitanja"));

                pitanje = new Pitanje(trenutniNaziv, trenutniNaziv, odgovori, tacan);

                boolean validan = true;

                for (int i = 0; i < azuriranaPitanja.size(); i++) {
                    if (azuriranaPitanja.get(i).getNaziv().equals(pitanje.getNaziv())) {
                        validan = false;
                    }
                    for (Pitanje p : dodanaPitanja) {
                        if (p.getNaziv().equals(azuriranaPitanja.get(i).getNaziv())) {
                            azuriranaPitanja.remove(i);
                            i--;
                            break;
                        }
                    }
                }

                if (!validan) {
                    pitanjeET.setError("Pitanje vec postoji");
                    pitanje = null;
                    return;
                }

                vratiPitanjeUPrethodnuAktivnost();
            }
        }
    }

    private boolean validniPodaci() {
        boolean ispravniPodaci = true;

        if (pitanjeET.getText() == null || pitanjeET.getText().toString().length() == 0) {
            pitanjeET.setError("Morate unijeti naziv pitanja");
            ispravniPodaci = false;
        }

        if (tacan == null) {
            ispravniPodaci = false;
            odgovorET.setError("Mora postojati tacan odgovor");
        }

        return ispravniPodaci;
    }

    private void dodajTacanOdgovor() {
        tacan = odgovorET.getText().toString();
        dodajOdgovor();
        dodajTacanButton.setEnabled(false);
    }
    private void dodajOdgovor() {
        odgovori.add(odgovorET.getText().toString());
        odgovoriAdapter.notifyDataSetChanged();
        odgovorET.setText("");
    }
    private boolean validanOdgovor() {
        if (odgovorET.getText() == null || odgovorET.getText().toString().length() == 0) {
            odgovorET.setText("");
            return false;
        }
        for (String odgovor : odgovori) {
            if (odgovor.equals(odgovorET.getText().toString())) {
                odgovorET.setText("");
                return false;
            }
        }
        return true;
    }

    private void vratiPitanjeUPrethodnuAktivnost() {
        if (pitanje != null) {
            Intent intent = new Intent();
            intent.putExtra("pitanje", pitanje);
            intent.putExtra("azuriranaPitanja", azuriranaPitanja);
            setResult(DodajKvizAkt.ADDED_PITANJE, intent);
            connectionStateMonitor.unregisterNetworkCallback();
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("azuriranaPitanja", azuriranaPitanja);
        setResult(DodajKvizAkt.BACK_FROM_PITANJA, intent);
        connectionStateMonitor.unregisterNetworkCallback();
        finish();
    }

    @Override
    public void onNetworkAvailable() {

    }

    @Override
    public void onNetworkLost() {
        Toast.makeText(this, "Ne mozete dodavati pitanje bez pristupa internetu", Toast.LENGTH_SHORT).show();
        connectionStateMonitor.unregisterNetworkCallback();
        finish();
    }
}
