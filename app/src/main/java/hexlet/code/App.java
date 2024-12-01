package hexlet.code;

import io.javalin.Javalin;
//import io.javalin.rendering.template.JavalinJte;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App {
    public static Javalin getApp() {
        var app = Javalin.create(config -> {
            config.bundledPlugins.enableDevLogging();
            //config.fileRenderer(new JavalinJte());
        });
        app.get("/", ctx -> {
            ctx.result("Hello, World!");
        });
        app.get("/testPage", ctx -> {
            ctx.result("Hello, Guest at my TEST PAGE!");
        });
        return app;
    }

    public static void main(String[] args) {
        getApp().start(7070);
    }
}
