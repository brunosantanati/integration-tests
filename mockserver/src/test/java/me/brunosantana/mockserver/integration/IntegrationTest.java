package me.brunosantana.mockserver.integration;

import jakarta.annotation.Resource;
import me.brunosantana.mockserver.util.FileReaderUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;
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

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

//https://www.baeldung.com/mockserver
//https://github.com/eugenp/tutorials/blob/master/testing-modules/mockserver/src/test/java/com/baeldung/mock/server/MockServerLiveTest.java

//Look at this later: @SpringBootTest(properties =...
//https://github.com/hmcts/camunda-bpm/blob/400ffe303ddf005985d98900e36a05cddf7ff3a3/src/testUtils/java/uk/gov/hmcts/reform/camunda/bpm/SpringBootIntegrationBaseTest.java#L18

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("profile1")
public class IntegrationTest {

    @LocalServerPort
    private int randomServerPort;
    @Resource
    MockMvc mockMvc;

    private ClientAndServer mockServer;
    private ClientAndServer mockServer2;

    @BeforeEach
    public void startServer() throws IOException, URISyntaxException {
        mockServer = startClientAndServer(1080);
        mockServer2 = startClientAndServer(1081);

        FileReaderUtil fileReaderUtil = new FileReaderUtil();
        String body = fileReaderUtil.read("jsonplaceholder-response.json");
        String body2 = fileReaderUtil.read("bible-response.json");

        new MockServerClient("127.0.0.1", 1080)
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/albums/[0-9]+/photos"),
                        exactly(1)
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeaders(
                                        new Header("Content-Type", "application/json; charset=utf-8"),
                                        new Header("Cache-Control", "public, max-age=86400")
                                )
                                .withBody(body)
                );

        new MockServerClient("127.0.0.1", 1081)
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/[a-z]+\\s[0-9]+:[0-9]+"),
                        exactly(1)
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeaders(
                                        new Header("Content-Type", "application/json; charset=utf-8"),
                                        new Header("Cache-Control", "public, max-age=86400")
                                )
                                .withBody(body2)
                );
    }

    @AfterEach
    public void stopServer() {
        mockServer.stop();
        mockServer2.stop();
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
                        .headers(new HttpHeaders()))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andExpect(MockMvcResultMatchers.jsonPath("$.album.photos[0].title").value("accusamus beatae ad facilis cum similique qui sunt mock"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.bible.reference").value("John 3:16 mock"))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }

}
