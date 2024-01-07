package me.brunosantana.wiremock.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.JvmProxyConfigurer;
import jakarta.annotation.Resource;
import me.brunosantana.wiremock.util.FileReaderUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.net.URISyntaxException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("profile2")
public class AnotherIntegrationTest {

    @Resource
    MockMvc mockMvc;

    WireMockServer wireMockServer;
    WireMockServer wireMockServer2;

    @BeforeEach
    public void startServer() throws IOException, URISyntaxException {
        FileReaderUtil fileReaderUtil = new FileReaderUtil();
        String body1 = fileReaderUtil.read("jsonplaceholder-response.json");
        String body2 = fileReaderUtil.read("bible-response.json");

        wireMockServer = new WireMockServer(1080);
        wireMockServer.start();

        wireMockServer2 = new WireMockServer(1081);
        wireMockServer2.start();

        WireMock.configureFor("127.0.0.1", 1080);
        WireMock.configureFor("127.0.0.1", 1081);

        wireMockServer.stubFor(get(urlPathMatching("/albums/[0-9]+/photos"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withBody(body1)));

        wireMockServer2.stubFor(get(urlPathMatching("/[a-z]+\\s[0-9]+:[0-9]+"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withBody(body2)));
    }

    @AfterEach
    public void stopServer() {
        wireMockServer.stop();
        JvmProxyConfigurer.restorePrevious();
    }

    @Test
    public void testAlbumBibleEndpointUsingRestAssured() {
        given()
        .when()
            .get("http://localhost:8080/albums/1/bible/john/3/16")
        .then()
            .log().all()
            .statusCode(200)
            .body("album.albumId", is(1))
            .body("album.photos[0].title", equalTo("accusamus beatae ad facilis cum similique qui sunt"))
            .body("bible.reference", equalTo("John 3:16"));
    }

    /*@Test
    public void testAlbumBibleEndpointUsingMockMvc() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("http://localhost:8080/albums/1/bible/john/3/16")
                        //.content("{}")
                        .header("Accept", "application/json")
                        .headers(new HttpHeaders()))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }*/

}