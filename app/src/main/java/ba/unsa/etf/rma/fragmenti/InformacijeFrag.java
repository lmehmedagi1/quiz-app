package ba.unsa.etf.rma.fragmenti;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.aktivnosti.IgrajKvizAkt;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.klase.Pitanje;

public class InformacijeFrag extends Fragment {

    public interface porukaOdInformacija {
        void porukaOdInformacija (Pitanje pitanje);
        void porukaOZadnjemPitanju();
    }

    private porukaOdInformacija callback;

    private Kviz kviz = null;
    private TextView nazivKvizaTV;
    private TextView brojTacnihPitanjaTV;
    private TextView brojPreostalihPitanjaTV;
    private TextView procenatTacniTV;
    private Button zavrsiButton;

    private ArrayList<Pitanje> postavljenaPitanja = new ArrayList<>();
    private ArrayList<Pitanje> preostalaPitanja = new ArrayList<>();

    private Pitanje trenutnoPitanje = null;

    private static int brojTacnih = 0;


    public InformacijeFrag() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (kviz == null)
            kviz = ((IgrajKvizAkt) getActivity()).dajKviz();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_informacije, container, false);

        brojTacnih = 0;

        nazivKvizaTV = (TextView) v.findViewById(R.id.infNazivKviza);
        brojTacnihPitanjaTV = (TextView) v.findViewById(R.id.infBrojTacnihPitanja);
        brojPreostalihPitanjaTV = (TextView) v.findViewById(R.id.infBrojPreostalihPitanja);
        procenatTacniTV = (TextView) v.findViewById(R.id.infProcenatTacni);
        zavrsiButton = (Button) v.findViewById(R.id.btnKraj);

        nazivKvizaTV.setText(kviz.getNaziv());
        brojTacnihPitanjaTV.setText("0");
        procenatTacniTV.setText("0%");

        if (kviz != null)
            preostalaPitanja.addAll(kviz.getPitanja());


        if (preostalaPitanja.size() == 0)
            brojPreostalihPitanjaTV.setText(String.valueOf(0));
        else
            brojPreostalihPitanjaTV.setText(String.valueOf(preostalaPitanja.size()-1));

        // šaljemo prvo pitanje
        trenutnoPitanje = dajRandomPitanje();
        if (trenutnoPitanje != null)
            callback.porukaOdInformacija(trenutnoPitanje);
        else
            callback.porukaOZadnjemPitanju();

        dodajListenerNaButton();
        return v;
    }

    private Pitanje dajRandomPitanje() {
        if (preostalaPitanja.size() == 0) return null;

        Collections.shuffle(preostalaPitanja);

        Pitanje pitanje = preostalaPitanja.get(0);
        preostalaPitanja.remove(0);
        postavljenaPitanja.add(pitanje);

        return pitanje;
    }

    private void dodajListenerNaButton() {
        zavrsiButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getActivity().finish();
            }
        });
    }



    public void primiPorukuOdPitanjaFragment(boolean tacanOdgovor) {
        if (tacanOdgovor) brojTacnih++;

        double procenat = 0;
        if (postavljenaPitanja.size() != 0) procenat = ((double)brojTacnih)/(postavljenaPitanja.size());

        NumberFormat format = NumberFormat.getPercentInstance(Locale.US);
        String percentage = format.format(procenat);

        brojTacnihPitanjaTV.setText(String.valueOf(brojTacnih));
        if (preostalaPitanja.size() == 0)
            brojPreostalihPitanjaTV.setText(String.valueOf(preostalaPitanja.size()));
        else
            brojPreostalihPitanjaTV.setText(String.valueOf(preostalaPitanja.size()-1));

        procenatTacniTV.setText(percentage);


        trenutnoPitanje = dajRandomPitanje();
        if (trenutnoPitanje != null)
            callback.porukaOdInformacija(trenutnoPitanje);
        else
            callback.porukaOZadnjemPitanju();

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof porukaOdInformacija)
            callback = (porukaOdInformacija) context;
    }
    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }
}
