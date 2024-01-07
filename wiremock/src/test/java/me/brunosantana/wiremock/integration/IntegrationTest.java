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
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.IOException;
import java.net.URISyntaxException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

//https://wiremock.org/docs/multi-domain-mocking/
//https://stackoverflow.com/questions/77774752/multi-domain-mocking-with-wiremock-is-not-working

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("profile1")
public class IntegrationTest {

    @LocalServerPort
    private int randomServerPort;

    @Resource
    MockMvc mockMvc;

    WireMockServer wireMockServer;

    @BeforeEach
    public void startServer() throws IOException, URISyntaxException {
        FileReaderUtil fileReaderUtil = new FileReaderUtil();
        String body1 = fileReaderUtil.read("jsonplaceholder-response.json");
        String body2 = fileReaderUtil.read("bible-response.json");

        wireMockServer = new WireMockServer(options()
                .dynamicPort()
                .enableBrowserProxying(true)
                .trustAllProxyTargets(true)
        );
        wireMockServer.start();

        JvmProxyConfigurer.configureFor(wireMockServer);

        wireMockServer.stubFor(get(urlPathMatching("/albums/[0-9]+/photos"))
                .withHost(WireMock.equalTo("jsonplaceholder.typicode.com"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withBody(body1)));

        wireMockServer.stubFor(get(urlPathMatching("/[a-z]+\\s[0-9]+:[0-9]+"))
                .withHost(WireMock.equalTo("my.random.site.com"))
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
            .get(String.format("http://localhost:%s/albums/1/bible/john/3/16", randomServerPort))
        .then()
            .log().all()
            .statusCode(200)
            .body("album.albumId", is(1))
            .body("album.photos[0].title", equalTo("accusamus beatae ad facilis cum similique qui sunt mock"))
            .body("bible.reference", equalTo("John 3:16 mock"));
    }

    @Test
    public void testAlbumBibleEndpointUsingMockMvc() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/albums/1/bible/john/3/16")
                        .header("Accept", "application/json")
                        .headers(new HttpHeaders()))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andExpect(MockMvcResultMatchers.jsonPath("$.album.photos[0].title").value("accusamus beatae ad facilis cum similique qui sunt mock"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.bible.reference").value("John 3:16 mock"))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }

}
