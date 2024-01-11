package me.brunosantana.wiremock.service;

import me.brunosantana.wiremock.model.Photo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public
class PhotoService {
  private final String baseUrl;
  private final RestTemplate restTemplate;

  PhotoService(@Value("${photos.api.base-url}") String baseUrl, RestTemplateBuilder builder) {
    this.baseUrl = baseUrl;
    this.restTemplate = builder.build();
  }

  public List<Photo> getPhotos(Long albumId) {
    ResponseEntity<List<Photo>> response =
        restTemplate.exchange(
            baseUrl, HttpMethod.GET, null, new ParameterizedTypeReference<>() {}, albumId);
    return response.getBody();
  }
}
