package ba.unsa.etf.rma.klase;

import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.common.collect.Lists;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import ba.unsa.etf.rma.R;

public class HttpPatchRequest extends AsyncTask<String, Void, Void> {

    @Override
    protected Void doInBackground(String... strings) {

        try {
            String token = strings[1];
            String urlString = strings[0];

            URL url = new URL(urlString + URLEncoder.encode(token, "UTF-8"));

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("PATCH");
            connection.setRequestProperty("Content-Type", "application/json"); //utf-8 je default encoding
            connection.setRequestProperty("Accept", "application/json");

            String dokument = strings[2];

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = dokument.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            InputStream odgovor = connection.getInputStream();

            try (BufferedReader br  = new BufferedReader(new InputStreamReader(odgovor, StandardCharsets.UTF_8))) {
                StringBuilder response  = new StringBuilder();
                String responseLine = null;

                while ((responseLine = br.readLine()) != null)
                    response.append(responseLine.trim());

                Log.wtf("ODGOVOR", response.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
