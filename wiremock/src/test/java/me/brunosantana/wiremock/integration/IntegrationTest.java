package me.brunosantana.wiremock.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.JvmProxyConfigurer;
import jakarta.annotation.Resource;
import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

//https://wiremock.org/docs/multi-domain-mocking/
//https://nikhils-devops.medium.com/keytool-generate-cacert-server-cert-from-url-and-port-ssl-from-aws-acm-fcf722fea8fe

//Links to solve the problem: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target
//https://medium.com/expedia-group-tech/how-to-import-public-certificates-into-javas-truststore-from-a-browser-a35e49a806dc
//https://blog.packagecloud.io/solve-unable-to-find-valid-certification-path-to-requested-target/
//https://www.baeldung.com/jvm-certificate-store-errors

/*
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
 */

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
//@SpringBootTest(classes = IntegrationTest.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class IntegrationTest {

    @Resource
    MockMvc mockMvc;

    private WireMockServer wireMockServer;

    @Before
    public void setup() {

        //System.setProperty("javax.net.ssl.trustStore", "/home/bruno/.jdks/corretto-17.0.5/lib/security/cacert");
        //System.setProperty("javax.net.ssl.trustStorePassword", "changeme");

        String body1 = "[\n" +
                "  {\n" +
                "    \"albumId\": 1,\n" +
                "    \"id\": 1,\n" +
                "    \"title\": \"accusamus beatae ad facilis cum similique qui suntt\",\n" +
                "    \"url\": \"https://via.placeholder.com/600/92c952\",\n" +
                "    \"thumbnailUrl\": \"https://via.placeholder.com/150/92c952\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"albumId\": 1,\n" +
                "    \"id\": 2,\n" +
                "    \"title\": \"reprehenderit est deserunt velit ipsam\",\n" +
                "    \"url\": \"https://via.placeholder.com/600/771796\",\n" +
                "    \"thumbnailUrl\": \"https://via.placeholder.com/150/771796\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"albumId\": 1,\n" +
                "    \"id\": 3,\n" +
                "    \"title\": \"officia porro iure quia iusto qui ipsa ut modi\",\n" +
                "    \"url\": \"https://via.placeholder.com/600/24f355\",\n" +
                "    \"thumbnailUrl\": \"https://via.placeholder.com/150/24f355\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"albumId\": 1,\n" +
                "    \"id\": 4,\n" +
                "    \"title\": \"culpa odio esse rerum omnis laboriosam voluptate repudiandae\",\n" +
                "    \"url\": \"https://via.placeholder.com/600/d32776\",\n" +
                "    \"thumbnailUrl\": \"https://via.placeholder.com/150/d32776\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"albumId\": 1,\n" +
                "    \"id\": 5,\n" +
                "    \"title\": \"natus nisi omnis corporis facere molestiae rerum in\",\n" +
                "    \"url\": \"https://via.placeholder.com/600/f66b97\",\n" +
                "    \"thumbnailUrl\": \"https://via.placeholder.com/150/f66b97\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"albumId\": 1,\n" +
                "    \"id\": 6,\n" +
                "    \"title\": \"accusamus ea aliquid et amet sequi nemo\",\n" +
                "    \"url\": \"https://via.placeholder.com/600/56a8c2\",\n" +
                "    \"thumbnailUrl\": \"https://via.placeholder.com/150/56a8c2\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"albumId\": 1,\n" +
                "    \"id\": 7,\n" +
                "    \"title\": \"officia delectus consequatur vero aut veniam explicabo molestias\",\n" +
                "    \"url\": \"https://via.placeholder.com/600/b0f7cc\",\n" +
                "    \"thumbnailUrl\": \"https://via.placeholder.com/150/b0f7cc\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"albumId\": 1,\n" +
                "    \"id\": 8,\n" +
                "    \"title\": \"aut porro officiis laborum odit ea laudantium corporis\",\n" +
                "    \"url\": \"https://via.placeholder.com/600/54176f\",\n" +
                "    \"thumbnailUrl\": \"https://via.placeholder.com/150/54176f\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"albumId\": 1,\n" +
                "    \"id\": 9,\n" +
                "    \"title\": \"qui eius qui autem sed\",\n" +
                "    \"url\": \"https://via.placeholder.com/600/51aa97\",\n" +
                "    \"thumbnailUrl\": \"https://via.placeholder.com/150/51aa97\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"albumId\": 1,\n" +
                "    \"id\": 10,\n" +
                "    \"title\": \"beatae et provident et ut vel\",\n" +
                "    \"url\": \"https://via.placeholder.com/600/810b14\",\n" +
                "    \"thumbnailUrl\": \"https://via.placeholder.com/150/810b14\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"albumId\": 1,\n" +
                "    \"id\": 11,\n" +
                "    \"title\": \"nihil at amet non hic quia qui\",\n" +
                "    \"url\": \"https://via.placeholder.com/600/1ee8a4\",\n" +
                "    \"thumbnailUrl\": \"https://via.placeholder.com/150/1ee8a4\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"albumId\": 1,\n" +
                "    \"id\": 12,\n" +
                "    \"title\": \"mollitia soluta ut rerum eos aliquam consequatur perspiciatis maiores\",\n" +
                "    \"url\": \"https://via.placeholder.com/600/66b7d2\",\n" +
                "    \"thumbnailUrl\": \"https://via.placeholder.com/150/66b7d2\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"albumId\": 1,\n" +
                "    \"id\": 13,\n" +
                "    \"title\": \"repudiandae iusto deleniti rerum\",\n" +
                "    \"url\": \"https://via.placeholder.com/600/197d29\",\n" +
                "    \"thumbnailUrl\": \"https://via.placeholder.com/150/197d29\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"albumId\": 1,\n" +
                "    \"id\": 14,\n" +
                "    \"title\": \"est necessitatibus architecto ut laborum\",\n" +
                "    \"url\": \"https://via.placeholder.com/600/61a65\",\n" +
                "    \"thumbnailUrl\": \"https://via.placeholder.com/150/61a65\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"albumId\": 1,\n" +
                "    \"id\": 15,\n" +
                "    \"title\": \"harum dicta similique quis dolore earum ex qui\",\n" +
                "    \"url\": \"https://via.placeholder.com/600/f9cee5\",\n" +
                "    \"thumbnailUrl\": \"https://via.placeholder.com/150/f9cee5\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"albumId\": 1,\n" +
                "    \"id\": 16,\n" +
                "    \"title\": \"iusto sunt nobis quasi veritatis quas expedita voluptatum deserunt\",\n" +
                "    \"url\": \"https://via.placeholder.com/600/fdf73e\",\n" +
                "    \"thumbnailUrl\": \"https://via.placeholder.com/150/fdf73e\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"albumId\": 1,\n" +
                "    \"id\": 17,\n" +
                "    \"title\": \"natus doloribus necessitatibus ipsa\",\n" +
                "    \"url\": \"https://via.placeholder.com/600/9c184f\",\n" +
                "    \"thumbnailUrl\": \"https://via.placeholder.com/150/9c184f\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"albumId\": 1,\n" +
                "    \"id\": 18,\n" +
                "    \"title\": \"laboriosam odit nam necessitatibus et illum dolores reiciendis\",\n" +
                "    \"url\": \"https://via.placeholder.com/600/1fe46f\",\n" +
                "    \"thumbnailUrl\": \"https://via.placeholder.com/150/1fe46f\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"albumId\": 1,\n" +
                "    \"id\": 19,\n" +
                "    \"title\": \"perferendis nesciunt eveniet et optio a\",\n" +
                "    \"url\": \"https://via.placeholder.com/600/56acb2\",\n" +
                "    \"thumbnailUrl\": \"https://via.placeholder.com/150/56acb2\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"albumId\": 1,\n" +
                "    \"id\": 20,\n" +
                "    \"title\": \"assumenda voluptatem laboriosam enim consequatur veniam placeat reiciendis error\",\n" +
                "    \"url\": \"https://via.placeholder.com/600/8985dc\",\n" +
                "    \"thumbnailUrl\": \"https://via.placeholder.com/150/8985dc\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"albumId\": 1,\n" +
                "    \"id\": 21,\n" +
                "    \"title\": \"ad et natus qui\",\n" +
                "    \"url\": \"https://via.placeholder.com/600/5e12c6\",\n" +
                "    \"thumbnailUrl\": \"https://via.placeholder.com/150/5e12c6\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"albumId\": 1,\n" +
                "    \"id\": 22,\n" +
                "    \"title\": \"et ea illo et sit voluptas animi blanditiis porro\",\n" +
                "    \"url\": \"https://via.placeholder.com/600/45601a\",\n" +
                "    \"thumbnailUrl\": \"https://via.placeholder.com/150/45601a\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"albumId\": 1,\n" +
                "    \"id\": 23,\n" +
                "    \"title\": \"harum velit vero totam\",\n" +
                "    \"url\": \"https://via.placeholder.com/600/e924e6\",\n" +
                "    \"thumbnailUrl\": \"https://via.placeholder.com/150/e924e6\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"albumId\": 1,\n" +
                "    \"id\": 24,\n" +
                "    \"title\": \"beatae officiis ut aut\",\n" +
                "    \"url\": \"https://via.placeholder.com/600/8f209a\",\n" +
                "    \"thumbnailUrl\": \"https://via.placeholder.com/150/8f209a\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"albumId\": 1,\n" +
                "    \"id\": 25,\n" +
                "    \"title\": \"facere non quis fuga fugit vitae\",\n" +
                "    \"url\": \"https://via.placeholder.com/600/5e3a73\",\n" +
                "    \"thumbnailUrl\": \"https://via.placeholder.com/150/5e3a73\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"albumId\": 1,\n" +
                "    \"id\": 26,\n" +
                "    \"title\": \"asperiores nobis voluptate qui\",\n" +
                "    \"url\": \"https://via.placeholder.com/600/474645\",\n" +
                "    \"thumbnailUrl\": \"https://via.placeholder.com/150/474645\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"albumId\": 1,\n" +
                "    \"id\": 27,\n" +
                "    \"title\": \"sit asperiores est quos quis nisi veniam error\",\n" +
                "    \"url\": \"https://via.placeholder.com/600/c984bf\",\n" +
                "    \"thumbnailUrl\": \"https://via.placeholder.com/150/c984bf\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"albumId\": 1,\n" +
                "    \"id\": 28,\n" +
                "    \"title\": \"non neque eligendi molestiae repudiandae illum voluptatem qui aut\",\n" +
                "    \"url\": \"https://via.placeholder.com/600/392537\",\n" +
                "    \"thumbnailUrl\": \"https://via.placeholder.com/150/392537\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"albumId\": 1,\n" +
                "    \"id\": 29,\n" +
                "    \"title\": \"aut ipsam quos ab placeat omnis\",\n" +
                "    \"url\": \"https://via.placeholder.com/600/602b9e\",\n" +
                "    \"thumbnailUrl\": \"https://via.placeholder.com/150/602b9e\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"albumId\": 1,\n" +
                "    \"id\": 30,\n" +
                "    \"title\": \"odio enim voluptatem quidem aut nihil illum\",\n" +
                "    \"url\": \"https://via.placeholder.com/600/372c93\",\n" +
                "    \"thumbnailUrl\": \"https://via.placeholder.com/150/372c93\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"albumId\": 1,\n" +
                "    \"id\": 31,\n" +
                "    \"title\": \"voluptate voluptates sequi\",\n" +
                "    \"url\": \"https://via.placeholder.com/600/a7c272\",\n" +
                "    \"thumbnailUrl\": \"https://via.placeholder.com/150/a7c272\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"albumId\": 1,\n" +
                "    \"id\": 32,\n" +
                "    \"title\": \"ad enim dignissimos voluptatem similique\",\n" +
                "    \"url\": \"https://via.placeholder.com/600/c70a4d\",\n" +
                "    \"thumbnailUrl\": \"https://via.placeholder.com/150/c70a4d\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"albumId\": 1,\n" +
                "    \"id\": 33,\n" +
                "    \"title\": \"culpa ipsam nobis qui fuga magni et mollitia\",\n" +
                "    \"url\": \"https://via.placeholder.com/600/501fe1\",\n" +
                "    \"thumbnailUrl\": \"https://via.placeholder.com/150/501fe1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"albumId\": 1,\n" +
                "    \"id\": 34,\n" +
                "    \"title\": \"vitae est facere quia itaque adipisci perferendis id maiores\",\n" +
                "    \"url\": \"https://via.placeholder.com/600/35185e\",\n" +
                "    \"thumbnailUrl\": \"https://via.placeholder.com/150/35185e\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"albumId\": 1,\n" +
                "    \"id\": 35,\n" +
                "    \"title\": \"tenetur minus voluptatum et\",\n" +
                "    \"url\": \"https://via.placeholder.com/600/c96cad\",\n" +
                "    \"thumbnailUrl\": \"https://via.placeholder.com/150/c96cad\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"albumId\": 1,\n" +
                "    \"id\": 36,\n" +
                "    \"title\": \"expedita rerum eaque\",\n" +
                "    \"url\": \"https://via.placeholder.com/600/4d564d\",\n" +
                "    \"thumbnailUrl\": \"https://via.placeholder.com/150/4d564d\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"albumId\": 1,\n" +
                "    \"id\": 37,\n" +
                "    \"title\": \"totam voluptas iusto deserunt dolores\",\n" +
                "    \"url\": \"https://via.placeholder.com/600/ea51da\",\n" +
                "    \"thumbnailUrl\": \"https://via.placeholder.com/150/ea51da\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"albumId\": 1,\n" +
                "    \"id\": 38,\n" +
                "    \"title\": \"natus magnam iure rerum pariatur molestias dolore nisi\",\n" +
                "    \"url\": \"https://via.placeholder.com/600/4f5b8d\",\n" +
                "    \"thumbnailUrl\": \"https://via.placeholder.com/150/4f5b8d\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"albumId\": 1,\n" +
                "    \"id\": 39,\n" +
                "    \"title\": \"molestiae nam ullam et rerum doloribus\",\n" +
                "    \"url\": \"https://via.placeholder.com/600/1e71a2\",\n" +
                "    \"thumbnailUrl\": \"https://via.placeholder.com/150/1e71a2\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"albumId\": 1,\n" +
                "    \"id\": 40,\n" +
                "    \"title\": \"est quas voluptates dignissimos sint praesentium nisi recusandae\",\n" +
                "    \"url\": \"https://via.placeholder.com/600/3a0b95\",\n" +
                "    \"thumbnailUrl\": \"https://via.placeholder.com/150/3a0b95\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"albumId\": 1,\n" +
                "    \"id\": 41,\n" +
                "    \"title\": \"in voluptatem doloremque cum atque architecto deleniti\",\n" +
                "    \"url\": \"https://via.placeholder.com/600/659403\",\n" +
                "    \"thumbnailUrl\": \"https://via.placeholder.com/150/659403\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"albumId\": 1,\n" +
                "    \"id\": 42,\n" +
                "    \"title\": \"voluptatibus a autem molestias voluptas architecto culpa\",\n" +
                "    \"url\": \"https://via.placeholder.com/600/ca50ac\",\n" +
                "    \"thumbnailUrl\": \"https://via.placeholder.com/150/ca50ac\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"albumId\": 1,\n" +
                "    \"id\": 43,\n" +
                "    \"title\": \"eius hic autem ad beatae voluptas\",\n" +
                "    \"url\": \"https://via.placeholder.com/600/6ad437\",\n" +
                "    \"thumbnailUrl\": \"https://via.placeholder.com/150/6ad437\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"albumId\": 1,\n" +
                "    \"id\": 44,\n" +
                "    \"title\": \"neque eum provident et inventore sed ipsam dignissimos quo\",\n" +
                "    \"url\": \"https://via.placeholder.com/600/29fe9f\",\n" +
                "    \"thumbnailUrl\": \"https://via.placeholder.com/150/29fe9f\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"albumId\": 1,\n" +
                "    \"id\": 45,\n" +
                "    \"title\": \"praesentium fugit quis aut voluptatum commodi dolore corrupti\",\n" +
                "    \"url\": \"https://via.placeholder.com/600/c4084a\",\n" +
                "    \"thumbnailUrl\": \"https://via.placeholder.com/150/c4084a\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"albumId\": 1,\n" +
                "    \"id\": 46,\n" +
                "    \"title\": \"quidem maiores in quia fugit dolore explicabo occaecati\",\n" +
                "    \"url\": \"https://via.placeholder.com/600/e9b68\",\n" +
                "    \"thumbnailUrl\": \"https://via.placeholder.com/150/e9b68\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"albumId\": 1,\n" +
                "    \"id\": 47,\n" +
                "    \"title\": \"et soluta est\",\n" +
                "    \"url\": \"https://via.placeholder.com/600/b4412f\",\n" +
                "    \"thumbnailUrl\": \"https://via.placeholder.com/150/b4412f\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"albumId\": 1,\n" +
                "    \"id\": 48,\n" +
                "    \"title\": \"ut esse id\",\n" +
                "    \"url\": \"https://via.placeholder.com/600/68e0a8\",\n" +
                "    \"thumbnailUrl\": \"https://via.placeholder.com/150/68e0a8\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"albumId\": 1,\n" +
                "    \"id\": 49,\n" +
                "    \"title\": \"quasi quae est modi quis quam in impedit\",\n" +
                "    \"url\": \"https://via.placeholder.com/600/2cd88b\",\n" +
                "    \"thumbnailUrl\": \"https://via.placeholder.com/150/2cd88b\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"albumId\": 1,\n" +
                "    \"id\": 50,\n" +
                "    \"title\": \"et inventore quae ut tempore eius voluptatum\",\n" +
                "    \"url\": \"https://via.placeholder.com/600/9e59da\",\n" +
                "    \"thumbnailUrl\": \"https://via.placeholder.com/150/9e59da\"\n" +
                "  }\n" +
                "]";

        String body2 = "{\"reference\":\"John 3:166\",\"verses\":[{\"book_id\":\"JHN\",\"book_name\":\"John\",\"chapter\":3,\"verse\":16,\"text\":\"\\nFor God so loved the world, that he gave his one and only Son, that whoever believes in him should not perish, but have eternal life.\\n\\n\"}],\"text\":\"\\nFor God so loved the world, that he gave his one and only Son, that whoever believes in him should not perish, but have eternal life.\\n\\n\",\"translation_id\":\"web\",\"translation_name\":\"World English Bible\",\"translation_note\":\"Public Domain\"}";

        wireMockServer = new WireMockServer(options()
                .enableBrowserProxying(true)
                .dynamicPort()
                .dynamicHttpsPort()
                .trustAllProxyTargets(true)
        );
        wireMockServer.start();

        JvmProxyConfigurer.configureFor(wireMockServer);

        wireMockServer.stubFor(WireMock.get("/albums/1/photos")
                .withHost(WireMock.equalTo("https://jsonplaceholder.typicode.com"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(body1)));

        wireMockServer.stubFor(WireMock.get(WireMock.urlPathMatching(".+\s.+"))
                .withHost(WireMock.equalTo("https://bible-api.com"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(body2)));
    }

    @After
    public void tearDown() {
        wireMockServer.stop();
        JvmProxyConfigurer.restorePrevious();
    }

    /*@Test
    public void testApiIntegrationWithExternal1() {
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
        mockMvc.perform(MockMvcRequestBuilders.get("http://localhost:8080/albums/1/bible/john/3/16")
                        .content("{}")
                        .headers(new HttpHeaders()))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }

}
