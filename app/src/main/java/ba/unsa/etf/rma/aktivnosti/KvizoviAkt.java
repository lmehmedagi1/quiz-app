package ba.unsa.etf.rma.aktivnosti;

import android.content.Intent;
import android.icu.text.IDNA;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.common.collect.Lists;
//import com.google.api.client.util.Lists;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.fragmenti.DetailFrag;
import ba.unsa.etf.rma.fragmenti.InformacijeFrag;
import ba.unsa.etf.rma.fragmenti.ListaFrag;
import ba.unsa.etf.rma.fragmenti.PitanjeFrag;
import ba.unsa.etf.rma.klase.AccessToken;
import ba.unsa.etf.rma.klase.Kategorija;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.klase.KvizAdapter;
import ba.unsa.etf.rma.klase.Pitanje;

public class KvizoviAkt extends AppCompatActivity implements ListaFrag.porukaOdListeFrag {

    private KvizAdapter kvizAdapter = null;
    private ArrayAdapter<Kategorija> kategorijaAdapter = null;

    private ListView listaKvizova;
    private Spinner spinner;

    private ArrayList<Kviz> kvizovi = new ArrayList<>();
    private ArrayList<Kategorija> kategorije = new ArrayList<>();

    private View elementZaDodavanje;

    private static final String LISTA_TAG = "lista";
    private static final String DETALJI_TAG = "detalji";

    private ListaFrag listaFrag = null;
    private DetailFrag detailFrag = null;

    private float dpwidth = 0;

    private String TOKEN = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //if (savedInstanceState != null) return; //myb
        setContentView(R.layout.activity_kvizovi_akt);

        try {
            AccessToken accessToken = new AccessToken();
            accessToken.execute(this);
            TOKEN = accessToken.get();
            System.out.print(TOKEN);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        dpwidth = displayMetrics.widthPixels / displayMetrics.density;

        if (dpwidth >= 550) {
            FragmentManager manager = getSupportFragmentManager();

            kategorije.add(new Kategorija("Svi", "-1"));

            detailFrag = (DetailFrag) manager.findFragmentByTag(DETALJI_TAG);
            if (detailFrag == null) {
                detailFrag = new DetailFrag();

                Bundle bundle = new Bundle();
                bundle.putSerializable("kvizovi", kvizovi);
                detailFrag.setArguments(bundle);

                manager.beginTransaction().replace(R.id.detailPlace, detailFrag, DETALJI_TAG).commit();
            }

            listaFrag = (ListaFrag) manager.findFragmentByTag(LISTA_TAG);
            if (listaFrag == null) {
                listaFrag = new ListaFrag();

                manager.beginTransaction().replace(R.id.listPlace, listaFrag, LISTA_TAG).commit();
            }

        }
        else {
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
    }

    private void dodajListenerNaListu() {
        listaKvizova.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3) {
                Kviz odabraniKviz = (Kviz)adapter.getItemAtPosition(position);
                otvoriAktivnostZaIgranjeKviza(odabraniKviz);
            }
        });

        elementZaDodavanje.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                otvoriNovuAktivnost(null);
            }
        });

        elementZaDodavanje.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                otvoriNovuAktivnost(null);
                return true;
            }
        });

        listaKvizova.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapter, View arg1, int position, long id) {
                Kviz odabraniKviz = (Kviz)adapter.getItemAtPosition(position);
                otvoriNovuAktivnost(odabraniKviz);
                return true;
            }
        });
    }

    public void otvoriAktivnostZaIgranjeKviza(Kviz odabraniKviz) {
        Intent intent = new Intent(KvizoviAkt.this, IgrajKvizAkt.class);
        intent.putExtra("kviz", odabraniKviz);
        KvizoviAkt.this.startActivityForResult(intent, 20);
    }

    public void otvoriNovuAktivnost(Kviz odabraniKviz) {
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

                if (dpwidth >= 550)
                    listaFrag.azurirajKategorije(noveKategorije);
                else
                    kategorijaAdapter.notifyDataSetChanged();

                String izmjena = data.getStringExtra("izmjena");

                if (izmjena.equals("izmjena")) {
                    String nazivIzmijenjenog = data.getStringExtra("odabraniKviz");

                    for (int i = 0; i<kvizovi.size(); i++) {
                        if (kvizovi.get(i).getNaziv().equals(nazivIzmijenjenog)) {
                            kvizovi.set(i, kviz);

                            if (dpwidth >= 550) {
                                detailFrag.azurirajKvizove(kvizovi);
                                return;
                            }
                            kvizAdapter.notifyDataSetChanged();
                            refreshList();
                            return;
                        }
                    }
                }
                else
                    dodajKviz(kviz);
            }
            else if (resultCode == 11) {
                ArrayList<Kategorija> noveKategorije = (ArrayList<Kategorija>) data.getSerializableExtra("kategorije");
                kategorije.clear();
                kategorije.addAll(noveKategorije);

                if (dpwidth >= 550)
                    listaFrag.azurirajKategorije(noveKategorije);
                else
                    kategorijaAdapter.notifyDataSetChanged();
            }
        }
    }

    private void dodajKviz(Kviz kviz) {
        kvizovi.add(kviz);

        if (dpwidth >= 550) {
            detailFrag.azurirajKvizove(kvizovi);
            return;
        }

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


    @Override
    public void porukaOdListeFrag(String nazivKategorije) {
        detailFrag.primiPorukuOdListeFrag(nazivKategorije);
    }
}
