package org.ogee.bootstrap;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.ogee.application.service.ParserService;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class JavaFxApplication extends Application {

    private ConfigurableApplicationContext context;

    @Override
    public void init() {
        context = new SpringApplicationBuilder(SpringContext.class)
                .run();
    }

    @Override
    public void start(Stage stage) {
        ParserService parserService = context.getBean(ParserService.class);

        Button button = new Button("Запустить парсинг");
        button.setOnAction(event ->parserService.testParse());

        VBox root = new VBox(10, button);
        Scene scene = new Scene(root, 800, 500);

        stage.setTitle("Test Parsing");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        context.close();
    }
}