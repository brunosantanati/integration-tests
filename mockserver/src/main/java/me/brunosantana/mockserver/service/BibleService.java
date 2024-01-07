package me.brunosantana.mockserver.service;

import me.brunosantana.mockserver.model.BibleResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;

@Service
public
class BibleService {
  private final String baseUrl;
  private final String port;
  private final RestTemplate restTemplate;

  BibleService(@Value("${bible.api.base-url}") String baseUrl, @Value("${bible.api.port}") String port, RestTemplateBuilder builder) {
    this.baseUrl = baseUrl;
    this.port = port;
    this.restTemplate = builder.build();
  }

  public BibleResponse getVerse(String book, String chapter, String verse) throws UnsupportedEncodingException {
    String url = String.format("%s:%s/%s %s:%s", baseUrl, port, book, chapter, verse);
    ResponseEntity<BibleResponse> response =
        restTemplate.exchange(
            url, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
    return response.getBody();
  }
}
