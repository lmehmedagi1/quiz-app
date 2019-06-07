package ba.unsa.etf.rma.baza;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.aktivnosti.KvizoviAkt;

public class AccessToken extends AsyncTask<Context, Integer, String> {

    private String TOKEN = "";

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }

    @Override
    protected String doInBackground(Context... params) {

        Context context = params[0];

        try {
            InputStream is = context.getResources().openRawResource(R.raw.secret);
            GoogleCredential credentials = null;
            credentials = GoogleCredential.fromStream(is).createScoped(Lists.newArrayList("https://www.googleapis.com/auth/datastore"));
            credentials.refreshToken();
            TOKEN = credentials.getAccessToken();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return TOKEN;
    }
}