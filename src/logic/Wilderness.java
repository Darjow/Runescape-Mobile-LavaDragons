package logic;

import helper.ScreenBoundingModel;
import org.powbot.api.Area;
import org.powbot.api.Condition;
import org.powbot.api.Random;
import org.powbot.api.Tile;
import org.powbot.api.rt4.*;

import java.util.Arrays;
import java.util.List;

public class Wilderness {

    static Area dragon = new Area(
            new Tile[]{
                    new Tile(3194, 3855, 0),
                    new Tile(3209, 3855, 0),
                    new Tile(3231, 3846, 0),
                    new Tile(3233, 3833, 0),
                    new Tile(3222, 3812, 0),
                    new Tile(3212, 3804, 0),
                    new Tile(3199, 3809, 0),
                    new Tile(3192, 3800, 0),
                    new Tile(3173, 3809, 0),
                    new Tile(3174, 3824, 0),
                    new Tile(3180, 3831, 0),
                    new Tile(3178, 3839, 0)
            }
    );

    public static int getWildernessLevel() {
        if (Widgets.widget(90).component(50).valid() && Widgets.widget(90).component(50).visible()) {
            try {
                return Integer.parseInt(Widgets.widget(90).component(50).text().split(": ")[1]);
            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println(e.getMessage());
                return 0;
            }
        }
        return 0;
    }

    public static boolean attackAblePlayersNearby() {
        List<Player> x = Players.stream().filtered(e -> !e.name().equals(Players.local().name())).within(45).list();
        if (x.size() > 0) {
            for (Player player : x) {
                if ((player.getCombatLevel() <= getWildernessLevel() + Players.local().getCombatLevel()) && player.getCombatLevel() >= Players.local().getCombatLevel() - getWildernessLevel()) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isBeingPked() {
        return Players.stream().filter(n->n.interacting().equals(Players.local())).isNotEmpty();
    }

    public static boolean teleport() {
        String glory = Equipment.itemAt(Equipment.Slot.NECK).name();
        if (Game.tab(Game.Tab.EQUIPMENT)) {
            if (Equipment.itemAt(Equipment.Slot.NECK).valid()) {
                if (Equipment.itemAt(Equipment.Slot.NECK).click("Edgeville", glory)) {
                    System.out.println("Sucessfully clicked the necklace to teleport");
                    Condition.wait(() -> !Equipment.itemAt(Equipment.Slot.NECK).name().equals(glory) && Wilderness.getWildernessLevel() == 0, 60, 70);
                } else {
                    System.out.println("Failed to click the necklace");
                }
                return !Equipment.itemAt(Equipment.Slot.NECK).name().equals(glory) && Wilderness.getWildernessLevel() == 0;
            }
        }
        return !Equipment.itemAt(Equipment.Slot.NECK).name().equals(glory) || Wilderness.getWildernessLevel() == 0;
    }


    public static boolean isAtDragons() {
        if (dragon.contains(Players.local().tile())) {
            return true;
        }
        return false;
    }

    public static boolean gateIsOpened() {
        return !getGate().actions().contains("Open");
    }

    public static boolean gateIsOnScreen() {

        return ScreenBoundingModel.screen.contains(getGate().centerPoint()) && getGate().getTile().distanceTo(Players.local().tile()) < 15;
    }

    public static GameObject getGate() {
        if (Random.nextInt(0, 1) == 0) return Objects.stream().at(new Tile(3202, 3856, 0)).first();
        return Objects.stream().at(new Tile(3201, 3856, 0)).first();
    }


    public static boolean openGate() {
        if (gateIsOnScreen()) {
            if (getGate().click("Open", "Gate")) {
                System.out.println("Succesfully opened the gate");
                Condition.wait(() -> gateIsOpened(), 100, 50);
                return true;
            } else {
                System.out.println("Failed to open the gate");
                return false;
            }
        }
        System.out.println("Gate is not on screen and u asking me to open it .. ");
        return false;
    }
}
