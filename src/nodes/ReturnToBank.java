package nodes;

import helper.Walker;
import logic.Restocking;
import logic.Wilderness;
import org.powbot.api.Random;
import org.powbot.api.Tile;
import org.powbot.api.rt4.*;
import script.LavaDragon;

public class ReturnToBank extends Node {


	public ReturnToBank(LavaDragon e) {
		super(e);
	}

	@Override
	public boolean validate() {
		if (Wilderness.getWildernessLevel() != 0) {
			if (Inventory.isFull() && Restocking.islootingBagIsFull()) {
				return true;
			}
			if (!Restocking.hasFood() || FightDragons.retreating) {
				return true;
			}
			if (Wilderness.isBeingPked()) {
				return true;
			}
			if (Restocking.getCharges() <= 0){
				return true;
			}
		}
		return false;
	}

	@Override
	public void execute() {
		script.setStatus(String.format("Running to: %s.", Wilderness.isAtDragons()? "Gate" : "Bank"));

		if(Wilderness.isAtDragons() && !Wilderness.gateIsOnScreen() && Wilderness.getWildernessLevel() != 0) {
			Movement.builder(Wilderness.getGate().tile())
					.setRunMin(5).setRunMax(30)
					.setForceWeb(true)
					.setWalkUntil(() -> {
						return (Wilderness.attackAblePlayersNearby() && !Wilderness.isBeingPked()) || Wilderness.gateIsOnScreen() || !Wilderness.isAtDragons() || Wilderness.getWildernessLevel() == 0;
					})
					.move();
		}
		else if(Wilderness.gateIsOnScreen() && !Wilderness.gateIsOpened() && Wilderness.isAtDragons()) {
			Wilderness.openGate();
		}
		else if(!Wilderness.isAtDragons() || Wilderness.isBeingPked()){
			Walker.wildernessWalker(Walker.fromDragonToSouth, 20, true, 30);
		}
		else if(Wilderness.gateIsOpened() && Wilderness.gateIsOnScreen()){
			Movement.walkTo(Walker.fromDragonToSouth[0].tile());

		}
	}

}

