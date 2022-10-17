package script;




import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.eventbus.Subscribe;
import data.LootTable;
import helper.Mouseclick;
import helper.ScreenBoundingModel;
import logic.Wilderness;
import logic.WorldHopping;
import org.powbot.api.*;
import org.powbot.api.event.*;
import org.powbot.api.rt4.*;
import org.powbot.api.rt4.walking.model.Skill;
import org.powbot.api.script.AbstractScript;
import org.powbot.api.script.ScriptCategory;
import org.powbot.api.script.ScriptManifest;
import org.powbot.api.script.paint.*;
import org.powbot.mobile.drawing.Graphics;
import org.powbot.mobile.service.ScriptUploader;

import logic.Restocking;
import nodes.Banking;
import nodes.FightDragons;
import nodes.Node;
import nodes.ReturnToBank;
import nodes.RunToDragons;

import javax.management.Notification;

import static logic.Restocking.*;
import static nodes.FightDragons.getCorrespondingSafeSpotTiles;


@ScriptManifest(name = "sLava Dragons", description = "Shocks lava dragon killer script. Anti-PK, gear detection (for restock), deathwalking, ... more info Shock#5170", version = "1.0.2", category = ScriptCategory.MoneyMaking)
public class LavaDragon extends AbstractScript {

	private String node = "";
	private String status = "Initialising ... ";
	private Restocking restock;
	private final List<Node> nodes = new ArrayList<>();

	@SuppressWarnings("resource")
	public static void main(String[] args) {
		/*1*/ //new ScriptUploader().uploadAndStart("sLava Dragons", "X", "localhost:59067", true, true);
		/*3*/  //new ScriptUploader().uploadAndStart("sLava Dragons", "X", "localhost:59080", true, true);
		/*5*/  //new ScriptUploader().uploadAndStart("sLava Dragons", "X", "localhost:59085", true, true);
		/*9*/  new ScriptUploader().uploadAndStart("sLava Dragons", "X", "localhost:59897", true, true);
		/*11*/  //new ScriptUploader().uploadAndStart("sLava Dragons", "X", "localhost:51751", true, true);

	}

	@Override
	public void onStart() {
		restock = new Restocking(this);
		restock.setGear();
		restock.setInventory();
		Combat.autoRetaliate(false);
		buildPaint();
		setNodes();
		Game.setSingleTapToggle(false);
		super.onStart();
	}

	@Override
	public void onStop() {
		LootTable.getLootSoFar().entrySet().stream().forEach(e -> System.out.printf("%s: %d", e.getKey(), e.getValue()));
	}

	private void setNodes() {
		nodes.add(new Banking(this));
		nodes.add(new RunToDragons(this));
		nodes.add(new FightDragons(this));
		nodes.add(new ReturnToBank(this));
	}

	public void setStatus(String s) {
		status = s;
	}

	private void buildPaint() {
		Paint paint = PaintBuilder.newBuilder()
				.x(50)
				.y(50)
				.trackInventoryItems(9194, 11286).withTotalLoot(true)
				.removeScriptNameVersion()
				.addString("Node: ", () -> node)
				.addString("Status: ", () -> status)
				.addString("Charges: ", () -> Integer.toString(getCharges()))
				.addString("Amount hopped: ", () -> Integer.toString(WorldHopping.HOPPED))
				.addString("Died: ", () -> String.valueOf(Restocking.DIED))
				.trackSkill(Skill.Magic)
				.trackSkill(Skill.Hitpoints)
				.trackSkill(Skill.Defence)
				.withoutDiscordWebhook()
				.build();
		addPaint(paint);
	}

	@Subscribe
	public void onInventoryChange(InventoryChangeEvent event) {
		if (event.getItemId() == 11941) {
			while (Inventory.stream().id(11941).first().valid()) {
				if (Game.tab(Game.Tab.INVENTORY)) {
					Inventory.stream().id(11941).first().click("Open", "Looting bag");
					Condition.wait(() -> !Inventory.stream().id(11941).first().valid(), 300, 4);
				}
			}
		}
		if (event.getItemId() == 229) {
			while (Inventory.stream().id(229).first().valid()) {
				if (Game.tab(Game.Tab.INVENTORY)) {
					Inventory.stream().id(229).first().click("Drop", event.getItemName());
					Condition.wait(() -> !Inventory.stream().id(229).first().valid(), 300, 4);
				}
			}
		}
	}

	@Subscribe
	public void onMessageChange(MessageEvent event) {
		int charges;
		if (event.getMessage().contains("Your weapon has")) {
			System.out.println(event.getMessage());
			if (event.getMessage().contains("one") || event.getMessage().contains("two")) {
				setCharges(0);
			} else {
				try {
					int size = event.getMessage().split(" ")[3].toCharArray().length;
					switch (size) {
						case 3:
							try {
								charges = Integer.parseInt(event.getMessage().split(" ")[3]);
								System.out.println("Charges: " + charges);
								setCharges(charges);
							} catch (NumberFormatException e) {
								setCharges(0);
							} finally {
								break;
							}

						case 5:
							charges = Integer.parseInt(Arrays.stream(event.getMessage().split(" ")[3].split(",")).collect(Collectors.joining()));
							System.out.println("Charges: " + charges);
							setCharges(charges);
							break;
						default:
							System.out.println("Error in checking charges on listener");
							setCharges(0);
					}
				} catch (NumberFormatException e) {
					System.out.println("ERROR parsing: " + event.getMessage());
				}
			}
		}

		if (event.getMessage().contains("have space in your looting bag for that")) {
			setLootingBagFull(false);
		}
		if (event.getMessage().contains("The effects of the divine potion have worn off")) {
			Restocking.shouldDrinkPotion(true);
		}
	}

	@Subscribe
	public void onLevelUp(SkillLevelUpEvent event) {
		System.out.println("We leveled up");
		int check = Restocking.getCharges();
		Condition.wait(() -> Wilderness.attackAblePlayersNearby(), Random.nextInt(200, 400), Random.nextInt(3, 5));
		if (Mouseclick.longPress(Npcs.stream().name("Lava dragon").interactingWithMe().first(), "Attack")) {
			Condition.wait(() -> Restocking.getCharges() != check, 300, 7);
		}

		if(check == Restocking.getCharges()){
			while(Chat.canContinue() || Chat.get().size() > 0){
				Chat.continueComponent().click();
				Condition.wait(() -> !Chat.canContinue(),100,3);
			}
		}
	}

	@Subscribe
	public void onPlayerAnimationChange(PlayerAnimationChangedEvent event) {
		if (event.getAnimation() == 1167) {
			setCharges(getCharges() - 1);
		}
	}

	/*@Subscribe
	public void onRender(RenderEvent event) {
		Graphics g = event.getGraphics();
		g.setScale(1.3f);
		g.setColor(Color.getRED());
		if (Wilderness.isAtDragons()) {
			for (Tile x : getCorrespondingSafeSpotTiles()) {
				if (x.valid() && x.isRendered() && x.reachable())
					x.matrix().draw(g);
			}
		}
	}*/

	@Subscribe
	public void onTick(TickEvent event) {
		if (Wilderness.getWildernessLevel() != 0 && Combat.wildernessLevel() != 0) {
			if (Wilderness.attackAblePlayersNearby() && !Wilderness.isBeingPked() && !Players.local().healthBarVisible()) {
				System.out.println("Hopping inside the tick event");
				WorldHopping.hop();
			}
		}
	}


	@Override
	public void poll() {

		for (Node n : nodes) {
			if (n.validate()) {
				node = n.status();
				n.execute();
				break;
			}
		}
	}



}







		

