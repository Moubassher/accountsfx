package com.moubassher.accountsfx.accessControl;

import com.context.our_utilities.MyNotifications;
import com.jfoenix.controls.JFXComboBox;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.ImageCursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import static com.moubassher.accountsfx.accessControl.PasswordStrength.STRONG;


public class AccountCreateCtrl implements Initializable {

    @FXML
    private VBox root;

    @FXML
    private TextField userNameField, visiblePasswordField, visibleConfirmPasswordField;

    @FXML
    private PasswordField passwordField, confirmPasswordField;

    @FXML
    private JFXComboBox<String> accountTypeSelector;

    @FXML
    private ProgressBar strengthBar;

    @FXML
    private Label strengthLabel;

    @FXML
    private Button showPasswordBtn1, showPasswordBtn2, createAccountBtn;


    @FXML
    private ProgressIndicator createAccountProgress;

    private Tooltip passwordMismatch;

    private AccountDao accountDao;

    private AccountValidator accountValidator;

    private boolean isAdminAccount, isAccountCreated;


    public AccountCreateCtrl(boolean isAdminAccount) {
        this.isAdminAccount = isAdminAccount;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        accountDao = AccountDao.getInstance();

        accountValidator = new AccountValidator();

        accountTypeSelector.getItems().setAll(accountDao.readuserTypes());

        visiblePasswordField.managedProperty().bind(visiblePasswordField.visibleProperty());

        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());

        visibleConfirmPasswordField.textProperty().bindBidirectional(confirmPasswordField.textProperty());

        onPasswordTyped(this::showPasswordStrength);

        passwordMismatch = new Tooltip();
        passwordMismatch.setText("يجب أن تتطابق كلمة المرور");
        passwordMismatch.setShowDelay(Duration.ZERO);
        passwordMismatch.setHideDelay(Duration.INDEFINITE);


        showPasswordBtn1.setOnMousePressed(e -> {
            visiblePasswordField.setVisible(true);
            passwordField.setVisible(false);
        });

        showPasswordBtn1.setOnMouseReleased(e -> {
            visiblePasswordField.setVisible(false);
            passwordField.setVisible(true);
        });

        showPasswordBtn2.setOnMousePressed(x -> {
            visibleConfirmPasswordField.setVisible(true);
            confirmPasswordField.setVisible(false);
        });

        showPasswordBtn2.setOnMouseReleased(x -> {
            visibleConfirmPasswordField.setVisible(false);
            confirmPasswordField.setVisible(true);
        });

        createAccountProgress.setVisible(false);

        createAccountBtn.setOnAction(x -> {

            String password = passwordField.getText();
            PasswordStrength strength = accountValidator.evaluateStrength(password);
            if(strength != STRONG) {
                MyNotifications.getInstance()
                        .showErrorAlert("كلمة المرور ليست قوية ما يكفي",
                                "يجب إضافة كلمة مرور قوية حسب الإرشادات لرفع مستوى الحماية");
                return;
            }

            String confirmedPassword = confirmPasswordField.getText();
            boolean match = accountValidator.validatePassword(password, confirmedPassword);
            if(match) {
                isAccountCreated = createAccount();
            } else {
                notifyOfPasswordMismatch(false);
            }
        });

        if(isAdminAccount) {
            enforceAdminAccountCreation();
        }

        Image notAllowedImage = new Image(this.getClass().getResource("not_allowed.png").toExternalForm());

        createAccountBtn.hoverProperty().addListener((a, b, hovered) -> {
            if(hovered) {
                String password = passwordField.getText();
                String confirmedPassword = confirmPasswordField.getText();
                if(!accountValidator.validatePassword(password, confirmedPassword)) {
                    createAccountBtn.setCursor(new ImageCursor(notAllowedImage));
                } else {
                    createAccountBtn.setCursor(null);
                }
            }
        });

    }


    public void enforceAdminAccountCreation() {
        accountTypeSelector.getSelectionModel().select("إدارة");
        accountTypeSelector.setDisable(true);
    }

    public boolean isAccountCreated() {
        return isAccountCreated;
    }

    private void onPasswordTyped(Consumer<String> passwordFunc) {
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            passwordFunc.accept(newVal);
        });
    }


    private  void showPasswordStrength(String password) {
        PasswordStrength strength = accountValidator.evaluateStrength(password);
        if (strength == PasswordStrength.WEAK) {
            strengthLabel.setText("كلمة المرور ضعيفة");
            strengthBar.setStyle("-fx-accent: red;");
        } else if (strength == PasswordStrength.MEDIUM) {
            strengthLabel.setText("كلمة المرور متوسطة الجودة");
            strengthBar.setStyle("-fx-accent: orange;");
        } else {
            strengthLabel.setText("كلمة المرور قوية ✔");
            strengthBar.setStyle("-fx-accent: green;");
        }

        double score = strength.getScore();
        strengthBar.setProgress(score);

    }


    private void notifyOfPasswordMismatch(boolean match) {
        if (!match) {
            passwordMismatch.show(confirmPasswordField.getScene().getWindow());
        } else {
            passwordMismatch.hide();
        }
    }


    ExecutorService executor = Executors.newSingleThreadExecutor();

    private boolean createAccount() {

        String userName = userNameField.getText();
        String password = passwordField.getText();
        String userType = accountTypeSelector.getSelectionModel().getSelectedItem();

        createAccountBtn.setDisable(true);
        createAccountProgress.setVisible(true);

        Task<String> hashingTask = new Task<>() {
            @Override
            protected String call() {
                return accountValidator.hashPassword(password);
            }
        };

        Future<?> result = executor.submit(hashingTask);
        boolean accountCreated = false;
        try {
            result.get();

            String hashedPassword = hashingTask.getValue();
            Account account = new Account(userName, hashedPassword, userType);
            accountCreated = accountDao.addNewAccount(account);

            createAccountProgress.setVisible(false);
            createAccountBtn.setDisable(false);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            MyNotifications.getInstance().showExceptionAlert(e);
        }
        return accountCreated;
    }



}
