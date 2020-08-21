/**
 * @author SridevBalakrishnan
 * @purpose Displays the results in a tabular format on the console
 */

package com.footy.stats;

import java.util.List;

import com.footy.stats.domain.TeamHistory;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class DisplayDataTable extends Application {

	static List<TeamHistory> teamHistoryLst;
	static int index = 0;
	static final String CSS_BOLD = "-fx-font-weight: bold;";
	static final String CSS_GREEN = "-fx-background-color: green;";
	static final String CSS_GOLD = "-fx-background-color: gold;";
	static final String CSS_RED = "-fx-background-color: red;";
	static final String CSS_CNTR = "-fx-alignment: center;";
	static final String BOTH_SCORED = "BOTH SCORED";
	static final String OVER_GOALS = "OVER GOALS";

	public static void main(String[] args) {
		launch(args);
	}

	public void start(Stage primaryStage) {

		TableView<TeamHistory> tableView = new TableView<>();

		TableColumn<TeamHistory, ?> column1 = new TableColumn<>("LEAGUE");
		column1.setCellValueFactory(new PropertyValueFactory<>("league"));

		TableColumn<TeamHistory, ?> column2 = new TableColumn<>("OPPONENT");
		column2.setCellValueFactory(new PropertyValueFactory<>("opponent"));

		TableColumn<TeamHistory, String> column3 = new TableColumn<>("RESULT");
		column3.setCellFactory(param -> stylingString (column3));

		TableColumn<TeamHistory, ?> column4 = new TableColumn<>("GOALS FOR");
		column4.setCellValueFactory(new PropertyValueFactory<>("homeGoals"));

		TableColumn<TeamHistory, ?> column5 = new TableColumn<>("GOALS AGAINST");
		column5.setCellValueFactory(new PropertyValueFactory<>("awayGoals"));

		TableColumn<TeamHistory, String> column6 = new TableColumn<>("BOTH SIDES SCORED");
		column6.setCellFactory(param -> stylingBoolean (column6, BOTH_SCORED));

		TableColumn<TeamHistory, String> column7 = new TableColumn<>("OVER 2.5 GOALS");
		column7.setCellFactory(param -> stylingBoolean (column6, OVER_GOALS));

		tableView.getColumns().add(column1);
		tableView.getColumns().add(column2);
		tableView.getColumns().add(column3);
		tableView.getColumns().add(column4);
		tableView.getColumns().add(column5);
		tableView.getColumns().add(column6);
		tableView.getColumns().add(column7);

		for (int i = 0; i < index; i++)
			tableView.getItems().add(new TeamHistory(teamHistoryLst.get(i).getLeague(), teamHistoryLst.get(i).getOpponent(), 
					teamHistoryLst.get(i).getMatchResult(), teamHistoryLst.get(i).getHomeGoals(), 
					teamHistoryLst.get(i).getAwayGoals(), teamHistoryLst.get(i).isBothSidesScored(), 
					teamHistoryLst.get(i).isMoreTotGoals()));

		VBox vbox = new VBox(tableView);

		Scene scene = new Scene(vbox);


		primaryStage.setScene(scene);

		primaryStage.show();
	}

	public static void getData (List<TeamHistory> teamHistory, int size) {
		teamHistoryLst = teamHistory;
		index = size;
		launch();
	}

	public TableCell<TeamHistory, String> stylingBoolean (TableColumn<TeamHistory, String> param, String column) {
		return new TableCell<TeamHistory, String>() {
			@Override
			protected void updateItem(String item, boolean empty) {
				boolean result = false;
				if (!empty) {
					int currentIndex = indexProperty()
							.getValue() < 0 ? 0
									: indexProperty().getValue();
					if (column.equalsIgnoreCase("BOTH SCORED")) {
						result = param
								.getTableView().getItems()
								.get(currentIndex).isBothSidesScored();
					}
					else {
						result = param
								.getTableView().getItems()
								.get(currentIndex).isMoreTotGoals();
					}                    	
					setStyle(CSS_BOLD);
					if (result) {
						setTextFill(Color.WHITE);
						setStyle(CSS_GREEN);
						setText("Y");
					}else {
						setTextFill(Color.WHITE);
						setStyle(CSS_RED);
						setText("N");
					}
				}
			}
		};
	}


	public TableCell<TeamHistory, String> stylingString (TableColumn<TeamHistory, String> param) {
		return new TableCell<TeamHistory, String>() {
			@Override
			protected void updateItem(String item, boolean empty) {
				if (!empty) {
					int currentIndex = indexProperty()
							.getValue() < 0 ? 0
									: indexProperty().getValue();
					String result = param
							.getTableView().getItems()
							.get(currentIndex).getMatchResult();
					setStyle(CSS_BOLD);
					if (result.equals("W")) {
						setTextFill(Color.WHITE);
						setStyle(CSS_GREEN);
						setText(result);
					} 
					else if (result.equals("D")){
						setTextFill(Color.WHITE);
						setStyle(CSS_GOLD);
						setText(result);
					}
					else {
						setTextFill(Color.WHITE);
						setStyle(CSS_RED);
						setText(result);
					}
				}
			}
		};
	}
}
