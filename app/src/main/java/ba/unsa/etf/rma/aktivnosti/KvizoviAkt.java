package ba.unsa.etf.rma.aktivnosti;

import android.content.Intent;
import android.os.Handler;
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
import ba.unsa.etf.rma.klase.GetRequestIntentService;
import ba.unsa.etf.rma.klase.GetRequestResultReceiver;
import ba.unsa.etf.rma.klase.HttpPatchRequest;
import ba.unsa.etf.rma.klase.Kategorija;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.klase.KvizAdapter;
import ba.unsa.etf.rma.klase.Pitanje;

public class KvizoviAkt extends AppCompatActivity implements ListaFrag.porukaOdListeFrag, GetRequestResultReceiver.Receiver {

    public static final int BACK_FROM_DODAJ_KVIZ = 105;
    public static final int ADDED_KVIZ = 115;

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
    private GetRequestResultReceiver receiver = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kvizovi_akt);

        Log.wtf("create", "POZVAN ON CREATEEEE");

        try {
            AccessToken accessToken = new AccessToken();
            accessToken.execute(this);
            TOKEN = accessToken.get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        receiver = new GetRequestResultReceiver(new Handler());
        receiver.setReceiver(this);

        azurirajPodatke(null);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        dpwidth = displayMetrics.widthPixels / displayMetrics.density;

        if (dpwidth >= 550) {
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

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        if (resultData != null) {
            if (resultCode == GetRequestIntentService.KVIZOVI_UPDATE) {
                ArrayList<Kategorija> noveKategorije = (ArrayList<Kategorija>) resultData.getSerializable("kategorije");
                ArrayList<Kviz> noviKvizovi          = (ArrayList<Kviz>) resultData.getSerializable("kvizovi");

                Kategorija kategorijaSvi = new Kategorija("Svi", "-1");
                kategorijaSvi.setIdDokumenta("SVIID");

                kategorije.clear();
                kategorije.add(kategorijaSvi);
                kategorije.addAll(noveKategorije);

                kvizovi.clear();
                kvizovi.addAll(noviKvizovi);

                if (dpwidth >= 550) {
                    listaFrag.azurirajKategorije(kategorije);
                    detailFrag.azurirajKvizove(kvizovi);
                    return;
                }

                kategorijaAdapter.notifyDataSetChanged();

                kvizAdapter = new KvizAdapter(getBaseContext(), kvizovi);
                listaKvizova.setAdapter(kvizAdapter);
                kvizAdapter.notifyDataSetChanged();
            }
            else if (resultCode == GetRequestIntentService.ODABRANA_KATEGORIJA) {
                ArrayList<Kviz> noviKvizovi = (ArrayList<Kviz>) resultData.getSerializable("kvizovi");
                kvizovi.clear();
                kvizovi.addAll(noviKvizovi);

                if (dpwidth >= 550) {
                    detailFrag.azurirajKvizove(kvizovi);
                    return;
                }

                kvizAdapter = new KvizAdapter(getBaseContext(), kvizovi);
                listaKvizova.setAdapter(kvizAdapter);
            }
        }
    }

    public void azurirajPodatke(Kategorija odabrana) {
        Intent intent = new Intent(Intent.ACTION_SYNC, null, this, GetRequestIntentService.class);
        intent.putExtra("TOKEN", TOKEN);

        if (odabrana == null || odabrana.getIdDokumenta().equals("SVIID"))
            intent.putExtra("akcija", GetRequestIntentService.AKCIJA_KVIZOVI);
        else {
            intent.putExtra("akcija", GetRequestIntentService.AKCIJA_ODABRANA_KATEGORIJA);
            intent.putExtra("kategorija", odabrana);
        }
        intent.putExtra("receiver", receiver);
        startService(intent);
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
                otvoriAktivnostZaDodavanjeKviza(null);
            }
        });

        elementZaDodavanje.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                otvoriAktivnostZaDodavanjeKviza(null);
                return true;
            }
        });

        listaKvizova.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapter, View arg1, int position, long id) {
                Kviz odabraniKviz = (Kviz)adapter.getItemAtPosition(position);
                otvoriAktivnostZaDodavanjeKviza(odabraniKviz);
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

    public void otvoriAktivnostZaDodavanjeKviza(Kviz odabraniKviz) {
        Intent intent = new Intent(KvizoviAkt.this, DodajKvizAkt.class);
        intent.putExtra("kategorije", kategorije);
        intent.putExtra("kviz", odabraniKviz);
        intent.putExtra("token", TOKEN);
        KvizoviAkt.this.startActivityForResult(intent, ADDED_KVIZ);
    }

    private void dodajListenerNaSpinner() {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Kategorija odabrana = (Kategorija) spinner.getSelectedItem();
                if (odabrana != null)
                    azurirajPodatke(odabrana);
            }
            public void onNothingSelected(AdapterView<?> parent) {
                spinner.setSelection(0);
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADDED_KVIZ) {

            if (resultCode == ADDED_KVIZ) {

                Kviz kviz = (Kviz) data.getSerializableExtra("kviz");

                kategorije.clear();
                kategorije.addAll((ArrayList<Kategorija>) data.getSerializableExtra("kategorije"));

                kvizovi.clear();
                kvizovi.addAll((ArrayList<Kviz>) data.getSerializableExtra("kvizovi"));

                if (dpwidth >= 550)
                    listaFrag.azurirajKategorije(kategorije);
                else
                    kategorijaAdapter.notifyDataSetChanged();

                String izmjena = data.getStringExtra("izmjena");

                if (izmjena.equals("izmjena")) {
                    String nazivIzmijenjenog = data.getStringExtra("odabraniKviz");

                    for (int i = 0; i<kvizovi.size(); i++) {
                        if (kvizovi.get(i).getNaziv().equals(nazivIzmijenjenog)) {
                            kvizovi.set(i, kviz);

                            dodajKviz(kvizovi.get(i));
                        }
                    }
                }
                else
                    dodajKviz(kviz);
            }
            else if (resultCode == BACK_FROM_DODAJ_KVIZ) {
                kategorije.clear();
                kategorije.addAll((ArrayList<Kategorija>) data.getSerializableExtra("kategorije"));

                if (dpwidth >= 550)
                    listaFrag.azurirajKategorije(kategorije);
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

        HttpPatchRequest patchRequest = new HttpPatchRequest();
        patchRequest.execute(url, TOKEN, dokument.toString());

        if (dpwidth >= 550) {
            detailFrag.azurirajKvizove(kvizovi);
            return;
        }

        kvizAdapter = new KvizAdapter(getBaseContext(), kvizovi);
        listaKvizova.setAdapter(kvizAdapter);
        kvizAdapter.notifyDataSetChanged();
    }


    @Override
    public void porukaOdListeFrag(Kategorija kategorija) {
        detailFrag.primiPorukuOdListeFrag(kategorija);
    }


    // invoked when the activity may be temporarily destroyed, save the instance state here
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("kvizovi", kvizovi);
        outState.putSerializable("kategorije", kategorije);
        super.onSaveInstanceState(outState);
    }
}
