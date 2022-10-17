package nodes;

import data.LootTable;
import helper.Mouseclick;
import helper.ScreenBoundingModel;
import logic.Restocking;
import logic.Wilderness;
import logic.WorldHopping;
import org.powbot.api.*;
import org.powbot.api.rt4.*;
import script.LavaDragon;
import java.util.List;

public class FightDragons extends Node {

	private static final Tile[] safeSpotTilesDragon1 = new Tile[]{new Tile(3222, 3845, 0), new Tile(3222, 3846, 0), new Tile(3222, 3847, 0), new Tile(3222, 3848, 0)};
	private static final Tile[] safeSpotTilesDragon2 = new Tile[]{new Tile(3214, 3835, 0), new Tile(3214, 3836, 0), new Tile(3215, 3835, 0), new Tile(3215, 3836, 0)};

	private static final Area dragon1 = new Area(new Tile(3204, 3854, 0), new Tile(3223, 3842, 0));
	private static final Area dragon2 = new Area(new Tile(3199, 3, 0), new Tile(3216, 3830, 0));
	private static Area currentDragon = dragon1;


	public static boolean retreating = false;

	public FightDragons(LavaDragon e) {
		super(e);

	}

	public static Tile[] getCorrespondingSafeSpotTiles() {
		if (dragon1 == currentDragon) {
			return safeSpotTilesDragon1;
		}
		if (dragon2 == currentDragon) {
			return safeSpotTilesDragon2;
		}
		System.out.println("Error retrieving safespot tiles");
		return null;
	}

	public static Locatable getRandomSafeSpotTile() {
		Tile[] tiles = getCorrespondingSafeSpotTiles();
		return tiles[Random.nextInt(0, tiles.length)];
	}

	/*private boolean isDragonNotValid(){
		return Npcs.stream().name("Lava Dragon").within(currentDragon).count() == 0;
	}
	*/

	@Override
	public boolean validate() {
		return Wilderness.isAtDragons() &&
				hasSufficientInventory() &&
				Restocking.hasFood() &&
				!retreating &&
				!Wilderness.isBeingPked() &&
				Restocking.getCharges() > 0;
	}

	private boolean hasSufficientInventory() {
		return !Inventory.isFull() ||
				!Restocking.islootingBagIsFull() ||
				(Inventory.isFull() && !Restocking.islootingBagIsFull()) ||
				(Inventory.isFull() && Restocking.islootingBagIsFull() && Restocking.hasFood());

	}

	private static boolean hasDiscoveredSafespotTile() {
		return Players.local().interacting().valid() && Npcs.stream().name("Lava dragon").interactingWithMe().first().valid() &&
				!Players.local().healthBarVisible() &&
				(Npcs.stream().name("Lava dragon").interactingWithMe().first().animation() == -1 || Npcs.stream().name("Lava dragon").interactingWithMe().first().animation() == 89) &&
				!Npcs.stream().name("Lava dragon").interactingWithMe().first().inMotion();
	}

	@Override
	public void execute() {
		if (isThereLootOnFloor() && !isSafeSpotting()) {
			checkLoot();
		}
		if (!isStandingOnSafeSpot() && !isThereLootOnFloor() && !retreating && !isSafeSpotting()) {
			System.out.println("Going to safespot");
			goToSafeSpot();
		}
		if(isStandingOnSafeSpot() && !isSafeSpotting()){
			if(Restocking.isUsingPotions() && (Restocking.shouldDrinkPotion() || Inventory.stream().name("Divine magic potion(4)").count() == 1)) {
				System.out.println("Drinking potion");
				drinkPotion();
			}
			if(Restocking.needHeal()){
				Restocking.heal(true);
				if(Restocking.needHeal()){
					Condition.sleep(Random.nextInt(350,750));
				}
			}
				attackDragon();
		}
		if (isSafeSpotting()) {
			sleep();
		}
	}

	private void drinkPotion() {
		if (Combat.health() > 10) {
			if(Game.closeOpenTab()){
				if (Game.tab(Game.Tab.INVENTORY)) {
					if (Inventory.stream().filtered(e -> e.name().contains("Divine magic potion")).first().valid()) {
						int heal = Players.local().healthPercent();
						if (Inventory.stream().filtered(e -> e.name().contains("Divine magic potion")).first().click("Drink")) {
							System.out.println("Successfully drank the potion");
							Restocking.shouldDrinkPotion(false);
							if (Condition.wait(() -> Players.local().healthPercent() < heal, 800, 5)) {
								if (Restocking.needHeal()) {
									Restocking.heal(true);
									//Condition.sleep(Random.nextInt(600,750));
								}
							}
						}
					}
				}
			}
		}
	}

	private Area calculateDragonLootSpot() {
		return currentDragon;
	}

	private boolean isThereLootOnFloor() {
		return GroundItems.stream().within(currentDragon).filtered(e -> LootTable.getLootTable().containsKey(e.name()) && !e.name().equals(Restocking.getFoodType())).count() != 0;
	}

	private void checkLoot() {
		script.setStatus("Looting.");
		Area toLoot = calculateDragonLootSpot();
		List<GroundItem> items = GroundItems.stream().within(toLoot).filtered(e -> LootTable.getLootTable().containsKey(e.name()) && !e.name().equals(Restocking.getFoodType())).list();
		if (Restocking.hasEnoughInventorySpace(items.size()) != 0 && Restocking.hasFood()) {
			while (Restocking.needHeal() && !Wilderness.attackAblePlayersNearby()) {
				int health = Combat.health();
				Restocking.heal(true);
				Condition.wait(() -> health < Combat.health() || Wilderness.attackAblePlayersNearby(), 60, 20);
				if (Restocking.needHeal() && !Wilderness.attackAblePlayersNearby())
					Condition.sleep(Random.nextInt(100, 500));
			}
			if (Restocking.hasEnoughInventorySpace(items.size()) != 0) {
				drop(Restocking.hasEnoughInventorySpace(items.size()));
			}
		}
		if (WorldHopping.isWorldOpen()) {
			if (items.size() > 0) {
				if (Movement.energyLevel() > 30) Movement.running(true);
				for (GroundItem x : items) {
					if (Inventory.isFull() || Wilderness.attackAblePlayersNearby()) {
						break;
					}
					while (x.valid() && !Inventory.isFull() && !Wilderness.attackAblePlayersNearby()) {
						long check = Inventory.stream().name(x.name()).count();
						if (!ScreenBoundingModel.screen.contains(x.centerPoint())) {
							dragScreen();
						} else {
							if (x.click("Take", x.name())) {
								System.out.println("Picking up: " + x.name());
								Condition.wait(() -> Inventory.stream().name(x.name()).count() == check + 1 || !x.valid() || Wilderness.attackAblePlayersNearby(), 80, 60);
							} else {
								x.click("Take", x.name());
							}
						}
					}
				}
				if (Inventory.stream().name(Restocking.getFoodType()).count() <= 2 && !isThereLootOnFloor() && Inventory.isFull()) {
					retreating = true;
				} else {
					retreating = false;
				}
			}
		}
	}

	private void drop(int dropQuantity) {
		int howManyToDrop = dropQuantity - (28 - Inventory.get().size());
		script.setStatus("Dropping food.");
		System.out.println("Need to  drop " + howManyToDrop + " to be able to pickup all loot");
		List<Item> food = Inventory.stream().name(Restocking.getFoodType()).list();
		if (food.size() > dropQuantity) {
			System.out.println("We have: " + food.size() + " more " + Restocking.getFoodType() + "to drop");
			Inventory.drop(food.subList(0, dropQuantity));
		} else {
			System.out.println("Need dont have " + howManyToDrop + " " + Restocking.getFoodType() + "to drop");
			System.out.println("We only have " + food.size());
			Inventory.drop(food);
		}
	}

	public void goToSafeSpot() {
		if (Restocking.needHeal()) Restocking.heal(true);
		Tile random;
		script.setStatus("Walking to safespot.");
		random = getRandomSafeSpotTile().tile();
		if (ScreenBoundingModel.screen.contains(random.matrix().centerPoint())) {
			if (random.matrix().interact("Walk here")) {
				Condition.wait(() -> isStandingOnSafeSpot() || Wilderness.attackAblePlayersNearby() || Restocking.needHeal(), 150, 25);
			}else{
				WebWalking.moveTo(random, false, () -> Wilderness.attackAblePlayersNearby() ||  !Wilderness.isAtDragons());
			}
		} else {
			System.out.println("Tile is not clickable, using webwalking");
			WebWalking.moveTo(random, false, () -> Wilderness.attackAblePlayersNearby() ||  !Wilderness.isAtDragons() || isStandingOnSafeSpot());
		}
	}

		private void determineAttackZone(){
			if(Npcs.stream().within(dragon1).name("Lava dragon").first().valid() &&
				Npcs.stream().within(dragon1).name("Lava dragon").first().healthPercent() != 0 &&
					!isSafeSpotting()){
				System.out.println("Dragon is valid");
				if(currentDragon != dragon1)Notifications.showNotification("Switching: Dragon 1");
				currentDragon = dragon1;
			}else{
				System.out.println("Dragon 2 is valid");
				if(currentDragon != dragon2)Notifications.showNotification("Switching: Dragon 2");
				currentDragon = dragon2;
			}
		}


	private void dragScreen(){
		Input.drag(new Point(100 + Random.nextInt(0, 400), 100 + Random.nextInt(100, 200)), new Point(75 + Random.nextInt(0, 450), 500 + Random.nextInt(100, 400)));
	}


	private void attackDragon() {
		if (!Restocking.isWeaponChargeable() && !Restocking.isAutoCasting() && Restocking.getCharges() > 0) {
			Restocking.setAutoCast("Fire Strike");
			System.out.println("Should have set autocast to fire strike");
		} else {
			script.setStatus("Attacking dragon");
			Npc dragon = Npcs.stream().name("Lava dragon").within(currentDragon).nearest().first();
			if (dragon.valid()) {
				if (dragon.healthPercent() != 0 && !Players.local().interacting().valid()) {
					if (!ScreenBoundingModel.screen.contains(dragon.centerPoint())) {
						System.out.println("No dragon clickable");
						dragScreen();
					} else {
						if (Mouseclick.longPress(dragon, "Attack")) {
							int check = Restocking.getCharges();
							Condition.wait(() -> check > Restocking.getCharges() || Wilderness.attackAblePlayersNearby(), 120, 30);
						} else {
							if (Random.nextInt(0, 4) == 2) {
								System.out.println("Failed to click dragon, adjusting camera a little bit");
								dragScreen();
							} else {
								System.out.println("Failed to click dragon, will do nothing");
							}
						}
					}
				}
			}
			if (Npcs.stream().name("Lava dragon").within(currentDragon).nearest().first().valid() &&
					Npcs.stream().name("Lava dragon").within(currentDragon).nearest().first().healthPercent() == 0) {
				System.out.println("Waiting for loot to be on the floor");
				Condition.wait(() -> Wilderness.attackAblePlayersNearby() || isThereLootOnFloor(), 100, 20);
				if (isThereLootOnFloor()) {
					checkLoot();
				}
			}
		}
	}


	public static boolean isStandingOnSafeSpot() {
		for (Tile x : getCorrespondingSafeSpotTiles()) {
			if (Players.local().tile().equals(x)) return true;
		}
		return false;
	}

	private boolean isSafeSpotting() {
		return Players.local().interacting().name().equals("Lava dragon") &&
				Npcs.stream().name("Lava dragon").nearest().interactingWithMe().first().valid() &&
				(isStandingOnSafeSpot() || hasDiscoveredSafespotTile());
	}


	private void sleep() {
		script.setStatus("Sleeping");
		WorldHopping.isWorldOpen();
		Condition.wait(() -> Wilderness.attackAblePlayersNearby() || isThereLootOnFloor() || !isSafeSpotting() , 300, 20);
	}
	private void forcedSleep() {
		script.setStatus("Forced Sleeping");
		WorldHopping.isWorldOpen();
		Condition.wait(() -> Wilderness.attackAblePlayersNearby() || isThereLootOnFloor() || Players.local().healthBarVisible(), 300, 50);
	}

}



