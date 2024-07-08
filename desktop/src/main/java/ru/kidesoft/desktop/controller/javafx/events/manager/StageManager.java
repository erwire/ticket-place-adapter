package ru.kidesoft.desktop.controller.javafx.events.manager;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Getter;
import net.rgielen.fxweaver.core.FxWeaver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.Notifications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import ru.kidesoft.desktop.controller.javafx.Controller;
import ru.kidesoft.desktop.controller.javafx.events.StageReadyEvent;
import ru.kidesoft.desktop.controller.javafx.events.StartSessionEvent;
import ru.kidesoft.desktop.controller.javafx.fxml.AuthController;
import ru.kidesoft.desktop.controller.javafx.fxml.BaseController;
import ru.kidesoft.desktop.domain.exception.ApiException;
import ru.kidesoft.desktop.domain.exception.AppException;
import ru.kidesoft.desktop.domain.exception.DbException;
import ru.kidesoft.desktop.domain.exception.KktException;
import ru.kidesoft.desktop.infrastructure.service.AuthService;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

@Component
public class StageManager implements ApplicationListener<StageReadyEvent> {
    private Logger logger = LogManager.getLogger(StageManager.class);

    private ConfigurableApplicationContext context;
    private final FxWeaver fxWeaver;
    @Getter
    private Stage stage;
    @Value("${spring.application.name}")
    private String title;

    @Value("${spring.application.icon}")
    private Resource logoImage;

    @Autowired
    public StageManager(
            FxWeaver fxWeaver,
            ConfigurableApplicationContext context) {
        this.context = context;
        this.fxWeaver = fxWeaver;
    }

    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        stage = event.getStage();
        Parent parent = fxWeaver.loadView(BaseController.class);
        Scene scene = new Scene(parent, 600, 415);

        String path = Objects.requireNonNull(getClass().getResource("/assets/css/notification.css")).toExternalForm();

        if (!scene.getStylesheets().contains(path)) {
            scene.getStylesheets().add(path);
        }

        stage.setScene(scene);
        stage.sizeToScene();

        try {
            stage.getIcons().add(new Image(logoImage.getURL().toString()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        stage.setTitle(title);
        stage.show();

        context.getBean(AuthService.class).getActiveLogin().ifPresentOrElse(
                currentLogin -> context.publishEvent(new StartSessionEvent(StartSessionEvent.StartSession.START))
                , () -> {
                    context.getBean(StageManager.class).show(AuthController.class);
                }
        );
    }


    public void show(Class<? extends Controller> controller) {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        MenuBar top = (MenuBar) root.getTop();

        if (controller == AuthController.class) {
            top.setDisable(true);
            top.setVisible(false);
        } else {
            top.setDisable(false);
            top.setVisible(true);
        }

        Parent parent = fxWeaver.loadView(controller);

        StackPane center = (StackPane) root.getCenter();
        center.getChildren().clear();
        center.getChildren().add(parent);
    }

    public void showError(AppException e) {
        Alert alert = new Alert(Alert.AlertType.ERROR, "Ошибка", ButtonType.OK);

        alert.setTitle("Ошибка");

        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setContentText(e.getMessage());

        try {
            ((Stage)alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image(logoImage.getURL().toString()));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        if (e instanceof DbException) {
            alert.setHeaderText("Внутренняя ошибка базы данных");
        } else if(e instanceof KktException) {
            alert.setHeaderText("Внутренняя ошибка ККТ");
        } else if (e instanceof ApiException) {
            alert.setHeaderText("Ошибка обращения к удаленному серверу");
        } else {
            alert.setHeaderText("Неизвестная ошибка");
        }

        alert.showAndWait();
    }

    public void showNotification(AppException e) {
        String path = getClass().getResource("/assets/css/cupertino-light.css").toExternalForm();

        stage.getScene().getStylesheets().add(path);

        var notification = Notifications.create().position(Pos.TOP_RIGHT).text(e.getMessage());

        if (e instanceof DbException) {
            notification.title("Внутренняя ошибка базы данных");
        } else if(e instanceof KktException) {
            notification.title("Внутренняя ошибка ККТ");
        } else if (e instanceof ApiException) {
            notification.title("Ошибка обращения к удаленному серверу");
        } else {
            notification.title("Неизвестная ошибка");
        }


        notification.show();
    }

    public void showNotification(String header, String text) {

        Notifications.create().position(Pos.TOP_RIGHT).text(text).title(header).show();
    }

    public void showWarning(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, "Предупреждение", ButtonType.OK);

        alert.setTitle("Предупреждение");

        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setHeaderText(header);
        alert.setContentText(message);

        try {
            ((Stage)alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image(logoImage.getURL().toString()));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        alert.showAndWait();
    }

    public Optional<String> showWarningWithPassword(String title, String header, String message) {
        var dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(message);
        dialog.initOwner(stage);

        return dialog.showAndWait();
    }

    }

