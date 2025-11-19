package com.moubassher.accountsfx.accessControl;

import at.favre.lib.crypto.bcrypt.BCrypt;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * A controller for the login window. The user
 */
public class LoginCtrl implements Initializable {


	private StringProperty loggedInUser = new SimpleStringProperty();

	private EmployeeDB employeeDB = EmployeeDB.getInstance();

	TaskDatabase taskDatabase = TaskDatabase.getInstance();

	@FXML
	private Label signInDisplay, credsInfoDisplay, accountTypeDisplay, userNameDisplay;

	@FXML
	private ChoiceBox<String> accountTypeSelector;

	@FXML
	private ChoiceBox<Employee> registeredUsersSelector;

	@FXML
	private PasswordField passwordField;

	@FXML
	private Button loginBtn, createAccountBtn;

	@FXML
	private Hyperlink forgotPasswordLink;

	@FXML
	private VBox loginRootPane;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		String title;
		if(isUserManager) {
			title = "تسجيل الدخول (الإدارة)";
		} else {
			title = "تسجيل الدخول (موظفين)";
		}
		signInDisplay.setText(title);

		accountTypeSelector.getItems().setAll(employeeDB.readDepartments());

		onAccountTypeSelected(selectedDept -> getRegisteredUsersInDepartment(selectedDept));

		credsInfoDisplay.setId("error");
		if(isUserManager) {
			credsInfoDisplay.setText("تسجيل دخول للإدارة");
		}


		loginBtn.setOnAction(x -> {
			login();
		});

		createAccountBtn.setOnAction(x -> {
			String description = "تم إرسال الطلب للإدارة";
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setContentText(description);
			ButtonType okBtn = new ButtonType("موافق");
			alert.getButtonTypes().setAll(List.of(okBtn));
			alert.showAndWait().ifPresent(result -> {
				if( result == okBtn) {
					MgmtMenuOptions mgmtMenuOptions = new MgmtMenuOptions();
					mgmtMenuOptions.installManagementWindow();

					loginWindow(true);
				}
			});

		});
	}

	private boolean createAccount() throws IOException {
		AccountCreateCtrl createAccountCtrl = new AccountCreateCtrl();
		FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/accessControl/create_account.fxml"));
		loader.setController(createAccountCtrl);
		Pane createAccountPane = loader.load();
		createAccountCtrl.forManagerOnly();

		boolean isAccountCreated = createAccountCtrl.isAccountCreated();
		return isAccountCreated;
	}

	public String getloggedInUser() {
		return loggedInUser.get();
	}

	public void loginWindow(boolean mgmt) {

		Stage stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.setAlwaysOnTop(true);

		Scene loginScene = new Scene(loginRootPane, 450, 500);
		loginScene.getStylesheets().add("GreyFabric.css");
		stage.setScene(loginScene);

		stage.setOnCloseRequest(close -> {
			if(mgmt) {
				stage.close();
			} else {
				close.consume();
			}
		});

		stage.setResizable(false);
		stage.showAndWait();
	}

	private void onAccountTypeSelected(Consumer<String> accountTypeSelectedFunc) {
		accountTypeSelector.getSelectionModel().selectedItemProperty()
				.addListener((a, b, selectedDept) -> {
					accountTypeSelectedFunc.accept(selectedDept);
				});
	}

	private void getRegisteredUsersInDepartment(String department) {
		Map<String, List<Employee>> deptEmployeeMap = employeeDB.getDepartment_EmployeeMap();
		List<Employee> employees = deptEmployeeMap.get(department);
		registeredUsersSelector.setItems(FXCollections.observableList(employees));
	}


	private boolean isUserManager;
	private boolean login() {


        Employee employee = registeredUsersSelector.getSelectionModel().getSelectedItem();
        BCrypt.Result result = null;
        if (employee != null) {
            String name = employee.getName();
            String password = passwordField.getText();

            String hashedPassword = BCrypt.withDefaults()
                    .hashToString(14, password.toCharArray());

            ValidationSupport validationSupport = new ValidationSupport();
            validationSupport.registerValidator(passwordField,
                    Validator.createEmptyValidator("يجب إدخال كلمة السر"));

            Predicate<String> passwordPredicate = getPasswordPredicate(hashedPassword);
            String invalidPasswordMsg = "كلمة السر غير صحيحة";

            Validator<String> passwordValidator = Validator.createPredicateValidator(passwordPredicate, invalidPasswordMsg);

            validationSupport.registerValidator(passwordField, passwordValidator);

            result = BCrypt.verifyer().verify(password.toCharArray(),
                    hashedPassword);

            boolean validCreds;
            if (isUserManager) {
                validCreds = employeeDB.loginManagement(name, password);
            } else {
                validCreds = employeeDB.loginEmployee(name, password);
            }

            if (validCreds) {
                String userName = employeeDB.getName();
                loggedInUser.set(userName);
            }
        }
        return result.verified;
    }

	private Predicate<String> getPasswordPredicate(String hashedPassword) {
		return password -> {
			if (password == null || password.isBlank()) return false;
			BCrypt.Result result = BCrypt.verifyer().verify(password.toString().toCharArray(), hashedPassword);
			return result.verified;
		};
	}

	public void logOutUser() {
		employeeDB.logoutUser();
		loginWindow(false);
	}

	/**This method explains to the user why certain functionalities are disabled due to the nature 
	 * of the logged-in account:
	 *
	 */
	public void showEditingRestrictionPopup(String restrictedOperations) {
		Label warning = new Label("تنبيه!");
		warning.setId("warning");
		warning.setPadding(new Insets(5));

		Label currentAccount = new Label(employeeDB.getAccountType());
		currentAccount.setId("current-account");
		Label restrictedOps = new Label(restrictedOperations);
		restrictedOps.setId("restrictedOps");
		String currentIs = "حساب المستخدم الحالي حساب ";
		Label pleaseLogWith = new Label("يجب تسجيل الدخول بحساب مناسب لعمليات ");

		Label operationsDisabled = new Label("النافذة الحالية للعرض فقط/ تم تعطيل خيارات التعديل.");
		Label restrictionLabel = new Label("");
		restrictionLabel.setText(currentIs);
		restrictionLabel.setId("restriction-label"); //$NON-NLS-1$

		FlowPane messagePane = new FlowPane();
		messagePane.getChildren().addAll(restrictionLabel, currentAccount, pleaseLogWith,
				restrictedOps, operationsDisabled);

		Button OK = new Button("موافق");
		OK.setOnAction(x -> {
			//stage.close();
		});
		Button logOut = new Button("تسجيل الخروج");
		logOut.setOnAction(x -> {
			logOutUser();
		});

		HBox actionsPane = new HBox(10);
		actionsPane.setAlignment(Pos.CENTER);
		actionsPane.getChildren().addAll(OK, logOut);
		VBox popupPane = new VBox(10);
		popupPane.setPadding(new Insets(10));
		popupPane.setAlignment(Pos.TOP_CENTER);
		popupPane.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

		popupPane.getChildren().addAll(warning, messagePane, actionsPane);

		Scene popupScene = new Scene(popupPane, 400, 250);

		//	stage.setScene(popupScene);
		//	stage.show();

	}

	public static void trialVersionEnded() {

		String msg =""" 
		عزيزنا المشترك،
		لقد انتهت الفترة التجريبية لاستخدام برنامج المحاسب الذهبي. 
		نرجو ان تكون قد حظيت بتجربة سلسة في استخدامك لمزايا البرنامج
		للمتابعة، يرجى تجديد الاشتراك
		""";
		Alert alert = new Alert(Alert.AlertType.INFORMATION);

		alert.setTitle("المحاسب الذهبي");
		alert.setHeaderText("لقد انتهت الفترة التجريبية");
		alert.getDialogPane().setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
		alert.setContentText(msg);

		alert.showAndWait().ifPresent(result -> {
			if(result == ButtonType.OK) {
				System.exit(0);
			}
		});

	}


	@FXML
	private void sendPasswordResetRequest() {
		boolean done = taskDatabase.addTask("طلب إعادة ضبط كلمة سر", "for " + employeeDB.getName());
		if(done) {
			//TODO replace with a suitable notification from MyNotification's.
			//Messages.showNotification("تم", "تم إرسال طلب للإدارة لإعادة ضبط كلمة السر إلى 123. وبعدما تتم إعادة الضبط. ادخل إلى حسابك باستخدام 123 (مرة واحدة)، ومن ثم قم بتغييرها من القائمة الرئيسية.");

		}
	}
}
//original line count: 276