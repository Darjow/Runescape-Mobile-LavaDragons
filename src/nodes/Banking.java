package nodes;

import org.powbot.api.Condition;
import org.powbot.api.Events;
import org.powbot.api.rt4.*;
import org.powbot.api.rt4.Equipment.Slot;

import logic.Restocking;
import org.powbot.mobile.script.ScriptManager;
import script.LavaDragon;

public class Banking extends Node{
	
	
	public Banking(LavaDragon e) {
		super(e);
	}
	
	@Override
	public boolean validate() {
		if(Bank.inViewport() && Objects.stream().id(679).name("Cave exit").action("Exit").count() == 0) {
			if(!Restocking.isCurrentGearSameAsRestockProfile()) {
				return true;
			}
			if(!Restocking.isCurrentInventorySameAsRestockProfile()){
				return true;
			}
			if(Restocking.isWeaponChargeable()){
				return Restocking.getCharges() < 500;
			}
			if(!Restocking.isWeaponChargeable() && !Restocking.isAutoCasting()){
				return true;
			}
			if(Equipment.itemAt(Slot.NECK).name().equalsIgnoreCase("Amulet of glory")){
				return true;
			}
		}
		return false;
	}
	@Override
	public void execute() {
			if (!Bank.nearest().isRendered()) {
				handleDeath();
			}
			if (Restocking.islootingBagIsFull() || Inventory.isFull()) {
				script.setStatus("Depositing looting bag | Emptying Inventory");
				Restocking.bankInventory();
			}
			if (Restocking.isWeaponChargeable()) {
				while (Restocking.getCharges() <= 0) {
					int check = Restocking.getCharges();
					if (Restocking.tryGetUpdatedCharges()) {
						Condition.wait(() -> check != Restocking.getCharges(), 50, 30);
					}
				}
				if (Restocking.getCharges() < 500) {
					script.setStatus("Need to recharge weapon");
					if(Inventory.isFull()){
						Restocking.bankInventory();
					}
					else if (Bank.opened()) {
						Bank.close();
					} else {
						System.out.println("Charges: " + Restocking.getCharges());
						if (Restocking.rechargeWeapon()) {
							System.out.println("Succesfully recharged weapon");
						}
					}
				}
			}
	/*		if (Movement.energyLevel() < 30) {
				script.setStatus("Sipping stamina");
				System.out.println("Sipping stamina");
				Restocking.sipStamina();
			}


		if(Combat.health() != Combat.maxHealth()){
			script.setStatus("Healing");
			System.out.println("Healing");
			Restocking.healAtBank();
		}
*/
			if (Equipment.itemAt(Slot.NECK).name().equalsIgnoreCase("Amulet of glory")) {
				script.setStatus("Need to replace glory");
				System.out.println("Need to replace glory");
				replaceGlory();
			}
			if (!Restocking.isCurrentGearSameAsRestockProfile()) {
				script.setStatus("Need to regear");
				System.out.println("Need to regear");
				Restocking.grabGear();
			}
			if (!Restocking.isCurrentInventorySameAsRestockProfile()) {
				script.setStatus("Need to reset inventory");
				System.out.println("Need to reset inventory");
				Restocking.grabInventory();
			}
			if (!Restocking.isWeaponChargeable() && !Restocking.isAutoCasting()) {
				script.setStatus("Ensuring we are autocasting");
				System.out.println("Setting auto cast");
				if (Restocking.setAutoCast("Fire Strike")) {
					System.out.println("Sucessfully set the spell");
				}
			}
	}

	private void handleDeath() {
		System.out.println("We died");
		Restocking.DIED++;
		Restocking.getGear().entrySet().stream().forEach(e -> {
			if (Inventory.stream().name(e.getValue()).first().valid()) {
				System.out.println("Item: " + e.getValue() + ", is part of our restocking gear, equipping it now");
				Restocking.equipGear(Inventory.stream().name(e.getValue()).first());
			}else{
				if(e.getValue().contains("glory")){
					if(Inventory.stream().id(1706,1708,1710,1712,11976,11978).first().valid()){
						Restocking.equipGear(Inventory.stream().id(1706,1708,1710,1712,11976,11978).first());
					}
				}
			}
		});
		while(!Bank.nearest().isRendered()){
			WebWalking.moveToBank();
		}
	}


	private void replaceGlory() {
		Restocking.openBank();
			String glory = "";
			if(Bank.stream().filtered(e -> e.name().contains("Amulet of glory(")).first().valid()){
				glory = Bank.stream().filtered(e -> e.name().contains("Amulet of glory(")).first().name();
			}else{
				System.out.println("No glory to withdraw");
				ScriptManager.INSTANCE.stop();
			}
			Restocking.withdraw(glory,1);
			while(!Equipment.itemAt(Slot.NECK).name().equals(glory)){
				String g = Inventory.stream().name(glory).first().name();
				Inventory.stream().name(g).first().click("Wear", g);
				Condition.wait(() -> Equipment.itemAt(Slot.NECK).name().equals(g),25,75);
			}
			Bank.deposit("Amulet of glory", Bank.Amount.ALL);
			Condition.wait(() -> Inventory.stream().name("Amulet of glory").count() == 0, 3,300);

	}



}
