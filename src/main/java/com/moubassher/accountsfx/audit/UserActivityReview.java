package com.moubassher.accountsfx.audit;

import com.moubassher.accountsfx.AccountDao;
import com.moubassher.accountsfx.Feature;
import com.moubassher.search.SearchBar;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;

public class UserActivityReview<F extends Enum<F> & Feature<F>> implements Initializable {

    @FXML
    private VBox actionContainer;

    @FXML
    private SearchBar<UserActivity<F>> userActivitySearchbar;
    private AccountDao<F> accountDao;
    private Class<F> featureEnum;
    public UserActivityReview(AccountDao<F> accountDao, Class<F> featureEnum) {
        this.accountDao = accountDao;
        this.featureEnum = featureEnum;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        List<UserActivity<F>> userActivity= accountDao.getAllUserActivity(featureEnum);
        userActivitySearchbar.initialize(userActivity, results -> {
            showUserActivity(results);
        });

        showUserActivity(userActivity);
    }

    private void showUserActivity(List<UserActivity<F>> userActivity) {
        actionContainer.getChildren().clear();
        for(int i = 0; i < userActivity.size(); i++) {
            var activity = userActivity.get(i);
            HBox actionRow = createActionRow(activity);
            if(i % 2 ==0) {
                actionRow.getStyleClass().add("even-row");
            } else {
                actionRow.getStyleClass().add("odd-row");
            }
            actionContainer.getChildren().add(actionRow);
        }
    }


    private HBox createActionRow(UserActivity<F> activity) {
        HBox actionRow = new HBox(10);
        String action = activity.getAction().name();
        String userName = activity.getUserName();
        String accountTypeName = activity.getAccountType().toString();
        String accountClass = activity.getAccountClass();
        LocalDateTime timestamp = activity.getTimestamp();

        actionRow.getChildren().addAll(new Label("المستخدم"),
                new Label(userName),
                new Label("بنوع حساب: " + accountTypeName),
                new Label("من فئة: " + accountClass),
                new Label( "قام بـ"+ action),
                new Label("في الساعة: "),
                new Label(timestamp.toLocalTime().toString()),
                new Label("يوم: "),
                new Label(timestamp.toLocalDate().toString()));

        return actionRow;
    }
}
