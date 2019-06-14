package ba.unsa.etf.rma.aktivnosti;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Handler;
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
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.UUID;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.baza.GetRequestIntentService;
import ba.unsa.etf.rma.baza.GetRequestResultReceiver;
import ba.unsa.etf.rma.baza.HttpPatchRequest;
import ba.unsa.etf.rma.klase.ConnectionStateMonitor;
import ba.unsa.etf.rma.klase.Kategorija;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.klase.Pitanje;
import ba.unsa.etf.rma.adapteri.PitanjeAdapter;

public class DodajKvizAkt extends AppCompatActivity implements GetRequestResultReceiver.Receiver, ConnectionStateMonitor.Network {

    public static final int BACK_FROM_PITANJA = 45;
    public static final int BACK_FROM_KATEGORIJE = 55;

    public static final int ADDED_PITANJE = 65;
    public static final int ADDED_KATEGORIJA = 75;

    public static final int IMPORT_QUIZ = 85;

    private ListView listaDodanihPitanja;
    private ListView listaMogucihPitanja;
    private Button dodajKvizButton;
    private EditText nazivKviza;
    private Spinner spinner;
    private Button importButton;

    private Kviz kviz = null;
    private ArrayList<Kategorija> kategorije = new ArrayList<>();
    private ArrayList<Kviz> kvizovi = new ArrayList<>();
    private ArrayList<Pitanje> dodanaPitanja = new ArrayList<>();
    private ArrayList<Pitanje> mogucaPitanja = new ArrayList<>();
    private ArrayList<Pitanje> svaPitanja = new ArrayList<>();

    private ArrayAdapter<Kategorija> kategorijaAdapter = null;
    private PitanjeAdapter dodanaPitanjaAdapter = null;
    private ArrayAdapter<Pitanje> mogucaPitanjaAdapter = null;

    private View elementZaDodavanje;

    private boolean izmjena = false;
    private String imeOdabranogKviza;

    private String TOKEN = "";
    private GetRequestResultReceiver receiver = null;

    private String trenutniNaziv = "";
    private Kategorija trenutnaKategorija = null;
    private ArrayList<Pitanje> trenutnoDodanaPitanja = null;

    private ConnectionStateMonitor connectionStateMonitor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dodaj_kviz_akt);

        connectionStateMonitor = new ConnectionStateMonitor(this, (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE));
        connectionStateMonitor.registerNetworkCallback();

        // da tastatura ne pomjeri layout
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        listaDodanihPitanja = (ListView) findViewById(R.id.lvDodanaPitanja);
        listaMogucihPitanja = (ListView) findViewById(R.id.lvMogucaPitanja);
        dodajKvizButton     = (Button) findViewById(R.id.btnDodajKviz);
        nazivKviza          = (EditText) findViewById(R.id.etNaziv);
        spinner             = (Spinner) findViewById(R.id.spKategorije);
        importButton        = (Button) findViewById(R.id.btnImportKviz);

        Intent intent = getIntent();
        TOKEN         = intent.getStringExtra("token");
        kviz          = (Kviz)intent.getSerializableExtra("kviz");

        Kategorija sviKategorija = new Kategorija("Svi", "-1");
        sviKategorija.setIdDokumenta("SVIID");
        kategorije.add(sviKategorija);

        kategorijaAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, kategorije);
        kategorijaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        dodanaPitanjaAdapter = new PitanjeAdapter(this, dodanaPitanja);
        mogucaPitanjaAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mogucaPitanja);

        elementZaDodavanje = dodanaPitanjaAdapter.dajElementZaDodavanje(listaDodanihPitanja);
        listaDodanihPitanja.addFooterView(elementZaDodavanje);

        spinner.setAdapter(kategorijaAdapter);
        listaDodanihPitanja.setAdapter(dodanaPitanjaAdapter);
        listaMogucihPitanja.setAdapter(mogucaPitanjaAdapter);

        receiver = new GetRequestResultReceiver(new Handler());
        receiver.setReceiver(this);

        uzmiKviz();

        dodajListenerNaListe();
        dodajListenerNaButton();
        dodajListenerNaSpinner();
        dodajListenerNaEditText();
    }

    private void uzmiKviz() {
        Intent intent = new Intent(Intent.ACTION_SYNC, null, this, GetRequestIntentService.class);
        intent.putExtra("TOKEN", TOKEN);
        intent.putExtra("akcija", GetRequestIntentService.AKCIJA_KVIZ);
        intent.putExtra("kviz", kviz);
        intent.putExtra("receiver", receiver);
        startService(intent);
    }

    private void dodajMogucaPitanja() {
        mogucaPitanja.clear();
        if (kviz == null) {
            mogucaPitanja.addAll(svaPitanja);
        }
        else {
            for (Pitanje p : svaPitanja) {
                int i = 0;
                for (; i<kviz.getPitanja().size(); i++) {
                    if (p.getIdDokumenta().equals(kviz.getPitanja().get(i).getIdDokumenta()))
                        break;
                }
                if (i == kviz.getPitanja().size())
                    mogucaPitanja.add(p);
            }
        }
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        if (resultData != null) {
            if (resultCode == GetRequestIntentService.AKCIJA_KVIZOVI) {
                kvizovi.clear();
                kvizovi.addAll((ArrayList<Kviz>) resultData.getSerializable("kvizovi"));

                for (Kviz k : kvizovi) {
                    if ((!izmjena || !trenutniNaziv.equals(imeOdabranogKviza)) && k.getNaziv().equals(trenutniNaziv)) {
                        nazivKviza.setError("Kviz vec postoji");
                        nazivKviza.setBackgroundColor(Color.RED);
                        return;
                    }
                }
                dodajKviz();
            }
            else if (resultCode == GetRequestIntentService.AKCIJA_IMPORT_KATEGORIJA) {
                String nazivImportovaneKategorije = resultData.getString("kategorija");
                String nazivImportovanogKviza = resultData.getString("kviz");
                ArrayList<Kategorija> azuriraneKategorije = (ArrayList<Kategorija>) resultData.getSerializable("kategorije");
                ArrayList<Pitanje> importovanaPitanja = (ArrayList<Pitanje>) resultData.getSerializable("pitanja");

                Kategorija importovanaKategorija = null;

                for (Kategorija k : azuriraneKategorije) {
                    if (k.getNaziv().equals(nazivImportovaneKategorije)) {
                        importovanaKategorija = k;
                        break;
                    }
                }

                Kategorija sviKategorija = new Kategorija("Svi", "-1");
                sviKategorija.setIdDokumenta("SVIID");

                kategorije.clear();
                kategorije.add(sviKategorija);
                kategorije.addAll(azuriraneKategorije);
                kategorije.add(new Kategorija("Dodaj kategoriju", "Dodaj kategoriju"));
                kategorijaAdapter.notifyDataSetChanged();

                Intent intent = new Intent(Intent.ACTION_SYNC, null, this, GetRequestIntentService.class);
                intent.putExtra("TOKEN", TOKEN);
                intent.putExtra("akcija", GetRequestIntentService.AKCIJA_IMPORT_PITANJE);
                intent.putExtra("pitanja", importovanaPitanja);
                intent.putExtra("kviz", nazivImportovanogKviza);
                intent.putExtra("kategorija", nazivImportovaneKategorije);
                intent.putExtra("receiver", receiver);
                intent.putExtra("trebaDodatiKategoriju", importovanaKategorija == null);

                startService(intent);

            }
            else if (resultCode == GetRequestIntentService.AKCIJA_IMPORT_PITANJE) {
                String nazivImportovaneKategorije = resultData.getString("kategorija");
                String nazivImportovanogKviza = resultData.getString("kviz");
                ArrayList<Pitanje> importovanaPitanja = (ArrayList<Pitanje>) resultData.getSerializable("pitanja");
                boolean trebaDodatiKategoriju = resultData.getBoolean("trebaDodatiKategoriju");

                mogucaPitanja.addAll(dodanaPitanja);
                mogucaPitanjaAdapter.notifyDataSetChanged();
                dodanaPitanja.clear();

                for (Pitanje p : importovanaPitanja)
                    dodajPitanje(p);

                if (trebaDodatiKategoriju) {
                    Kategorija importovanaKategorija = new Kategorija(nazivImportovaneKategorije, "-1");
                    dodajKategoriju(importovanaKategorija);
                }
                else {
                    for (int i = 0; i<kategorije.size(); i++)
                        if (kategorije.get(i).getNaziv().equals(nazivImportovaneKategorije)) {
                            spinner.setSelection(i);
                            kategorijaAdapter.notifyDataSetChanged();
                            break;
                        }
                }
                nazivKviza.setText(nazivImportovanogKviza);
            }
            else if (resultCode == GetRequestIntentService.IMPORT_PITANJE_ERROR) {
                izbaciAlert("Pitanje kviza kojeg importujete vec postoji");
            }
            else if (resultCode == GetRequestIntentService.AKCIJA_KVIZ) {
                svaPitanja.clear();
                svaPitanja.addAll((ArrayList<Pitanje>) resultData.getSerializable("pitanja"));

                kategorije.clear();
                Kategorija sviKategorija = new Kategorija("Svi", "-1");
                sviKategorija.setIdDokumenta("SVIID");
                kategorije.add(sviKategorija);
                kategorije.addAll((ArrayList<Kategorija>) resultData.getSerializable("kategorije"));
                kategorije.add(new Kategorija("Dodaj kategoriju", "Dodaj kategoriju"));
                kategorijaAdapter.notifyDataSetChanged();

                kviz = (Kviz) resultData.getSerializable("kviz");

                if (kviz != null) {
                    for (int i = 0; i<kategorije.size(); i++)
                        if (kategorije.get(i).getNaziv().equals(kviz.getKategorija().getNaziv())) {
                            spinner.setSelection(i);
                            kategorijaAdapter.notifyDataSetChanged();
                            break;
                        }

                    nazivKviza.setText(kviz.getNaziv());
                    imeOdabranogKviza = kviz.getNaziv();
                    dodanaPitanja.clear();
                    dodanaPitanja.addAll(kviz.getPitanja());
                    izmjena = true;
                }

                dodajMogucaPitanja();

                dodanaPitanjaAdapter = new PitanjeAdapter(this, dodanaPitanja);
                listaDodanihPitanja.setAdapter(dodanaPitanjaAdapter);
                mogucaPitanjaAdapter.notifyDataSetChanged();
            }
        }
    }

    private void ocistiBoje() {
        nazivKviza.setBackgroundColor(0);
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

                ocistiBoje();
            }
            public void onNothingSelected(AdapterView<?> parent) {
                spinner.setSelection(0);
                ocistiBoje();
            }
        });
    }
    private void dodajListenerNaButton() {
        dodajKvizButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validniPodaci()) return;
                trenutniNaziv = nazivKviza.getText().toString();
                trenutnaKategorija = (Kategorija) spinner.getSelectedItem();
                trenutnoDodanaPitanja = new ArrayList<>();
                trenutnoDodanaPitanja.addAll(dodanaPitanja);
                uzmiSveKvizove();
            }
        });
        importButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("text/*");
                startActivityForResult(intent, IMPORT_QUIZ);
            }
        });
    }

    private void uzmiSveKvizove() {
        Intent intent = new Intent(Intent.ACTION_SYNC, null, this, GetRequestIntentService.class);
        intent.putExtra("TOKEN", TOKEN);
        intent.putExtra("akcija", GetRequestIntentService.AKCIJA_KVIZOVI);
        intent.putExtra("receiver", receiver);
        startService(intent);
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

    private boolean validniPodaci() {
        if (nazivKviza.getText() == null || nazivKviza.getText().toString().length() == 0) {
            nazivKviza.setBackgroundColor(Color.RED);
            nazivKviza.setError("Morate unijeti naziv kviza");
            return false;
        }
        return true;
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
        intent.putExtra("token", TOKEN);
        intent.putExtra("dodanaPitanja", dodanaPitanja);
        DodajKvizAkt.this.startActivityForResult(intent, ADDED_PITANJE);
    }

    private void otvoriAktivnostZaDodavanjeKategorije() {
        Intent intent = new Intent(DodajKvizAkt.this, DodajKategorijuAkt.class);
        intent.putExtra("TOKEN", TOKEN);
        DodajKvizAkt.this.startActivityForResult(intent, ADDED_KATEGORIJA);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADDED_PITANJE) {
            if (resultCode == ADDED_PITANJE) {
                Pitanje pitanje = (Pitanje)data.getSerializableExtra("pitanje");
                ArrayList<Pitanje> azuriranaPitanja = (ArrayList<Pitanje>) data.getSerializableExtra("azuriranaPitanja");

                mogucaPitanja.clear();
                mogucaPitanja.addAll(azuriranaPitanja);
                mogucaPitanjaAdapter.notifyDataSetChanged();

                dodajPitanje(pitanje);
            }
            else if (resultCode == BACK_FROM_PITANJA) {
                ArrayList<Pitanje> azuriranaPitanja = (ArrayList<Pitanje>) data.getSerializableExtra("azuriranaPitanja");

                if (azuriranaPitanja != null && azuriranaPitanja.size() != 0) {
                    mogucaPitanja.clear();
                    mogucaPitanja.addAll(azuriranaPitanja);
                    mogucaPitanjaAdapter.notifyDataSetChanged();
                }

            }
        }
        else if (requestCode == ADDED_KATEGORIJA) {
            if (resultCode == ADDED_KATEGORIJA) {
                Kategorija kategorija = (Kategorija) data.getSerializableExtra("kategorija");

                Kategorija sviKategorija = new Kategorija("Svi", "-1");
                sviKategorija.setIdDokumenta("SVIID");

                kategorije.clear();
                kategorije.add(sviKategorija);
                kategorije.addAll((ArrayList<Kategorija>) data.getSerializableExtra("kategorije"));
                kategorije.add(new Kategorija("Dodaj kategoriju", "Dodaj kategoriju"));
                kategorijaAdapter.notifyDataSetChanged();

                dodajKategoriju(kategorija);
            }
            else if (resultCode == BACK_FROM_KATEGORIJE) {
                ArrayList<Kategorija> azuriraneKategorije = (ArrayList<Kategorija>) data.getSerializableExtra("kategorije");
                if (azuriraneKategorije != null && azuriraneKategorije.size() != 0) {
                    Kategorija sviKategorija = new Kategorija("Svi", "-1");
                    sviKategorija.setIdDokumenta("SVIID");

                    kategorije.clear();
                    kategorije.add(sviKategorija);
                    kategorije.addAll(azuriraneKategorije);
                    kategorije.add(new Kategorija("Dodaj kategoriju", "Dodaj kategoriju"));
                    kategorijaAdapter.notifyDataSetChanged();
                }
                spinner.setSelection(0);
            }
        }
        else if (requestCode == IMPORT_QUIZ  && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (data != null) {
                uri = data.getData();
                try {
                    ArrayList<String> importData = readTextFromUri(uri);
                    dodajImportovaniKviz(importData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private void dodajImportovaniKviz(ArrayList<String> importData) {
        try {

            String naziv = "";
            Kategorija kategorija = null;
            ArrayList<Pitanje> pitanjaZaImportovaniKviz = new ArrayList<>();


            if (importData == null || importData.size() == 0) {
                izbaciAlert("Datoteka kviza kojeg importujete nema ispravan format!");
                return;
            }

            String prviRed = importData.get(0);

            StringTokenizer tokenizer = new StringTokenizer(prviRed, ",");
            int i = 0;
            String nazivKategorije = "";
            int brojPitanja = -1;

            while (tokenizer.hasMoreTokens()) {
                if (i == 0) naziv = tokenizer.nextToken();
                if (i == 1) nazivKategorije = tokenizer.nextToken();
                if (i == 2) brojPitanja = Integer.parseInt(tokenizer.nextToken());
                i++;
                if (i == 4) break;
            }

            if (i != 3) {
                izbaciAlert("Datoteka kviza kojeg importujete nema ispravan format!");
                return;
            }

            for (Kviz k : kvizovi) {
                if (k.getNaziv().equals(naziv)) {
                    izbaciAlert("Kviz kojeg importujete već postoji!");
                    return;
                }
            }

            for (Kategorija k : kategorije) {
                if (k.getNaziv().equals(nazivKategorije)) {
                    kategorija = k;
                }
            }


            if (brojPitanja != importData.size() - 1) {
                izbaciAlert("Kviz kojeg imporujete ima neispravan broj pitanja!");
                return;
            }


            for (int j = 1; j < importData.size(); j++) {
                String pitanjeInfo = importData.get(j);

                if (pitanjeInfo == null || pitanjeInfo.length() == 0) {
                    izbaciAlert("Datoteka kviza kojeg importujete nema ispravan format!");
                    return;
                }

                StringTokenizer tokenizerPitanja = new StringTokenizer(pitanjeInfo, ",");
                int k = 0;
                String nazivPitanja = "";
                int brojOdgovora = -1, indeksTacnog = -1;

                ArrayList<String> odgovori = new ArrayList<>();

                while (tokenizerPitanja.hasMoreTokens()) {
                    if (k == 0) nazivPitanja = tokenizerPitanja.nextToken();
                    if (k == 1) brojOdgovora = Integer.parseInt(tokenizerPitanja.nextToken());
                    if (k == 2) indeksTacnog = Integer.parseInt(tokenizerPitanja.nextToken());

                    if (k > 2) {
                        String odgovor = tokenizerPitanja.nextToken();

                        for (String o : odgovori) {
                            if (odgovor.equals(o)) {
                                izbaciAlert("Kviz kojeg importujete nije ispravan postoji ponavljanje odgovora!");
                                return;
                            }
                        }

                        odgovori.add(odgovor);
                    }

                    k++;
                }

                for (Pitanje p : pitanjaZaImportovaniKviz) {
                    if (p.getNaziv().equals(nazivPitanja)) {
                        izbaciAlert("Kviz nije ispravan postoje dva pitanja sa istim nazivom!");
                        return;
                    }
                }

                if (brojOdgovora != odgovori.size() || brojOdgovora == 0) {
                    izbaciAlert("Kviz kojeg importujete ima neispravan broj odgovora!");
                    return;
                }

                if (indeksTacnog < 0 || indeksTacnog >= odgovori.size()) {
                    izbaciAlert("Kviz kojeg importujete ima neispravan index tačnog odgovora!");
                    return;
                }

                pitanjaZaImportovaniKviz.add(new Pitanje(nazivPitanja, nazivPitanja, odgovori, odgovori.get(indeksTacnog)));
            }


            if (kategorija == null) {
                Intent intent = new Intent(Intent.ACTION_SYNC, null, this, GetRequestIntentService.class);
                intent.putExtra("TOKEN", TOKEN);
                intent.putExtra("akcija", GetRequestIntentService.AKCIJA_IMPORT_KATEGORIJA);
                intent.putExtra("pitanja", pitanjaZaImportovaniKviz);
                intent.putExtra("kviz", naziv);
                intent.putExtra("kategorija", nazivKategorije);
                intent.putExtra("receiver", receiver);
                startService(intent);
            }
            else {
                Intent intent = new Intent(Intent.ACTION_SYNC, null, this, GetRequestIntentService.class);
                intent.putExtra("TOKEN", TOKEN);
                intent.putExtra("akcija", GetRequestIntentService.AKCIJA_IMPORT_PITANJE);
                intent.putExtra("pitanja", pitanjaZaImportovaniKviz);
                intent.putExtra("kviz", naziv);
                intent.putExtra("kategorija", nazivKategorije);
                intent.putExtra("trebaDodatiKategoriju", false);
                intent.putExtra("receiver", receiver);
                startService(intent);
            }
        }
        catch (Exception e) {
            izbaciAlert("Datoteka kviza kojeg importujete nema ispravan format!");
        }
    }

    private void izbaciAlert(String poruka) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(poruka);
        builder.setNeutralButton("OK", null);
        builder.create().show();
    }

    private ArrayList<String> readTextFromUri(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        if (inputStream == null) return null;
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        ArrayList<String> importData = new ArrayList<>();

        String line;
        while ((line = reader.readLine()) != null) {
            importData.add(line);
        }
        inputStream.close();
        return importData;
    }

    private void dodajKategoriju(Kategorija kategorija) {

        String id = UUID.randomUUID().toString();
        kategorija.setIdDokumenta(id);

        String url = "https://firestore.googleapis.com/v1/projects/rma18174-firebase/databases/(default)/documents/Kategorije/" + id + "?access_token=";
        String dokument = "{\"fields\": { \"naziv\": {\"stringValue\": \"" + kategorija.getNaziv() + "\"}," +
                                         "\"idIkonice\": {\"integerValue\": \"" + kategorija.getId() + "\"}}}";

        HttpPatchRequest postRequest = new HttpPatchRequest();
        postRequest.execute(url, TOKEN, dokument);

        kategorije.remove(kategorije.size()-1);
        kategorije.add(kategorija);
        kategorije.add(new Kategorija("Dodaj kategoriju", "Dodaj kategoriju"));
        kategorijaAdapter.notifyDataSetChanged();

        KvizoviAkt.getDatabaseHelper().dodajKategorija(kategorija);

        spinner.setSelection(kategorije.size()-2);
    }

    private void dodajPitanje(Pitanje pitanje) {

        String id = UUID.randomUUID().toString();
        pitanje.setIdDokumenta(id);

        String url = "https://firestore.googleapis.com/v1/projects/rma18174-firebase/databases/(default)/documents/Pitanja/" + id + "?access_token=";
        String dokument = "{\"fields\": { \"naziv\": {\"stringValue\": \"" + pitanje.getNaziv() + "\"}," +
                                         "\"odgovori\": {\"arrayValue\": {\"values\": [";

        int indexTacnog = 0;

        ArrayList<String> odgovori = pitanje.getOdgovori();
        for (int i = 0; i<odgovori.size(); i++) {
            dokument += "{\"stringValue\": \"" + odgovori.get(i) + "\"}";
            if (i < odgovori.size() - 1)
                dokument += ",";
            if (odgovori.get(i).equals(pitanje.getTacan()))
                indexTacnog = i;
        }

        dokument += "]}}, \"indexTacnog\": {\"integerValue\": \"" + indexTacnog + "\"}}}";

        HttpPatchRequest patchRequest = new HttpPatchRequest();
        patchRequest.execute(url, TOKEN, dokument);

        KvizoviAkt.getDatabaseHelper().dodajPitanje(pitanje);

        dodanaPitanja.add(pitanje);

        dodanaPitanjaAdapter = new PitanjeAdapter(this, dodanaPitanja);
        listaDodanihPitanja.setAdapter(dodanaPitanjaAdapter);
    }

    private void dodajKviz() {
        Intent intent = new Intent();
        String idDokumenta = "";

        if (kviz == null) intent.putExtra("izmjena", "dodavanje");
        else {
            intent.putExtra("izmjena", "izmjena");
            idDokumenta = kviz.getIdDokumenta();
        }

        kviz = new Kviz(trenutniNaziv, trenutnoDodanaPitanja, trenutnaKategorija);
        kviz.setIdDokumenta(idDokumenta);
        intent.putExtra("kviz", kviz);
        intent.putExtra("odabraniKviz", imeOdabranogKviza);
        intent.putExtra("kvizovi", kvizovi);

        kategorije.remove(kategorije.size()-1);
        intent.putExtra("kategorije", kategorije);

        setResult(KvizoviAkt.ADDED_KVIZ, intent);
        connectionStateMonitor.unregisterNetworkCallback();
        finish();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        kategorije.remove(kategorije.size()-1);
        intent.putExtra("kategorije", kategorije);
        setResult(KvizoviAkt.BACK_FROM_DODAJ_KVIZ, intent);
        connectionStateMonitor.unregisterNetworkCallback();
        finish();
    }

    @Override
    public void onNetworkAvailable() {}

    @Override
    public void onNetworkLost() {
        Toast.makeText(this, "Ne mozete dodavati kviz bez pristupa internetu", Toast.LENGTH_SHORT).show();
        finish();
    }
}
