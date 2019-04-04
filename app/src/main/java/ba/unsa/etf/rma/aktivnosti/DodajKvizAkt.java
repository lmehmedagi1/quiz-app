package ba.unsa.etf.rma.aktivnosti;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.klase.Kategorija;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.klase.KvizAdapter;
import ba.unsa.etf.rma.klase.Pitanje;
import ba.unsa.etf.rma.klase.PitanjeAdapter;

public class DodajKvizAkt extends AppCompatActivity {

    private ListView listaDodanihPitanja = (ListView) findViewById(R.id.lvDodanaPitanja);
    private ListView listaMogucihPitanja = (ListView) findViewById(R.id.lvMogucaPitanja);
    private Button dodajKvizButton = (Button) findViewById(R.id.btnDodajKviz);
    private EditText nazivKviza = (EditText) findViewById(R.id.etNaziv);
    private Spinner spinner = (Spinner) findViewById(R.id.spKategorije);

    private Kviz kviz = null;
    private ArrayList<Kategorija> kategorije = new ArrayList<>();
    private ArrayList<Kviz> kvizovi = new ArrayList<>();
    private ArrayList<Pitanje> dodanaPitanja = new ArrayList<>();
    private ArrayList<Pitanje> mogucaPitanja = new ArrayList<>();

    private ArrayAdapter<Kategorija> kategorijaAdapter = null;
    private PitanjeAdapter dodanaPitanjaAdapter = null;
    private ArrayAdapter<Pitanje> mogucaPitanjaAdapter = null;

    private View elementZaDodavanje;
    private String odabranaKategorija;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dodaj_kviz_akt);

        Intent intent = getIntent();

        kviz = (Kviz)intent.getSerializableExtra("kviz");
        kvizovi = (ArrayList<Kviz>)intent.getSerializableExtra("kvizovi");
        kategorije = (ArrayList<Kategorija>)intent.getSerializableExtra("kategorije");
        kategorije.remove(0);

        dodajDodanaPitanja();
        dodajNazivKviza();
        dodajKategorijuKviza();

        kategorijaAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, kategorije);
        kategorijaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        dodanaPitanjaAdapter = new PitanjeAdapter(this, dodanaPitanja);
        mogucaPitanjaAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mogucaPitanja);

        elementZaDodavanje = dodanaPitanjaAdapter.dajElementZaDodavanje(listaDodanihPitanja);
        listaDodanihPitanja.addFooterView(elementZaDodavanje);

        spinner.setAdapter(kategorijaAdapter);
        listaDodanihPitanja.setAdapter(dodanaPitanjaAdapter);
        listaMogucihPitanja.setAdapter(mogucaPitanjaAdapter);

        dodajListenerNaListe();
        dodajListenerNaButton();
        dodajListenerNaSpinner();
    }

    private void dodajListenerNaSpinner() {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String odabrana = parent.getItemAtPosition(position).toString();

                if (odabranaKategorija.equals("Dodaj kategoriju"))
                    otvoriAktivnostZaDodavanjeKategorije();
                else odabranaKategorija = odabrana;
            }
            public void onNothingSelected(AdapterView<?> parent) {
                dodajKategorijuKviza();
            }
        });
    }


    private void dodajKategorijuKviza() {
        if (kviz != null) {
            for (int i = 0; i<kategorije.size(); i++)
                if (kategorije.get(i).getId().equals(kviz.getKategorija().getId()))
                    spinner.setSelection(i);
            kategorijaAdapter.notifyDataSetChanged();
        }
    }
    private void dodajNazivKviza() {
        if (kviz != null) nazivKviza.setText(kviz.getNaziv());
    }
    private void dodajDodanaPitanja() {
        if (kviz != null)
            dodanaPitanja = kviz.getPitanja();
    }

    private void dodajListenerNaButton() {
        dodajKvizButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validniPodaci()) return;
                dodajKviz();
            }
        });
    }

    private void dodajKviz() {
        //poslat nove inf
        finish();
    }

    private boolean validniPodaci() {
        //ovdje mijenjati boju
        return true;
    }


    private void dodajListenerNaListe() {
        odabranElementListe(listaDodanihPitanja, mogucaPitanja, dodanaPitanja);
        odabranElementListe(listaMogucihPitanja, dodanaPitanja, mogucaPitanja);

        elementZaDodavanje.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                otvoriAktivnostZaDodavanjePitanja();
            }
        });
    }

    private void odabranElementListe(ListView lista, final ArrayList<Pitanje> trenutnaPitanja, final ArrayList<Pitanje> odredisnaPitanja) {
        lista.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3) {
                Pitanje odabranoPitanje = (Pitanje)adapter.getItemAtPosition(position);

                trenutnaPitanja.add(odabranoPitanje);
                odredisnaPitanja.remove(odabranoPitanje);

                dodanaPitanjaAdapter.notifyDataSetChanged();
                mogucaPitanjaAdapter.notifyDataSetChanged();
            }
        });
    }

    private void otvoriAktivnostZaDodavanjePitanja() {
    }

    private void otvoriAktivnostZaDodavanjeKategorije() {
    }
}
