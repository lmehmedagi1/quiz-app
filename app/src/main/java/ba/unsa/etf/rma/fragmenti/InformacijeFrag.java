package ba.unsa.etf.rma.fragmenti;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.aktivnosti.IgrajKvizAkt;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.klase.Pitanje;

public class InformacijeFrag extends Fragment {

    public interface porukaOdInformacija {
        void porukaOdInformacija (Pitanje pitanje);
        void porukaOZadnjemPitanju(String s, double p);
        void imeNijeUneseno();
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

    private View v = null;
    private double procenat = 0;


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
        v = inflater.inflate(R.layout.fragment_informacije, container, false);

        brojTacnih = 0;

        nazivKvizaTV = (TextView) v.findViewById(R.id.infNazivKviza);
        brojTacnihPitanjaTV = (TextView) v.findViewById(R.id.infBrojTacnihPitanja);
        brojPreostalihPitanjaTV = (TextView) v.findViewById(R.id.infBrojPreostalihPitanja);
        procenatTacniTV = (TextView) v.findViewById(R.id.infProcenatTacni);
        zavrsiButton = (Button) v.findViewById(R.id.btnKraj);

        dodajListenerNaButton();

        nazivKvizaTV.setText(kviz.getNaziv());
        brojTacnihPitanjaTV.setText("0");
        procenatTacniTV.setText("0%");

        if (kviz != null)
            preostalaPitanja.addAll(kviz.getPitanja());


        if (preostalaPitanja.size() == 0) {
            brojPreostalihPitanjaTV.setText(String.valueOf(0));
            return v;
        }
        else
            brojPreostalihPitanjaTV.setText(String.valueOf(preostalaPitanja.size()-1));

        // šaljemo prvo pitanje
        trenutnoPitanje = dajRandomPitanje();
        if (trenutnoPitanje != null)
            callback.porukaOdInformacija(trenutnoPitanje);
        else
            zadnjePitanje();

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
                ((IgrajKvizAkt) getActivity()).zavrsiIgranje();
            }
        });
    }



    public void primiPorukuOdPitanjaFragment(boolean tacanOdgovor) {
        if (tacanOdgovor) brojTacnih++;

        procenat = 0;
        if (postavljenaPitanja.size() != 0) procenat = ((double)brojTacnih)/(postavljenaPitanja.size());

        NumberFormat format = NumberFormat.getPercentInstance(Locale.US);
        format.setMaximumFractionDigits(2);
        format.setMinimumFractionDigits(2);
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
            zadnjePitanje();

    }

    private void zadnjePitanje() {

        final EditText input = new EditText(v.getContext());
        String ime = "";

        AlertDialog alert = new AlertDialog.Builder(getActivity())
                .setTitle("Kviz završen")
                .setMessage("Unesite ime i prezime")
                .setView(input)
                .setPositiveButton("OK", null)
                .create();

        alert.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(final DialogInterface dialog) {
                Button buttonOk = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                if (buttonOk != null) {
                    buttonOk.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (input.getText() != null && !input.getText().toString().equals("")) {
                                String ime = input.getText().toString();
                                dialog.cancel();
                                zavrsiKviz(ime);
                            }
                            else {
                                input.setError("Morate unijeti ime i prezime");
                            }
                        }
                    });
                }
            }
        });

        alert.show();
        callback.imeNijeUneseno();
    }

    private void zavrsiKviz(String ime) {
        callback.porukaOZadnjemPitanju(ime, procenat);
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
