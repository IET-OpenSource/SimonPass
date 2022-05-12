package it.iet.interfaces.facade.impl;

import it.iet.interfaces.facade.ServerService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Service
public class ServerServiceImpl implements ServerService {
    Map<String, URI> serverUris;

    public ServerServiceImpl() {
        this.serverUris = new HashMap<>();
    }

    @Override
    public void addServer(String url, String key) throws URISyntaxException {
        var uri = new URI(url);
        serverUris.put(key, uri);
    }
}
