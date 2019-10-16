/**
 * 
 */
package com.riskgame.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import com.riskgame.config.StageManager;
import com.riskgame.model.Continent;
import com.riskgame.model.GamePlayPhase;
import com.riskgame.model.Player;
import com.riskgame.model.PlayerTerritory;
import com.riskgame.model.RiskMap;
import com.riskgame.model.Territory;
import com.riskgame.service.Impl.MapManagementImpl;
import com.riskgame.view.FxmlView;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * 
 * @author <a href="mailto:p_thumma@encs.concordia.ca">Piyush Thummar</a>
 */
@Controller
public class RiskPlayScreenController implements Initializable {

	@FXML
	private ListView<?> lvTerritoryList;

	@FXML
	private Button btnAttack;

	@FXML
	private ListView<?> lvTerritoryPlayerArmy;

	@FXML
	private Button btnExecute;

	@FXML
	private Button btnReinforcement;

	@FXML
	private Button btnFortify;

	@FXML
	private ListView<?> lvCurrentLog;

	@FXML
	private ListView<?> lvContinent;

	@FXML
	private Button btnEndTurn;

	@FXML
	private ListView<?> lvLog;

	@FXML
	private ListView<?> lvAdjTerritoryList;

	@FXML
	private ComboBox<?> cbAdjTerritoryList;

	@FXML
	private TextField txtCommandLine;

	@FXML
	private TextField txtMoveNumArmy;

	@FXML
	private ComboBox<?> cbTerritoryList;

	@FXML
	private TextArea txtConsoleLog;

	@FXML
	private Button btnExit;

	@Autowired
	public MapManagementImpl mapManagementImpl;

	@FXML
	void placeReinforcement(ActionEvent event) {

	}

	@FXML
	void attack(ActionEvent event) {

	}

	@FXML
	void fortify(ActionEvent event) {

	}

	@FXML
	void endTurn(ActionEvent event) {

	}

	@FXML
	void fireCommand(ActionEvent event) {
		txtConsoleLog.clear();
		try {
			String command = txtCommandLine.getText();
			if (command != null && !command.isEmpty()) {
				if (command.startsWith("reinforce")) {
					txtConsoleLog.setText(placeReinforcement(command));
				} else if (command.startsWith("fortify")) {
					txtConsoleLog.setText(fortification(command));
				} else
					txtConsoleLog.setText("Please Enter Valid Command");
			} else {
				txtConsoleLog.setText("Please Enter valid Command");
			}

		} catch (Exception e) {
			e.printStackTrace();
			txtConsoleLog.setText("Please Enter Valid Command");
		}
	}

	/**
	 * @param command
	 * @return
	 */
	private String fortification(String command) {

		return null;
	}

	/**
	 * @param command
	 * @return
	 */
	private String placeReinforcement(String command) {
		StringBuilder result = new StringBuilder();
		Player player = new Player();
		String countryName = "";
		int armyToPlace;
		String[] dataArray = command.split(" ");
		List<String> commandData = Arrays.asList(dataArray);
		if (commandData.size() != 3) {
			result.append("Please Enter Valid Command");
		} else {
			countryName = commandData.get(1);
			armyToPlace = Integer.parseInt(commandData.get(2));
			reinforce(armyToPlace, countryName, player);
		}
		return result.toString();
	}

	/**
	 * @param armyToMove
	 * @param countryName
	 * @param playerName
	 */

	private void reinforce(int armyToPlace, String countryName, Player player) {
		GamePlayPhase gPhase = new GamePlayPhase();
		int addExtraArmy = 0;
		RiskMap riskmap = mapManagementImpl.readMap(gPhase.getFileName());
		Map<Integer, Continent> continentMap = riskmap.getContinents();
		List<Continent> continentList = new ArrayList<Continent>();
		for (Entry<Integer, Continent> e : continentMap.entrySet()) {
			continentList.add(e.getValue());
		}
		List<Player> playerList = gPhase.getGameState();
		for (Player ply : playerList) {
			List<PlayerTerritory> playerTerritories = ply.getPlayerterritories();
			List<String> playerTerritoriesStringList = new ArrayList<>();
			for (PlayerTerritory p : playerTerritories) {
				playerTerritoriesStringList.add(p.getTerritoryName());
			}
			int totalOwnedCountries = playerTerritories.size();
			for (PlayerTerritory playerTerritory : playerTerritories) {
				if (countryName.equals(playerTerritory.getTerritoryName())) {
					for (Continent ct : continentList) {
						List<Territory> territoryList = ct.getTerritoryList();
						int count = 0;
						for (Territory territory : territoryList) {
							if (playerTerritoriesStringList.contains(territory.getTerritoryName())) {
								count++;
							}
						}
						if (count == territoryList.size()) {
							addExtraArmy = ct.getContinentValue();
							break;
						}
					}
					int totalArmyforReinforce = checkForReinforcement(totalOwnedCountries);
					
				}
				break;
			}
			break;
		}
	}

	/**
	 * @param armyToPlace
	 * @param totalOwnedCountries
	 * @param addExtraArmy
	 * @return 
	 */
	private int checkForReinforcement(int totalOwnedCountries) {
		int total = Math.floorDiv(totalOwnedCountries, 3);
		int totalArmyforReinforce = Math.max(total, 3);
		return totalArmyforReinforce;
		
	}

	@Lazy
	@Autowired
	private StageManager stageManager;

	@FXML
	void exitGame(ActionEvent event) {
		stageManager.switchScene(FxmlView.WELCOME);
	}

	/**
	 * @see javafx.fxml.Initializable#initialize(java.net.URL,
	 *      java.util.ResourceBundle)
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {

	}
	
	 public void transferMessage(String message) {
	        //Display the message
	        System.out.println(message);
	    }

}
