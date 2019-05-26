package ba.unsa.etf.rma.fragmenti;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.lang.reflect.Array;
import java.util.ArrayList;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.klase.Kategorija;
import ba.unsa.etf.rma.klase.Kviz;

public class ListaFrag extends Fragment {


    public interface porukaOdListeFrag {
        void porukaOdListeFrag (String nazivKategorije);
    }

    private porukaOdListeFrag callback;

    private ArrayList<Kategorija> kategorije = new ArrayList<>();

    private ListView listaKategorija;
    private ArrayAdapter<Kategorija> kategorijaAdapter = null;

    private View view = null;


    public ListaFrag() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_lista, container, false);

        listaKategorija = (ListView) view.findViewById(R.id.listaKategorija);

        Bundle bundle = this.getArguments();
        kategorije = (ArrayList<Kategorija>) bundle.getSerializable("kategorije");

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        kategorijaAdapter = new ArrayAdapter<Kategorija>(view.getContext(), android.R.layout.simple_list_item_1, kategorije);
        listaKategorija.setAdapter(kategorijaAdapter);
        dodajListenerNaListu();
    }

    private void dodajListenerNaListu() {
        listaKategorija.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3) {
                Kategorija odabranaKategorija = (Kategorija) adapter.getItemAtPosition(position);
                callback.porukaOdListeFrag(odabranaKategorija.getNaziv());
            }
        });

    }

    public void azurirajKategorije(ArrayList<Kategorija> noveKategorije) {
        kategorije.clear();
        kategorije.addAll(noveKategorije);
        kategorijaAdapter.notifyDataSetChanged();
        callback.porukaOdListeFrag("Svi");
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof porukaOdListeFrag)
            callback = (porukaOdListeFrag) context;
    }
    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }
}
