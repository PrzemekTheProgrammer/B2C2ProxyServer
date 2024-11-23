package pszerszenowicz.b2c2proxyserver.proxy;

import pszerszenowicz.b2c2proxyserver.client.TargetConnection;
import pszerszenowicz.b2c2proxyserver.server.Server;

public class Proxy {

    public static void main(String[] args) throws Exception {
        start();
    }

    private static void start() throws Exception {
        Server server = new Server();
        server.start();
    }
}
