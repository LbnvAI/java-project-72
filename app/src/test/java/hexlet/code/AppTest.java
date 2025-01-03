package hexlet.code;

import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.repository.UrlChecksRepository;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.NamedRoutes;
import java.util.List;
import io.javalin.Javalin;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.javalin.testtools.JavalinTest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AppTest {
    private static Javalin app;
    private static MockWebServer mockWebServer;

    public static final String TEST_HTML_PAGE = "testPage.html";

    private static Path getFixturePath(String fileName) {
        return Paths.get("src", "test", "resources", "fixtures", fileName)
                .toAbsolutePath().normalize();
    }

    private static String readFixture(String fileName) throws Exception {
        Path filePath = getFixturePath(fileName);
        return Files.readString(filePath).trim();
    }

    @BeforeAll
    static void generalSetUp() throws Exception {
        mockWebServer = new MockWebServer();
        MockResponse mockResponse = new MockResponse()
                .setBody(readFixture(TEST_HTML_PAGE))
                .setResponseCode(200);
        mockWebServer.enqueue(mockResponse);
        mockWebServer.start();
    }

    @BeforeEach
    public final void setUpForEachTest() throws IOException, SQLException {
        app = App.getApp();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
        app.stop();
    }

    @Test
    public void testMainPage() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.get("/");
            assertEquals(200, response.code());
            assertTrue(response.body().string().contains("Анализатор страниц"));
        });
    }

    @Test
    public void testUrlsPage() {
        JavalinTest.test(app, (server, client) -> {
            String urlName = "https://www.example.com";
            Url url = new Url(urlName);
            UrlRepository.save(url);

            var response = client.get("/urls");
            assertEquals(200, response.code());
            assertTrue(response.body().string().contains("https://www.example.com"));
        });
    }

    @Test
    public void testCreateUrl() {
        JavalinTest.test(app, (server, client) -> {
            String requestBody = "url=https://www.example1.com";
            var response = client.post("/urls", requestBody);
            assertEquals(200, response.code());

            Url url = UrlRepository.getEntities().getFirst();
            assertEquals("https://www.example1.com", url.getName());
        });
    }

    @Test
    public void testCreateInvalidUrl() {
        JavalinTest.test(app, (server, client) -> {
            var requestBody = "url=hhhh3333";
            var response = client.post("/urls", requestBody);
            assertEquals(200, response.code());
            assertTrue(!response.body().string().contains("hhhh3333"));
        });
    }

    @Test
    public void testUrlPage() throws SQLException {
        Url url = new Url("https://www.example2.com");
        UrlRepository.save(url);
        JavalinTest.test(app, (server, client) -> {
            var response = client.get("/urls/" + url.getId());
            assertEquals(200, response.code());
            assertTrue(response.body().string().contains("https://www.example2.com"));
        });
    }

    @Test
    public void testCheckUrl() {
        JavalinTest.test(app, (server, client) -> {
            String mockUrlName = mockWebServer.url("/").toString();
            Url mockUrl = new Url(mockUrlName);
            UrlRepository.save(mockUrl);

            var response = client.post(NamedRoutes.checkUrlPath(String.valueOf(mockUrl.getId())));
            assertEquals(200, response.code());

            List<UrlCheck> urlChecks = UrlChecksRepository.getAllChecksForUrl(mockUrl.getId());
            assertEquals(1, urlChecks.size());

            UrlCheck lastUrlCheck = UrlChecksRepository.getAllChecksForUrl(mockUrl.getId()).getFirst();
            assertEquals(1, lastUrlCheck.getUrlId());
            assertEquals(200, lastUrlCheck.getStatusCode());
            assertTrue(lastUrlCheck.getTitle().contains("HTML test page"));
            assertTrue(lastUrlCheck.getH1().contains("test page"));
            assertTrue(lastUrlCheck.getDescription().contains("Some content"));
        });
    }
}
