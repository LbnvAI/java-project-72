package hexlet.code.controller;

import hexlet.code.dto.UrlPage;
import hexlet.code.dto.UrlsPage;
import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.repository.UrlChecksRepository;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.Utils;

import hexlet.code.util.NamedRoutes;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import kong.unirest.core.HttpResponse;
import kong.unirest.core.Unirest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import static io.javalin.rendering.template.TemplateUtil.model;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class UrlsController {
    public static void create(Context ctx) throws SQLException {
        String urlString = ctx.formParam("url").toLowerCase().trim();

        String normalizedUrlString;
        try {
            normalizedUrlString = Utils.normalizeUrlString(urlString);
        } catch (MalformedURLException | IllegalArgumentException | URISyntaxException e) {
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("alertType", "danger");
            ctx.redirect(NamedRoutes.rootPath());
            return;
        }

        if (!UrlRepository.urlExists(normalizedUrlString)) {
            Url url = new Url(normalizedUrlString);
            UrlRepository.save(url);

            ctx.sessionAttribute("flash", "Страница успешно добавлена");
            ctx.sessionAttribute("alertType", "success");
            ctx.redirect(NamedRoutes.urlsPath());
        } else {
            ctx.sessionAttribute("flash", "Страница уже существует");
            ctx.sessionAttribute("alertType", "danger");
            ctx.redirect(NamedRoutes.urlsPath());
        }
    }

    public static void show(Context ctx) throws SQLException {
        int id = ctx.pathParamAsClass("id", Integer.class).get();
        Url url = UrlRepository.find(id)
                .orElseThrow(() -> new NotFoundResponse("Page not found"));
        List<UrlCheck> urlChecks = UrlChecksRepository.getAllChecksForUrl(id);

        UrlPage page = new UrlPage(url);
        page.setUrlChecks(urlChecks);
        page.setFlash(ctx.consumeSessionAttribute("flash"));
        page.setAlertType(ctx.consumeSessionAttribute("alertType"));
        ctx.render("urls/show.jte", model("page", page));
    }

    public static void index(Context ctx) throws SQLException {
        List<Url> urls = UrlRepository.getEntities();
        Map<Integer, UrlCheck> allUrlsLastChecks = UrlChecksRepository.getAllUrlsLastChecks();
        UrlsPage page = new UrlsPage(urls, allUrlsLastChecks);
        page.setFlash(ctx.consumeSessionAttribute("flash"));
        page.setAlertType(ctx.consumeSessionAttribute("alertType"));
        ctx.render("urls/index.jte", model("page", page));
    }

    public static void createCheck(Context ctx) throws SQLException {
        int urlId = ctx.pathParamAsClass("id", Integer.class).get();
        Url url = UrlRepository.find(urlId)
                .orElseThrow(() -> new NotFoundResponse("Url not found"));

        HttpResponse<String> response;
        try {
            response = Unirest.get(url.getName()).asString();
            Unirest.shutDown();
        } catch (Exception e) {
            ctx.sessionAttribute("flash", "Invalid URL");
            ctx.sessionAttribute("alertType", "danger");
            ctx.redirect(NamedRoutes.urlPath(String.valueOf(urlId)));
            return;
        }

        int statusCode = response.getStatus();
        String responseHTMLBody = response.getBody();

        UrlCheck urlCheck = new UrlCheck(urlId, statusCode);

        Document document = Jsoup.parse(responseHTMLBody);
        urlCheck.setTitle(document.title());
        Element h1 = document.select("h1").first();
        urlCheck.setH1(h1 == null ? null : h1.text());
        Element content = document.select("meta[name=description]").first();
        urlCheck.setDescription(content == null ? null : content.attr("content"));
        UrlChecksRepository.save(urlCheck);

        ctx.sessionAttribute("flash", "Страница успешно проверена");
        ctx.sessionAttribute("alertType", "success");
        ctx.redirect(NamedRoutes.urlPath(String.valueOf(urlId)));
    }
}
