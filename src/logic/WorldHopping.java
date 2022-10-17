package logic;

import helper.ScreenBoundingModel;
import org.powbot.api.*;

import org.powbot.api.rt4.*;
import org.powbot.mobile.script.ScriptManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class WorldHopping {
    private static final List<Integer> worlds = new ArrayList<>(Arrays.asList(
            302,303,304,305,306,307,309,310,311,312,313,314,315,317,320,321,322,323,324,325,327,328,329,330,331,332,333,334,336,337,338,
            339,340,341,342,343,344,346, 347,348,350,351,352,354,355,356,357,358,359,360,362,367,368,369,370,374,375,376,377,378,386,
            387,388,389,390,395,421,424,443,444,445,446,463,464,465,466,477,478,479,480,481,482,484,485,486,487,488,489,490,491,492,
            493,494,495,496,505,506,507,508,509,510,511,512,513,514,515,516,517,518,519,520,521,522,523,524,525,531,532,533,534,535));

    private static Rectangle worldListCoordinates;
    private static int currentworld;
    public static int HOPPED = 0;


    public static boolean hop(){
        if (isWorldOpen()) {
            if(tryChangeWorld(getRandomWorld())){
                HOPPED++;
                if(Random.nextInt(1,2) == 1){
                    System.out.println("Swiping wordlist a bit to get new random worlds");
                    swipeWorldList();
                }
                return true;
            }
        }else {
            System.out.println("We were trying to hop but the world was not open");
            return false;
        }
        return false;
    }
    private static boolean tryChangeWorld(Component world) {
        System.out.println("Hopping to world: " + world);
        if(world.click("Switch")){
            System.out.println("We clicked the world");
            int temp = currentworld;
            Condition.wait(() -> (isWorldOpen() && temp != currentworld) || Wilderness.isBeingPked() || Players.local().healthBarVisible(), 600, 20);
            return temp != currentworld;
        }else{
            return tryChangeWorld(world);
        }
    }
    private static int generateXCoordinate(int xTaken){
        if(xTaken > 810){
            return xTaken - Random.nextInt(0,10);
        }else if(xTaken <= 810 && xTaken > 770){
            return xTaken + Random.nextInt(-8,8);
        }else if(xTaken <= 770 && xTaken > 700){
            return  xTaken + Random.nextInt(-8,8);
        }else if(xTaken <= 700 && xTaken > 670 ){
            return  xTaken + Random.nextInt(-6,6);
        }else{
            return xTaken + Random.nextInt(3,8);
        }

    }
    public static void swipeWorldList() {
        /*
        Width: 147
        Height: 167

         */

        if (isWorldOpen() && Widgets.widget(69).valid() && Widgets.widget(69).component(15).visible()) {
            System.out.println("Swiping worldlist to find new worlds");
            int xTaken = ScreenBoundingModel.worldHoppingScreen.getX() + (ScreenBoundingModel.worldHoppingScreen.getWidth() / 2 ) + Random.nextInt(-25,25);
            int xNeeded = generateXCoordinate(xTaken);
            int yTaken = ScreenBoundingModel.worldHoppingScreen.getY() + (ScreenBoundingModel.worldHoppingScreen.getHeight() / 10) + Random.nextInt(-5,30);
            int yNeeded = ScreenBoundingModel.worldHoppingScreen.getY() + (ScreenBoundingModel.worldHoppingScreen.getHeight() / 2)  + Random.nextInt(0,70);


            //upwards: Input.drag(new Point(xNeeded, yNeeded), new Point(xTaken, yTaken));
            //downwards: Input.drag(new Point(xTaken, yTaken), new Point(xNeeded, yNeeded));
            int world = Arrays.stream(Widgets.widget(69).component(16).components()).filter(e -> worldListCoordinates.contains(e.screenPoint())).findFirst().isPresent()?
                    Arrays.stream(Widgets.widget(69).component(16).components()).filter(e -> worldListCoordinates.contains(e.screenPoint())).findFirst().get().index() : currentworld;

            if (getWorldComponents().isEmpty() || world > 490 || (Random.nextInt(0,3) == 1 && world < 430)){
                do {
                    System.out.println("Swiping 1 - Downwards");
                    Input.drag(new Point(xTaken, yTaken), new Point(xNeeded, yNeeded));
                    Condition.sleep(125);
                } while (getWorldComponents().isEmpty() && Game.tab() == Game.Tab.LOGOUT && Wilderness.getWildernessLevel() != 0  && Combat.wildernessLevel() != 0);
            }else if (world < 320 || (Random.nextInt(0,3) == 1 && world < 430)){
                do{
                    System.out.println("Swiping 2 - Upwards");
                    Input.drag(new Point(xNeeded, yNeeded), new Point(xTaken, yTaken));
                    Condition.sleep(125);
                }while(getWorldComponents().isEmpty() && Game.tab() == Game.Tab.LOGOUT && Wilderness.getWildernessLevel() != 0  && Combat.wildernessLevel() != 0);
            }else {
                if(Random.nextInt(0,3) == 2 && world > 430) {
                    do {
                        System.out.println("Swiping 3 - Downwards");
                        Input.drag(new Point(xTaken, yTaken), new Point(xNeeded, yNeeded));
                        Condition.sleep(125);
                    } while (getWorldComponents().isEmpty() && Game.tab() == Game.Tab.LOGOUT && Wilderness.getWildernessLevel() != 0 && Combat.wildernessLevel() != 0);
                }else if(Random.nextInt(0,3) == 2 && world < 430){
                    do{
                        System.out.println("Swiping 4 - Upwards");
                        Input.drag(new Point(xNeeded, yNeeded), new Point(xTaken, yTaken));
                        Condition.sleep(125);
                    }while(getWorldComponents().isEmpty() && Game.tab() == Game.Tab.LOGOUT && Wilderness.getWildernessLevel() != 0 && Combat.wildernessLevel() != 0);
                }
                else{
                    if(world > 360){
                        do {
                            System.out.println("Swiping 5 - Downwards");
                            Input.drag(new Point(xTaken, yTaken), new Point(xNeeded, yNeeded));
                        } while (getWorldComponents().isEmpty() && Game.tab() == Game.Tab.LOGOUT && Wilderness.getWildernessLevel() != 0 && Combat.wildernessLevel() != 0);
                    }else{
                        do{
                            System.out.println("Swiping 6 - Upwards");
                            Input.drag(new Point(xNeeded, yNeeded), new Point(xTaken, yTaken));
                        }while(getWorldComponents().isEmpty() && Game.tab() == Game.Tab.LOGOUT && Wilderness.getWildernessLevel() != 0 && Combat.wildernessLevel() != 0);
                    }
                }
            }
        }
    }
    private static List<Component> getWorldComponents(){
            return Arrays.stream(Widgets.widget(69).component(16).components()).filter(
                            e -> worlds.contains(e.index()) &&
                            worldListCoordinates.contains(e.centerPoint()) &&
                            e.index() != currentworld)
                    .collect(Collectors.toList());
        }
    private static Component getRandomWorld() {
        List<Component> validWorlds = getWorldComponents();
       // if (validWorlds.size() == 0) {
       //     swipeWorldList();
       // }
        if (validWorlds.size() != 0 && isWorldOpen()) {
            System.out.println("Succesfully found a world");
            return validWorlds.get(Random.nextInt(0, validWorlds.size() - 1));
        }
        else{
            Notifications.showNotification("Failsafe: No worlds to hop to.");
            swipeWorldList();
            return getRandomWorld();
        }
    }
    public static boolean isWorldOpen() {
        while (Game.tab(Game.Tab.LOGOUT)) {
            if(Components.stream().text("Tap here to logout").viewable().findFirst().isPresent()) {
                Components.stream().text("World Switcher").viewable().first().click();
                Condition.wait(() -> Components.stream().text("Current world - ").viewable().findFirst().isPresent(), 150, 15);
            }

            if(Components.stream().text("Current world - ").viewable().findFirst().isPresent()) {
                worldListCoordinates = Widgets.widget(69).component(15).boundingRect();
                String text = Components.stream().text("Current world - ").first().text();
                currentworld = Integer.parseInt(text.substring(text.lastIndexOf(" ") + 1));

                return true;
            }
        }
        return false;
    }
}

