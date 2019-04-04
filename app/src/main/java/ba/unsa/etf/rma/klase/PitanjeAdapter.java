package ba.unsa.etf.rma.klase;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ba.unsa.etf.rma.R;

public class PitanjeAdapter extends ArrayAdapter<Pitanje> {

    public PitanjeAdapter(Context context, ArrayList<Pitanje> pitanja) {
        super(context, R.layout.element_liste, pitanja);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View customView = inflater.inflate(R.layout.element_liste, parent, false);

        Pitanje pitanje = getItem(position);
        TextView tekst = (TextView) customView.findViewById(R.id.tekstPitanja);

        tekst.setText(pitanje.getNaziv());

        return customView;
    }

    public View dajElementZaDodavanje(ViewGroup parent) {
        View element = LayoutInflater.from(getContext()).inflate(R.layout.element_liste, parent, false);
        ((TextView) element.findViewById(R.id.tekstPitanja)).setText("Dodaj pitanje");
        return element;
    }
}
