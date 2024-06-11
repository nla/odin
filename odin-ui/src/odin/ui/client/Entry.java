package odin.ui.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.rpc.AsyncCallback;
import loki.ErrorHandler;
import loki.ui.Document;
import loki.ui.Element;
import loki.ui.Label;
import loki.ui.UIUtils;
import loki.ui.client.Navigator;
import loki.ui.client.async.AsyncBatch;
import loki.ui.client.async.AsyncTask;
import loki.ui.client.elements.FlexFlow;
import loki.ui.client.elements.Spacer;
import loki.ui.client.elements.chart.Chart;
import loki.ui.client.elements.map.Map;
import loki.ui.client.screen.Screen;
import odin.ui.client.screen.Screens;
import odin.ui.client.screen.ui.Dialog;
import odin.ui.client.screen.ui.FontIcon;

/**
 * The entry point class of the ODIN GWT client application.
 */

public class Entry implements EntryPoint
{
    private UIClientContext context;
    private Navigator<UIClientContext> navigator;
    private Element mainPanel;

    /**
     * The entry point method of the ODIN GWT client application. Sets up the application and navigates to the default screen.
     */

    public void onModuleLoad()
    {
        // Workaround or firefox framerate rendering (Firefox renders at a low FPS if the GWT iframe is hidden, so we move it to the corner of the page)

        implementFirefoxWorkaround();

        // Render shell

        mainPanel = FlexFlow.create().setAdditionalClassNames("stretched-x stretched-y");
        Document.getRootDocument().getBody().add(mainPanel);

        // Create error handler

        ErrorHandler errorHandler = new ErrorHandler(){
            public void handleError(Throwable e)
            {
                UIUtils.log(e);

                Dialog dialog = new Dialog("dialog-error");
                dialog.setTitle(FlexFlow.create().directionRight().justifyStart().with(new FontIcon("fas fa-triangle-exclamation"), Spacer.wide("0.5em"), new Label("Hold on")));
                dialog.setContent(FlexFlow.create().setAdditionalClassNames("growing").with(new Element("div", "scrolling").with(new Label(e.getMessage()))));
                dialog.show();
            }
        };

        context = new UIClientContext(errorHandler);

        // Batch asynchronous tasks

        AsyncBatch batch = new AsyncBatch(new AsyncCallback<Void>(){
            public void onFailure(Throwable e)
            {
                errorHandler.handleError(e);
            }

            public void onSuccess(Void result)
            {
                // Asynchronously tasks are complete, initialise the UI

                navigator = new Navigator<UIClientContext>(mainPanel, Screens.STATS, errorHandler){
                    protected Screen<UIClientContext> getScreen(String id)
                    {
                        return Screens.getScreen(id);
                    }
                };
                context.setNavigator(navigator);
                navigator.initialise(context, true);
            }
        });

        // Asynchronously enable chart rendering

        batch.addTask(new AsyncTask(){
            public void run(AsyncCallback<Void> onComplete)
            {
                Chart.enableChartRendering(new Runnable(){
                    public void run()
                    {
                        onComplete.onSuccess(null);
                    }
                });
            }
        });

        // Asynchronously enable map rendering

        batch.addTask(new AsyncTask(){
            public void run(AsyncCallback<Void> onComplete)
            {
                Map.enableMapRendering(new Runnable(){
                    public void run()
                    {
                        onComplete.onSuccess(null);
                    }
                });
            }
        });

        batch.run();
    }

    private static native void implementFirefoxWorkaround()
    /*-{
		var gwtFrame = $doc.getElementById('odin_ui');
		gwtFrame.style.left = '0px';
		gwtFrame.style.top = '0px';
    }-*/;
}
