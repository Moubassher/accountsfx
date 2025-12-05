package com.moubassher.accountsfx;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

public abstract class BaseAdminPrivileges<F extends Enum<F> & Feature<F>>  implements Initializable {

	static AccountDao accountDao;
	AccountTable accountTable;

	private Button addNewUser, modifyUser, deleteUser;

	@FXML
	private TabPane tabPane;

	@FXML
	private Tab pharmacyTab, userTab, attendanceTab;

	private List<Tab> tabs;

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		VBox box = Pharmacy.getInstance().pharmacyInfoPane();
		pharmacyTab.setContent(box);

		VBox employeeTabPane = this.createUserPane();
		userTab.setContent(employeeTabPane);

		this.installAttendanceTableTab();

		if(tabs != null) {
			tabPane.getTabs().addAll(tabs);
		}
	}

	private VBox createUserPane() {

		HBox actionPane= this.installActionPane();

		VBox employeeTabPane = new VBox(20);
		employeeTabPane.setPadding(new Insets(20));
		employeeTabPane.getChildren().addAll(actionPane, accountTable);
		return employeeTabPane;
	}

	public HBox installActionPane() {
		HBox actionsPane = new HBox(10);
		ChoiceBox<Account> accounts = new ChoiceBox<>();
		accounts.setItems(accountDao.readAccounts());
		addNewUser = new Button("إضافة حساب");
		modifyUser = new Button("تعديل");
		Button resetPasswordBtn = new Button("اعادة ضبط كلمة المرور");
		resetPasswordBtn.setOnAction(x -> {
			confirmResettingEmployeePassword();
		});
		deleteUser = new Button("حذف حساب");

		addNewUser.setOnAction(x -> createAccount());

		deleteUser.setOnAction(x -> {
					Account<F> account = accounts.getSelectionModel().getSelectedItem();
					confirmDeletingAccount(account);
				}
		);

		actionsPane.getChildren().addAll(accounts, addNewUser, modifyUser,
				resetPasswordBtn, deleteUser);
		return actionsPane;
	}



	private void confirmDeletingAccount(Account<F> account) {

		String accountUsername = account.getName();
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("تأكيد حذف حساب");
		String action = "هل تريد حذف (";
		action += accountUsername;
		action += ")";
		alert.setHeaderText(action);

		ButtonType yes = new ButtonType("نعم");
		ButtonType no = new ButtonType("رجوع");

		alert.getButtonTypes().clear();
		alert.getButtonTypes().addAll(yes, no);
		Scene alertScene = alert.getDialogPane().getScene();
		alertScene.getStylesheets().add("GreyFabric.css");
		alert.getDialogPane().setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
		Optional<ButtonType> result = alert.showAndWait();
		if(result.isPresent() && result.get() == yes) {
			accountDao.deleteAccount(account);
			accountTable.reloadTable();
		} else if(result.isPresent() && result.get() == no) {
			alert.close();
		}
	}

	private static  void createAccount() {

		Stage employeeAcctStage = new Stage();
		employeeAcctStage.initModality(Modality.WINDOW_MODAL);
		Label nameLabel = new Label("اسم الحساب");
		TextField employeeName = new TextField();

		Label dptLabel = new Label("القسم/ الإدارة");
		ChoiceBox<AccountType> accountSelector = new ChoiceBox<>();

		Label salaryLabel = new Label("المرتب");
		TextField salaryTF = new TextField();

		Label shiftStartLabel = new Label("بداية الدوام");
		ShiftPane shiftStartPane = setupShiftPane();

		Label shiftEndLabel = new Label("نهاية الدوام");
		ShiftPane shiftEndPane = setupShiftPane();

		Button add = new Button("تسجيل حساب جديد");
		Button clear = new Button("مسح البيانات");

		add.setOnAction(x -> {
			String name = employeeName.getText();
			String defaultPassword = "123";
			AccountType dpt = accountSelector.getSelectionModel().getSelectedItem();
			int salary = Integer.valueOf(salaryTF.getText());

			int startHour = shiftStartPane.getHour();
			int startMinutes = shiftStartPane.getMinutes();
			String startTimeOfDay = shiftStartPane.getTimeOfDay();

			LocalTime shiftStart = shiftStartPane.setTimestamp(startHour, startMinutes, startTimeOfDay);

			int finishHour = shiftEndPane.getHour();
			int finishMinutes = shiftEndPane.getMinutes();
			String finishTimeOfDay = shiftEndPane.getTimeOfDay();

			LocalTime shiftFinish= shiftEndPane.setTimestamp(finishHour, finishMinutes, finishTimeOfDay);

			Account<?> account = new Account<>(-1, name, defaultPassword, dpt, salary, shiftStart, shiftFinish);
			boolean success = accountDao.addNewAccount(account);

			Alert employeeAdded = new Alert(AlertType.INFORMATION);

			employeeAdded.setHeaderText("تمت إضافة الحساب بنجاح.");
			Scene alertScene = employeeAdded.getDialogPane().getScene();
			employeeAdded.getDialogPane().setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
			if(success) {
				employeeAdded.show();
			} else {
				employeeAdded.setHeaderText("تعذّرت إضافة الحساب إلى قاعدة البيانات.");
				employeeAdded.setContentText("حدث عطل ما. الرجاء الإتصال بالمبرمج");
				employeeAdded.show();
			}

		});

		HBox actionsPane = new HBox(10);
		actionsPane.getChildren().addAll(add, clear);

		VBox employeeAcctPane = new VBox(10);
		employeeAcctPane.getChildren().addAll(nameLabel, employeeName, dptLabel, accountSelector,
				salaryLabel, salaryTF, shiftStartLabel, shiftStartPane, shiftEndLabel, shiftEndPane, actionsPane);

		employeeAcctPane.setPadding(new Insets(20));
		employeeAcctPane.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

		Scene employeeAcctScene = new Scene(employeeAcctPane, 400, 600);

		employeeAcctStage.setScene(employeeAcctScene);
		employeeAcctStage.show();

	}

	private static ShiftPane setupShiftPane() {

		ShiftPane shiftPane = new ShiftPane();

		Spinner<Integer> shift_hour = new Spinner<>(1, 12, 9);
		shift_hour.setEditable(true);
		shift_hour.setMaxWidth(60);

		shiftPane.hourProperty().bind(shift_hour.valueProperty());

		TextFormatter<Integer> hourLimit = setInputLimit(1, 12);
		shift_hour.getEditor().setTextFormatter(hourLimit);

		Label colon = new Label(":");

		Spinner<Integer> shift_minutes = new Spinner<>(00, 59, 00);
		shift_minutes.setEditable(true);
		shift_minutes.setMaxWidth(60);

		shiftPane.minutesProperty().bind(shift_minutes.valueProperty());

		Spinner<String> shift_am_pm = new Spinner<String>();
		shift_am_pm.setMaxWidth(100);

		TextFormatter<Integer> inputLimit = setInputLimit(0, 59);
		shift_hour.getEditor().setTextFormatter(inputLimit);

		ObservableList<String> am_pm = FXCollections.observableArrayList();
		am_pm.add("صباحاً");
		am_pm.add("مساءً");
		shift_am_pm.setValueFactory(new SpinnerValueFactory.ListSpinnerValueFactory<String>(am_pm));

		shiftPane.timeOfDayProperty().bind(shift_am_pm.valueProperty());

		shiftPane.getChildren().addAll(shift_hour, colon, shift_minutes, shift_am_pm);

		return shiftPane;
	}

	private static TextFormatter<Integer> setInputLimit(int minValue, int maxValue) {
		TextFormatter<Integer> inputLimit = new TextFormatter<>(t -> {
			String inputString = t.getControlNewText();
			if(inputString.matches("[0-9]+")) {
				int input = Integer.valueOf(inputString);
				if(input > minValue && input < maxValue) {
					return t;
				}
			}
			return null;
		});
		return inputLimit;
	}

	private void installAttendanceTableTab() {
		AttendanceCtrl attendanceCtrl = new AttendanceCtrl();
		FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("attendance_table.fxml"));
		fxmlLoader.setController(attendanceCtrl);
		try {
			TableView<Login> attendanceTable = fxmlLoader.load();
			attendanceTab.setContent(attendanceTable);
		} catch (IOException e) {
			e.printStackTrace();
		}

		//TODO add more insights to attendance table than just showing it: e.g. most hours put in.

	}

	private void confirmResettingEmployeePassword() {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("المحاسب الذهبي");
		alert.setHeaderText("إعادة ضبط كلمة المرور");
		alert.setContentText("""
						يمكنك إعادة ضبط كلمة السر لأحد الحسابين من حسابك الإداري هذا من هذه النافذة.
						اختر الحساب الذي طلب إعادة كلمة السر واضغط على الزر أدناه ليتم ضبط كلمة السر إلى 123.
						ليستخدمها الحساب مرة واحدة للدخول إلى حسابه، ثم تغيير كلمة السر من داخل حسابه.
				""");
		ChoiceBox<String> employees = new ChoiceBox<>(accountDao.readAllUserNames());
		Label nameDisplay = new Label("اسم الحساب");
		VBox box= new VBox(10, nameDisplay, employees);
		alert.getDialogPane().setContent(box);
		alert.getButtonTypes().clear();

		ButtonType resetPassword = new ButtonType("إعادة ضبط كلمة المرور");
		alert.getButtonTypes().add(resetPassword);
		alert.showAndWait().ifPresent(button -> {
			if(button == resetPassword) {
				String name = employees.getSelectionModel().getSelectedItem();
				accountDao.resetPassword(name, "123");
			}
		});
	}



	public BaseAdminPrivileges() {

	}

	public abstract void addCustomTabs(List<Tab> tabs);

	private static class ShiftPane extends HBox{

		IntegerProperty hour, minutes;
		StringProperty timeOfDay;


		public IntegerProperty hourProperty() {
			return hour;
		}

		public IntegerProperty minutesProperty() {
			return minutes;
		}

		public StringProperty timeOfDayProperty() {
			return timeOfDay;
		}
		public int getHour() {
			return hour.get();
		}

		public int getMinutes() {
			return minutes.get();
		}

		public String getTimeOfDay() {
			String tod = am_pm.get(timeOfDay.get());

			return tod;
		}

		Map<String, String> am_pm = new HashMap<String, String>();


		public ShiftPane() {

			hour = new SimpleIntegerProperty();
			minutes = new SimpleIntegerProperty();
			timeOfDay = new SimpleStringProperty();

			am_pm.put("صباحاً", "AM");
			am_pm.put("مساءً", "PM");

			this.setSpacing(10);
		}

		public LocalTime setTimestamp(int hour, int minute, String timeOfDay) {

			String hourString = String.format("%02d", hour);
			String minuteString = String.format("%02d", minute);

			String stamp = hourString+ ":" + minuteString + timeOfDay;


			LocalTime time = LocalTime.parse(stamp, DateTimeFormatter.ofPattern("h:mma"));

			return time;
		}
	}
}
