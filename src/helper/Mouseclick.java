package helper;

import org.powbot.api.Condition;
import org.powbot.api.Input;
import org.powbot.api.Point;
import org.powbot.api.event.GameActionEvent;
import org.powbot.api.rt4.Menu;
import org.powbot.api.rt4.Npc;
import script.LavaDragon;

import java.util.Arrays;

public class Mouseclick {




    public static boolean longPress(Npc npc, String action) {
        if (npc.inViewport() && npc.isRendered()) {
            Point p = npc.nextPoint();
            Input.press(p);
            if (Condition.wait(Menu::opened, 20, 60)) {
                Input.release(p);
                if (Menu.containsAction(action)) {
                    if (Menu.click(String -> String.getAction().equals(action))) {
                        return true;
                    }
                    } else {
                        Menu.close();
                        return false;
                }
            } else {
                Input.release(p);
                return false;
            }
        }
        return false;
    }
}
