package ba.unsa.etf.rma.aktivnosti;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.AlarmClock;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;


import java.util.Calendar;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.fragmenti.InformacijeFrag;
import ba.unsa.etf.rma.fragmenti.IstekloVrijemeFrag;
import ba.unsa.etf.rma.fragmenti.PitanjeFrag;
import ba.unsa.etf.rma.fragmenti.RangLista;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.klase.Pitanje;
import ba.unsa.etf.rma.klase.RangListaItem;

public class IgrajKvizAkt extends AppCompatActivity implements InformacijeFrag.porukaOdInformacija, PitanjeFrag.porukaOdPitanja {

    private class AlarmReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            istekloVrijemeFrag = (IstekloVrijemeFrag) getSupportFragmentManager().findFragmentByTag(ISTEKLO_VRIJEME_TAG);
            if (istekloVrijemeFrag == null) {
                istekloVrijemeFrag = new IstekloVrijemeFrag();
                Bundle bundle = new Bundle();
                bundle.putString("poruka", "Vrijeme isteklo");
                istekloVrijemeFrag.setArguments(bundle);
                getSupportFragmentManager().beginTransaction().replace(R.id.pitanjePlace, istekloVrijemeFrag, ISTEKLO_VRIJEME_TAG).addToBackStack(null).commit();
            }

            Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

            if (alert == null) {
                alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                if (alert == null)
                    alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            }

            ringtone = RingtoneManager.getRingtone(context, alert);
            if (ringtone != null)
                ringtone.play();
        }
    }

    private InformacijeFrag informacijeFrag;
    private PitanjeFrag pitanjeFrag;
    private RangLista rangListaFrag;
    private IstekloVrijemeFrag istekloVrijemeFrag;

    private static final String INFO_TAG = "info";
    private static final String PITANJE_TAG = "pitanje";
    private static final String RANG_LISTA_TAG = "ranglista";
    private static final String ISTEKLO_VRIJEME_TAG = "isteklovrijeme";

    private Kviz kviz = null;
    private String token = "";

    private FragmentManager manager = null;
    private Ringtone ringtone;
    private AlarmReceiver alarmReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) return;

        setContentView(R.layout.activity_igraj_kviz_akt);

        Intent intent = getIntent();
        kviz = (Kviz)intent.getSerializableExtra("kviz");
        token = intent.getStringExtra("token");

        manager = getSupportFragmentManager();

        if (kviz.getPitanja() == null || kviz.getPitanja().size() == 0) {
            istekloVrijemeFrag = (IstekloVrijemeFrag) getSupportFragmentManager().findFragmentByTag(ISTEKLO_VRIJEME_TAG);
            if (istekloVrijemeFrag == null) {
                istekloVrijemeFrag = new IstekloVrijemeFrag();
                Bundle bundle = new Bundle();
                bundle.putString("poruka", "Kviz nema pitanja");
                istekloVrijemeFrag.setArguments(bundle);
                getSupportFragmentManager().beginTransaction().add(R.id.pitanjePlace, istekloVrijemeFrag, ISTEKLO_VRIJEME_TAG).addToBackStack(null).commit();
            }
        }
        else {
            alarmReceiver = new AlarmReceiver();
            this.registerReceiver(alarmReceiver, new IntentFilter("ba.unsa.etf.rma.aktivnosti.IgrajKvizAkt$AlarmReceiver"));

            Calendar time = Calendar.getInstance();
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent alarmIntent = new Intent("ba.unsa.etf.rma.aktivnosti.IgrajKvizAkt$AlarmReceiver");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, alarmIntent, 0);

            alarmManager.setExact(AlarmManager.RTC_WAKEUP, time.getTimeInMillis() + kviz.getPitanja().size() * 30300, pendingIntent);

            pitanjeFrag = (PitanjeFrag) manager.findFragmentByTag(PITANJE_TAG);
            if (pitanjeFrag == null) {
                pitanjeFrag = new PitanjeFrag();
                manager.beginTransaction().add(R.id.pitanjePlace, pitanjeFrag, PITANJE_TAG).commit();
            }
        }

        informacijeFrag = (InformacijeFrag) manager.findFragmentByTag(INFO_TAG);
        if (informacijeFrag == null) {
            informacijeFrag = new InformacijeFrag();

            Bundle bundle = new Bundle();
            bundle.putSerializable("kviz", kviz);
            informacijeFrag.setArguments(bundle);

            manager.beginTransaction().add(R.id.informacijePlace, informacijeFrag, INFO_TAG).commit();
        }

    }

    @Override
    public void imeNijeUneseno() {
        pitanjeFrag.primiPorukuOZadnjemPitanju();
    }

    @Override
    public void porukaOdInformacija(Pitanje pitanje) {
        pitanjeFrag.primiPorukuOdInformacijaFragment(pitanje);
    }

    @Override
    public void porukaOZadnjemPitanju(String ime, double procenat) {
        iskljuciAlarm();

        RangListaItem noviIgrac = new RangListaItem(ime, kviz.getNaziv(), procenat, 1);

        rangListaFrag = (RangLista) getSupportFragmentManager().findFragmentByTag(RANG_LISTA_TAG);
        if (rangListaFrag == null) {
            rangListaFrag = new RangLista();
            Bundle bundle = new Bundle();
            bundle.putString("token", token);
            bundle.putSerializable("item", noviIgrac);
            rangListaFrag.setArguments(bundle);

            getSupportFragmentManager().beginTransaction().replace(R.id.pitanjePlace, rangListaFrag, RANG_LISTA_TAG).addToBackStack(null).commit();
        }
    }

    @Override
    public void porukaOdPitanja(boolean tacanOdgovor) {
       informacijeFrag.primiPorukuOdPitanjaFragment(tacanOdgovor);
    }

    public Kviz dajKviz() {
        return kviz;
    }

    public void iskljuciAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent("ba.unsa.etf.rma.aktivnosti.IgrajKvizAkt$AlarmReceiver");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, 0);
        alarmManager.cancel(pendingIntent);
    }

    public void zavrsiIgranje() {
        unregisterReceiver(alarmReceiver);
        if (ringtone != null)
            ringtone.stop();
        iskljuciAlarm();
        finish();
    }


    @Override
    public void onBackPressed() {
        unregisterReceiver(alarmReceiver);
        if (ringtone != null)
            ringtone.stop();
        finish();
    }
}
