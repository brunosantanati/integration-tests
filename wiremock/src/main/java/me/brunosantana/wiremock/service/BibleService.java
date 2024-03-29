package me.brunosantana.wiremock.service;

import me.brunosantana.wiremock.interceptor.LoggingInterceptor;
import me.brunosantana.wiremock.model.BibleResponse;
import me.brunosantana.wiremock.model.Photo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public
class BibleService {
  private final String baseUrl;
  private final RestTemplate restTemplate;

  BibleService(@Value("${bible.api.base-url}") String baseUrl, RestTemplateBuilder builder) {
    this.baseUrl = baseUrl;
    this.restTemplate = builder.build();
  }

  public BibleResponse getVerse(String book, String chapter, String verse) throws UnsupportedEncodingException {
    String url = String.format("%s/%s %s:%s", baseUrl, book, chapter, verse);
    //restTemplate.getInterceptors().add(new LoggingInterceptor());
    ResponseEntity<BibleResponse> response =
        restTemplate.exchange(
            url, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
    return response.getBody();
  }
}
