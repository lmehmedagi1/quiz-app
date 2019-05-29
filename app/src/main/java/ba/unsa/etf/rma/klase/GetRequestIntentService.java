package ba.unsa.etf.rma.klase;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class GetRequestIntentService extends IntentService {

    private static final String TAG = GetRequestIntentService.class.getSimpleName();

    public static final int STATUS_ERROR = 0;
    public static final int KVIZOVI_UPDATE = 10;
    public static final int KATEGORIJE_UPDATE = 20;
    public static final int PITANJA_UPDATE = 30;
    public static final int RANGLISTE_UPDATE = 40;
    public static final int ODABRANA_KATEGORIJA = 50;

    public static final int AKCIJA_KVIZOVI = 11;
    public static final int AKCIJA_PITANJA = 12;
    public static final int AKCIJA_KATEGORIJE = 13;
    public static final int AKCIJA_RANGLISTE = 14;
    public static final int AKCIJA_ODABRANA_KATEGORIJA = 15;

    private HttpURLConnection connection = null;
    private URL url = null;
    private String token = "";


    public GetRequestIntentService() {
        super("GetRequestIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final ResultReceiver receiver = intent.getParcelableExtra("receiver");
            Bundle bundle = new Bundle();

            int akcija = intent.getIntExtra("akcija", 0);

            boolean trebaKvizove       = intent.getBooleanExtra("trebaKvizove", false);
            boolean trebaKategorije    = intent.getBooleanExtra("trebaKategorije", false);
            boolean trebaPitanja       = intent.getBooleanExtra("trebaPitanja", false);
            boolean trebaRangliste     = intent.getBooleanExtra("trebaRangliste", false);
            boolean odabranaKategorija = intent.getBooleanExtra("odabranaKategorija", true);

            String urlString, result;
            token = intent.getStringExtra("TOKEN");

            try {
                if (akcija == AKCIJA_KVIZOVI) {
                    ArrayList<Pitanje> pitanja = new ArrayList<>();
                    ArrayList<Kategorija> kategorije = new ArrayList<>();

                    urlString = "https://firestore.googleapis.com/v1/projects/rma18174-firebase/databases/(default)/documents/Kategorije?access_token=";
                    url = new URL(urlString + URLEncoder.encode(token, "UTF-8"));
                    result = getResponse("", true);
                    kategorije = ucitajKategorije(result);
                    bundle.putSerializable("kategorije", kategorije);

                    urlString = "https://firestore.googleapis.com/v1/projects/rma18174-firebase/databases/(default)/documents/Pitanja?access_token=";
                    url = new URL(urlString + URLEncoder.encode(token, "UTF-8"));
                    result = getResponse("", true);
                    pitanja = ucitajPitanja(result);
                    bundle.putSerializable("pitanja", pitanja);

                    urlString = "https://firestore.googleapis.com/v1/projects/rma18174-firebase/databases/(default)/documents/Kvizovi?access_token=";
                    url = new URL(urlString + URLEncoder.encode(token, "UTF-8"));
                    result = getResponse("", true);
                    bundle.putSerializable("kvizovi", ucitajKvizove(result, kategorije, pitanja, null));

                    receiver.send(KVIZOVI_UPDATE, bundle);
                }
                else if (akcija == AKCIJA_ODABRANA_KATEGORIJA) {
                    Kategorija odabrana = (Kategorija) intent.getSerializableExtra("kategorija");

                    urlString = "https://firestore.googleapis.com/v1/projects/rma18174-firebase/databases/(default)/documents/Pitanja?access_token=";
                    url = new URL(urlString + URLEncoder.encode(token, "UTF-8"));
                    result = getResponse("", true);
                    ArrayList<Pitanje> pitanja = ucitajPitanja(result);
                    bundle.putSerializable("pitanja", pitanja);

                    bundle.putSerializable("kvizovi", dajKvizoveIzKategorije(odabrana, pitanja));
                    receiver.send(ODABRANA_KATEGORIJA, bundle);
                }
                else if (akcija == AKCIJA_KATEGORIJE) {
                    urlString = "https://firestore.googleapis.com/v1/projects/rma18174-firebase/databases/(default)/documents/Kategorije?access_token=";
                    url = new URL(urlString + URLEncoder.encode(token, "UTF-8"));
                    result = getResponse("", true);
                    bundle.putSerializable("kategorije", ucitajKategorije(result));
                    receiver.send(KATEGORIJE_UPDATE, bundle);
                }
                else if (akcija == AKCIJA_PITANJA) {
                    urlString = "https://firestore.googleapis.com/v1/projects/rma18174-firebase/databases/(default)/documents/Pitanja?access_token=";
                    url = new URL(urlString + URLEncoder.encode(token, "UTF-8"));
                    result = getResponse("", true);
                    bundle.putSerializable("pitanja", ucitajPitanja(result));
                    receiver.send(PITANJA_UPDATE, bundle);
                }
                else if (akcija == AKCIJA_RANGLISTE) {
                    urlString = "https://firestore.googleapis.com/v1/projects/rma18174-firebase/databases/(default)/documents/Rangliste?access_token=";
                    url = new URL(urlString + URLEncoder.encode(token, "UTF-8"));
                    result = getResponse("", true);
                    bundle.putSerializable("rangliste", ucitajRangListu(result));
                    receiver.send(RANGLISTE_UPDATE, bundle);
                }
            }
            catch(Exception e){
                e.printStackTrace();
                receiver.send(STATUS_ERROR, bundle);
            }
        }
    }

    private ArrayList<RangListaItem> ucitajRangListu(String result) {
        if (result.equals("{}")) return new ArrayList<>();

        ArrayList<RangListaItem> rangListaArray = new ArrayList<>();

        String[] rangListaCollection = result.split("\\{\n\"name\": ");

        for (int i = 1; i < rangListaCollection.length; i++) {
            Log.wtf("TAGELEMENTID", rangListaCollection[i]);


            String rangListaDocument = rangListaCollection[i];

            String[] rows = rangListaDocument.split("\n");

            String id = rows[0].substring(rows[0].length() - 38, rows[0].length() - 2);
            String nazivKviza = "";
            String imeIgraca = "";
            int pozicija = 0;
            double procenat = 0;

            for (int j = 1; j < rows.length; j++) {
                if (rows[j].contains("\"nazivKviza\": "))
                    nazivKviza = rows[j + 1].substring(16, rows[j + 1].length() - 1);

                if (rows[j].contains("\"imeIgraca\": "))
                    imeIgraca = rows[j + 1].substring(16, rows[j + 1].length() - 1);

                if (rows[j].contains("\"pozicija\": "))
                    pozicija = Integer.parseInt(rows[j + 1].substring(17, rows[j + 1].length() - 1));

                if (rows[j].contains("\"procenatTacnih\": "))
                    procenat = Double.parseDouble(rows[j + 1].substring(15));
            }

            RangListaItem rangLista = new RangListaItem(imeIgraca, nazivKviza, procenat, pozicija);
            rangLista.setIdDokumenta(id);
            rangListaArray.add(rangLista);
        }
        return rangListaArray;
    }

    private ArrayList<Pitanje> ucitajPitanja(String result) {
        if (result.equals("{}")) return new ArrayList<>();

        ArrayList<Pitanje> pitanja = new ArrayList<>();

        String[] pitanjeCollection = result.split("\\{\n\"name\": ");

        for (int i = 1; i < pitanjeCollection.length; i++) {

            String pitanjeDocument = pitanjeCollection[i];

            String[] rows = pitanjeDocument.split("\n");

            String id = rows[0].substring(rows[0].length() - 38, rows[0].length() - 2);
            int indexTacnog = 0;
            String naziv = "";
            ArrayList<String> odgovori = new ArrayList<>();

            for (int j = 1; j < rows.length; j++) {
                if (rows[j].contains("\"naziv\": "))
                    naziv = rows[j + 1].substring(16, rows[j + 1].length() - 1);

                if (rows[j].contains("\"indexTacnog\": "))
                    indexTacnog = Integer.parseInt(rows[j + 1].substring(17, rows[j + 1].length() - 1));

                if (rows[j].contains("\"arrayValue\": ")) {
                    if (rows[j+1].contains("values")) {
                        while (!rows[j].equals("]")) {
                            if (rows[j].contains("\"stringValue\": "))
                                odgovori.add(rows[j].substring(16, rows[j].length()-1));
                            j++;
                        }
                    }
                }
            }

            Pitanje pitanje = new Pitanje(naziv, naziv, odgovori, odgovori.get(indexTacnog));
            pitanje.setIdDokumenta(id);
            pitanja.add(pitanje);
        }
        return pitanja;
    }

    private ArrayList<Kategorija> ucitajKategorije(String result) {
        if (result.equals("{}")) return new ArrayList<>();

        ArrayList<Kategorija> kategorije = new ArrayList<>();

        String[] kategorijaCollection = result.split("\\{\n\"name\": ");

        for (int i = 1; i < kategorijaCollection.length; i++) {

            String kategorijaDocument = kategorijaCollection[i];
            String[] rows = kategorijaDocument.split("\n");

            String id = rows[0].substring(rows[0].length() - 38, rows[0].length() - 2);
            String naziv = "";
            String idIkonice = "";

            for (int j = 1; j<rows.length; j++) {
                if (rows[j].contains("\"naziv\": "))
                    naziv = rows[j + 1].substring(16, rows[j + 1].length() - 1);

                if (rows[j].contains("\"idIkonice\": "))
                    idIkonice = rows[j + 1].substring(17, rows[j + 1].length() - 1);
            }

            Kategorija kategorija = new Kategorija(naziv, idIkonice);
            kategorija.setIdDokumenta(id);
            kategorije.add(kategorija);
        }
        return kategorije;
    }

    private ArrayList<Kviz> ucitajKvizove(String result, ArrayList<Kategorija> kategorije, ArrayList<Pitanje> pitanja, Kategorija odabrana) {
        if (result.equals("{}") || result.equals("[{]\n]")) return new ArrayList<>();

        ArrayList<Kviz> kvizovi = new ArrayList<>();

        String[] kvizCollection = result.split("\\{\n\"name\": ");

        for (int i = 1; i < kvizCollection.length; i++) {

            String kvizDocument = kvizCollection[i];

            String[] rows = kvizDocument.split("\n");

            String id = rows[0].substring(rows[0].length() - 38, rows[0].length() - 2);
            String naziv = "";
            String idKategorije = "";
            Kategorija kategorija = null;
            ArrayList<String> idPitanja = new ArrayList<>();
            ArrayList<Pitanje> pitanjaZaKviz = new ArrayList<>();

            for (int j = 1; j < rows.length; j++) {
                if (rows[j].contains("\"naziv\": "))
                    naziv = rows[j + 1].substring(16, rows[j + 1].length() - 1);

                if (rows[j].contains("\"idKategorije\": "))
                    idKategorije = rows[j + 1].substring(16, rows[j + 1].length() - 1);

                if (rows[j].contains("\"arrayValue\": ")) {
                    if (rows[j+1].contains("values")) {
                        while (!rows[j].equals("]")) {
                            if (rows[j].contains("\"stringValue\": "))
                                idPitanja.add(rows[j].substring(16, rows[j].length()-1));
                            j++;
                        }
                    }
                }
            }

            if (odabrana == null) {
                for (Kategorija k : kategorije)
                    if (k.getIdDokumenta().equals(idKategorije))
                        kategorija = k;
                if (kategorija == null)
                    kategorija = new Kategorija("Svi", "-1");
            }
            else
                kategorija = odabrana;


            for (Pitanje p : pitanja) {
                for (String pitanjeID : idPitanja) {
                    if (p.getIdDokumenta().equals(pitanjeID))
                        pitanjaZaKviz.add(p);
                }
            }

            Kviz kviz = new Kviz(naziv, pitanjaZaKviz, kategorija);
            kviz.setIdDokumenta(id);
            kvizovi.add(kviz);
        }

        return kvizovi;
    }

    private String getResponse(String upit, boolean isGET) {
        try {
            connect(isGET);
            if (!isGET) {
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = upit.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
            }

            InputStreamReader streamReader = new InputStreamReader(connection.getInputStream());
            BufferedReader reader = new BufferedReader(streamReader);
            StringBuilder stringBuilder = new StringBuilder();

            String inputLine;
            while((inputLine = reader.readLine()) != null) {
                stringBuilder.append(inputLine.trim());
                stringBuilder.append("\n");
            }

            reader.close();
            streamReader.close();

            return stringBuilder.toString();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private void connect(boolean isGET) throws Exception {
        connection =(HttpURLConnection) url.openConnection();
        if (isGET) {
            connection.setRequestMethod("GET");
        }
        else {
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
        }
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
    }

    private ArrayList<Kviz> dajKvizoveIzKategorije(Kategorija odabrana, ArrayList<Pitanje> pitanja) throws Exception {

        String upit = "{\n" +
                " \"structuredQuery\": {\n" +
                "  \"select\": {\n" +
                "   \"fields\": [\n" +
                "    {\n" +
                "     \"fieldPath\": \"idKategorije\"\n" +
                "    },\n" +
                "    {\n" +
                "     \"fieldPath\": \"naziv\"\n" +
                "    },\n" +
                "    {\n" +
                "     \"fieldPath\": \"pitanja\"\n" +
                "    }\n" +
                "   ]\n" +
                "  },\n" +
                "  \"from\": [\n" +
                "   {\n" +
                "    \"collectionId\": \"Kvizovi\"\n" +
                "   }\n" +
                "  ],\n" +
                "  \"where\": {\n" +
                "   \"fieldFilter\": {\n" +
                "    \"field\": {\n" +
                "     \"fieldPath\": \"idKategorije\"\n" +
                "    },\n" +
                "    \"op\": \"EQUAL\",\n" +
                "    \"value\": {\n" +
                "     \"stringValue\": \"" + odabrana.getIdDokumenta() + "\"\n" +
                "    }\n" +
                "   }\n" +
                "  },\n" +
                "  \"limit\": 1000\n" +
                " }\n" +
                "}\n" +
                "\n";

        String stringURL = "https://firestore.googleapis.com/v1/projects/rma18174-firebase/databases/(default)/documents:runQuery?fields=document(fields%2Cname)&access_token=";

        url = new URL(stringURL + URLEncoder.encode(token, "UTF-8"));

        String result = getResponse(upit, false);
        return ucitajKvizove(result, null, pitanja, odabrana);
    }
}
