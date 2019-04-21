package ba.unsa.etf.rma.fragmenti;


import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
    private ArrayAdapter<String> odgovoriAdapter = null;
    private ArrayList<String> odgovori = new ArrayList<>();
    private Pitanje pitanje = null;

    private boolean odabranOdgovor = false;
    private String odabraniOdgovor = "";


    public PitanjeFrag() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_pitanje, container, false);

        tekstPitanjaTV = (TextView) v.findViewById(R.id.tekstPitanja);
        listaOdgovora = (ListView) v.findViewById(R.id.odgovoriPitanja);

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        odgovoriAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, odgovori) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View row = super.getView(position, null, parent);

                if (odabranOdgovor && getItem(position).equals(pitanje.getTacan()))
                    row.setBackgroundColor(getResources().getColor(R.color.zelena));
                else if (odabranOdgovor && getItem(position).equals(odabraniOdgovor) && !odabraniOdgovor.equals(pitanje.getTacan()))
                    row.setBackgroundColor(getResources().getColor(R.color.crvena));
                else
                    row.setBackgroundColor(0);

                return row;
            }

            @Override
            public boolean isEnabled(int position) {
                return !odabranOdgovor;
            }
        };

        listaOdgovora.setAdapter(odgovoriAdapter);
        dodajListenerNaListu();
    }

    private void dodajListenerNaListu() {
        listaOdgovora.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(final AdapterView<?> adapter, View v, int position, long arg3) {
                final String odabrani = (String)adapter.getItemAtPosition(position);

                odabraniOdgovor = odabrani;
                odabranOdgovor = true;

                odgovoriAdapter.notifyDataSetChanged();



                // sačekaj 2s
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (pitanje != null)
                            callback.porukaOdPitanja(odabraniOdgovor.equals(pitanje.getTacan()));
                    }
                }, 2000);
            }
        });
    }


    public void primiPorukuOdInformacijaFragment(Pitanje novoPitanje) {

        if (tekstPitanjaTV == null) return;

        pitanje = novoPitanje;

        odgovori.clear();
        odgovori.addAll(pitanje.dajRandomOdgovore());
        tekstPitanjaTV.setText(pitanje.getTekstPitanja());

        odabranOdgovor = false;
        odabraniOdgovor = "";

        odgovoriAdapter.notifyDataSetChanged();
    }


    public void primiPorukuOZadnjemPitanju() {
        odgovori.clear();
        odgovoriAdapter.notifyDataSetChanged();
        tekstPitanjaTV.setText("Kviz je završen!");
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
