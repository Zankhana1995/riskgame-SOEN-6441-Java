/**
 * 
 */
package com.riskgame.controller;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import com.riskgame.config.StageManager;
import com.riskgame.model.GamePlayPhase;
import com.riskgame.model.Player;
import com.riskgame.model.PlayerTerritory;
import com.riskgame.model.RiskMap;
import com.riskgame.service.Impl.MapManagementImpl;
import com.riskgame.service.Impl.RiskPlayImpl;
import com.riskgame.view.FxmlView;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

	GamePlayPhase gameplayphase = new GamePlayPhase();
	@FXML
	private ListView<?> lvTerritoryList;

	@FXML
	private Button btnAttack;

	@FXML
	private ListView<?> lvTerritoryPlayerArmy;

	@FXML
	private Button btnFireCommand;

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
	
	@Autowired
	public RiskPlayImpl riskPlayImpl;
	
	@Lazy
	@Autowired
	private StageManager stageManager;
	
	private GamePlayPhase GamePlayPhase;
	
	private RiskMap riskMap;
	
	private StringBuilder sb;
	
	private int playerIndex = 0;
	private String playerName  = "";
	//private int playerLeftArmy = 0;
	private int playerReinforceArmy = 0;
	
	private static String turnStartedMsg  = "";
	private static String leftArmyMsg = "";
	
	public static final String NEUTRAL = "NEUTRAL";
	
	private static String NEWLINE = System.getProperty("line.separator");
	
	private ObservableList<Player> playerList = FXCollections.observableArrayList();
	
	
	/**
	 * @see javafx.fxml.Initializable#initialize(java.net.URL,
	 *      java.util.ResourceBundle)
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {

		GamePlayPhase = new GamePlayPhase();
		riskMap = new RiskMap();
		sb = new StringBuilder();
		
		
	}

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
		
		//txtConsoleLog.clear();
		
		System.out.println("playerIndex => "+playerIndex);
		System.out.println("playerName => "+playerName);
		System.out.println("playerReinforceArmy => "+playerReinforceArmy);
		
		System.out.println("List = > "+ playerList);
		
		
		
		try {
			String command = txtCommandLine.getText();
			if (command != null && !command.isEmpty()) {
				if (command.startsWith("reinforce")) {
					
					if(playerReinforceArmy !=0) {
						txtConsoleLog.setText(placeReinforcement(command));
					}else {
						sb.append(playerName).append(" 's Reinforcement Phase done please go to the Fortification phase").append(NEWLINE);
						txtConsoleLog.setText(sb.toString());
					}
					
				} else if (command.startsWith("fortify")) {
					
					if(playerReinforceArmy == 0){
						txtConsoleLog.setText(fortification(command));
					}else {
						sb.append("you have left ").append(playerReinforceArmy).append(" to reinforcement").append(NEWLINE);
						txtConsoleLog.setText(sb.toString());
					}
					
					
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

	
	private String fortification(String command) {

		String[] dataArray = command.split(" ");
		List<String> commandData = Arrays.asList(dataArray);
		
		if (commandData.get(0).equalsIgnoreCase("fortify")) {
			
			if (commandData.size() == 2 && commandData.get(0).equals("fortify") && commandData.get(1).equals("none")) {
				
				
				sb.append(playerName).append(" 's turn ended.!").append(NEWLINE);
				
				System.out.println("FORTIFY ====> "+playerList);
				
				if(playerIndex<playerList.size()-1) {
					
					playerIndex++;
					
					if(playerList.get(playerIndex).getPlayerName().equalsIgnoreCase(NEUTRAL)) {
						playerIndex = 0;
					}
					
				}else {
					playerIndex = 0;
				}
				
			    txtCommandLine.clear();
		        txtConsoleLog.clear();
		        
		        playerName = playerList.get(playerIndex).getPlayerName();
		        playerReinforceArmy = riskPlayImpl.checkForReinforcement(playerList.get(playerIndex).getPlayerterritories().size());
		        
		        
		        turnStartedMsg = playerName +"'s turn is started";
		        leftArmyMsg = "You have "+playerReinforceArmy+" armies to Reinforcement";
		        sb.append(turnStartedMsg).append(NEWLINE).append(leftArmyMsg).append(NEWLINE);
		    
		        txtConsoleLog.setText(sb.toString());
				
			} else {
				
				if (commandData.size() != 4) {
					
					sb.append("Please Enter Valid command").append(NEWLINE);
					
				} else {
					
					if (validateInput(commandData.get(3), "[1-9][0-9]*") 							
							&& validateInput(commandData.get(1), "^([a-zA-Z]-+\\s)*[a-zA-Z-]+$")
							&& validateInput(commandData.get(2), "^([a-zA-Z]-+\\s)*[a-zA-Z-]+$")) {
						
						String fromCountry = commandData.get(1);
						String toCountry = commandData.get(2);
						int armytoMove = Integer.parseInt(commandData.get(3));
						
						PlayerTerritory fromTerritory = playerList.get(playerIndex).getPlayerterritories().stream().
								filter(x -> fromCountry.equals(x.getTerritoryName())).findAny().orElse(null);
						
						PlayerTerritory toTerritory = playerList.get(playerIndex).getPlayerterritories().stream().
								filter(x -> toCountry.equals(x.getTerritoryName())).findAny().orElse(null);
						
						if(fromTerritory != null && toTerritory != null) {
							
							List<String> neighbourCountriesList = mapManagementImpl.getNeighbourCountriesListByCountryName(riskMap, fromCountry);
							
							System.out.println("neighbourCountriesList ==> "+ neighbourCountriesList);
							
							if(neighbourCountriesList.contains(toCountry)) {
								
								if(fromTerritory.getArmyOnterritory()>armytoMove ) {
									
									int left = fromTerritory.getArmyOnterritory() - armytoMove;
									fromTerritory.setArmyOnterritory(left);
									toTerritory.setArmyOnterritory(toTerritory.getArmyOnterritory()+armytoMove);
									
									sb.append(playerName).append("s fortification done").append(NEWLINE);
									sb.append(playerName).append(" 's turn ended.!").append(NEWLINE);
									
									System.out.println("FORTIFY ====> "+playerList);
									
									if(playerIndex<playerList.size()-1) {
										
										playerIndex++;
										
										if(playerList.get(playerIndex).getPlayerName().equalsIgnoreCase(NEUTRAL)) {
											playerIndex = 0;
										}
										
									}else {
										playerIndex = 0;
									}
									
								    txtCommandLine.clear();
							        txtConsoleLog.clear();
							        
							        playerName = playerList.get(playerIndex).getPlayerName();
							        playerReinforceArmy = riskPlayImpl.checkForReinforcement(playerList.get(playerIndex).getPlayerterritories().size());
							        
							        
							        turnStartedMsg = playerName +"'s turn is started";
							        leftArmyMsg = "You have "+playerReinforceArmy+" armies to Reinforcement";
							        sb.append(turnStartedMsg).append(NEWLINE).append(leftArmyMsg).append(NEWLINE);
							    
							        txtConsoleLog.setText(sb.toString());
									
									
									
									
								}else {
									sb.append("you should have at least one army in your territory after fortification").append(fromCountry).append(NEWLINE);
								}
								
							}else {
								sb.append(toCountry).append(" is not neighbour country of ").append(fromCountry).append(NEWLINE);
							}
							
						}else {
							sb.append("fromcountry or tocountry not found : Please Enter Valid country Name").append(NEWLINE);
						}

					} else {
						sb.append("Please Enter Valid command").append(NEWLINE);
					}
						
				}
			}
		} else {
			
			sb.append("Please Enter Valid command").append(NEWLINE);
		}
			
		return sb.toString();
	}


	private boolean validateInput(String value, String pattern) {
		if (!value.isEmpty()) {
			Pattern p = Pattern.compile(pattern);
			Matcher m = p.matcher(value);
			if (m.find() && m.group().equals(value)) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
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
		if (commandData.size() == 3 
			&& validateInput(commandData.get(1), "^([a-zA-Z]-+\\s)*[a-zA-Z-]+$") 
			&& validateInput(commandData.get(2), "[1-9][0-9]*") ) {
			
			countryName = commandData.get(1);
			armyToPlace = Integer.parseInt(commandData.get(2));
			
			final String cName = countryName;
			
			PlayerTerritory playerTerritory = playerList.get(playerIndex).getPlayerterritories().stream().
					filter(x -> cName.equals(x.getTerritoryName())).findAny().orElse(null);
			
			if(playerTerritory != null) {
				
				if(armyToPlace<=playerReinforceArmy && armyToPlace !=0 && playerReinforceArmy != 0) {
					
					playerTerritory.setArmyOnterritory(playerTerritory.getArmyOnterritory()+ armyToPlace);
					playerReinforceArmy = playerReinforceArmy - armyToPlace;
					
					String message = armyToPlace +" Assigned to "+cName;
					
					sb.append(message).append(NEWLINE);
					sb.append("you have left ").append(playerReinforceArmy).append(" to reinforcement").append(NEWLINE);
					if(playerReinforceArmy ==0) {
						sb.append(playerName).append(" 's Reinforcement Phase done please go to the Fortification phase").append(NEWLINE);
					}
					
					System.out.println("REINFORCEMENT ====> "+playerList);
					
				}else {
					sb.append("Please provide Valid Army details").append(NEWLINE).append("you have left ").append(playerReinforceArmy)
					.append(" to reinforcement").append(NEWLINE);
				}
				
				
			}else {
				sb.append("Country not found : Please Enter Valid country Name").append(NEWLINE);
			}
			
			//reinforce(armyToPlace, countryName, player);
			
		} else {
			
			sb.append("Please Enter Valid Command").append(NEWLINE);
			
			
		}
		return sb.toString();
	}

	




	@FXML
	void exitGame(ActionEvent event) {
		stageManager.switchScene(FxmlView.WELCOME,null);
	}


	
	 public void transferGamePlayPhase(Object object) {
		 
	        //Display the message
		 	gameplayphase = (GamePlayPhase) object;
		 	riskMap = mapManagementImpl.readMap(gameplayphase.getFileName());
	        System.out.println("===> "+gameplayphase);
	        System.out.println(riskMap);
	        
	        playerList.addAll(gameplayphase.getGameState());
	        
	        txtCommandLine.clear();
	        txtConsoleLog.clear();
	        
	        playerName = playerList.get(playerIndex).getPlayerName();
	        playerReinforceArmy = riskPlayImpl.checkForReinforcement(playerList.get(playerIndex).getPlayerterritories().size());
	        
	        
	        turnStartedMsg = playerName +"'s turn is started";
	        leftArmyMsg = "You have "+playerReinforceArmy+" armies to Reinforcement";
	        sb.append(turnStartedMsg).append(NEWLINE).append(leftArmyMsg).append(NEWLINE);
	    
	        txtConsoleLog.setText(sb.toString());
	        
	        
	        
	        
	    }

}
