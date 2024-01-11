package me.brunosantana.wiremock.controller;

import me.brunosantana.wiremock.model.Album;
import me.brunosantana.wiremock.model.BibleResponse;
import me.brunosantana.wiremock.model.Photo;
import me.brunosantana.wiremock.model.Response;
import me.brunosantana.wiremock.service.BibleService;
import me.brunosantana.wiremock.service.PhotoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientResponseException;

import java.io.UnsupportedEncodingException;
import java.util.List;

@RestController
class MyController {

  private static final Logger logger = LoggerFactory.getLogger(MyController.class);

  private final PhotoService photoService;
  private final BibleService bibleService;

  MyController(PhotoService photoService, BibleService bibleService) {
    this.photoService = photoService;
    this.bibleService = bibleService;
  }

  @GetMapping("/albums/{albumId}/bible/{book}/{chapter}/{verse}")
  public ResponseEntity<Response> getAlbumById(@PathVariable Long albumId,
                                            @PathVariable String book,
                                            @PathVariable String chapter,
                                            @PathVariable String verse) throws UnsupportedEncodingException {
    try {
      List<Photo> photos = photoService.getPhotos(albumId);
      BibleResponse bible = bibleService.getVerse(book, chapter, verse);
      return ResponseEntity.ok(new Response(new Album(albumId, photos), bible));
    } catch (RestClientResponseException e) {
      logger.error("Failed to get info", e);
      return ResponseEntity.internalServerError().body(new Response());
    }
  }
}
