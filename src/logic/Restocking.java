package logic;


import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import helper.ScreenBoundingModel;
import org.powbot.api.Condition;
import org.powbot.api.Input;
import org.powbot.api.Notifications;
import org.powbot.api.Random;
import org.powbot.api.rt4.*;
import org.powbot.api.rt4.Bank.Amount;

import org.powbot.mobile.script.ScriptManager;
import script.LavaDragon;

import org.powbot.api.rt4.Equipment.Slot;
import org.powbot.api.rt4.Game.Tab;
import org.powbot.api.rt4.walking.model.Skill;

import javax.management.Notification;


public class Restocking {

    public static int DIED = 0;
    private static boolean needToDrink = true;
    private static final EnumMap<Slot, String> GEAR = new EnumMap<>(Slot.class);
    private static Map<String, Long> INVENTORY = new HashMap<>();
    private static int CHARGES = -1;
    private static LavaDragon script;
    private static String foodType;
    private static boolean lootingBagIsFull = false;
    public Restocking(LavaDragon x) {
        script = x;
    }

    private static void grabRequiredItems(List<String> resourcesNeeded) {
        openBank();
        if (hasEnoughInventorySpace(resourcesNeeded.size()) != 0) {
            Bank.depositAllExcept(getGear().get(Slot.MAIN_HAND));
        }
        for (String x : resourcesNeeded) {
            withdraw(x, Amount.ALL);
        }
        while (Bank.opened()) {
            Bank.close();
            Condition.wait(() -> !Bank.opened(), 20, 120);
        }
        if (needToBank(resourcesNeeded)) {
            System.out.println("ERROR in grabbing items from bank: Exitting script. ( we prob dont have enough runes )");
            ScriptManager.INSTANCE.stop();
        }
    }

    public static void setCharges(int x) {
        CHARGES = x;
    }

    public static int getCharges() {
        if (isWeaponChargeable()) {
            return CHARGES;
        } else {
            if (getGear().get(Slot.MAIN_HAND).equals("Smoke battlestaff")) {
                return Inventory.stream().name("Mind rune").first().valid() ? Inventory.stream().name("Mind rune").first().stackSize() : 0;
            }
            return 0;
        }
    }

    public static Map<String, Long> getInventory() {
        return INVENTORY;
    }

    public static EnumMap<Slot, String> getGear() {
        return GEAR;
    }

    public static void healAtBank() {
        if (!Inventory.stream().name(Restocking.getFoodType()).first().valid()) {
            openBank();
            if (Inventory.isFull()) {   bankInventory();       }

            while (Restocking.needHeal()) {
                Restocking.withdraw(Restocking.getFoodType(), 1);
                int health = Combat.health();
                Inventory.stream().name(Restocking.getFoodType()).first().click("Eat", Restocking.getFoodType());
                Condition.wait(() -> health != Combat.health(), 300, 20);
                if (Restocking.needHeal()) Condition.sleep(Random.nextInt(400, 5));
            }
        }
        else{
            if(Bank.opened()) {
                while (Restocking.needHeal() && Inventory.stream().name(Restocking.getFoodType()).first().valid()) {
                    Restocking.withdraw(Restocking.getFoodType(), 1);
                    int health = Combat.health();
                    Inventory.stream().name(Restocking.getFoodType()).first().click("Eat", Restocking.getFoodType());
                    Condition.wait(() -> health != Combat.health(), 300, 20);
                    if (Restocking.needHeal()) Condition.sleep(Random.nextInt(400, 5));
                }
            }else{
                while (Restocking.needHeal()) {
                    int health = Combat.health();
                    Inventory.stream().name(Restocking.getFoodType()).first().click("Eat", Restocking.getFoodType());
                    Condition.wait(() -> health != Combat.health(), 300, 20);
                    if (Restocking.needHeal()) Condition.sleep(Random.nextInt(400, 5));
                }
            }

        }
    }
    public static boolean sipStamina() {
        openBank();
        if(Bank.stream().filtered(e -> e.name().contains("Stamina potion")).first().valid()){
            System.out.println("We have stamina in our bank, we will grab one");
            withdraw(Bank.stream().filtered(e -> e.name().contains("Stamina potion")).first().name(), 1);
            if(Inventory.stream().filtered(e -> e.name().contains("Stamina potion")).first().click("Drink", Inventory.stream().filtered(e -> e.name().contains("Stamina potion")).first().name())){
                System.out.println("Succesfully sipped the potion");
                Condition.sleep(Random.nextInt(600,900));
                String name = Inventory.stream().filtered(e -> e.name().contains("Stamina") || e.name().contains("Vial")).first().name();
                if(Inventory.stream().name(name).first().valid()){
                    if(Inventory.stream().name(name).first().click("Deposit-1")){
                        Condition.sleep(Random.nextInt(600,750));
                        return true;
                    }
                }
            }
        }else{
            System.out.println("No stamina to sip.");
            ScriptManager.INSTANCE.stop();
            return false;
        }
        return false;
    }

    public static boolean isUsingPotions(){
        return getInventory().containsKey("Divine magic potion(4)");
    }

    public static void shouldDrinkPotion(boolean b) {
        needToDrink = b;
    }

    public static boolean shouldDrinkPotion() {
        return needToDrink;
    }

    public void setInventory() {
        INVENTORY = Inventory.get().stream().collect(Collectors.groupingBy(Item::name, HashMap::new, Collectors.counting()));
        for (Item x : Inventory.get()) {
            if (x.name().contains("Games necklace")) {
                INVENTORY.remove(x.name());
                INVENTORY.put("Games necklace(8)", (long) 1);
            }
            if(x.name().contains("Divine magic potion")){
                INVENTORY.remove(x.name());
                INVENTORY.put("Divine magic potion(4)", 1L);
            }
            else if (x.stackSize() > 1) {
                INVENTORY.put(x.name(), (long) x.stackSize());
            }
        }
        INVENTORY.put("Looting bag", (long) 1);
        setFoodType();

        if (INVENTORY.entrySet().stream().filter(e -> e.getKey().contains("Games necklace(8)")).count() == 0)
            INVENTORY.put("Games necklace(8)", (long) 1);
        if (INVENTORY.size() > 28) {
            System.out.println("No place for games necklace, exiting script");
            ScriptManager.INSTANCE.stop();
        }
    }

    private void setFoodType() {
        foodType = Inventory.stream().name("Lobster","Bass", "Salmon", "Tuna", "Monkfish", "Shark", "Swordfish").first().name();
    }

    public void setGear() {
        addGear(Slot.HEAD, Equipment.itemAt(Slot.HEAD).name());
        addGear(Slot.HEAD, Equipment.itemAt(Slot.HEAD).name());
        addGear(Slot.NECK, "Amulet of glory(6)");
        addGear(Slot.CAPE, Equipment.itemAt(Slot.CAPE).name());
        addGear(Slot.TORSO, Equipment.itemAt(Slot.TORSO).name());
        addGear(Slot.LEGS, Equipment.itemAt(Slot.LEGS).name());
        addGear(Slot.OFF_HAND, Equipment.itemAt(Slot.OFF_HAND).name());
        addGear(Slot.RING, Equipment.itemAt(Slot.RING).name());
        addGear(Slot.HANDS, Equipment.itemAt(Slot.HANDS).name());
        addGear(Slot.FEET, Equipment.itemAt(Slot.FEET).name());
        addGear(Slot.MAIN_HAND, Equipment.itemAt(Slot.MAIN_HAND).name().contains("Trident of the seas") ? "Trident of the seas" : Equipment.itemAt(Slot.MAIN_HAND).name());
    }

    private static void addGear(Slot x, String name) {
        if (name != null && name != "" && !name.isEmpty() && !name.isBlank()) {
            GEAR.put(x, name);
        }
    }

    public static boolean isWeaponChargeable() {
        return getGear().get(Slot.MAIN_HAND).contains("Trident");
    }

    private static boolean combineItems(Item x, Item y) {
        while (!Inventory.opened()) {
            Inventory.open();
            Condition.wait(() -> Inventory.opened(), 20, 75);
        }
        x.click("Use", x.name());
        Condition.wait(() -> Inventory.selectedItem().name().equals(x.name()), 150, 5);
        if (Inventory.selectedItem().name().equals(x.name())) {
            Inventory.stream().name(y.name()).first().click("Use");
            if(Condition.wait(() -> Widgets.widget(162).component(41).visible(), 300, 3)){
                if (Widgets.widget(162).component(41).visible()) {
                    Condition.sleep(Random.nextInt(500, 1000));
                    int chargesadded;
                    try {
                        chargesadded = Integer.parseInt(Widgets.widget(162).component(41).text().split("- ")[1].split(Pattern.quote(")"))[0]);
                    } catch (NumberFormatException error) {
                        chargesadded = Integer.parseInt(Arrays.stream(Widgets.widget(162).component(41).text().split("- ")[1].split(Pattern.quote(")"))[0].split(",")).collect(Collectors.joining()));
                    }
                    Condition.sleep(Random.nextInt(500, 3000));
                    Input.sendln(Integer.toString(chargesadded));
                    Condition.wait(() -> Widgets.widget(193).component(2).visible(), 200, 20);
                    if (Widgets.widget(193).component(2).visible()) {
                        Condition.sleep(Random.nextInt(500, 1000));
                        System.out.println(Widgets.widget(193).component(2).text());
                        try {
                            setCharges(Integer.parseInt(Widgets.widget(193).component(2).text().split(": ")[1]));
                        } catch (NumberFormatException e) {
                            int charges = Integer.parseInt(Arrays.stream(Widgets.widget(193).component(2).text().split(": ")[1].split(",")).collect(Collectors.joining()));
                            setCharges(charges);
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean needToBank(List<String> resourcesNeeded) {
        if (Inventory.stream().filter(e -> resourcesNeeded.stream()
                .anyMatch(f -> f.equals(e.name()))
        ).count() == resourcesNeeded.size()) {
            if (Inventory.stream().name("Fire rune").first().stackSize() >= 5) {
                return false;
            }
        }
        return true;
    }

    public static boolean equipGear(Item gear) {
        script.setStatus("Equipping gear: " + gear.name());
        System.out.println("Equipping gear: " + gear.name());
        while(Bank.opened()){
            Bank.close(true);
        }
        while (Equipment.stream().filter(e -> e.name().contains(gear.name())).count() == 0) {
            if (Game.tab(Game.Tab.INVENTORY) || Bank.opened()) {
                String option = gear.actions().stream().filter(e -> e.equals("Wield") || e.equals("Wear")).findFirst().get();
                gear.click(option, gear.name());
                Condition.wait(() -> Equipment.stream().filter(e -> e.name().equals(gear.name())).count() > 0, 10, 150);
            }
        }
        System.out.println("Succesfully equipped");
        return true;
    }

    public static void bankInventory() {
        script.setStatus("Banking Inventory ...");
        System.out.println("Banking Inventory");
        while(!Bank.opened()) {
            openBank();
        }
        while (Components.stream().action("View").findFirst().isPresent() && Components.stream().action("View").findFirst().get().name().contains("Looting bag")) {
            depositLootingBag();
        }
        while(Inventory.get().size() > 0) {
            Bank.depositInventory();
            Condition.wait(() -> Inventory.isEmpty(), 3, 300);
        }
    }

    public static void depositLootingBag() {
        System.out.println("We have a looting bag that needs to be emptied");
        while (!Components.stream().text("Bank your loot").viewable().findFirst().isPresent()) {
            System.out.println("Opening looting bag");
            Inventory.stream().name("Looting bag").first().click("View", "Looting bag");
            if (Condition.wait(() -> !Components.stream().text("Bank your loot").viewable().findFirst().isPresent(), 100, 5)) {
                Condition.sleep(Random.nextInt(100, 300));
            }
        }
        while (!Components.stream().text("The bag is empty.").viewable().findFirst().isPresent()) {
            System.out.println("Depositing loot into bank");
            Components.stream().action("Deposit loot").viewable().findFirst().get().click("Deposit loot");
            if (Condition.wait(() -> Components.stream().text("The bag is empty.").viewable().findFirst().isPresent(), 100, 5)) {
                Condition.sleep(Random.nextInt(100, 300));
            }
        }
        while (Components.stream().action("Dismiss").viewable().findFirst().isPresent()) {
            System.out.println("Closing looting bag");
            Components.stream().action("Dismiss").viewable().findFirst().get().click("Dismiss");
            if (Condition.wait(() -> !Components.stream().action("Dismiss").viewable().findFirst().isPresent(), 100, 5)) {
                Condition.sleep(Random.nextInt(100, 300));
            }
        }
        setLootingBagFull(false);
    }

    public static void openBank(){
        while(!Bank.opened()){
            if(ScreenBoundingModel.screen.contains(Bank.nearest().tile().matrix().centerPoint())){
                Bank.open();
                Condition.wait(() -> Bank.opened(), 300,15);
            }else{
                WebWalking.moveToBank();
            }
        }
    }
    public static boolean rechargeWeapon() {
        try {
            Item weapon = null;
            List<String> resourcesNeeded = new ArrayList<>(Arrays.asList("Chaos rune", "Death rune", "Coins", "Fire rune"));


            if (Inventory.isFull() || Inventory.get().size() + resourcesNeeded.size() > 28) {
                while (!Bank.opened()) {
                    openBank();
                }
                bankInventory();
                grabRequiredItems(resourcesNeeded);
            }

            while (Bank.opened()) {
                Bank.close();
                Condition.wait(() -> !Bank.opened(), 100, 10);
            }


            if (Equipment.itemAt(Slot.MAIN_HAND).valid()) {
                unequipWeapon();
            }
            if (Inventory.stream().name(getGear().get(Slot.MAIN_HAND)).first().valid()) {
                weapon = Inventory.stream().name(getGear().get(Slot.MAIN_HAND)).first();
            }
            if (!weapon.valid()) {
                Notifications.showNotification("Error recharging weapon");
            }
            script.setStatus("Recharging: " + weapon.name());
            System.out.println("Recharging Weapon");
            int random = Random.nextInt(0, resourcesNeeded.size());
            boolean check;

            if (!needToBank(resourcesNeeded)) {
                System.out.println("We have all the runes needed in our inventory.");
                while (Bank.opened()) {
                    Bank.close();
                    Condition.wait(() -> !Bank.opened(), 10, 150);
                }
                check = combineItems(weapon, Inventory.stream().name(resourcesNeeded.get(random)).first());
            } else {
                System.out.println("We need to bank to get the runes needed");
                grabRequiredItems(resourcesNeeded);
                while (Bank.opened()) {
                    Bank.close();
                    Condition.wait(() -> !Bank.opened(), 10, 150);
                }
                check = combineItems(weapon, Inventory.stream().name(resourcesNeeded.get(random)).first());
            }
            if (check) {
                equipGear(weapon);
                openBank();
                for (String x : resourcesNeeded) {
                    while (Inventory.stream().name(x).first().valid()) {
                        Bank.deposit(x, Amount.ALL);
                        Condition.sleep(org.powbot.api.Random.nextInt(250, 1000));
                    }
                }
            } else {
                if (Inventory.stream().name(weapon.name()).first().valid()) {
                    equipGear(Inventory.stream().name(weapon.name()).first());
                }
            }
            return check;
        } catch(Exception e){
            System.out.println("Something went wrong in rechargeweapon");
            while(Bank.opened()){
                Bank.close(true);
            }
        }
        return false;
    }

    public static int hasEnoughInventorySpace(int size) {
        int free = 28 - Inventory.get().size();
        if (size - free < 0) {
            return 0;
        } else {
            return size - free;
        }
    }

    private static void unequipWeapon() {
        while(Bank.opened()){
            Bank.close();
            Condition.wait(() -> !Bank.opened(), 100,3);
        }
        if (Game.tab(Tab.EQUIPMENT)) {
            while (Equipment.itemAt(Slot.MAIN_HAND).valid()) {
                System.out.println("Unequipping weapon");
                Equipment.itemAt(Slot.MAIN_HAND).click("Remove", Equipment.itemAt(Slot.MAIN_HAND).name());
                Condition.wait(() -> Equipment.itemAt(Slot.MAIN_HAND).actions().size() == 0, 10, 150);
                Condition.wait(() -> Inventory.stream().name(getGear().get(Slot.MAIN_HAND)).first().valid(), 300,5);
            }
        }
    }

    public static void withdraw(String x, long q) {
        if (Bank.stream().name(x).filtered(e -> e.valid() && e.stackSize() >= q).count() >= 1) {
            System.out.println("Withdrawing: " + x + ". Quantity: " + q);
            boolean stackable = Bank.stream().name(x).filtered(e -> e.valid() && e.stackSize() >= q).first().stackable();

            if ((stackable && Inventory.get().size() >= 27) || (!stackable && 28 - Inventory.get().size() < q)) {
                System.out.println("Not enough inventory space");
                bankInventory();
            }

            if (stackable) {
                System.out.println("Trying to withdraw a stackable item");
                while (Inventory.stream().name(x).count() != 1 && Inventory.stream().name(x).first().stackSize() != q) {
                    Bank.withdraw(x, Integer.parseInt(String.valueOf(q)));
                    Condition.wait(() -> Inventory.stream().name(x).first().valid() && Inventory.stream().name(x).first().stackSize() == q, 300, 5);
                    if (Inventory.stream().name(x).first().valid() && Inventory.stream().name(x).first().stackSize() > q) {
                        System.out.println("We withdrew too many stackables");
                        Bank.deposit(x, Amount.ALL);
                    }
                }
                System.out.println("Succesfully have the item in our inventory");
            } else {
                System.out.println("Trying to withdraw a regular item");
                while (Inventory.stream().name(x).count() != q) {
                    Bank.withdraw(x, Integer.parseInt(String.valueOf(q)));
                    Condition.wait(() -> Inventory.stream().name(x).count() == q, 3, 500);
                    if (Inventory.stream().name(x).count() > q) {
                        Bank.deposit(x, Amount.ALL);
                    }
                }
                System.out.println("Succesfully withdrew: " + q + " " + x);
            }
        } else {
            System.out.println("This item is invalid: " + x);
            System.out.println("Potential problem: not enough in bank.");
            System.out.println("Validating: Bank - " + (Bank.stream().name(x).first().valid() ? Bank.stream().name(x).first().stackSize() : 0) + ". Needed - " + q);
        }
    }

    public static void withdraw(String x, Amount q) {
        if (Bank.stream().name(x).first().valid()) {
            while (Bank.stream().name(x).first().valid() && Bank.stream().name(x).first().stackSize() > 0) {
                System.out.println("Withdrawing: " + x + ". Quantity: " + q.name());
                Bank.withdraw(x, q);
                Condition.wait(() -> Inventory.stream().name(x).count() != 0, 3, 500);
            }
        } else {
            System.out.println("Error: We were trying to withdraw an item that was not in the bank: " + x);
        }
    }

    public static void grabInventory() {
        script.setStatus("Setting inventory...");
        openBank();
        if (Inventory.get().size() > 0) {
            bankInventory();
        }
        try {
            getInventory().entrySet().forEach(e -> {
                if (!Bank.stream().name(e.getKey()).first().valid()) {
                    if (e.getKey().equals("Divine magic potion(4)")) {
                        System.out.println("Couldnt find Divine magic potion(4), will search for other.");
                        if (!Bank.stream().filtered(f -> f.name().contains("Divine magic potion")).first().valid()) {
                            System.out.println("We dont have any magic potions in the bank");
                            ScriptManager.INSTANCE.stop();
                        } else {
                            withdraw(Bank.stream().filtered(f -> f.name().contains("Divine magic potion")).first().name(), 1);
                        }
                    }
                    if (e.getKey().equals("Games necklace(8)")) {
                        System.out.println("Couldnt find Games necklace(8), will search for other.");
                        if (!Bank.stream().filtered(f -> f.name().contains("Games necklace")).first().valid()) {
                            System.out.println("We dont have any games necklaces anymore.");
                            ScriptManager.INSTANCE.stop();
                        } else {
                            withdraw(Bank.stream().filtered(f -> f.name().contains("Games necklace")).first().name(), 1);
                        }
                    } else if (e.getKey().equals("Looting bag")) {
                        System.out.println("No looting bag in bank");
                    } else {
                        System.out.println("Tried withdrawing " + e.getKey() + " , but we dont have any");
                        ScriptManager.INSTANCE.stop();
                    }
                } else {
                    withdraw(e.getKey(), e.getValue());
                }
            });
            while (Bank.opened()) {
                Bank.close();
                Condition.wait(() -> !Bank.opened(), 10, 50);
            }
        }catch(Exception e){
            script.getLog().severe("Error withdrawing items");
        }
    }


    public static void grabGear() {
        script.setStatus("Regearing...");
        if (!Bank.opened()) {
            if (!Bank.inViewport()) {
                WebWalking.walkTo(Bank.nearest().tile(), false, true, false);
                Condition.wait(() -> Bank.inViewport(), 10, 300);
            }
            if (Bank.inViewport()) {
               openBank();
            }
        }
        Bank.depositEquipment();
        Condition.wait(() -> Equipment.get().size() == 0, 3, 500);
        if (Inventory.get().size() + getGear().size() > 28) {
            System.out.println("Not enough inventory spaces left to withdraw our gear --> depositing inventory");
            bankInventory();
        }

        getGear().entrySet().forEach(e -> {
            if (!Bank.stream().name(e.getValue()).first().valid()) {
                if (!e.getValue().isEmpty()) {
                    if (e.getKey().equals(Slot.NECK)) {
                        findGlory(true);
                    } else {
                        if (!Bank.stream().name(e.getValue()).first().valid()) {
                            System.out.println("We tried grabbing: " + e.getValue() + " but we were out.");
                            ScriptManager.INSTANCE.stop();
                        }
                    }
                }
            } else {
                System.out.println("Withdrawing gear: " + e.getValue());
                withdraw(e.getValue(), 1);
                equipGear(Inventory.stream().name(e.getValue()).first());
            }
        });
    }

    public static void findGlory(boolean b) {
        Item glory = Bank.stream().filtered(e -> e.name().contains("Amulet of glory(")).first();
        if (glory.valid()) {
            System.out.println("No Amulet of glory(6) but we have found a " + glory.name());
            withdraw(glory.name(), 1);
        } else {
            System.out.println("We are out of glories");
            ScriptManager.INSTANCE.stop();
        }
        if (b) {
            equipGear(glory);
        }
    }

    public static boolean isCurrentGearSameAsRestockProfile() {
        Map<Slot, String> map = new HashMap<>();
        for (Slot x : Slot.values()) {
            if (Equipment.itemAt(x) != null && !Equipment.itemAt(x).name().isEmpty() && !Equipment.itemAt(x).name().isBlank()) {
                if (x == Slot.NECK && Equipment.itemAt(x).name().contains("glory(")) {
                    map.put(x, "Amulet of glory(6)");
                } else if (x == Slot.MAIN_HAND && Equipment.itemAt(x).name().contains("Trident of the sea")) {
                    map.put(x, "Trident of the seas");
                } else {
                    map.put(x, Equipment.itemAt(x).name());
                }
            }
        }
        String one = map.entrySet().stream().sorted(Map.Entry.comparingByValue()).map(e -> String.format("- %s: %s -", e.getKey(), e.getValue())).collect(Collectors.joining("\n"));
        String two = getGear().entrySet().stream().sorted(Map.Entry.comparingByValue()).map(e -> String.format("- %s: %s -", e.getKey(), e.getValue())).collect(Collectors.joining("\n"));


        return one.equals(two);
    }

    public static boolean isCurrentInventorySameAsRestockProfile() {
        Map<String, Long> map = Inventory.get().stream().filter(e -> !e.name().contains("Games necklace")).collect(Collectors.groupingBy(Item::name, HashMap::new, Collectors.counting()));
        for (Item x : Inventory.get()) {
            if (x.name().contains("Games n")) {
                map.put("Games necklace(8)", (long) 1);
            }
            if (x.stackSize() > 1) {
                map.put(x.name(), (long) x.stackSize());
            }
        }

        map.put("Looting bag", (long) 1);


        String one = getInventory().entrySet().stream().sorted(Map.Entry.comparingByKey()).map(e -> String.format("- %s: %d -", e.getKey(), e.getValue())).collect(Collectors.joining("\n"));
        String two = map.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(e -> String.format("- %s: %d -", e.getKey(), e.getValue())).collect(Collectors.joining("\n"));

        return one.equals(two);
    }

    public static boolean tryGetUpdatedCharges() {
        while(Bank.opened()){
            Bank.close();
            Condition.wait(() -> !Bank.opened(), 100,10);
        }
        if (Inventory.stream().name(getGear().get(Slot.MAIN_HAND)).first().valid()) {
            equipGear(Inventory.stream().name(getGear().get(Slot.MAIN_HAND)).first());
        }
        if (Game.tab(Tab.EQUIPMENT)) {
            if (Equipment.itemAt(Slot.MAIN_HAND).valid()) {
                if (Equipment.itemAt(Slot.MAIN_HAND).actions().contains("Check")) {
                    if (Equipment.itemAt(Slot.MAIN_HAND).click("Check", Equipment.itemAt(Slot.MAIN_HAND).name())) {
                        return true;
                    }
                    if (!Equipment.itemAt(Slot.MAIN_HAND).valid()) {
                        equipGear(Inventory.stream().name(getGear().get(Slot.MAIN_HAND)).first());
                        if (Equipment.itemAt(Slot.MAIN_HAND).click("Check", Equipment.itemAt(Slot.MAIN_HAND).name())) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static String getFoodType() {
        return foodType;
    }

    public static boolean hasFood() {
        return Inventory.stream().name(getFoodType()).count() != 0;
    }

    public static boolean islootingBagIsFull() {
        return lootingBagIsFull;
    }

    public static void setLootingBagFull(boolean b) {
        lootingBagIsFull = b;
    }

    public static boolean needHeal() {
        int currentHealth = fromHealthPercentageToCurrentHitpoints(Players.local().healthPercent());
        int hitpoints = Skill.Hitpoints.realLevel();
        if (getGear().containsKey(Slot.OFF_HAND) && getGear().get(Slot.OFF_HAND).equals("Anti-dragon shield")) {
            if (hitpoints > 80) {
                return currentHealth < (int) Math.ceil(hitpoints * 0.7);
            } else if (hitpoints > 60) {
                return currentHealth < (int) Math.ceil(hitpoints * 0.75);
            } else if (hitpoints > 40) {
                return currentHealth < (int) Math.ceil(hitpoints * 0.8);
            } else
                return currentHealth < (int) Math.ceil(hitpoints * 0.85);
        }else{
            if (hitpoints > 65) {
                return currentHealth < (50 + (0.1 * hitpoints));
            } else if (hitpoints > 50) {
                return currentHealth < (50 + (0.2 * (hitpoints - 50)));
            } else if (hitpoints > 40) {
                return currentHealth < (hitpoints - 3);
            } else
                return currentHealth != hitpoints;
        }
    }

    private static int fromHealthPercentageToCurrentHitpoints(int percentage) {
        int max = Skill.Hitpoints.realLevel();
        return (int) Math.floor(max / 100.0 * percentage);

    }

    public static boolean heal(boolean b) {
        System.out.println("Heal event received");
        if (Inventory.stream().name(getFoodType()).count() == 0) {
            System.out.println("- But we have no food in inventory");
            return false;
        }
        if (Restocking.needHeal() || b) {
            if (Game.tab(Tab.INVENTORY)) {
                boolean check = Inventory.stream().name(Restocking.getFoodType()).first().click("Eat", Restocking.getFoodType());
                Condition.sleep(Random.nextInt(150, 500));
                return check;
            }
        }
        return false;
    }

    public static boolean isAutoCasting() {
        if(Game.tab(Tab.ATTACK)) {
            return Components.stream().widget(593).texture(21).count() == 2;
        }else{
            return false;
        }
    }

    public static boolean setAutoCast(String spell) {
        while(Bank.opened()){Bank.close(); Condition.wait(() -> !Bank.opened(), 200,5);}
            if(!isAutoCasting()){
                if(Components.stream().widget(593).text("Spell").last().click() || Components.stream().text("Select a Combat Spell").viewable().findFirst().isPresent()) {
                    if (Condition.wait(() -> Components.stream().text("Select a Combat Spell").viewable().findFirst().isPresent() || isAutoCasting(), 300, 5)) {
                        if (Components.stream().action(spell).first().click()) {
                            Condition.wait(() -> Components.stream().widget(593).widget(28).viewable().findFirst().isPresent() || isAutoCasting(), 500, 3);
                            return true;
                        }
                    }
                }
            }else{
                return true;
            }
        return isAutoCasting();
    }
}
