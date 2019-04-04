package ba.unsa.etf.rma.aktivnosti;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.klase.Kategorija;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.klase.KvizAdapter;
import ba.unsa.etf.rma.klase.Pitanje;

public class KvizoviAkt extends AppCompatActivity {

    private KvizAdapter kvizAdapter = null;
    private ArrayAdapter<Kategorija> kategorijaAdapter = null;

    private ListView listaKvizova = (ListView) findViewById(R.id.lvKvizovi);
    private Spinner spinner = (Spinner) findViewById(R.id.spPostojeceKategorije);

    private ArrayList<Kviz> kvizovi = new ArrayList<>();
    private ArrayList<Kategorija> kategorije = new ArrayList<>();

    private View elementZaDodavanje;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kvizovi_akt);

        dodajKategorije();
        dodajKvizove();

        kategorijaAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, kategorije);
        kategorijaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(kategorijaAdapter);

        kvizAdapter = new KvizAdapter(this, kvizovi);

        elementZaDodavanje = kvizAdapter.dajElementZaDodavanje(listaKvizova);
        listaKvizova.addFooterView(elementZaDodavanje);

        listaKvizova.setAdapter(kvizAdapter);

        dodajListenerNaSpinner();
        dodajListenerNaListu();
    }

    private void dodajListenerNaListu() {
        listaKvizova.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3) {
                Kviz odabraniKviz = (Kviz)adapter.getItemAtPosition(position);
                otvoriNovuAktivnost(odabraniKviz);
            }
        });

        elementZaDodavanje.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                otvoriNovuAktivnost(null);
            }
        });
    }

    private void otvoriNovuAktivnost(Kviz odabraniKviz) {
        Intent intent = new Intent(KvizoviAkt.this, DodajKvizAkt.class);
        intent.putExtra("kvizovi", kvizovi);
        intent.putExtra("kategorije", kategorije);
        intent.putExtra("kviz", odabraniKviz);
        KvizoviAkt.this.startActivity(intent);
    }

    private void dodajListenerNaSpinner() {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String odabranaKategorija = parent.getItemAtPosition(position).toString();

                ArrayList<Kviz> kvizoviIzOdabraneKategorije = new ArrayList<>();

                for (Kviz k : kvizovi) {
                    if (odabranaKategorija.equals("Svi") || odabranaKategorija.equals(k.getKategorija().getNaziv()))
                        kvizoviIzOdabraneKategorije.add(k);
                }

                kvizAdapter = new KvizAdapter(getBaseContext(), kvizoviIzOdabraneKategorije);
                listaKvizova.setAdapter(kvizAdapter);
            }
            public void onNothingSelected(AdapterView<?> parent) {
                spinner.setSelection(0);
            }
        });
    }

    public void dodajKategorije() {
        kategorije.add(new Kategorija("Svi", "sviId"));
        kategorije.add(new Kategorija("Sport", "sportId"));
        kategorije.add(new Kategorija("Umjetnost", "umjetnostId"));
        kategorije.add(new Kategorija("Nauka", "naukaId"));
    }

    public void dodajKvizove() {
        ArrayList<Pitanje> pitanja = new ArrayList<>();

        ArrayList<String> odgovoriZaKosarku = new ArrayList<>();
        odgovoriZaKosarku.add("1"); odgovoriZaKosarku.add("3"); odgovoriZaKosarku.add("5");

        pitanja.add(new Pitanje("Kosarka", "Koliko igraca po ekipi?", odgovoriZaKosarku, "5"));

        kvizovi.add(new Kviz("Kviz 1", pitanja, kategorije.get(0)));
    }




}
