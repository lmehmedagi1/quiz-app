package ba.unsa.etf.rma.fragmenti;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ba.unsa.etf.rma.R;

public class IstekloVrijemeFrag extends Fragment {

    private TextView tekst;

    public IstekloVrijemeFrag() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_isteklo_vrijeme, container, false);

        tekst = (TextView) v.findViewById(R.id.tekst);

        Bundle bundle = this.getArguments();
        String poruka = bundle.getString("poruka");

        tekst.setText(poruka);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}
