package ba.unsa.etf.rma.klase;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

public class GetRequestResultReceiver extends ResultReceiver {
    private Receiver receiver;

    public GetRequestResultReceiver(Handler handler) {
        super(handler);
    }

    public void setReceiver(Receiver r) {
        receiver = r;
    }


    public interface Receiver {
        public void onReceiveResult(int resultCode, Bundle resultData);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (receiver != null)
            receiver.onReceiveResult(resultCode, resultData);
    }
}
