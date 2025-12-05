package com.moubassher.accountsfx;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;


import java.io.IOException;
import java.util.List;

public class MgmtMenuOptions<F extends Enum<F> & Feature<F>>  {

	
	public void installManagementWindow() {
		Stage mgmtStage = new Stage();
		mgmtStage.initModality(Modality.WINDOW_MODAL);
		mgmtStage.setTitle("الإدارة");

		BaseAdminPrivileges<F> baseAdminPrivileges = new BaseAdminPrivileges<>() {

			@Override
			public void addCustomTabs(List<Tab> tabs) {

			}
		};

		FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("manager_privileges.fxml"));
		fxmlLoader.setController(baseAdminPrivileges);
        Pane pane = null;
        try {
            pane = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Scene mgmtScene = new Scene(pane, 700, 600);
		mgmtStage.setScene(mgmtScene);
		mgmtStage.show();
	}
}
