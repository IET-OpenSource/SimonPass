package it.iet.interfaces.facade;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public interface ServerService {
    Map<String,URI> getServerUris();
    void setServerUris(Map<String,URI> serversList);
    void addServer(String url, String key) throws URISyntaxException;
}
