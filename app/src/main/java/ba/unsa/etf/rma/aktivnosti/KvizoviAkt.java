package ba.unsa.etf.rma.aktivnosti;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.UUID;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.fragmenti.DetailFrag;
import ba.unsa.etf.rma.fragmenti.ListaFrag;
import ba.unsa.etf.rma.klase.AccessToken;
import ba.unsa.etf.rma.klase.HttpGetRequest;
import ba.unsa.etf.rma.klase.HttpPatchRequest;
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
    private ArrayList<Pitanje> pitanja = new ArrayList<>();

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
        setContentView(R.layout.activity_kvizovi_akt);

        Log.wtf("ONCRETAE", "Pozvan je on create");

        if (savedInstanceState != null) {
            Log.wtf("SAVEDINSTANCE", "Saved instance nije null");

            kvizovi = (ArrayList<Kviz>) savedInstanceState.getSerializable("kvizovi");
            kategorije = (ArrayList<Kategorija>) savedInstanceState.getSerializable("kategorije");
            pitanja = (ArrayList<Pitanje>) savedInstanceState.getSerializable("pitanja");

            for (int i = 0; i<kvizovi.size(); i++) {
                if (kvizovi.get(i).getNaziv().equals("Dodaj kviz")) {
                    kvizovi.remove(i);
                    i--;
                }
            }
        }


        try {
            AccessToken accessToken = new AccessToken();
            accessToken.execute(this);
            TOKEN = accessToken.get();
            System.out.print(TOKEN);

            if (savedInstanceState == null) {
                Log.wtf("HTTP", "Ucitavaju se vrijednosti iz baze");

                HttpGetRequest getRequest = new HttpGetRequest();
                ArrayList<Object> result = getRequest.execute(TOKEN, "pokretanje aplikacije").get();

                kategorije.add(new Kategorija("Svi", "-1"));

                kvizovi = (ArrayList<Kviz>) result.get(0);
                kategorije.addAll((ArrayList<Kategorija>) result.get(1));
                pitanja = (ArrayList<Pitanje>) result.get(2);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.wtf("PITANJAA", "U onCreatu BROJ PITANJE JE" + String.valueOf(pitanja.size()));


        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        dpwidth = displayMetrics.widthPixels / displayMetrics.density;

        if (dpwidth >= 550) {
            Log.wtf("HTTP", "Sirok displej");
            FragmentManager manager = getSupportFragmentManager();

            detailFrag = (DetailFrag) manager.findFragmentByTag(DETALJI_TAG);
            if (detailFrag == null) {
                Log.wtf("HTTP", "Pravi se detailFrag");

                detailFrag = new DetailFrag();

                Bundle bundle = new Bundle();
                bundle.putSerializable("kvizovi", kvizovi);
                detailFrag.setArguments(bundle);

                manager.beginTransaction().replace(R.id.detailPlace, detailFrag, DETALJI_TAG).commit();
            }

            listaFrag = (ListaFrag) manager.findFragmentByTag(LISTA_TAG);
            if (listaFrag == null) {
                Log.wtf("HTTP", "Pravi se lista frag");

                listaFrag = new ListaFrag();

                Bundle bundle = new Bundle();
                bundle.putSerializable("kategorije", kategorije);
                listaFrag.setArguments(bundle);

                manager.beginTransaction().replace(R.id.listPlace, listaFrag, LISTA_TAG).commit();
            }

        }
        else {
            Log.wtf("HTTP", " NIJE Sirok displej");

            listaKvizova = (ListView) findViewById(R.id.lvKvizovi);
            spinner = (Spinner) findViewById(R.id.spPostojeceKategorije);

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
        intent.putExtra("token", TOKEN);
        KvizoviAkt.this.startActivityForResult(intent, 20);
    }

    public void otvoriNovuAktivnost(Kviz odabraniKviz) {
        Log.wtf("PITANJAA", "U otvaranju nove aktivnosti broj pitanja je" + String.valueOf(pitanja.size()));

        Intent intent = new Intent(KvizoviAkt.this, DodajKvizAkt.class);
        intent.putExtra("kvizovi", kvizovi);
        intent.putExtra("kategorije", kategorije);
        intent.putExtra("pitanja", pitanja);
        intent.putExtra("kviz", odabraniKviz);
        intent.putExtra("token", TOKEN);
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

                Log.wtf("PITANJAA", "Dok dodajem kviz broj pitanja je " + String.valueOf(pitanja.size()));


                Kviz kviz = (Kviz) data.getSerializableExtra("kviz");
                ArrayList<Kategorija> noveKategorije = (ArrayList<Kategorija>) data.getSerializableExtra("kategorije");
                ArrayList<Pitanje> novaPitanja = (ArrayList<Pitanje>) data.getSerializableExtra("dodanaPitanja");
                novaPitanja.addAll((ArrayList<Pitanje>) data.getSerializableExtra("mogucaPitanja"));
                pitanja.clear();
                pitanja.addAll(novaPitanja);
                kategorije.clear();
                kategorije.addAll(noveKategorije);

                Log.wtf("PITANJAA", "Ali sad ih ima "+ String.valueOf(pitanja.size()));


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

                            dodajKviz(kvizovi.get(i));

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

                Log.wtf("PITANJAA", "Kliknut bek ima ih " +  String.valueOf(pitanja.size()));


                ArrayList<Pitanje> novaPitanja = (ArrayList<Pitanje>) data.getSerializableExtra("dodanaPitanja");
                novaPitanja.addAll((ArrayList<Pitanje>) data.getSerializableExtra("mogucaPitanja"));
                pitanja.clear();
                pitanja.addAll(novaPitanja);

                Log.wtf("PITANJAA", "Nakon modifikacije " + String.valueOf(pitanja.size()));


                if (dpwidth >= 550)
                    listaFrag.azurirajKategorije(noveKategorije);
                else
                    kategorijaAdapter.notifyDataSetChanged();
            }
        }
    }


    private void dodajKviz(Kviz kviz) {
        if (kviz.getIdDokumenta() == null || kviz.getIdDokumenta().length() == 0) {
            kvizovi.add(kviz);
            String id = UUID.randomUUID().toString();
            kviz.setIdDokumenta(id);
        }

        String url = "https://firestore.googleapis.com/v1/projects/rma18174-firebase/databases/(default)/documents/Kvizovi/" + kviz.getIdDokumenta() + "?access_token=";
        StringBuilder dokument = new StringBuilder("{\"fields\": { \"naziv\": {\"stringValue\": \"" + kviz.getNaziv() + "\"}," +
                "\"idKategorije\": {\"stringValue\": \"" + kviz.getKategorija().getIdDokumenta() + "\"}," +
                "\"pitanja\": {\"arrayValue\": { \"values\": [");
        ArrayList<Pitanje> pitanja = kviz.getPitanja();
        for (int i = 0; i<pitanja.size(); i++) {
            dokument.append("{\"stringValue\": \"").append(pitanja.get(i).getIdDokumenta()).append("\"}");
            if (i<pitanja.size()-1) dokument.append(",");
        }
        dokument.append("]}}}}");

        Log.wtf("TOKEN", TOKEN);

        HttpPatchRequest patchRequest = new HttpPatchRequest();
        patchRequest.execute(url, TOKEN, dokument.toString());

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


    // invoked when the activity may be temporarily destroyed, save the instance state here
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("kvizovi", kvizovi);
        outState.putSerializable("kategorije", kategorije);
        outState.putSerializable("pitanja", pitanja);
        super.onSaveInstanceState(outState);
    }
}
