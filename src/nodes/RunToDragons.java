package nodes;

import helper.Walker;
import logic.Wilderness;
import logic.WorldHopping;
import org.powbot.api.*;
import org.powbot.api.rt4.*;


import logic.Restocking;
import script.LavaDragon;

import static logic.Wilderness.*;

public class RunToDragons extends Node {


	public RunToDragons(LavaDragon e) {
		super(e);
	}

	@Override
	public boolean validate() {
		if (Wilderness.getWildernessLevel() == 0) {
			if (isAtCave()) {
				return true;
			}
			return (Restocking.isCurrentGearSameAsRestockProfile() && Restocking.isCurrentInventorySameAsRestockProfile() && Restocking.isWeaponChargeable() ? Restocking.getCharges() > 500 : Restocking.isAutoCasting());
		} else {
			return Restocking.hasFood() && !Wilderness.isAtDragons() && !FightDragons.retreating && Restocking.getCharges() >= 1 && !Wilderness.isBeingPked();
		}
	}



	@Override
	public void execute() {
			if (Bank.inViewport()) {
				teleportToCorp();
			}
			if (isAtCave()) {
				handleExit();
			}
			if (!isAtDragons()) {
				if(!Wilderness.gateIsOnScreen()) {
					script.setStatus("Running to dragons");
					Walker.wildernessWalker(Walker.fromCaveToDragon, 30, false, 0);
				}
				if (Wilderness.gateIsOnScreen() && !Wilderness.gateIsOpened()) {
					openGate();
				}
				if (Wilderness.getGate().tile().distanceTo(Players.local()) < 20 && Wilderness.gateIsOpened()) {
					script.setStatus("Initialising fight dragons");
					if (!Wilderness.attackAblePlayersNearby() && Wilderness.getWildernessLevel() != 0 && !Wilderness.isAtDragons()) {
						Tile random = new Tile(Wilderness.getGate().getTile().getX() + Random.nextInt(1, 3), Wilderness.getGate().getTile().getY() - Random.nextInt(3, 6), 0);
						Movement.builder(random).setWalkUntil(() -> Wilderness.attackAblePlayersNearby() || Wilderness.isBeingPked() || Wilderness.getWildernessLevel() == 0).move();
						System.out.println("Walking to random location south of gate.");
					}
				}
			}
		}


	private boolean isAtCave(){
		return Objects.stream().id(679).name("Cave exit").count() == 1 && Wilderness.getWildernessLevel() == 0;
	}



	private void teleportToCorp(){
		while(Bank.opened()){
			Bank.close();
			Condition.wait(() -> !Bank.opened(), 100,5);
		}
		while (Bank.nearest().isRendered()) {
			script.setStatus("Teleporting to corp ...");
			while (!Game.tab(Game.Tab.INVENTORY)) {
				Condition.wait(() -> Game.tab(Game.Tab.INVENTORY), 4, 300);
			}
			Item gn = Inventory.stream().filtered(e -> e.name().contains("Games necklace")).first();
			gn.click("Rub", gn.name());
			Condition.wait(() -> Chat.get().size() == 5, 300, 5);
			Chat.completeChat("Corporeal Beast.");
			Condition.wait(() -> Objects.stream().within(10).id(679).count() == 1, 175, 10);
		}
	}
	private boolean handleExit() {
		WorldHopping.isWorldOpen();
		Movement.running(true);
		script.setStatus("Handling Exit...");
		GameObject x = Objects.stream().id(679).name("Cave exit").action("Exit").first();
		if (x.valid()) {
			if (x.click("Exit", x.name())) {
				Condition.wait(() -> Chat.chatting(), 300, 20);
				if (Chat.chatting()) {
					if (Chat.completeChat("Yes.")) {
						Condition.wait(() -> !Chat.chatting(), 300, 4);
						return !Chat.chatting();
					}
				}
			}
		} else {
			return false;
		}

		return false;
	}
}


