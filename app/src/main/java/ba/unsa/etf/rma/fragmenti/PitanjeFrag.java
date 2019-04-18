package ba.unsa.etf.rma.fragmenti;


import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
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
    private boolean odabranTacanOdgovor = false;
    private String odabraniOdgovor = "";


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

        odgovoriAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, odgovori);
        odgovoriAdapter = (new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, odgovori) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View row = super.getView(position, convertView, parent);

                if (odabranOdgovor && pitanje != null && getItem(position).equals(odabraniOdgovor)) {
                    if (odabranTacanOdgovor)
                        row.setBackgroundColor(getResources().getColor(R.color.crvena));
                    else
                        row.setBackgroundColor(getResources().getColor(R.color.zelena));
                }
                if (odabranOdgovor && pitanje != null && getItem(position).equals(pitanje.getTacan()))
                    row.setBackgroundColor(getResources().getColor(R.color.zelena));
                else
                    row.setBackgroundColor(0);

                return row;
            }
        });

        listaOdgovora.setAdapter(odgovoriAdapter);

        dodajListenerNaListu();
        return v;
    }

    private void dodajListenerNaListu() {
        listaOdgovora.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3) {
                String odabraniOdgovor = (String)adapter.getItemAtPosition(position);
                for (int i=0; i<odgovori.size(); i++) {
                    if (odgovori.get(i).equals(odabraniOdgovor)) {

                        if (pitanje != null && odabraniOdgovor.equals(pitanje.getTacan())) {
                            izabranTacanOdgovor(true, odabraniOdgovor);
                        }
                        else if (pitanje != null)
                            izabranTacanOdgovor(false, odabraniOdgovor);
                    }
                }
            }
        });
    }

    private void izabranTacanOdgovor(boolean tacan, String odabrani) {
        odabranTacanOdgovor = tacan;
        odabranOdgovor = true;
        odabraniOdgovor = odabrani;

        odgovoriAdapter.notifyDataSetChanged();

        // sačekaj 2s
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        callback.porukaOdPitanja(tacan);
    }

    public void primiPorukuOdInformacijaFragment(Pitanje novoPitanje) {

        odabranTacanOdgovor = false;
        odabranOdgovor = false;
        odabraniOdgovor = "";

        if (tekstPitanjaTV == null) return;

        pitanje = novoPitanje;

        odgovori.clear();
        odgovori.addAll(pitanje.dajRandomOdgovore());
        tekstPitanjaTV.setText(pitanje.getTekstPitanja());

        odgovoriAdapter.notifyDataSetChanged();
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
