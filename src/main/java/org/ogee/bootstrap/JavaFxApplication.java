package org.ogee.bootstrap;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.ogee.SpringContext;
import org.ogee.ui.MainWindow;
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
        MainWindow mainWindow = context.getBean(MainWindow.class);

        Scene scene = new Scene(mainWindow.createContent(), 1100, 650);

        stage.setTitle("Parser");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        context.close();
    }
}