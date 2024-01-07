package me.brunosantana.wiremock.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.JvmProxyConfigurer;
import jakarta.annotation.Resource;
import me.brunosantana.wiremock.util.FileReaderUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
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

//StackOverflow
//https://stackoverflow.com/questions/77762851/getting-unable-to-find-valid-certification-path-to-requested-target-in-the-int
//https://stackoverflow.com/questions/6908948/java-sun-security-provider-certpath-suncertpathbuilderexception-unable-to-find
//https://stackoverflow.com/questions/36965751/make-wiremock-accept-any-certificate
//https://stackoverflow.com/questions/37708040/sslhandshakeexception-no-cipher-suites-in-common-using-asynchttpclient-and-wire

//https://wiremock.org/docs/multi-domain-mocking/
//https://nikhils-devops.medium.com/keytool-generate-cacert-server-cert-from-url-and-port-ssl-from-aws-acm-fcf722fea8fe

//Links to solve the problem: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target
//https://medium.com/expedia-group-tech/how-to-import-public-certificates-into-javas-truststore-from-a-browser-a35e49a806dc
//https://blog.packagecloud.io/solve-unable-to-find-valid-certification-path-to-requested-target/
//https://www.baeldung.com/jvm-certificate-store-errors

/*
VM options to debug:
-Djavax.net.debug=SSL -Djavax.net.ssl.trustStore=/home/bruno/.jdks/corretto-17.0.5/lib/security/cacerts -Djavax.net.ssl.trustStorePassword=changeit -Djavax.net.debug=ssl:handshake:verbose

openssl s_client -showcerts -connect host.name.com:443 -servername host.name.com  </dev/null | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > host.name.com.pem
openssl s_client -showcerts -connect bible-api.com:443 -servername bible-api.com  </dev/null | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > bible-api.com.pem

openssl x509 -inform PEM -in host.name.com.pem -outform DER -out host.name.com.cer
openssl x509 -inform PEM -in bible-api.com.pem -outform DER -out bible-api.com.cer

cd /home/bruno/.jdks/corretto-17.0.5/
keytool -importcert -alias ${cert.alias} -keystore ${keystore.file} -file ${cer.file}
sudo keytool -import -alias testCert -keystore $JAVA_HOME/jre/lib/security/cacerts -file example.cer
keytool -import -alias bible -keystore cacerts -file ~/IdeaProjects/integration-tests/wiremock/certificates/bible-api.com.cer
alias - alias for the certificate so have a meaningful name
file - exported .cer certificate from the browser
The default password for the truststore: changeit

keytool -import -alias bible1 -keystore /home/bruno/.jdks/corretto-17.0.5/lib/security/cacerts -file ~/IdeaProjects/integration-tests/wiremock/certificates/bible-api.com.pem
keytool -import -alias bible2 -keystore /home/bruno/.jdks/corretto-17.0.5/lib/security/cacerts -file ~/IdeaProjects/integration-tests/wiremock/certificates/bible-api-2.pem
keytool -import -alias bible3 -keystore /home/bruno/.jdks/corretto-17.0.5/lib/security/cacerts -file ~/IdeaProjects/integration-tests/wiremock/certificates/bible-api-3.pem

corretto-17.0.5/lib/security$ keytool -list -keystore cacerts
keytool -keystore /home/bruno/.jdks/corretto-17.0.5/lib/security/cacerts -list -v

keytool -delete -alias ${cert.alias} -keystore ${keystore.file}
keytool -delete -alias bible-api -keystore /home/bruno/.jdks/corretto-17.0.5/lib/security/cacerts

https://superuser.com/questions/97201/how-to-save-a-remote-server-ssl-certificate-locally-as-a-file
 */

/*
Examples GitHub:
https://github.com/carl-don-it/document/blob/2be445db950cb8f829646c4cca606013b3c751e9/3.%20%E4%B8%BB%E6%B5%81%E6%A1%86%E6%9E%B6/Junit/%E5%8D%95%E5%85%83%E6%B5%8B%E8%AF%95.md?plain=1#L95
https://github.com/uqbar-project/eg-peliculas-microservicios/blob/2c21a0b82709d8ad5dae89bff6627dba77966476/peliculas-microservice-ranking/README.md?plain=1#L142
https://github.com/mia-platform/documentation/blob/d396b5dc3928befe8d3c49dc19aac3fbbdae3cd5/versioned_docs/version-11.x.x/getting-started/tutorials/create-a-custom-microservice.mdx#L488
https://github.com/Xray-App/xray-maven-plugin/blob/5e55540351a70f9f4fddd0e6148a9765603f7ca5/src/test/java/app/getxray/xray/it/import_results/XrayCloudIT.java#L56
https://github.com/i-novus-llc/n2o-framework/blob/70a74bf2bb325a13ee7258bab74918cbcff3e0ca/backend/n2o/n2o-sandbox/src/test/java/net/n2oapp/framework/sandbox/service/SandboxExportTest.java#L93
https://github.com/cryptr-auth/cryptr-kotlin/blob/fa709f28f1ddb6653dd903b420515f25a29ecd94/src/test/kotlin/cryptr/kotlin/CryptrHeadlessTest.kt#L42
https://github.com/zregvart/camel-netty-proxy/blob/d6e2873017e66334e2be209dace6d306c5d2bc4e/src/test/java/com/github/zregvart/cnp/ProxyAppIntegrationTest.java#L63
https://github.com/michaelcowan/gregbot/blob/a180f1c6b11fbd59b6177e71657ebc58dc765617/src/test/java/io/blt/gregbot/plugin/secrets/vault/VaultOidcTest.java#L176
https://github.com/europeana/metis-framework/blob/97cf8b4eeb81e66f0ff8add66bd4d85113ef6eac/metis-enrichment/metis-enrichment-client/src/test/java/eu/europeana/enrichment/rest/client/dereference/DereferencerImplTest.java#L177
https://github.com/TravelerWays/TravelWaysApi/blob/21fdba091f7360ec1f774686d3987ec40addc382/src/test/java/travel/ways/travelwaysapi/map/controller/MapControllerTest.java#L58
 */

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
//@SpringBootTest(classes = IntegrationTest.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class IntegrationTest {

    @Resource
    MockMvc mockMvc;

    /*@Test
    public void testApiIntegrationTest() {
        given()
                .when()
                .get("http://localhost:8080/albums/1/bible/john/3/16")
                .then()
                .log().all()
                .statusCode(200)
                .body("album.albumId", is(1))
                .body("album.photos[0].title", equalTo("accusamus beatae ad facilis cum similique qui sunt"))
                .body("bible.reference", equalTo("John 3:16"));
    }*/

    @Test
    public void testApi() throws Exception {
        //SETUP

        FileReaderUtil fileReaderUtil = new FileReaderUtil();
        String body1 = fileReaderUtil.read("jsonplaceholder-response.json");
        String body2 = fileReaderUtil.read("bible-response.json");

        WireMockServer wireMockServer = new WireMockServer(options()
                .dynamicPort()
                .enableBrowserProxying(true)
                //.dynamicHttpsPort()
                //.trustStorePath("/home/bruno/.jdks/corretto-17.0.5/lib/security/cacerts")
                //.trustStorePassword("changeit")
                //.keystorePath("/home/bruno/.jdks/corretto-17.0.5/lib/security/cacerts")
                //.keystorePassword("changeit")
                //.keyManagerPassword("changeit")
                //.httpsPort(443)
                .trustAllProxyTargets(true)
        );
        wireMockServer.start();

        JvmProxyConfigurer.configureFor(wireMockServer);

        wireMockServer.stubFor(get(urlPathMatching("/albums/[0-9]+/photos"))
                //.withScheme("https")
                .withHost(WireMock.equalTo("jsonplaceholder.typicode.com"))
                //.withPort(443)
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withBody(body1)));

        wireMockServer.stubFor(get(urlPathMatching("/[a-z]+\\s[0-9]+:[0-9]+"))
        //wireMockServer.stubFor(get("/john 3:16")
                //.withScheme("https")
                .withHost(WireMock.equalTo("bible.api.com"))
                //.withPort(443)
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withBody(body2)));

        //TEST
        mockMvc.perform(MockMvcRequestBuilders.get("http://localhost:8080/albums/1/bible/john/3/16")
                        //.content("{}")
                        .header("Accept", "application/json")
                        .headers(new HttpHeaders()))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        //TEAR DOWN
        wireMockServer.stop();
        JvmProxyConfigurer.restorePrevious();
    }

}
