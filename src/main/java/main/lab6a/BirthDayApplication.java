package main.lab6a;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;

public class BirthDayApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(BirthDayApplication.class.getResource("BirthDayView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        stage.setTitle("BirthDays");
        stage.setScene(scene);
        BirthDateController controller = fxmlLoader.getController();

        stage.setOnShowing(new EventHandler<WindowEvent>() {
            @Override
            public void handle(javafx.stage.WindowEvent event) {
                controller.loadPaymentsFromDB();
            }
        });
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(javafx.stage.WindowEvent event) {
                controller.deleteBirthDatesFromDB();
                controller.insertPaymentsIntoDB();
            }
    
        });

        stage.show();
    }

    public void end(){

    }

    public static void main(String[] args) {
        launch();
    }
}