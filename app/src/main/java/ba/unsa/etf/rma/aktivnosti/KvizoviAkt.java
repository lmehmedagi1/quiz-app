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

    private ListView listaKvizova;
    private Spinner spinner;

    private ArrayList<Kviz> kvizovi = new ArrayList<>();
    private ArrayList<Kategorija> kategorije = new ArrayList<>();

    private View elementZaDodavanje;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kvizovi_akt);

        listaKvizova = (ListView) findViewById(R.id.lvKvizovi);
        spinner = (Spinner) findViewById(R.id.spPostojeceKategorije);

        kategorije.add(new Kategorija("Svi", "-1"));

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
        KvizoviAkt.this.startActivityForResult(intent, 10);
    }

    private void dodajListenerNaSpinner() {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refreshList();
            }
            public void onNothingSelected(AdapterView<?> parent) {
                spinner.setSelection(0);
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10) {

            if (resultCode == 10) {

                Kviz kviz = (Kviz) data.getSerializableExtra("kviz");
                ArrayList<Kategorija> noveKategorije = (ArrayList<Kategorija>) data.getSerializableExtra("kategorije");
                kategorije.clear();
                kategorije.addAll(noveKategorije);
                kategorijaAdapter.notifyDataSetChanged();

                String izmjena = data.getStringExtra("izmjena");

                if (izmjena.equals("izmjena")) {
                    String nazivIzmijenjenog = data.getStringExtra("odabraniKviz");

                    for (int i = 0; i<kvizovi.size(); i++) {
                        if (kvizovi.get(i).getNaziv().equals(nazivIzmijenjenog)) {
                            kvizovi.set(i, kviz);
                            kvizAdapter.notifyDataSetChanged();
                            refreshList();
                            return;
                        }
                    }
                }
                else
                    dodajKviz(kviz);
            }
        }
    }

    private void dodajKviz(Kviz kviz) {
        kvizovi.add(kviz);
        kvizAdapter.notifyDataSetChanged();
        refreshList();
    }

    private void refreshList() {
        String odabranaKategorija = ((Kategorija) spinner.getSelectedItem()).getNaziv();
        ArrayList<Kviz> kvizoviIzOdabraneKategorije = new ArrayList<>();

        for (Kviz k : kvizovi) {
            if (odabranaKategorija.equals("Svi") || odabranaKategorija.equals(k.getKategorija().getNaziv()))
                kvizoviIzOdabraneKategorije.add(k);
        }
        kvizAdapter = new KvizAdapter(getBaseContext(), kvizoviIzOdabraneKategorije);
        listaKvizova.setAdapter(kvizAdapter);
    }
}
