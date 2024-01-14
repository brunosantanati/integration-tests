package me.brunosantana.wiremock.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("profile3")
public class FifthIntegrationTest {

    @LocalServerPort
    private int randomServerPort;

    @Resource
    MockMvc mockMvc;

    WireMockServer wireMockServer;

    @BeforeEach
    public void startServer() throws IOException, URISyntaxException {
        FileReaderUtil fileReaderUtil = new FileReaderUtil();

        wireMockServer = new WireMockServer(1080);
        wireMockServer.start();

        WireMock.configureFor("127.0.0.1", 1080);

        // If the URL paths don't overlap, I am able to use only one wireMockServer
        wireMockServer.stubFor(get(urlPathMatching("/albums/[0-9]+/photos"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withBody(fileReaderUtil.read("jsonplaceholder-response.json"))));

        wireMockServer.stubFor(get(urlPathMatching("/[a-z]+.+:.+"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withBody(fileReaderUtil.read("bible-response.json"))));
    }

    @AfterEach
    public void stopServer() {
        wireMockServer.stop();
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

    @Test
    public void testStub2() throws Exception {
        //Override WireMock config for this stub
        wireMockServer.stubFor(get(urlPathMatching("/albums/[0-9]+/photos"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withBody(new FileReaderUtil().read("jsonplaceholder-response-2.json"))));

        mockMvc.perform(MockMvcRequestBuilders.get("/albums/1/bible/john/3/16")
                        .header("Accept", "application/json")
                        .headers(new HttpHeaders()))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andExpect(MockMvcResultMatchers.jsonPath("$.album.photos[0].title").value("non sunt voluptatem placeat consequuntur rem incidunt mock 2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.bible.reference").value("John 3:16 mock"))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }

}
