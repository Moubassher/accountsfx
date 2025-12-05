package com.moubassher.accountsfx;

import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.FlowPane;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

//TODO: Finish implementing creating a maintainer account:
/* A maintainer account is created before anything else, including an admin account.
    A maintainer account can create ACCOUNT TYPES and assign access to features for each
    type of account. The feature list is an enum provided by the dependent project.
 */

public class MaintainerAccountCtrl <F extends Enum<F> & Feature<F>> implements Initializable {
    @FXML
    private JFXTextField accountTypeField;

    @FXML
    private Button addAccountTypeBtn;

    @FXML
    private FlowPane featurePane;

    @FXML
    private Accordion accountTypeAccordion;

    @FXML
    private CheckBox isAdminBox;

    private Class<F> featureEnumClass;

    private AccountDao<F> accountDao;
    public MaintainerAccountCtrl(AccountDao<F> accountDao, Class<F> featureEnumClass) {
        this.accountDao = accountDao;
        this.featureEnumClass = featureEnumClass;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        accountTypeAccordion.getPanes().clear();

        

        for (F feature : featureEnumClass.getEnumConstants()) {
            CheckBox checkBox = new CheckBox(feature.name());
            checkBox.setUserData(feature); // store enum value
            featurePane.getChildren().add(checkBox);
        }

        addAccountTypeBtn.setOnAction(x -> {

            String accountTypeName = accountTypeField.getText();
            boolean isAdmin = isAdminBox.isSelected();

            List<F> selectedPrivileges = readSelectedPrivileges();

            TitledPane accountTypePane = displayCreatedAccountType(accountTypeName, selectedPrivileges);

            accountTypeAccordion.getPanes().add(accountTypePane);

            AccountType<F> accountType = AccountType.of(accountTypeName, selectedPrivileges, isAdmin);

            boolean added = accountDao.addAccountType(accountType);
            if(added) {
                accountTypeField.clear();
                isAdminBox.setSelected(false);
                clearSelectedPrivileges();
            }


        });

    }

    private TitledPane displayCreatedAccountType(String accountTypeName, List<F> selectedPrivileges) {
        TitledPane accountTypePane = new TitledPane();
        accountTypePane.setText(accountTypeName);
        FlowPane featurePane = new FlowPane(10, 10);
        accountTypePane.setContent(featurePane);
        selectedPrivileges.forEach(selectedFeature -> {
            CheckBox featureBox = new CheckBox(selectedFeature.name());
            featureBox.setSelected(true);
            featurePane.getChildren().add(featureBox);
        });
        return accountTypePane;
    }

    private List<F> readSelectedPrivileges() {
        List<F> selectedPrivileges = new ArrayList<>();

        featurePane.getChildren().stream()
                .filter(node -> node instanceof CheckBox cb && cb.isSelected())
                .map(featureBox -> ((CheckBox) featureBox).getText())
                .forEach(feature -> {
                    selectedPrivileges.add(F.valueOf(featureEnumClass, feature));
                });
        return selectedPrivileges;
    }

    private void clearSelectedPrivileges() {
        featurePane.getChildren().stream()
                .filter(node -> node instanceof CheckBox cb && cb.isSelected())
                .forEach(feature -> ((CheckBox) feature).setSelected(false));
    }
}
