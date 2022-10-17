package data;

import java.util.HashMap;

public class LootTable {
    public static HashMap<String, Integer> loot = new HashMap<>();
    public static HashMap<String, Integer> totalLoot = new HashMap<>();

    public static HashMap<String,Integer> getLootTable() {
        if (loot.isEmpty()) {
            loot.put("Looting bag", 1);
            loot.put("Draconic visage", 11286);
            loot.put("Onyx bolt tips", 9194);
            loot.put("Dragon javelin heads", 3925);
            loot.put("Fire orb", 570);
            loot.put("Death rune", 560);
            loot.put("Law rune", 563);
            loot.put("Silver ore", 443);
            loot.put("Rune javelin", 830);
            loot.put("Blood rune", 565);
            loot.put("Runite bolts", 9169);
            loot.put("Death rune",560);
            loot.put("Rune dart", 811);
            loot.put("Rune knife", 868);
            loot.put("Nature rune", 561);
            loot.put("Coins",995);
            loot.put("Shield left half", 2366);
            loot.put("Shield right half", 2368);
            loot.put("Dragon med helm", 1149);
            loot.put("Adamant platebody", 1123);
            loot.put("Rune axe", 1359);
            loot.put("Rune kiteshield", 1201);
            loot.put("Rune longsword", 1303);
            loot.put("Rune med helm", 1147);
            loot.put("Rune full helm", 1163);
            loot.put("Rune 2h sword", 1319);
            loot.put("Dragon spear", 1249);
            loot.put("Rune kiteshield", 1201);
            loot.put("Rune battleaxe", 1373);
            loot.put("Rune sq shield", 1185);
            loot.put("Tooth half of key", 985);
            loot.put("Loop half of key", 987);
            loot.put("Runite bar", 2363);
            loot.put("Rune spear", 1247);
            loot.put("Lava battlestaff", 3053);
            loot.put("Dragonstone", 1615);
            loot.put("Uncut diamond", 1617);
            loot.put("Grimy ranarr weed", 207);
            loot.put("Lava dragon bones", 11943);
            loot.put("Black dragonhide", 1747);
            loot.put("Lava scale", 11992);
            loot.put("Adamant 2h sword", 1317);
            loot.put("Grimy avantoe", 211);
            loot.put("Grimy kwuarm", 213);
            loot.put("Grimy cadantine", 215);
            loot.put("Adamantite bar", 2361);
            loot.put("Fire talisman", 1442);





        }
        return loot;
    }
    public static HashMap<String, Integer> getLootSoFar(){
        return totalLoot;
    }
    public static void setLootUpdate(String item, int value){
        if(totalLoot.isEmpty()){
            loot.put("Looting bag",0);
            loot.put("Lava dragon bones", 0);
            loot.put("Black dragonhide", 0);
            loot.put("Lava scale", 0);
            loot.put("Rune dart", 0);
            loot.put("Rune knife", 0);
            loot.put("Lava battlestaff", 0);
            loot.put("Adamant 2h sword", 0);
            loot.put("Adamant platebody", 0);
            loot.put("Rune axe", 0);
            loot.put("Rune kiteshield", 0);
            loot.put("Rune longsword", 0);
            loot.put("Rune med helm", 0);
            loot.put("Rune full helm", 0);
            loot.put("Rune javelin", 0);
            loot.put("Blood rune", 0);
            loot.put("Runite bolts", 0);
            loot.put("Death rune",0);
            loot.put("Law rune",0);
            loot.put("Grimy ranarr weed", 0);
            loot.put("Grimy avantoe", 0);
            loot.put("Grimy kwuarm", 0);
            loot.put("Grimy cadantine", 0);
            loot.put("Dragon javelin heads", 0);
            loot.put("Fire orb", 0);
            loot.put("Adamantite bar", 0);
            loot.put("Onyx bolt tips", 0);
            loot.put("Fire talisman", 0);
            loot.put("Shield left half", 0);
            loot.put("Shield right half", 0);
            loot.put("Dragon med helm", 0);
            loot.put("Rune 2h sword", 0);
            loot.put("Dragon spear", 0);
            loot.put("Rune kiteshield", 0);
            loot.put("Rune battleaxe", 0);
            loot.put("Rune sq shield", 0);
            loot.put("Nature rune", 0);
            loot.put("Runite bar", 0);
            loot.put("Rune spear", 0);
            loot.put("Dragonstone", 0);
            loot.put("Tooth half of key", 0);
            loot.put("Loop half of key", 0);
            loot.put("Death rune", 0);
            loot.put("Law rune", 0);
            loot.put("Silver ore", 0);
            loot.put("Uncut diamond", 0);
            loot.put("Draconic visage",0);
        }
        loot.put(item,value);
    }
}
