package com.moubassher.accountsfx;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.context.our_utilities.MyNotifications;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.controlsfx.control.MaskerPane;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

/**
 * A controller for the login window. The user
 */
public class LoginCtrl<F extends Enum<F> & Feature<F>>  implements Initializable {

	private AccountDao<F> accountDao;

	TaskDatabase taskDatabase = TaskDatabase.getInstance();

	@FXML
	private Label signInDisplay, credsInfoDisplay, accountTypeDisplay, userNameDisplay;

	@FXML
	private ChoiceBox<AccountType<F>> accountTypeSelector;

	@FXML
	private ChoiceBox<Account<F>> registeredUsersSelector;

	@FXML
	private PasswordField passwordField;

	@FXML
	private Button loginBtn, createAccountBtn;

	@FXML
	private Hyperlink forgotPasswordLink;

	@FXML
	private StackPane loginRootPane;

	@FXML
	private MaskerPane masker;

	private boolean isUserManager;

	private Account<F> currentUser;


	private ValidationSupport validationSupport;

	public LoginCtrl(AccountDao<F> accountDao) {
		this.accountDao = accountDao;
	}

	public LoginCtrl(Account<F> currentUser) {
		this.currentUser = currentUser;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		String title;
		if(isUserManager) {
			title = "تسجيل الدخول (الإدارة)";
		} else {
			title = "تسجيل الدخول (حساب)";
		}
		signInDisplay.setText(title);

		accountTypeSelector.getItems().setAll(accountDao.readAccountTypes());

		onAccountTypeSelected(accountType -> getAccountsForType(accountType.getTypeName()));

		credsInfoDisplay.setId("error");
		if(isUserManager) {
			credsInfoDisplay.setText("تسجيل دخول للإدارة");
		}

		validationSupport = new ValidationSupport();
		validationSupport.registerValidator(passwordField,
				Validator.createEmptyValidator("يجب إدخال كلمة السر"));


		loginBtn.setOnAction(x -> login());

		forgotPasswordLink.setOnAction(x -> {
			sendPasswordResetRequest();
		});

		createAccountBtn.setOnAction(x -> {
			String description = "تم إرسال الطلب للإدارة";
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setContentText(description);
			alert.getDialogPane().setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
			ButtonType okBtn = new ButtonType("موافق");
			alert.getButtonTypes().setAll(List.of(okBtn));
			alert.showAndWait().ifPresent(result -> {
				if( result == okBtn) {
					loginWindow(true);
				}
			});
		});
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

	private void onAccountTypeSelected(Consumer<AccountType> accountTypeSelectedFunc) {
		accountTypeSelector.getSelectionModel().selectedItemProperty()
				.addListener((a, b, selectedDept) -> {
					accountTypeSelectedFunc.accept(selectedDept);
				});
	}

	private void getAccountsForType(String accountType) {
		List<Account<F>> allAccounts = accountDao.readAccounts();
		List<Account<F>> filteredAccounts = allAccounts.stream()
				.filter(account ->
						account.getAccountType().getTypeName().equals(accountType))
				.toList();
		registeredUsersSelector.setItems(FXCollections.observableList(filteredAccounts));
	}


	public void enforceAccountClass(AccountType.Class accountClass) {

		AccountType<F> maintainerType = accountTypeSelector.getItems()
				.stream()
				.filter(type -> type.getAccountClass().equals(accountClass))
				.findFirst()
				.orElse(null);

		if (maintainerType != null) {
			accountTypeSelector.getSelectionModel().select(maintainerType);
			accountTypeSelector.setDisable(true);

			// ensure accounts list is populated immediately
			getAccountsForType(maintainerType.getTypeName());
		}
	}

	private void login() {
		Account<F> account = registeredUsersSelector.getSelectionModel().getSelectedItem();
		if (account == null) {
			return;
		}

		String password = passwordField.getText();

		Validator<String> emptyValidator =
				Validator.createEmptyValidator("يجب إدخال كلمة السر");

		validationSupport.registerValidator(passwordField, emptyValidator);

		if (validationSupport.isInvalid()) {
			return; // Stop if field is empty
		}

		masker.setMouseTransparent(false);

		Task<Boolean> task = new Task<>() {
			@Override
			protected Boolean call() {
				boolean passwordMatches =
						BCrypt.verifyer().verify(password.toCharArray(), account.getHashedPassword()).verified;

				if (!passwordMatches) {
					return false; // Invalid password
				}

				return accountDao.loginAccount(account);
			}
		};

		loginBtn.disableProperty().bind(task.runningProperty());
		masker.visibleProperty().bind(task.runningProperty());

		task.setOnSucceeded(e -> {
			boolean success = task.getValue();

			loginBtn.disableProperty().unbind();
			masker.visibleProperty().unbind();
			if (success) {
				currentUser = account;
				Stage stage = (Stage) loginRootPane.getScene().getWindow();
				stage.close();
			} else {
				MyNotifications.getInstance().showErrorAlert("كلمة السر غير صحيحة", "حاول مرة أخرى أو أرسل طلب تغيير كلمة السر من أحد مستخدمي الحسابات الإدارية");
			}
		});

		task.setOnFailed(e -> {
			task.getException().printStackTrace();
			loginBtn.disableProperty().unbind();
		});

		new Thread(task).start();
	}


	public Account<F> getCurrentUser() {
		return currentUser;
	}

	public void logOutUser() {
		accountDao.logoutAccount(currentUser);
		loginWindow(false);
	}

	/**This method explains to the user why certain functionalities are disabled due to the nature 
	 * of the logged-in account:
	 *
	 */
	public void showFeatureRestrictedPopup(String restrictedOperations) {
		Label warning = new Label("تنبيه!");
		warning.setPadding(new Insets(5));

		//TODO: update disabling features' logic
		Label currentAccount = new Label(accountDao.getAccountType(-1)+"");
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
		boolean done = taskDatabase.addTask("طلب إعادة ضبط كلمة سر", "for " + currentUser.getName());
		if(done) {
			//TODO implement all possible ways to reset a password.
			/* 1. Send the reset request to the server (to be received by an admin).
				2. Send reset request to local admin, requiring a login with an admin account.
			 */
			MyNotifications.getInstance().showMessage("تم", "تم إرسال طلب للإدارة لإعادة ضبط كلمة السر إلى 123. وبعدما تتم إعادة الضبط. ادخل إلى حسابك باستخدام 123 (مرة واحدة)، ومن ثم قم بتغييرها من القائمة الرئيسية.");

		}
	}
}
//original line count: 276