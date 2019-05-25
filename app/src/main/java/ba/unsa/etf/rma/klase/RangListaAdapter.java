package ba.unsa.etf.rma.klase;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.maltaisn.icondialog.IconHelper;

import org.w3c.dom.Text;

import java.util.ArrayList;

import ba.unsa.etf.rma.R;

public class RangListaAdapter extends ArrayAdapter<String[]> {

    public RangListaAdapter(Context context, ArrayList<String[]> strings) {
        super(context, R.layout.element_rang_liste, strings);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.element_liste, parent, false);

        TextView pozicijaTV = (TextView) view.findViewById(R.id.pozicijaTV);
        TextView imeIgracaTV = (TextView) view.findViewById(R.id.imeIgracaTV);
        TextView procenatTacnih = (TextView) view.findViewById(R.id.procenatTV);

        String[] strings = getItem(position);

        pozicijaTV.setText(strings[0]);
        imeIgracaTV.setText(strings[1]);
        procenatTacnih.setText(strings[2]);

        return view;
    }
}
