package me.brunosantana.mockserver.service;

import me.brunosantana.mockserver.model.Photo;
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

  private final String port;
  private final RestTemplate restTemplate;

  PhotoService(@Value("${photos.api.base-url}") String baseUrl, @Value("${photos.api.port}") String port, RestTemplateBuilder builder) {
    this.baseUrl = baseUrl;
    this.port = port;
    this.restTemplate = builder.build();
  }

  public List<Photo> getPhotos(Long albumId) {
    String url = baseUrl + ":" + port + "/albums/{albumId}/photos";
    ResponseEntity<List<Photo>> response =
        restTemplate.exchange(
            url, HttpMethod.GET, null, new ParameterizedTypeReference<>() {}, albumId);
    return response.getBody();
  }
}
