package odin.ui.client.screen;

import java.util.HashMap;

/**
 * A static class that holds a map of screens and their IDs.
 * Useful for referencing instances of screens.
 * New screen implementations should be added under the 'List of screens' section.
 */

public class Screens
{
    private static final HashMap<String, UIScreen> SCREENS = new HashMap<String, UIScreen>();
    
    // List of screens
    
    public static final StatsScreen STATS = add(new StatsScreen());
    
    //
    
    private static <T extends UIScreen> T add(T screen)
    {
        SCREENS.put(screen.getId(), screen);
        return screen;
    }
    
    /**
     * @return      the screen represented by the specified ID, or null if none exists.
     */
    
    public static UIScreen getScreen(String id)
    {
        return SCREENS.get(id);
    }
}