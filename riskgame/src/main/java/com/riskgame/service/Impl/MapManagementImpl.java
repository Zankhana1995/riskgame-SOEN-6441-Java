package com.riskgame.service.Impl;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import com.riskgame.dto.ContinentDto;
import com.riskgame.dto.CountryDto;
import com.riskgame.dto.NeighbourTerritoriesDto;
import com.riskgame.model.Continent;
import com.riskgame.model.RiskMap;
import com.riskgame.model.Territory;
import com.riskgame.service.MapManagementInterface;

/**
 * @author <a href="mailto:z_tel@encs.concordia.ca">Zankhanaben Patel</a>
 * @see com.riskgame.model.RiskMap
 * @see com.riskgame.model.Continent
 * @see com.riskgame.model.Territory
 */

@Service
public class MapManagementImpl implements MapManagementInterface {
	
	public static final String NEW_LINE = "line.separator";
	public static final String MAP_DIR_PATH = "src/main/resources/maps/";

	public RiskMap convertToRiskMap(List<ContinentDto> continentList, List<CountryDto> countryList,List<NeighbourTerritoriesDto> neighbourList) {
		RiskMap riskMap = new RiskMap();

		int continentIndex = 1;
		int territoryIndex = 1;
		Map<Integer, Continent> continentMap = new HashMap<Integer, Continent>();
		Continent continent = null;
		Territory territory = null;
		
		for (ContinentDto continentDto : continentList) {
			continent = new Continent();
			continent.setContinentName(continentDto.getContinentName());
			continent.setContinentValue(continentDto.getContinentValue());
			
			for (CountryDto countryDto : countryList) {
				
				if(countryDto.getContinentName().equalsIgnoreCase(continentDto.getContinentName())) {
					
					territory = new Territory();
					territory.setTerritoryIndex(territoryIndex);
					territoryIndex++;
					territory.setContinentIndex(continentIndex);
					territory.setTerritoryName(countryDto.getCountryName());
					territory.setXAxis(0);
					territory.setYAxis(0);
					List<String> neighbourTerritory = new ArrayList<String>();
					
					for (NeighbourTerritoriesDto neighbourTerritoriesDto : neighbourList) {
						if(countryDto.getCountryName().equalsIgnoreCase(neighbourTerritoriesDto.getCountryName())) {
							neighbourTerritory.add(neighbourTerritoriesDto.getCountryNeighbourName());
						}
					}
					
					territory.setNeighbourTerritories(neighbourTerritory);
					
					continent.getTerritoryList().add(territory);
					
				}
				
			}
			
			continent.setContinentIndex(continentIndex);
			continentMap.put(continentIndex, continent);
			continentIndex++;

		}
		riskMap.setContinents(continentMap);

		return riskMap;
	}

	@Override
	public Map<String, Object> convertRiskMapToDtos(RiskMap riskMap) {
		
		Map<String,Object> map = new HashMap<String, Object>();
		List<ContinentDto> continentDtoList = new ArrayList<ContinentDto>();
		List<CountryDto> countryDtoList = new ArrayList<CountryDto>();
		List<NeighbourTerritoriesDto> neighbourTerritoriesDtoList = new ArrayList<NeighbourTerritoriesDto>();
		ContinentDto continentDto = null;
		CountryDto countryDto = null;
		NeighbourTerritoriesDto territoriesDto = null;
		Map<Integer,Continent> continentMap = riskMap.getContinents();
		int neighboutIndex = 1;
		for (Map.Entry<Integer,Continent> entry : continentMap.entrySet()) {
			
			continentDto = new ContinentDto();
			Continent continent = entry.getValue();
			continentDto.setId(continent.getContinentIndex());
			continentDto.setContinentName(continent.getContinentName());
			continentDto.setContinentValue(continent.getContinentValue());
			continentDtoList.add(continentDto);
			
			List<Territory> territoriList = continent.getTerritoryList();
			for (Territory territory : territoriList) {
				
				countryDto = new CountryDto();
				countryDto.setId(territory.getTerritoryIndex());
				countryDto.setCountryName(territory.getTerritoryName());
				countryDto.setContinentName(continent.getContinentName());
				countryDtoList.add(countryDto);
				
				List<String> neighbourList = territory.getNeighbourTerritories();
				if(neighbourList != null && !neighbourList.isEmpty()) {
					
					for (String neighbourName : neighbourList) {
						territoriesDto = new NeighbourTerritoriesDto();
						territoriesDto.setCountryName(territory.getTerritoryName());
						territoriesDto.setCountryNeighbourName(neighbourName);
						territoriesDto.setId(neighboutIndex);
						neighboutIndex++;
						neighbourTerritoriesDtoList.add(territoriesDto);
					}
					
				}
				
				
			}
			
			
		}
		
		map.put("ContinentList", continentDtoList);
		map.put("CountryList", countryDtoList);
		map.put("NeighbourList", neighbourTerritoriesDtoList);
		
		
		return map;
	}
	

	@Override
	public List<String> getAvailableMap() {
		List<String> mapList = new ArrayList<String>();
		
		try (Stream<Path> path = Files.walk(Paths.get(MAP_DIR_PATH))) {

			mapList = path.map(filePath -> filePath.toFile().getName()).filter(fileName -> fileName.endsWith(".map")).collect(Collectors.toList());

		} catch (IOException e) {
			e.printStackTrace();
		}

		return mapList;
	}

	@Override
	public boolean saveMapToFile(RiskMap riskMap)throws UnsupportedEncodingException, FileNotFoundException, IOException {
		boolean result = false;

		System.out.println(riskMap);

		StringBuilder sbContinent = new StringBuilder("[continents]").append(System.getProperty(NEW_LINE));
		StringBuilder sbCountry = new StringBuilder("[countries]").append(System.getProperty(NEW_LINE));
		StringBuilder sbNeighbour = new StringBuilder("[borders]").append(System.getProperty(NEW_LINE));

		try (PrintWriter writer = new PrintWriter(new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(MAP_DIR_PATH + riskMap.getMapName()), "utf-8")))) {

			Map<Integer, Continent> continentMap = riskMap.getContinents();

			for (Map.Entry<Integer, Continent> entry : continentMap.entrySet()) {

				Continent continent = entry.getValue();

				sbContinent.append(continent.getContinentName() + " " + continent.getContinentValue() + " " + "none").append(System.getProperty(NEW_LINE));

				List<Territory> territoriList = continent.getTerritoryList();
				for (Territory territory : territoriList) {

					if (territory != null) {

						sbCountry.append(territory.getTerritoryIndex() + " " + territory.getTerritoryName() + " "
										+ territory.getContinentIndex() + " " + "0" + " " + "0")
								.append(System.getProperty(NEW_LINE));

						List<String> neighbourList = territory.getNeighbourTerritories();
						if (neighbourList != null && !neighbourList.isEmpty()) {

							sbNeighbour.append(territory.getTerritoryIndex());
							for (String neighbourName : neighbourList) {
								int neighbourIndex = getNeighbourIndexFromMap(riskMap, neighbourName);
								sbNeighbour.append(" " + neighbourIndex);
							}
							sbNeighbour.append(System.getProperty(NEW_LINE));

						}

					}

				}

			}
			
			
			writer.println("; Yura Mamyrin");
			writer.println("; Risk Map");
			writer.println();
			writer.println("; Risk "+riskMap.getMapName()+" Game Map");
			writer.println("; Dimensions: 677 x 425 Pixels");
			writer.println();
			writer.println("; Made by Zankhanaben Patel");
			writer.println(";         Koshaben Patel");
			writer.println(";         Piyush Thummar");
			writer.println(";         Raj Mistry");
			writer.println(";         Jaswanth Banavathu");
			writer.println();
			writer.println("name "+riskMap.getMapName()+" map");
			writer.println();
			writer.println("[files]");
			writer.println("pic world_pic.jpg");
			writer.println("map world_map.gif");
			writer.println("crd risk.cards");
			writer.println("prv world.jpg");
			writer.println();
			
			writer.println(sbContinent.toString());
			writer.println(sbCountry.toString());
			writer.println(sbNeighbour.toString());
			
		}

		return result;
	}
	
	private int getNeighbourIndexFromMap(RiskMap riskMap, String neighbourName) {
		int index = 0;

		Map<Integer, Continent> continentMap = riskMap.getContinents();

		for (Map.Entry<Integer, Continent> entry : continentMap.entrySet()) {

			Continent continent = entry.getValue();

			List<Territory> territoriList = continent.getTerritoryList();
			for (Territory territory : territoriList) {

				if (territory != null) {

					if (neighbourName.equalsIgnoreCase(territory.getTerritoryName())) {
						index = territory.getTerritoryIndex();
						break;
					}

				}

			}

		}

		return index;

	}
	
	
	
	

}
