package odin.ui.client.screen.ui;

import loki.ui.Element;

public class Button extends Element
{
    public Button(String classNames, Element content, Runnable onClick)
    {
        super("div", "button "+classNames);
        add(content);
        setOnClick(onClick);
    }
}
