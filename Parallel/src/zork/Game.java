package zork;

import zork.proto.Exit;
import zork.proto.Inventory;
import zork.proto.Item;
import zork.proto.Player;
import zork.proto.Room;
import zork.proto.Character;
import zork.threads.CommandListener;
import zork.threads.DialogueThread;
import zork.utils.CommandLoader;
import zork.utils.MinigameLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Game {

	public static HashMap<String, Room> roomMap = new HashMap<String, Room>();
	public static HashMap<String, Item> itemList = new HashMap<String, Item>();
	public static HashMap<String, Character> characterList = new HashMap<String, Character>();

    public static Player player;

    private static Game game = new Game();

    private static final Thread cmdListener = new CommandListener(), dialogueThread = new DialogueThread();

	static final String GREEN_TEXT = "\033[1;32m", RESET = "\033[0m", RED_TEXT = "\033[1;31m";

	/**
	 * Create the game and initialise its internal map.
	 */
	public Game() {
		try {
            CommandLoader.init();
            MinigameLoader.init();
			initCharacters();
			initItems();
			initRooms();

            player = new Player("userRoom",100);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Game getCurrentGame() {
		return game;
	}

	private void initCharacters() throws Exception {
		Path path = Path.of("src\\zork\\data\\npc.json");
		String jsonString = Files.readString(path);
		JSONParser parser = new JSONParser();
		JSONObject json = (JSONObject) parser.parse(jsonString);

		JSONArray jsonItems = (JSONArray) json.get("characters");

		for (Object characterObj : jsonItems) {
			String characterName = (String) ((JSONObject) characterObj).get("name");
			String characterId = (String) ((JSONObject) characterObj).get("id");
			String characterDescription = (String) ((JSONObject) characterObj).get("description");

			Character character = new Character(characterId, characterName, characterDescription);

			characterList.put(characterId, character);
		}
	}

	private void initItems() throws Exception {
		Path path = Path.of("src\\zork\\data\\items.json");
		String jsonString = Files.readString(path);
		JSONParser parser = new JSONParser();
		JSONObject json = (JSONObject) parser.parse(jsonString);

		JSONArray jsonItems = (JSONArray) json.get("items");

		for (Object itemObj : jsonItems) {
			String itemName = (String) ((JSONObject) itemObj).get("name");
			String itemId = (String) ((JSONObject) itemObj).get("id");
			Double itemWeight = Double.parseDouble(((JSONObject) itemObj).get("weight") + "");
			Boolean isOpenable = (Boolean) ((JSONObject) itemObj).get("isOpenable");
			Boolean needsContext = (Boolean) ((JSONObject) itemObj).get("needsContext");
			Item item = new Item(itemName, itemWeight, isOpenable, needsContext);

			item.setId(itemId);
			itemList.put(itemId, item);
		}
	}

	private void initRooms() throws Exception {
		Path path = Path.of("src\\zork\\data\\rooms.json");
		String jsonString = Files.readString(path);
		JSONParser parser = new JSONParser();
		JSONObject json = (JSONObject) parser.parse(jsonString);

		JSONArray jsonRooms = (JSONArray) json.get("rooms");

		for (Object roomObj : jsonRooms) {
			Room room = new Room();
			String roomName = (String) ((JSONObject) roomObj).get("name");
			String roomId = (String) ((JSONObject) roomObj).get("id");
			int roomCapacity = Integer.parseInt(String.valueOf(((JSONObject) roomObj).get("capacity")));
			String roomDescription = (String) ((JSONObject) roomObj).get("description");
			room.setDescription(roomDescription);
			room.setRoomName(roomName);

			JSONArray jsonCharacters = (JSONArray) ((JSONObject) roomObj).get("npcs");
			ArrayList<Character> characters = new ArrayList<Character>();
			for(Object charObj : jsonCharacters) {
				if(((JSONObject) charObj).get("id") != null) { characters.add(characterList.get((String) ((JSONObject) charObj).get("id"))); }
			}
			room.setCharacters(characters);

			JSONArray jsonItems = (JSONArray) ((JSONObject) roomObj).get("items");
			Inventory roomItems = new Inventory(roomCapacity);
			for(Object itemObj : jsonItems) {
				if(((JSONObject) itemObj).get("id") != null) {
					roomItems.addItem(itemList.get((String) ((JSONObject) itemObj).get("id")));
				}
			}
			room.setRoomItems(roomItems);

			JSONArray jsonExits = (JSONArray) ((JSONObject) roomObj).get("exits");
			ArrayList<Exit> exits = new ArrayList<Exit>();
			for (Object exitObj : jsonExits) {
				String direction = (String) ((JSONObject) exitObj).get("direction");
				String adjacentRoom = (String) ((JSONObject) exitObj).get("adjacentRoom");
				String keyId = (String) ((JSONObject) exitObj).get("keyId");
				Boolean isLocked = (Boolean) ((JSONObject) exitObj).get("isLocked");
				Boolean isOpen = (Boolean) ((JSONObject) exitObj).get("isOpen");
				Exit exit = new Exit(direction, adjacentRoom, isLocked, keyId, isOpen);
				exits.add(exit);
			}
			room.setExits(exits);
			roomMap.put(roomId, room);
		}
	}

	/**
	 * Main play routine. Loops until end of play.
	 */
	public void play() throws InterruptedException {
		printWelcome();
		player.setPlayerName();
		print("Hey there, "+player.getPlayerName()+". Get ready for an eventful stretch of time coming your way.");
		printStory();
		print(Game.player.getCurrentRoom().longDescription());
		
        cmdListener.start();
		dialogueThread.start();		
	}

	private void printStory() throws InterruptedException {
		System.out.println();
		print("You went on a trip with your friends Brent and Glenn to a fancy tropical resort.");
		print("However, you woke up this morning,");
		print("and something was off........");
		print("Travel around to see what's going on.");
		System.out.println();
	}

	/**
	 * Print out the opening message for the player.
	 */
	private void printWelcome() throws InterruptedException{
		;System.out.println();
		Thread.sleep(1000);System.out.println("Welcome to Whodunit!");
		Thread.sleep(1000);System.out.println("Whodunit is a murder mystery game in which you find clues and crack a case.");
		Thread.sleep(1500);System.out.println("Type 'help' if you need help.");
		Thread.sleep(1000);System.out.println();
	}

	public static void dialogueLoop(String id) {
		if(id.equals("police")) {
			print("Hey there /p, I was looking to speak with you.");
			print("I heard your buddy died and came running here.");
			print("So far, we don't know much about the case, but it might be");
			print("helpful to speak with the /rreceptionist/g about accessing Glenn's room.");
		}
		if(id.equals("receptionist")) {
			print("Hi /p! The officer over there told me to give you this.");
			print("/bThe receptionist gave you Glenn's Room Key!");
			print("Use this to continue your investigation into your friend's murder.");
			print("I feel super bad for you, but I'm afraid this is all I can help with.");
			print("I'll call you if I ever find out more information.");
		}
	}

	public static void print(String line) {
		line = playerEscapeSequence(line);
		line = redEscapeSequence(line);
		line = greenEscapeSequence(line);
		line = resetEscapeSequence(line);

		try {
			System.out.print(GREEN_TEXT);

			for (int i = 0; i < line.length(); i++) {
				System.out.print(line.charAt(i));
				Thread.sleep(20);
			}
			System.out.println(RESET);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static String resetEscapeSequence(String line) {
		while(line.indexOf("/b") != -1) {
			int p = line.indexOf("/b");
			line = line.substring(0, p) + RESET + line.substring(p + 2);
		}

		return line;
	}

	private static String greenEscapeSequence(String line) {
		while(line.indexOf("/g") != -1) {
			int p = line.indexOf("/g");
			line = line.substring(0, p) + GREEN_TEXT + line.substring(p + 2);
		}

		return line;
	}

	private static String redEscapeSequence(String line) {
		while(line.indexOf("/r") != -1) {
			int p = line.indexOf("/r");
			line = line.substring(0, p) + RED_TEXT + line.substring(p + 2);
		}

		return line;
	}

	public static String playerEscapeSequence(String line) {
		while(line.indexOf("/p") != -1) {
			int p = line.indexOf("/p");
			line = line.substring(0, p) + player.getPlayerName() + line.substring(p + 2);
		}

		return line;
	}
}