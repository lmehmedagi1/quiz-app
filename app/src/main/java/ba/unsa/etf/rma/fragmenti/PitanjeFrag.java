package ba.unsa.etf.rma.fragmenti;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.klase.Pitanje;

public class PitanjeFrag extends Fragment {

    public interface porukaOdPitanja {
        void porukaOdPitanja (boolean tacanOdgovor);
    }

    private porukaOdPitanja callback;


    private TextView tekstPitanjaTV;
    private ListView listaOdgovora;

    private ArrayList<String> randomOdgovori = new ArrayList<>();
    private Pitanje pitanje = null;


    public PitanjeFrag() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_pitanje, container, false);

        tekstPitanjaTV = (TextView) v.findViewById(R.id.tekstPitanja);
        listaOdgovora = (ListView) v.findViewById(R.id.odgovoriPitanja);



        return v;
    }

    public void primiPorukuOdInformacijaFragment(Pitanje novoPitanje) {
        pitanje = novoPitanje;

        randomOdgovori = pitanje.dajRandomOdgovore();
        tekstPitanjaTV.setText(pitanje.getTekstPitanja());
        // dodat odgovre u listu
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof porukaOdPitanja)
            callback = (porukaOdPitanja) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }
}
