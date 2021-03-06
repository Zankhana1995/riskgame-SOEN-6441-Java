/**
 * 
 */
package com.riskgame.strategy.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import com.riskgame.model.GamePlayPhase;
import com.riskgame.model.Player;
import com.riskgame.model.PlayerTerritory;
import com.riskgame.model.RiskCard;
import com.riskgame.service.MapManagementInterface;
import com.riskgame.service.RiskPlayInterface;
import com.riskgame.service.Impl.MapManagementImpl;
import com.riskgame.service.Impl.RiskPlayImpl;
import com.riskgame.strategy.StrategyInterface;

/**
 * Concrete implementation of Strategy interface of design pattern for Random
 * strategy of player
 * 
 * @author <a href="mailto:z_tel@encs.concordia.ca">Zankhanaben Patel</a>
 * @see com.riskgame.strategy
 * @version 1.0.0
 */
public class RandomPlayer implements StrategyInterface {

	public static StringBuilder sb;
	private static String NEWLINE = System.getProperty("line.separator");

	private MapManagementInterface mapManagementImpl;
	private RiskPlayInterface riskPlayImpl;

	/**
	 * Constructor which will initialize object of services
	 */
	public RandomPlayer() {
		mapManagementImpl = new MapManagementImpl();
		riskPlayImpl = new RiskPlayImpl();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.riskgame.strategy.StrategyInterface#reinforce(com.riskgame.model.GamePlayPhase)
	 */
	@Override
	public GamePlayPhase reinforce(GamePlayPhase gamePlayPhase) {
		sb = new StringBuilder();
		//List<String> countriesList = new ArrayList<String>();

		for (Player player : gamePlayPhase.getPlayerList()) {

			if (player.getPlayerId() == gamePlayPhase.getCurrentPlayerId()) {

				if (player.getPlayerterritories().size() > 0) {

					int randomIndex = riskPlayImpl.generateRandomIntRange(0, player.getPlayerterritories().size() - 1);

					PlayerTerritory playerTerritory = player.getPlayerterritories().get(randomIndex);
					sb.append("Before reinforce Territories :").append(playerTerritory.getTerritoryName())
							.append("Army :").append(playerTerritory.getArmyOnterritory()).append(NEWLINE);
					playerTerritory
							.setArmyOnterritory(playerTerritory.getArmyOnterritory() + player.getPlayerReinforceArmy());
					sb.append("After reinforce Territories :").append(playerTerritory.getTerritoryName())
							.append("Army :").append(playerTerritory.getArmyOnterritory()).append(NEWLINE);
					player.setPlayerReinforceArmy(0);
					break;
				}

			}
		}

		gamePlayPhase.setStatus(sb.toString());
		System.out.println("RandomPlayer reinforce - "+gamePlayPhase.getStatus());
		return gamePlayPhase;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.riskgame.strategy.StrategyInterface#attack(com.riskgame.model.GamePlayPhase)
	 */
	@Override
	public GamePlayPhase attack(GamePlayPhase gamePlayPhase) {

		Player currentPlayer = null;
		sb = new StringBuilder();
		List<String> playerCountry = new ArrayList<>();
		String fromCountry = null;
		String toCountry = null;
		for (Player player : gamePlayPhase.getPlayerList()) {
			if (player.getPlayerId() == gamePlayPhase.getCurrentPlayerId()) {
				currentPlayer = player;
				for (PlayerTerritory territory : player.getPlayerterritories()) {
					playerCountry.add(territory.getTerritoryName());
				}
				break;
			}
		}

		if (currentPlayer.getPlayerterritories().size() > 0) {
			int randomNumberAttack = riskPlayImpl.generateRandomIntRange(1,
					currentPlayer.getPlayerterritories().size());
			for (int index = 0; index < randomNumberAttack; index++) {

				PlayerTerritory playerTerritory = currentPlayer.getPlayerterritories()
						.get(riskPlayImpl.generateRandomIntRange(0, currentPlayer.getPlayerterritories().size() - 1));

				if (playerTerritory.getArmyOnterritory() > 1) {

					fromCountry = playerTerritory.getTerritoryName();

					List<String> neighbourCountriesList = mapManagementImpl
							.getNeighbourCountriesListByCountryName(gamePlayPhase.getRiskMap(), fromCountry);
					for (String neighbourCountry : neighbourCountriesList) {
						if (!playerCountry.contains(neighbourCountry)) {
							toCountry = neighbourCountry;
							break;
						}
					}
					if (toCountry != null) {

						int armyOnFromC = playerTerritory.getArmyOnterritory();
						int armyOnToC = riskPlayImpl.getCurrentAramyByCountryName(toCountry,
								gamePlayPhase.getPlayerList());

						int attackerDice = riskPlayImpl.getAttackerDiesCount(armyOnFromC);
						int defenderDice = riskPlayImpl.getDefenderDiceCount(armyOnToC);

						if (attackerDice > 0 && defenderDice > 0) {

							List<Integer> attackerList = riskPlayImpl.getCountFromDies(attackerDice);
							List<Integer> defenderList = riskPlayImpl.getCountFromDies(defenderDice);
							int min = Math.min(attackerList.size(), defenderList.size());

							for (int i = 0; i < min; i++) {

								if (defenderList.get(i) == attackerList.get(i)) {
									updateArmyAfterBattle(fromCountry, "attacker", gamePlayPhase);
								} else if (defenderList.get(i) > attackerList.get(i)) {
									updateArmyAfterBattle(fromCountry, "attacker", gamePlayPhase);
								} else if (defenderList.get(i) < attackerList.get(i)) {
									updateArmyAfterBattle(toCountry, "defender", gamePlayPhase);
								}
							}

							Player defenderPlayer = riskPlayImpl.getPlayerByCountry(toCountry,
									gamePlayPhase.getPlayerList());
							PlayerTerritory toTerritory = riskPlayImpl.getPlayerTerritoryByCountryName(toCountry,
									defenderPlayer);

							if (toTerritory.getArmyOnterritory() == 0) {

								// allOutTerritoryConqured = true;
								// attackMove = true;
								// alloutFinish = true;

								moveCountryToWinPlayer(fromCountry, toCountry, gamePlayPhase);
								Player attackerPlayer = riskPlayImpl.getPlayerByCountry(fromCountry,
										gamePlayPhase.getPlayerList());
								PlayerTerritory fromTerritory = riskPlayImpl
										.getPlayerTerritoryByCountryName(fromCountry, attackerPlayer);

								if (fromTerritory.getArmyOnterritory() > 1) {
									int armyToMove = fromTerritory.getArmyOnterritory() - 1;
									moveArmy(fromCountry, toCountry, armyToMove, gamePlayPhase);
								}

								if (gamePlayPhase.getRiskCardList().size() > 0) {
									RiskCard riskCard = gamePlayPhase.getRiskCardList().get(0);
									currentPlayer.getCardListOwnedByPlayer().add(riskCard);
									sb.append(riskCard).append(" Assigned to ").append(currentPlayer.getPlayerName())
											.append(NEWLINE);
									gamePlayPhase.getRiskCardList().remove(0);

								}
							}

						} else {
							// alloutFinish = true;
							sb.append("Attack not possible - not an valid army on country").append(NEWLINE);
						}

					} else {
						sb.append(
								"Attack is not possible because player's countries all neighbour countries are part of player's country!");
					}

				} else {

					sb.append("Can't attack because selected territory : ").append(playerTerritory.getTerritoryName())
							.append(" has only ").append(playerTerritory.getArmyOnterritory()).append(" army ")
							.append(NEWLINE);
				}

			}

		}

		gamePlayPhase.setGamePhase("ATTACK");
		riskPlayImpl.checkForWinner(gamePlayPhase);
		gamePlayPhase.setStatus(sb.toString() + gamePlayPhase.getStatus());
		System.out.println("RandomPlayer attack - "+gamePlayPhase.getStatus());
		return gamePlayPhase;

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.riskgame.strategy.StrategyInterface#fortify(com.riskgame.model.GamePlayPhase)
	 */
	@Override
	public GamePlayPhase fortify(GamePlayPhase gamePlayPhase) {
		Player currentPlayer = null;
		sb = new StringBuilder();
		List<String> playerCountry = new ArrayList<>();
		//String fromCountry = null;
		//String toCountry = null;
		for (Player player : gamePlayPhase.getPlayerList()) {
			if (player.getPlayerId() == gamePlayPhase.getCurrentPlayerId()) {
				currentPlayer = player;

				for (PlayerTerritory territory : player.getPlayerterritories()) {
					playerCountry.add(territory.getTerritoryName());
				}
				break;
			}
		}

		if (currentPlayer.getPlayerterritories().size() >= 2) {

			PlayerTerritory playerTerritory = currentPlayer.getPlayerterritories()
					.get(riskPlayImpl.generateRandomIntRange(0, currentPlayer.getPlayerterritories().size() - 1));

			if (playerTerritory.getArmyOnterritory() > 1) {

				List<String> neighbourCountriesList = mapManagementImpl.getNeighbourCountriesListByCountryName(
						gamePlayPhase.getRiskMap(), playerTerritory.getTerritoryName());

				List<String> toCountries = new ArrayList<String>();
				for (String neighbourCountry : neighbourCountriesList) {
					if (playerCountry.contains(neighbourCountry)) {
						toCountries.add(neighbourCountry);
					}
				}

				if (toCountries.size() > 0) {

					int randomDestinationIndex = riskPlayImpl.generateRandomIntRange(0, toCountries.size() - 1);

					String destinationCountry = toCountries.get(randomDestinationIndex);

					playerTerritory.setArmyOnterritory(playerTerritory.getArmyOnterritory() - 1);
					for (PlayerTerritory territory : currentPlayer.getPlayerterritories()) {
						if (territory.getTerritoryName().equalsIgnoreCase(destinationCountry)) {
							territory.setArmyOnterritory(territory.getArmyOnterritory() + 1);
							sb.append("Moved 1 army from ").append(playerTerritory.getTerritoryName()).append(" to ")
									.append(destinationCountry).append(NEWLINE);
						}
					}

				} else {
					sb.append("Fortificaition not possible for random player : not destination country")
							.append(NEWLINE);
				}

			} else {

				sb.append("Fortificaition not possible for random player : not enough army on "
						+ playerTerritory.getTerritoryName()).append(NEWLINE);

			}

		} else {

			sb.append("Fortificaition not possible for random player").append(NEWLINE);
		}

		gamePlayPhase.setStatus(sb.toString());
		System.out.println("RandomPlayer fortify - "+gamePlayPhase.getStatus());
		return gamePlayPhase;
	}

	/**
	 * This method will update number of army from attcker or defender territory
	 * after battle completed everytime
	 * 
	 * @param country
	 * @param name    attcker or defender
	 */
	private GamePlayPhase updateArmyAfterBattle(String country, String name, GamePlayPhase gamePlayPhase) {
		// sb = new StringBuilder();
		for (Player player : gamePlayPhase.getPlayerList()) {

			for (PlayerTerritory playerTerritory : player.getPlayerterritories()) {

				if (country.equalsIgnoreCase(playerTerritory.getTerritoryName())) {

					playerTerritory.setArmyOnterritory(playerTerritory.getArmyOnterritory() - 1);
					player.setArmyOwns(player.getArmyOwns() - 1);

					//sb.append(name).append(" Country loses 1 army ").append(name).append(" has left with ").append(playerTerritory.getArmyOnterritory()).append(NEWLINE);

					break;

				}
			}
		}
		gamePlayPhase.setStatus(gamePlayPhase.getStatus() + sb.toString());
		return gamePlayPhase;
	}

	/**
	 * This method will move conquered country to attacker
	 * 
	 * @param fromCountryAttack
	 * @param toCountryAttack
	 */
	private GamePlayPhase moveCountryToWinPlayer(String fromCountryAttack, String toCountryAttack,
			GamePlayPhase gamePlayPhase) {
		// sb = new StringBuilder();
		Player player = riskPlayImpl.getPlayerByCountry(toCountryAttack, gamePlayPhase.getPlayerList());
		PlayerTerritory playerTerritory = riskPlayImpl.getPlayerTerritoryByCountryName(toCountryAttack, player);

		// for Adding country
		for (Player playerFromLst : gamePlayPhase.getPlayerList()) {

			ListIterator<PlayerTerritory> playerTerritories = playerFromLst.getPlayerterritories().listIterator();

			while (playerTerritories.hasNext()) {
				if (playerTerritories.next().getTerritoryName().equalsIgnoreCase(fromCountryAttack)) {
					playerTerritories.add(playerTerritory);
				}
			}
		}
		

		// Fore removing country
		for (Player playertoLst : gamePlayPhase.getPlayerList()) {

			ListIterator<PlayerTerritory> playerTerritories = playertoLst.getPlayerterritories().listIterator();

			while (playerTerritories.hasNext()) {
				if (playerTerritories.next().getTerritoryName().equalsIgnoreCase(playerTerritory.getTerritoryName()) && player.getPlayerId()==playertoLst.getPlayerId()) {
					playerTerritories.remove();
				}
			}
		}
		
		sb.append(toCountryAttack).append(" country has been conquered ").append(NEWLINE);
		return gamePlayPhase;
	}

	/**
	 * This method will move army from attcker country to conqured country
	 * 
	 * @param fromCountryAttack
	 * @param toCountryAttack
	 * @param armyToMove
	 */
	private GamePlayPhase moveArmy(String fromCountryAttack, String toCountryAttack, int armyToMove,
			GamePlayPhase gamePlayPhase) {
		// sb = new StringBuilder();
		for (Player player : gamePlayPhase.getPlayerList()) {

			for (PlayerTerritory playerTerritory : player.getPlayerterritories()) {

				if (toCountryAttack.equalsIgnoreCase(playerTerritory.getTerritoryName())) {

					playerTerritory.setArmyOnterritory(playerTerritory.getArmyOnterritory() + armyToMove);

					sb.append(armyToMove).append(" Army Moved Successfully on ").append(toCountryAttack)
							.append(" country").append(NEWLINE);

				} else if (fromCountryAttack.equalsIgnoreCase(playerTerritory.getTerritoryName())) {

					playerTerritory.setArmyOnterritory(playerTerritory.getArmyOnterritory() - armyToMove);

				}
			}
		}

		gamePlayPhase.setStatus(gamePlayPhase.getStatus() + sb.toString());
		return gamePlayPhase;
	}

}
