package ba.unsa.etf.rma.aktivnosti;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.klase.Pitanje;

public class DodajPitanjeAkt extends AppCompatActivity {

    private EditText pitanjeET;
    private EditText odgovorET;
    private ListView listaOdgovora;
    private Button dodajOdgovorButton;
    private Button dodajTacanButton;
    private Button dodajPitanjeButton;

    private ArrayList<String> odgovori = new ArrayList<>();
    private ArrayList<Pitanje> dodanaPitanja = new ArrayList<>();
    private ArrayList<Pitanje> mogucaPitanja = new ArrayList<>();

    private ArrayAdapter<String> odgovoriAdapter = null;

    private String tacan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dodaj_pitanje_akt);

        // da tastatura ne pomjeri layout
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        pitanjeET = (EditText)findViewById(R.id.etNaziv);
        odgovorET = (EditText)findViewById(R.id.etOdgovor);
        listaOdgovora = (ListView)findViewById(R.id.lvOdgovori);
        dodajOdgovorButton = (Button)findViewById(R.id.btnDodajOdgovor);
        dodajTacanButton = (Button)findViewById(R.id.btnDodajTacan);
        dodajPitanjeButton = (Button)findViewById(R.id.btnDodajPitanje);

        Intent intent = getIntent();

        dodanaPitanja = (ArrayList<Pitanje>)intent.getSerializableExtra("listaDodanih");
        mogucaPitanja = (ArrayList<Pitanje>)intent.getSerializableExtra("listaMogucih");

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
        pitanjeET.setBackgroundColor(Color.WHITE);
        odgovorET.setBackgroundColor(Color.WHITE);
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
                vratiPitanjeUPrethodnuAktivnost();
            }
        });


    }

    private boolean validniPodaci() {
        boolean ispravniPodaci = true;

        String nazivPitanja = pitanjeET.getText().toString();

        if (nazivPitanja == null || nazivPitanja.length() == 0) {
            pitanjeET.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
            ispravniPodaci = false;
        }
        else {
            ispravniPodaci = provjeriPitanjeUListi(dodanaPitanja, ispravniPodaci);
            ispravniPodaci = provjeriPitanjeUListi(mogucaPitanja, ispravniPodaci);
        }

        if (tacan == null) {
            ispravniPodaci = false;
            odgovorET.setBackgroundColor(Color.RED);
        }

        return ispravniPodaci;
    }


    private boolean provjeriPitanjeUListi(ArrayList<Pitanje> listaPitanja, boolean ispravniPodaci) {
        for (Pitanje p : listaPitanja) {
            if (p.getNaziv().equals(pitanjeET.getText().toString())) {
                pitanjeET.setBackgroundColor(Color.RED);
                return false;
            }
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
        Pitanje pitanje = new Pitanje(pitanjeET.getText().toString(), pitanjeET.getText().toString(), odgovori, tacan);

        Intent intent = new Intent();
        intent.putExtra("pitanje", pitanje);
        setResult(1, intent);
        finish();
    }
}
