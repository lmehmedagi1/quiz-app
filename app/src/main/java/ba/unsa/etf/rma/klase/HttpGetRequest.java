package ba.unsa.etf.rma.klase;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class HttpGetRequest extends AsyncTask<String, Void, ArrayList<Object>> {

    String token = "";
    ArrayList<Kviz> kvizovi = new ArrayList<>();
    ArrayList<Kategorija> kategorije = new ArrayList<>();
    ArrayList<Pitanje> pitanja = new ArrayList<>();

    @Override
    protected ArrayList<Object> doInBackground(String... strings) {
        token = strings[0];
        String result, urlString;
        URL url;

        try {
            urlString = "https://firestore.googleapis.com/v1/projects/rma18174-firebase/databases/(default)/documents/Kategorije?access_token=";
            url = new URL(urlString + URLEncoder.encode(token, "UTF-8"));
            result = getResponse(url);
            ucitajKategorije(result);

            urlString = "https://firestore.googleapis.com/v1/projects/rma18174-firebase/databases/(default)/documents/Pitanja?access_token=";
            url = new URL(urlString + URLEncoder.encode(token, "UTF-8"));
            result = getResponse(url);
            ucitajPitanja(result);

            urlString = "https://firestore.googleapis.com/v1/projects/rma18174-firebase/databases/(default)/documents/Kvizovi?access_token=";
            url = new URL(urlString + URLEncoder.encode(token, "UTF-8"));
            result = getResponse(url);
            ucitajKvizove(result);

            ArrayList<Object> podaci = new ArrayList<>();
            podaci.add(kvizovi);
            podaci.add(kategorije);
            podaci.add(pitanja);

            return podaci;
        }
        catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }

    private void ucitajPitanja(String result) {
        if (result.equals("{}")) return;

        String[] pitanjeCollection = result.split("\\{\n\"name\": ");

        for (int i = 1; i < pitanjeCollection.length; i++) {
            Log.wtf("TAGELEMENTID", pitanjeCollection[i]);

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
    }

    private void ucitajKategorije(String result) {
        if (result.equals("{}")) return;

        String[] kategorijaCollection = result.split("\\{\n\"name\": ");

        for (int i = 1; i < kategorijaCollection.length; i++) {
            Log.wtf("TAGELEMENTID", kategorijaCollection[i]);

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
    }

    private void ucitajKvizove(String result) {
        if (result.equals("{}")) return;

        String[] kvizCollection = result.split("\\{\n\"name\": ");

        for (int i = 1; i < kvizCollection.length; i++) {
            Log.wtf("TAGELEMENTID", kvizCollection[i]);

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

            for (Kategorija k : kategorije)
                if (k.getIdDokumenta().equals(idKategorije))
                    kategorija = k;
            if (kategorija == null)
                kategorija = new Kategorija("Svi", "-1");


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
    }

    private String getResponse(URL url) {
        try {
            String inputLine;

            HttpURLConnection connection =(HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");

            InputStreamReader streamReader = new InputStreamReader(connection.getInputStream());
            BufferedReader reader = new BufferedReader(streamReader);
            StringBuilder stringBuilder = new StringBuilder();

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
}
