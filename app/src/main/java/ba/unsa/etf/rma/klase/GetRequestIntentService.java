package ba.unsa.etf.rma.klase;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class GetRequestIntentService extends IntentService {

    public static final int STATUS_ERROR = 0;
    public static final int AKCIJA_KVIZOVI = 11;
    public static final int AKCIJA_PITANJA = 12;
    public static final int AKCIJA_KATEGORIJE = 13;
    public static final int AKCIJA_RANGLISTE = 14;
    public static final int AKCIJA_ODABRANA_KATEGORIJA = 15;
    public static final int AKCIJA_IMPORT_KATEGORIJA = 16;
    public static final int AKCIJA_IMPORT_PITANJE = 17;
    public static final int IMPORT_PITANJE_ERROR = 18;

    private HttpURLConnection connection = null;
    private URL url = null;
    private String token = "", urlString = "", result = "";
    private Bundle bundle;


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
            bundle = new Bundle();

            int akcija = intent.getIntExtra("akcija", 0);
            token = intent.getStringExtra("TOKEN");

            try {
                if (akcija == AKCIJA_KVIZOVI)
                    akcijaKvizovi(receiver);
                else if (akcija == AKCIJA_ODABRANA_KATEGORIJA)
                    akcijaOdabranaKategorija(receiver, intent);
                else if (akcija == AKCIJA_KATEGORIJE)
                    akcijaKategorije(receiver, intent);
                else if (akcija == AKCIJA_PITANJA)
                    akcijaPitanja(receiver, intent);
                else if (akcija == AKCIJA_RANGLISTE)
                    akcijaRangliste(receiver, intent);
                else if (akcija == AKCIJA_IMPORT_KATEGORIJA)
                    akcijaImportKategorije(receiver, intent);
                else if (akcija == AKCIJA_IMPORT_PITANJE)
                    akcijaImportPitanja(receiver, intent);
            }
            catch(Exception e){
                e.printStackTrace();
                receiver.send(STATUS_ERROR, bundle);
            }
        }
    }



    private void akcijaOdabranaKategorija(ResultReceiver receiver, Intent intent) throws Exception {
        Kategorija odabrana = (Kategorija) intent.getSerializableExtra("kategorija");

        urlString = "https://firestore.googleapis.com/v1/projects/rma18174-firebase/databases/(default)/documents/Pitanja?access_token=";
        url = new URL(urlString + URLEncoder.encode(token, "UTF-8"));
        result = getResponse("", true);
        ArrayList<Pitanje> pitanja = ucitajPitanja(result);
        bundle.putSerializable("pitanja", pitanja);

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

        bundle.putSerializable("kvizovi", ucitajKvizove(result, null, pitanja, odabrana));
        receiver.send(AKCIJA_ODABRANA_KATEGORIJA, bundle);
    }

    private void akcijaKvizovi(ResultReceiver receiver) throws Exception {
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

        receiver.send(AKCIJA_KVIZOVI, bundle);
    }

    private void akcijaKategorije(ResultReceiver receiver, Intent intent) throws Exception {
        urlString = "https://firestore.googleapis.com/v1/projects/rma18174-firebase/databases/(default)/documents/Kategorije?access_token=";
        url = new URL(urlString + URLEncoder.encode(token, "UTF-8"));
        result = getResponse("", true);
        bundle.putSerializable("kategorije", ucitajKategorije(result));
        receiver.send(AKCIJA_KATEGORIJE, bundle);
    }

    private void akcijaPitanja(ResultReceiver receiver, Intent intent) throws Exception {
        urlString = "https://firestore.googleapis.com/v1/projects/rma18174-firebase/databases/(default)/documents/Pitanja?access_token=";
        url = new URL(urlString + URLEncoder.encode(token, "UTF-8"));
        result = getResponse("", true);
        bundle.putSerializable("pitanja", ucitajPitanja(result));
        receiver.send(AKCIJA_PITANJA, bundle);
    }

    private void akcijaRangliste(ResultReceiver receiver, Intent intent) throws Exception {
        String nazivKviza = intent.getStringExtra("nazivkviza");
        urlString = "https://firestore.googleapis.com/v1/projects/rma18174-firebase/databases/(default)/documents:runQuery?fields=document(fields%2Cname)&access_token=";
        url = new URL(urlString + URLEncoder.encode(token, "UTF-8"));

        String upit = "{\n" +
                " \"structuredQuery\": {\n" +
                "  \"select\": {\n" +
                "   \"fields\": [\n" +
                "    {\n" +
                "     \"fieldPath\": \"nazivKviza\"\n" +
                "    },\n" +
                "    {\n" +
                "     \"fieldPath\": \"lista\"\n" +
                "    }\n" +
                "   ]\n" +
                "  },\n" +
                "  \"from\": [\n" +
                "   {\n" +
                "    \"collectionId\": \"Rangliste\"\n" +
                "   }\n" +
                "  ],\n" +
                "  \"where\": {\n" +
                "   \"fieldFilter\": {\n" +
                "    \"field\": {\n" +
                "     \"fieldPath\": \"nazivKviza\"\n" +
                "    },\n" +
                "    \"op\": \"EQUAL\",\n" +
                "    \"value\": {\n" +
                "     \"stringValue\": \"" + nazivKviza + "\"\n" +
                "    }\n" +
                "   }\n" +
                "  },\n" +
                "  \"limit\": 1000\n" +
                " }\n" +
                "}\n" +
                "\n";

        result = getResponse(upit, false);
        bundle.putSerializable("ranglista", ucitajRangListu(result));
        receiver.send(AKCIJA_RANGLISTE, bundle);
    }

    private void akcijaImportKategorije(ResultReceiver receiver, Intent intent) throws Exception {
        String nazivKategorije = intent.getStringExtra("kategorija");
        ArrayList<Pitanje> pitanja = (ArrayList<Pitanje>) intent.getSerializableExtra("pitanja");
        String nazivKviza = intent.getStringExtra("kviz");

        urlString = "https://firestore.googleapis.com/v1/projects/rma18174-firebase/databases/(default)/documents/Kategorije?access_token=";
        url = new URL(urlString + URLEncoder.encode(token, "UTF-8"));
        result = getResponse("", true);

        bundle.putSerializable("kategorije", ucitajKategorije(result));
        bundle.putString("kategorija", nazivKategorije);
        bundle.putSerializable("pitanja", pitanja);
        bundle.putString("kviz", nazivKviza);

        receiver.send(AKCIJA_IMPORT_KATEGORIJA, bundle);
    }

    private void akcijaImportPitanja(ResultReceiver receiver, Intent intent) {
        ArrayList<Pitanje> pitanja = (ArrayList<Pitanje>) intent.getSerializableExtra("pitanja");

        boolean postojiPitanje = false;
        for (Pitanje p : pitanja) {
            String naziv = p.getNaziv();
            if (postojiPitanjeSaNazivom(naziv)) {
                postojiPitanje = true;
                break;
            }
        }

        if (postojiPitanje)
            receiver.send(IMPORT_PITANJE_ERROR, bundle);
        else {
            bundle.putString("kviz", intent.getStringExtra("kviz"));
            bundle.putString("kategorija", intent.getStringExtra("kategorija"));
            bundle.putSerializable("pitanja", pitanja);
            bundle.putBoolean("trebaDodatiKategoriju", intent.getBooleanExtra("trebaDodatiKategoriju", false));
            receiver.send(AKCIJA_IMPORT_PITANJE, bundle);
        }
    }


    private ArrayList<RangListaItem> ucitajRangListu(String result) {
        if (result.equals("{}") || result.replaceAll("\n","").equals("[{}]")) return new ArrayList<>();

        ArrayList<RangListaItem> rangListaArray = new ArrayList<>();

        String[] rangListaCollection = result.split("\\{\n\"name\": ");

        for (int i = 1; i < rangListaCollection.length; i++) {

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
        if (result.equals("{}") || result.replaceAll("\n","").equals("[{}]")) return new ArrayList<>();

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

    private boolean postojiPitanjeSaNazivom(String naziv) {
        String upit = "{\n" +
                " \"structuredQuery\": {\n" +
                "  \"select\": {\n" +
                "   \"fields\": [\n" +
                "    {\n" +
                "     \"fieldPath\": \"indexTacnog\"\n" +
                "    },\n" +
                "    {\n" +
                "     \"fieldPath\": \"naziv\"\n" +
                "    },\n" +
                "    {\n" +
                "     \"fieldPath\": \"odgovori\"\n" +
                "    }\n" +
                "   ]\n" +
                "  },\n" +
                "  \"from\": [\n" +
                "   {\n" +
                "    \"collectionId\": \"Pitanja\"\n" +
                "   }\n" +
                "  ],\n" +
                "  \"where\": {\n" +
                "   \"fieldFilter\": {\n" +
                "    \"field\": {\n" +
                "     \"fieldPath\": \"naziv\"\n" +
                "    },\n" +
                "    \"op\": \"EQUAL\",\n" +
                "    \"value\": {\n" +
                "     \"stringValue\": \"" + naziv + "\"\n" +
                "    }\n" +
                "   }\n" +
                "  },\n" +
                "  \"limit\": 1000\n" +
                " }\n" +
                "}\n" +
                "\n";

        String stringURL = "https://firestore.googleapis.com/v1/projects/rma18174-firebase/databases/(default)/documents:runQuery?fields=document(fields%2Cname)&access_token=";

        try {
            url = new URL(stringURL + URLEncoder.encode(token, "UTF-8"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String result = getResponse(upit, false);
        if (result.replaceAll("\n","").equals("{}") || result.replaceAll("\n","").equals("[{}]"))
            return false;

        return true;
    }
}
