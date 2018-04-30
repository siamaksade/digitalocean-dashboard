package com.redhat.devops.digitaloceandashboard;

import org.infinispan.Cache;
import org.infinispan.manager.EmbeddedCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
public class DropletsController {
    private static final String DIGITALOCEAN_API = "https://api.digitalocean.com/v2/droplets?page=1&per_page=200";
    private static final String KEY_DROPLETS = "DROPLETS";
    private static final String CACHE_DROPLETS = "DROPLETS";

    @Value("${digitalocean.api.token}")
    private String apiToken;

    @Autowired
    private EmbeddedCacheManager cacheManager;

    private JsonParser parser = JsonParserFactory.getJsonParser();

    @GetMapping("/droplets")
    public String droplets(Model model) {
        model.addAttribute("droplets", getDroplets());
        return "droplets";
    }

    private List<Droplet> getDroplets() {
        Cache<Object, Object> cache = cacheManager.getCache(CACHE_DROPLETS, true);
        List<Droplet> droplets = (List<Droplet>)cache.get(KEY_DROPLETS);

        if (droplets == null) {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.set("Authorization", "Bearer " + apiToken);
            HttpEntity<?> entity = new HttpEntity<>(headers);
            String json = restTemplate.exchange(DIGITALOCEAN_API, HttpMethod.GET, entity, String.class).getBody();

            List<Object> list = (List) parser.parseMap(json).get("droplets");

            droplets = list.stream().map(o -> {
                    Map<String, Object> dropletObj = (Map<String, Object>) o;
                    return new Droplet((Integer) dropletObj.get("id"), (String) dropletObj.get("name"), (String) dropletObj.get("status"));
                }).collect(Collectors.toList());
            cache.put(KEY_DROPLETS, droplets, 15, TimeUnit.SECONDS);
        }

        return droplets;
    }
}