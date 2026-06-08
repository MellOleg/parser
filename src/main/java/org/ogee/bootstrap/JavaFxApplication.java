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
    private MainWindow mainWindow;

    @Override
    public void init() {
        context = new SpringApplicationBuilder(SpringContext.class)
                .run();
    }

    @Override
    public void start(Stage stage) {
        mainWindow = context.getBean(MainWindow.class);

        Scene scene = new Scene(mainWindow.createContent(), 1200, 700);

        stage.setTitle("STALCRAFT Market Scanner");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        if (mainWindow != null) {
            mainWindow.shutdown();
        }

        if (context != null) {
            context.close();
        }
    }
}