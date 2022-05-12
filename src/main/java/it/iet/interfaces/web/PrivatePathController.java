package it.iet.interfaces.web;

import it.iet.interfaces.facade.ServerService;
import it.iet.util.Utils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.mvc.ProxyExchange;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

@Slf4j
@RestController
public class PrivatePathController {

    @Autowired
    ServerService serverService;

    String gateway = "/gateway";

    @GetMapping("/gateway/**")
    public ResponseEntity<?> proxyGet(ProxyExchange<byte[]> proxy) {
        Map<String, URI> uris = serverService.getServerUris();
        String path = proxy.path(gateway);
        String prefix = Utils.getServerName(path);
        var baseUrl = uris.get(prefix).toString();
        return proxy.uri(baseUrl + path).header("Connection", "keep-alive").header("Transport-Encoding", "chunked").get();
    }

    @PostMapping(value = "/gateway/**", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> proxyPost(ProxyExchange<byte[]> proxy, HttpServletRequest request) {
        Map<String, URI> uris = serverService.getServerUris();
        String path = proxy.path(gateway);
        String prefix = Utils.getServerName(path);
        var baseUrl = uris.get(prefix).toString();
        return proxy.uri(baseUrl + path).post();
    }

    @SneakyThrows
    @PostMapping(value = "/gateway/**", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> proxyPost2(ProxyExchange<byte[]> proxy, StandardMultipartHttpServletRequest request, HttpServletResponse resp) {
        var restTemplate = new RestTemplate();
        Map<String, URI> uris = serverService.getServerUris();
        String path = proxy.path(gateway);
        String prefix = Utils.getServerName(path);
        var baseUrl = uris.get(prefix).toString();

        var httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        httpHeaders.setConnection("keep-alive");
        httpHeaders.set("Transport-Encoding", "chunked");

        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        request.getParameterMap().forEach((key, value) -> {
            if (value.length > 0)
                form.add(key, value[0]);
        });
        request.getMultiFileMap().forEach((key, value) -> {
            try {
                form.add(key, value.get(0).getBytes());
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        });
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(form, httpHeaders);
        return restTemplate.postForEntity(baseUrl + path, requestEntity, String.class);
    }

    @SneakyThrows
    @PutMapping(value = "/gateway/**", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public void proxyPut2(ProxyExchange<byte[]> proxy, StandardMultipartHttpServletRequest request, HttpServletResponse resp) {
        var restTemplate = new RestTemplate();
        Map<String, URI> uris = serverService.getServerUris();
        String path = proxy.path(gateway);
        String prefix = Utils.getServerName(path);
        var baseUrl = uris.get(prefix).toString();

        var httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        request.getParameterMap().forEach((key, value) -> form.add(key, value[0]));
        request.getMultiFileMap().forEach((key, value) -> {
            try {
                form.add(key, value.get(0).getBytes());
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        });
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(form, httpHeaders);
        restTemplate.put(baseUrl + path, requestEntity);
    }

    @PutMapping("/gateway/**")
    public ResponseEntity<?> proxyPut(ProxyExchange<byte[]> proxy) {
        Map<String, URI> uris = serverService.getServerUris();
        String path = proxy.path(gateway);
        String prefix = Utils.getServerName(path);
        var baseUrl = uris.get(prefix).toString();
        return proxy.uri(baseUrl + path).put();
    }

}
