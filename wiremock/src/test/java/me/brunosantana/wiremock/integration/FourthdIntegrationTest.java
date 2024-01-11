package me.brunosantana.wiremock.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
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

import javax.annotation.Resource;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("profile3")
public class FourthdIntegrationTest {

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

        wireMockServer = new WireMockServer(1080);
        wireMockServer.start();

        WireMock.configureFor("127.0.0.1", 1080);

        // If the URL paths don't overlap, I am able to use only one wireMockServer
        wireMockServer.stubFor(get(urlPathMatching("/albums/1/photos?test=1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withBody(body1)));

        wireMockServer.stubFor(get(urlPathMatching("/albums/1/photos?test=2"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withBody(body1)));

        wireMockServer.stubFor(get(urlPathMatching("/[a-z]+.+:.+"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withBody(body2)));
    }

    @AfterEach
    public void stopServer() {
        wireMockServer.stop();
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

        Class<?> classOfMap = System.getenv().getClass();
        Field field = classOfMap.getDeclaredField("m");
        field.setAccessible(true);
        Map<String, String> writeableEnvironmentVariables = (Map<String, String>)field.get(System.getenv());
        writeableEnvironmentVariables.put("PHOTOS_API", "http://127.0.0.1:1080/albums/{albumId}/photos?test=1");

        mockMvc.perform(MockMvcRequestBuilders.get("/albums/1/bible/john/3/16")
                        //.content("{}")
                        .header("Accept", "application/json")
                        .headers(new HttpHeaders()))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andExpect(MockMvcResultMatchers.jsonPath("$.album.photos[0].title").value("accusamus beatae ad facilis cum similique qui sunt mock"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.bible.reference").value("John 3:16 mock"))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }

}
