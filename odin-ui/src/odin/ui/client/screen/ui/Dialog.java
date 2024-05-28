package odin.ui.client.screen.ui;

import loki.ui.Document;
import loki.ui.Element;
import loki.ui.client.elements.FlexFlow;

public class Dialog extends Element
{
    private Element dialogPanel;
    private Element titlePanel;
    private Element contentPanel;
    
    private boolean closable;
    
    public Dialog(String classNames)
    {
        this(classNames, true);
    }
    
    public Dialog(String classNames, boolean closable)
    {
        super("div", "dialog-container");
        
        this.closable = closable;
        this.dialogPanel = new Element("div", "dialog "+classNames);
        this.titlePanel = new Element("div", "dialog-title-panel");
        this.contentPanel = new Element("div", "dialog-content-panel");
        
        render();
    }
    
    private void render()
    {
        FlexFlow topPanel = FlexFlow.create()
                .setAdditionalClassNames("dialog-top-panel")
                .directionRight()
                .justifySpaceBetween()
                .alignCenter();
        topPanel.add(titlePanel);
        
        if(closable)
        {
            topPanel.add(new Button("dialog-close", new FontIcon("fas fa-times"), new Runnable(){
                public void run()
                {
                    close();
                }
            }));
        }
        
        FlexFlow flow = FlexFlow.create()
                .setAdditionalClassNames("growing")
                .directionDown()
                .justifyCenter()
                .alignStretch();
        flow.add(topPanel);
        flow.add(contentPanel);
        
        dialogPanel.add(flow);
        
        Element fadeAcross = FlexFlow.create().setAdditionalClassNames("dialog-container-stretch-w").directionRight().with(dialogPanel);
        Element fadeDown = FlexFlow.create().setAdditionalClassNames("dialog-container-stretch-h").directionDown().with(fadeAcross);
        add(fadeDown);
        
        if(closable)
        {
            applyCloseOnClick(this, fadeAcross, fadeDown);
        }
    }
    
    public Dialog setTitle(Element title)
    {
        titlePanel.clear();
        titlePanel.add(title);
        return this;
    }
    
    public Dialog setContent(Element content)
    {
        contentPanel.clear();
        contentPanel.add(content);
        return this;
    }
    
    public void show()
    {
        Document.getRootDocument().getBody().add(this);
    }
    
    public void close()
    {
        removeSelf();
    }
    
    private void applyCloseOnClick(Element... elements)
    {
        for(Element element: elements)
        {
            element.setOnClickSelfOnly(new Runnable(){
                public void run()
                {
                    close();
                }
            });
        }
    }
}