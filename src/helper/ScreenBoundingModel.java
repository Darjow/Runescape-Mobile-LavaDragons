package helper;

import org.powbot.api.Rectangle;
import org.powbot.api.rt4.Components;
import org.powbot.api.rt4.Widgets;

public class ScreenBoundingModel {

    public static final Rectangle screen = Widgets.widget(122).component(1).visible()?  Widgets.widget(122).component(1).boundingRect() :  Widgets.widget(651).component(1).boundingRect();
    public static final Rectangle worldHoppingScreen = Widgets.widget(69).component(15).boundingRect();
}
