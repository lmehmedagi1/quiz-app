package ba.unsa.etf.rma.aktivnosti;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
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

    private ListView listaDodanihPitanja;
    private ListView listaMogucihPitanja;
    private Button dodajKvizButton;
    private EditText nazivKviza;
    private Spinner spinner;

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

    private boolean izmjena = false;
    private String imeOdabranogKviza;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dodaj_kviz_akt);

        // da tastatura ne pomjeri layout
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        listaDodanihPitanja = (ListView) findViewById(R.id.lvDodanaPitanja);
        listaMogucihPitanja = (ListView) findViewById(R.id.lvMogucaPitanja);
        dodajKvizButton = (Button) findViewById(R.id.btnDodajKviz);
        nazivKviza = (EditText) findViewById(R.id.etNaziv);
        spinner = (Spinner) findViewById(R.id.spKategorije);

        Intent intent = getIntent();

        kviz = (Kviz)intent.getSerializableExtra("kviz");
        kvizovi = (ArrayList<Kviz>)intent.getSerializableExtra("kvizovi");
        kategorije = (ArrayList<Kategorija>)intent.getSerializableExtra("kategorije");
        kategorije.add(new Kategorija("Dodaj kategoriju", "Dodaj kategoriju"));

        kategorijaAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, kategorije);
        kategorijaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        dodanaPitanjaAdapter = new PitanjeAdapter(this, dodanaPitanja);
        mogucaPitanjaAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mogucaPitanja);

        elementZaDodavanje = dodanaPitanjaAdapter.dajElementZaDodavanje(listaDodanihPitanja);
        listaDodanihPitanja.addFooterView(elementZaDodavanje);

        dodajDodanaPitanja();
        dodajNazivKviza();
        dodajKategorijuKviza();

        spinner.setAdapter(kategorijaAdapter);
        listaDodanihPitanja.setAdapter(dodanaPitanjaAdapter);
        listaMogucihPitanja.setAdapter(mogucaPitanjaAdapter);

        dodajListenerNaListe();
        dodajListenerNaButton();
        dodajListenerNaSpinner();
        dodajListenerNaEditText();
    }

    private void ocistiBoje() {
        nazivKviza.getBackground().clearColorFilter();
    }

    private void dodajListenerNaEditText() {
        nazivKviza.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ocistiBoje();
            }
        });
    }

    private void dodajListenerNaSpinner() {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String odabrana = parent.getItemAtPosition(position).toString();

                if (odabrana.equals("Dodaj kategoriju"))
                    otvoriAktivnostZaDodavanjeKategorije();
                else odabranaKategorija = odabrana;
            }
            public void onNothingSelected(AdapterView<?> parent) {
                dodajKategorijuKviza();
            }
        });
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


    private void dodajKategorijuKviza() {
        if (kviz != null) {
            for (int i = 0; i<kategorije.size(); i++)
                if (kategorije.get(i).getId().equals(kviz.getKategorija().getId())) {
                    spinner.setSelection(i);
                    kategorijaAdapter.notifyDataSetChanged();
                }
        }
    }
    private void dodajNazivKviza() {
        if (kviz != null) {
            nazivKviza.setText(kviz.getNaziv());
            imeOdabranogKviza = kviz.getNaziv();
            izmjena = true;
        }
    }
    private void dodajDodanaPitanja() {
        if (kviz != null)
            dodanaPitanja = kviz.getPitanja();
    }


    private void dodajKviz() {

        int resultCode = -1; // izmjena
        if (kviz == null) resultCode = 0; // dodavanje

        kviz = new Kviz(nazivKviza.getText().toString(), dodanaPitanja, (Kategorija) spinner.getSelectedItem());
        Intent intent = new Intent();
        intent.putExtra("kviz", kviz);
        intent.putExtra("odabraniKviz", imeOdabranogKviza);

        kategorije.remove(kategorije.size()-1);
        intent.putExtra("kategorije", kategorije);
        setResult(resultCode, intent);

        finish();
    }

    private boolean validniPodaci() {
        boolean ispravniPodaci = true;

        if (nazivKviza.getText() == null || nazivKviza.length() == 0) {
            nazivKviza.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
            ispravniPodaci = false;
        }
        else if (!izmjena || (izmjena && !nazivKviza.getText().toString().equals(imeOdabranogKviza))) {   //ili dodavanje ili promjena trenutnog kviza
            for (Kviz k : kvizovi) {
                if (nazivKviza.getText().equals(k.getNaziv())) {
                    nazivKviza.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
                    ispravniPodaci = false;
                }
            }
        }
        return ispravniPodaci;
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
        Intent intent = new Intent(DodajKvizAkt.this, DodajPitanjeAkt.class);
        intent.putExtra("listaDodanih", dodanaPitanja);
        intent.putExtra("listaMogucih", mogucaPitanja);
        DodajKvizAkt.this.startActivityForResult(intent, 1);
    }

    private void otvoriAktivnostZaDodavanjeKategorije() {
        Intent intent = new Intent(DodajKvizAkt.this, DodajKategorijuAkt.class);
        intent.putExtra("kategorije", kategorije);
        DodajKvizAkt.this.startActivityForResult(intent, 2);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == 1) {
                Pitanje pitanje = (Pitanje)data.getSerializableExtra("pitanje");
                dodajPitanje(pitanje);
            }
        }
        else if (requestCode == 2) {
            if (resultCode == 2) {
                Kategorija kategorija = (Kategorija) data.getSerializableExtra("kategorija");
                dodajKategoriju(kategorija);
            }
        }
    }

    private void dodajKategoriju(Kategorija kategorija) {
        kategorije.remove(kategorije.size()-1);
        kategorije.add(kategorija);
        kategorije.add(new Kategorija("Dodaj kategoriju", "Dodaj kategoriju"));
        kategorijaAdapter.notifyDataSetChanged();
    }

    private void dodajPitanje(Pitanje pitanje) {
        dodanaPitanja.add(pitanje);
        dodanaPitanjaAdapter.notifyDataSetChanged();
    }
}
