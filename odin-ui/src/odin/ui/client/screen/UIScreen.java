package odin.ui.client.screen;

import loki.ui.client.screen.Screen;
import odin.ui.client.UIClientContext;

public abstract class UIScreen extends Screen<UIClientContext>
{
    public UIScreen(String id)
    {
        super(id);
    }
}
