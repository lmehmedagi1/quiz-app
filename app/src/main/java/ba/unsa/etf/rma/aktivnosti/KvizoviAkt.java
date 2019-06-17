package ba.unsa.etf.rma.aktivnosti;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.baza.SQLiteDBHelper;
import ba.unsa.etf.rma.fragmenti.DetailFrag;
import ba.unsa.etf.rma.fragmenti.ListaFrag;
import ba.unsa.etf.rma.baza.AccessToken;
import ba.unsa.etf.rma.baza.GetRequestIntentService;
import ba.unsa.etf.rma.baza.GetRequestResultReceiver;
import ba.unsa.etf.rma.baza.HttpPatchRequest;
import ba.unsa.etf.rma.klase.ConnectionStateMonitor;
import ba.unsa.etf.rma.klase.Kategorija;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.adapteri.KvizAdapter;
import ba.unsa.etf.rma.klase.Pitanje;
import ba.unsa.etf.rma.klase.RangListaItem;

public class KvizoviAkt extends AppCompatActivity implements ListaFrag.porukaOdListeFrag, GetRequestResultReceiver.Receiver, ConnectionStateMonitor.Network {

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

    private static SQLiteDBHelper databaseHelper;
    public static boolean isOnline = false;
    private ConnectionStateMonitor connectionStateMonitor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kvizovi_akt);

        receiver = new GetRequestResultReceiver(new Handler());
        receiver.setReceiver(this);

        connectionStateMonitor = new ConnectionStateMonitor(this, (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE));
        connectionStateMonitor.registerNetworkCallback();

        databaseHelper = new SQLiteDBHelper(this);

        try {
            AccessToken accessToken = new AccessToken();
            accessToken.execute(this);
            TOKEN = accessToken.get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        dpwidth = displayMetrics.widthPixels / displayMetrics.density;

        if (dpwidth >= 550) {
            FragmentManager manager = getSupportFragmentManager();

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

        azurirajPodatke(null);
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        if (resultData != null) {
            if (resultCode == GetRequestIntentService.AKCIJA_KVIZOVI) {
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
                    listaFrag.azurirajKategorije(kategorije, kvizovi);
                    return;
                }

                kategorijaAdapter.notifyDataSetChanged();

                kvizAdapter = new KvizAdapter(getBaseContext(), kvizovi);
                listaKvizova.setAdapter(kvizAdapter);
                kvizAdapter.notifyDataSetChanged();
            }
            else if (resultCode == GetRequestIntentService.AKCIJA_ODABRANA_KATEGORIJA) {
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
            else if (resultCode == GetRequestIntentService.AKCIJA_SQLITE_GET) {
                ArrayList<Kviz> noviKvizovi = (ArrayList<Kviz>) resultData.getSerializable("kvizovi");
                ArrayList<Kategorija> noveKategorije = (ArrayList<Kategorija>) resultData.getSerializable("kategorije");
                ArrayList<Pitanje> novaPitanja = (ArrayList<Pitanje>) resultData.getSerializable("pitanja");
                ArrayList<RangListaItem> noveRangListe = (ArrayList<RangListaItem>) resultData.getSerializable("rangliste");

                for (Kviz k : noviKvizovi)
                    databaseHelper.dodajKviz(k);

                for (Kategorija k : noveKategorije)
                    databaseHelper.dodajKategorija(k);

                for (Pitanje p : novaPitanja)
                    databaseHelper.dodajPitanje(p);

                for (RangListaItem r : noveRangListe)
                    databaseHelper.dodajRangListItem(r);

                Kategorija kategorijaSvi = new Kategorija("Svi", "-1");
                kategorijaSvi.setIdDokumenta("SVIID");

                kategorije.clear();
                kategorije.add(kategorijaSvi);
                kategorije.addAll(noveKategorije);

                kvizovi.clear();
                kvizovi.addAll(noviKvizovi);

                if (dpwidth >= 550) {
                    listaFrag.azurirajKategorije(kategorije, kvizovi);
                    return;
                }

                kategorijaAdapter.notifyDataSetChanged();

                kvizAdapter = new KvizAdapter(getBaseContext(), kvizovi);
                listaKvizova.setAdapter(kvizAdapter);
                kvizAdapter.notifyDataSetChanged();
            }
            else if (resultCode == GetRequestIntentService.AKCIJA_SQLITE_POST) {
                // ucitavamo podatke iz firebase u SQLite
                Intent intent = new Intent(Intent.ACTION_SYNC, null, this, GetRequestIntentService.class);
                intent.putExtra("TOKEN", TOKEN);
                intent.putExtra("akcija", GetRequestIntentService.AKCIJA_SQLITE_GET);
                intent.putExtra("receiver", receiver);
                startService(intent);
            }
        }
    }

    public void azurirajPodatke(Kategorija odabrana) {

        if (!isOnline && odabrana == null) {
            // pokretanje aplikacije bez interneta, uzmi sve kvizove iz bsze
            ArrayList<Kategorija> noveKategorije = databaseHelper.dajSveKategorije();
            ArrayList<Kviz> noviKvizovi          = databaseHelper.dajSveKvizove();

            Kategorija kategorijaSvi = new Kategorija("Svi", "-1");
            kategorijaSvi.setIdDokumenta("SVIID");

            kategorije.clear();
            kategorije.add(kategorijaSvi);
            kategorije.addAll(noveKategorije);

            kvizovi.clear();
            kvizovi.addAll(noviKvizovi);

            if (dpwidth >= 550) {
                listaFrag.azurirajKategorije(kategorije, kvizovi);
                return;
            }

            kategorijaAdapter.notifyDataSetChanged();

            kvizAdapter = new KvizAdapter(getBaseContext(), kvizovi);
            listaKvizova.setAdapter(kvizAdapter);
            kvizAdapter.notifyDataSetChanged();
        }
        else if (!isOnline) {
            // filtriranje kvizova bez interneta
            ArrayList<Kviz> noviKvizovi = databaseHelper.dajSveKvizove();
            ArrayList<Kviz> azuriraniKvizovi = new ArrayList<>();
            if (odabrana.getNaziv().equals("Svi"))
                azuriraniKvizovi.addAll(noviKvizovi);
            else {
                for (Kviz k : noviKvizovi) {
                    if (k.getKategorija().getNaziv().equals(odabrana.getNaziv()))
                        azuriraniKvizovi.add(k);
                }
            }

            if (dpwidth >= 550) {
                detailFrag.primiPorukuOdListeFrag(azuriraniKvizovi);
                return;
            }

            kvizAdapter = new KvizAdapter(getBaseContext(), azuriraniKvizovi);
            listaKvizova.setAdapter(kvizAdapter);
            kvizAdapter.notifyDataSetChanged();
        }
        else {
            // filtriranje kvizova sa pristupom internetu
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
    }

    public void izbaciAlert(String poruka) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(poruka);
        builder.setNeutralButton("OK", null);
        builder.create().show();
    }

    private void dodajListenerNaListu() {
        listaKvizova.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3) {
                Kviz odabraniKviz = (Kviz)adapter.getItemAtPosition(position);
                String event = postojiDogadjaj(odabraniKviz.getPitanja().size() * 30000);
                if (event == null)
                    otvoriAktivnostZaIgranjeKviza(odabraniKviz);
                else
                    izbaciAlert(event);
            }
        });

        elementZaDodavanje.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isOnline) {
                    izbaciAlert("Spojite se na internet da dodate kviz");
                }
                else
                    otvoriAktivnostZaDodavanjeKviza(null);
            }
        });

        elementZaDodavanje.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!isOnline) {
                    izbaciAlert("Spojite se na internet da dodate kviz");
                }
                else
                    otvoriAktivnostZaDodavanjeKviza(null);
                return true;
            }
        });

        listaKvizova.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapter, View arg1, int position, long id) {
                if (!isOnline) {
                    izbaciAlert("Spojite se na internet da uredite kviz");
                }
                else {
                    Kviz odabraniKviz = (Kviz) adapter.getItemAtPosition(position);
                    otvoriAktivnostZaDodavanjeKviza(odabraniKviz);
                }
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
                    listaFrag.azurirajKategorije(kategorije, kvizovi);
                else {
                    kategorijaAdapter.notifyDataSetChanged();
                    spinner.setSelection(0);
                }

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
                    listaFrag.azurirajKategorije(kategorije, null);
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

        databaseHelper.dodajKviz(kviz);

        if (dpwidth >= 550) {
            detailFrag.azurirajKvizove(kvizovi);
            return;
        }

        kvizAdapter = new KvizAdapter(getBaseContext(), kvizovi);
        listaKvizova.setAdapter(kvizAdapter);
        kvizAdapter.notifyDataSetChanged();
    }

    public String postojiDogadjaj(long vrijemeIgranjaKviza) {

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALENDAR}, 420);

        String[] select = {
                CalendarContract.Events._ID,
                CalendarContract.Events.TITLE,
                CalendarContract.Events.DTSTART,
                CalendarContract.Events.DTEND,
        };

        String where = "(" + CalendarContract.Events.DTSTART + ">= ? AND "
                + CalendarContract.Events.DTSTART + "<= ?)"
                + "OR (" + CalendarContract.Events.DTSTART + " <= ? AND "
                + CalendarContract.Events.DTEND + ">= ?)";

        String[] args = {"", "", "", ""};
        args[0] = String.valueOf(Calendar.getInstance().getTimeInMillis());
        args[1] = String.valueOf(Calendar.getInstance().getTimeInMillis() + vrijemeIgranjaKviza);
        args[2] = args[0];
        args[3] = args[0];

        try {
            Cursor cursor = getContentResolver().query(
                    CalendarContract.Events.CONTENT_URI,
                    select,
                    where,
                    args,
                    "DTSTART,DTEND ASC");

            if (cursor == null || cursor.getCount() < 1)
                return null;
            else {
                cursor.moveToFirst();
                String[] info = new String[3];
                info[0] = cursor.getString(1);

                SimpleDateFormat sdfDate = new SimpleDateFormat("dd-MMM-yyyy  HH:mm:ss", Locale.getDefault());
                info[1] = sdfDate.format(new Date(cursor.getLong(2)));
                info[2] = sdfDate.format(new Date(cursor.getLong(3)));

                String event = "Imate događaj " + info[0] + " u kalendaru\n\n" +
                        "Početak: " + info[1] + "\n" +
                        "Kraj: " + info[2] + "\n";

                cursor.close();
                return event;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void porukaOdListeFrag(ArrayList<Kviz> noviKvizovi) {
        detailFrag.primiPorukuOdListeFrag(noviKvizovi);
    }

    @Override
    public void onNetworkLost() {
        isOnline = false;
    }

    @Override
    public void onNetworkAvailable() {
        // ukoliko je aplikacija pokrenuta bez interneta
        if (TOKEN == null || TOKEN.length() == 0) {
            try {
                AccessToken accessToken = new AccessToken();
                accessToken.execute(this);
                TOKEN = accessToken.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        isOnline = true;

        // ucitati podatke o ranglistama iz SQLite u Firebase:
        azurirajRangListuUFirebaseu();
    }

    public void azurirajRangListuUFirebaseu() {
        ArrayList<RangListaItem> rangListaItems = databaseHelper.dajRangListe();
        Intent intent = new Intent(Intent.ACTION_SYNC, null, this, GetRequestIntentService.class);
        intent.putExtra("TOKEN", TOKEN);
        intent.putExtra("akcija", GetRequestIntentService.AKCIJA_SQLITE_POST);
        intent.putExtra("receiver", receiver);
        intent.putExtra("rangListe", rangListaItems);
        startService(intent);
    }

    // invoked when the activity may be temporarily destroyed, save the instance state here
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("kvizovi", kvizovi);
        outState.putSerializable("kategorije", kategorije);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        connectionStateMonitor.unregisterNetworkCallback();
    }

    public static SQLiteDBHelper getDatabaseHelper() {
        return databaseHelper;
    }
}
