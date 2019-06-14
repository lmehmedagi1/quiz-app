package ba.unsa.etf.rma.klase;

import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;

public class ConnectionStateMonitor extends ConnectivityManager.NetworkCallback {

    public interface Network {
        void onNetworkLost();
        void onNetworkAvailable();
    }

    private final Network network;
    private final NetworkRequest networkRequest;
    private ConnectivityManager connectivityManager;

    public ConnectionStateMonitor(Network network, ConnectivityManager connectivityManager) {
        networkRequest = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build();

        this.network = network;
        this.connectivityManager = connectivityManager;
    }

    public void registerNetworkCallback() {
        connectivityManager.registerNetworkCallback(networkRequest, this);
    }

    public void unregisterNetworkCallback() {
        connectivityManager.unregisterNetworkCallback(this);
    }

    @Override
    public void onAvailable(android.net.Network network) {
        this.network.onNetworkAvailable();
    }

    @Override
    public void onLost(android.net.Network network) {
        this.network.onNetworkLost();
    }

}