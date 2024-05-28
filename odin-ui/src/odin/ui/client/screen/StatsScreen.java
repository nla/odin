package odin.ui.client.screen;

import loki.ui.Element;
import loki.ui.Label;
import loki.ui.client.elements.FlexFlow;
import loki.ui.client.elements.Spacer;
import loki.ui.client.elements.chart.BarChart;
import loki.ui.client.elements.chart.BarChartData;
import loki.ui.client.elements.chart.BarChartDataset;
import loki.ui.client.elements.chart.ChartLegendClickListener;
import loki.ui.client.elements.chart.ChartOptions;
import odin.stats.DomainStats;
import odin.stats.DomainStatsConstants;
import odin.stats.DomainStatsCounts;
import odin.stats.DomainStatsSeries;
import odin.ui.client.UIClientContext;
import odin.ui.client.screen.ui.Button;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import loki.CommonUtils;
import loki.StringPair;

/**
 * A screen that renders bulk harvest statistics data.
 */

public class StatsScreen extends UIScreen
{
    // Screen arg names
    
    private static final String ARG_CATEGORY = "category";
    private static final String ARG_COUNT_TYPE = "count";
    private static final String ARG_CATEGORY_STRUCTURE = "category-list";
    private static final String ARG_HIDDEN_CATEGORIES = "hidden-category";
    
    // Category groupings
    
    private static final StringPair CATEGORY_LIST__SHOW_TOP_20 = new StringPair("show-top-20", "LIST TOP 20");
    private static final StringPair CATEGORY_LIST__OCCLUDE_LOWEST_1P = new StringPair("occlude-lowest-1p", "GROUP LOWEST 1%");
    private static final StringPair CATEGORY_LIST__OCCLUDE_LOWEST_10P = new StringPair("occlude-lowest-10p", "GROUP LOWEST 10%");
    private static final StringPair CATEGORY_LIST__ALL = new StringPair("all", "LIST ALL");
    private static final StringPair[] CATEGORY_LIST_OPTIONS = new StringPair[]{
            CATEGORY_LIST__SHOW_TOP_20,
            CATEGORY_LIST__OCCLUDE_LOWEST_1P,
            CATEGORY_LIST__OCCLUDE_LOWEST_10P,
            CATEGORY_LIST__ALL
    };
    
    private static final String CATEGORY_ENTITY__OTHER = "Other";
    private static final String HIDDEN_CATEGORY_DELIM = ";";
    
    /**
     * Creates a new instance of this screen.
     * This should only been called from Screens.java
     */
    
    public StatsScreen()
    {
        super("stats");
    }
    
    /**
     * Creates a new state from this screen.
     * This is called by the navigator.
     */
    
    public ScreenState createState()
    {
        return new StatsScreenState();
    }
    
    /**
     * A static class that holds a rendered state of this screen.
     */
    
    public class StatsScreenState extends ScreenState
    {
        private UIClientContext context;
        private Element screenContainer;
        private Element canvasContainer;
        private Element dataSelectionPanel;
        private Element countTypePanel;
        private Element categoryPanel;
        
        private String currentCategory;
        private String currentCountType;
        private String currentCategoryStructure;
        
        private DomainStatsSeries currentSeriesData;
        private BarChart currentBarChart;
        
        private List<String> categoryIdList;
        private HashMap<String, CategoryState> categoryStates;
        private List<String> hiddenCategories;
        
        /**
         * This is called by the application's {@link loki.client.Navigator} to render the screen.
         * 
         * Renders the statistics screen, setting the filters and grouping according to any args present.
         * 
         * @param context       The web-app's context containing useful methods and objects to be used by the screen state.
         * @param container     The container to which the UI should is rendered.
         * @param args          The args (if any) that were specified when navigating to this screen.
         */
        
        public void renderScreen(UIClientContext context, Element container, StringPair... args) throws Exception
        {
            this.context = context;
            this.screenContainer = container;
            this.hiddenCategories = new ArrayList<String>();
            renderMain();
            
            // Parse args
            
            for(StringPair arg: args)
            {
                switch(arg.getKey())
                {
                    case ARG_CATEGORY:
                    {
                        currentCategory = arg.getValue();
                        break;
                    }
                    case ARG_COUNT_TYPE:
                    {
                        currentCountType = arg.getValue();
                        break;
                    }
                    case ARG_CATEGORY_STRUCTURE:
                    {
                        currentCategoryStructure = arg.getValue();
                        break;
                    }
                    case ARG_HIDDEN_CATEGORIES:
                    {
                        if(arg.getValue()!=null)
                        {
                            String[] split = arg.getValue().split(HIDDEN_CATEGORY_DELIM);
                            
                            for(String categoryId: split)
                            {
                                if(!categoryId.isEmpty())
                                {
                                    hiddenCategories.add(categoryId);
                                }
                            }
                        }
                        
                        break;
                    }
                    default:
                    {
                        throw new Exception("Arg ["+arg.getKey()+"] not supported.");
                    }
                }
            }
            if(currentCategory==null)
            {
                currentCategory = DomainStatsConstants.STATS_VIEW_CATEGORY__BY_TLD;
            }
            if(currentCountType==null)
            {
                currentCountType = DomainStatsConstants.STATS_VIEW_COUNT_TYPE__DOMAIN_COUNT;
            }
            if(currentCategoryStructure==null)
            {
                currentCategoryStructure = CATEGORY_LIST__SHOW_TOP_20.getKey();
            }
            
            // Load data
            
            refreshData();
        }
        
        private void renderMain()
        {
            // Render the main page layout
            
            canvasContainer = new Element("div");
            dataSelectionPanel = FlexFlow.create().directionDown().alignStretch();
            countTypePanel = FlexFlow.create().directionDown().alignStretch();
            categoryPanel = FlexFlow.create().directionDown().alignStretch();
            
            Element vFlow = FlexFlow.create().directionDown();
            
            Element hFlow = FlexFlow.create().directionRight().with(dataSelectionPanel);
            vFlow.add(hFlow);
            
            hFlow = FlexFlow.create().directionRight().with(countTypePanel, canvasContainer, categoryPanel);
            vFlow.add(hFlow);
            
            hFlow = FlexFlow.create().directionRight().with(new Button("option right reset", new Label("RESET FILTERS"), new Runnable(){
                public void run()
                {
                    for(CategoryState state: categoryStates.values())
                    {
                        state.setVisible(true);
                        currentBarChart.showDataset(state.getIndex());
                    }
                    
                    currentBarChart.update();
                    hiddenCategories.clear();
                    updateNavigationURL();
                }
            }));
            vFlow.add(Spacer.tall("1em"), hFlow);
            
            screenContainer.add(vFlow);
        }
        
        private void refreshData()
        {
            // If the currently selected category type does not use the currently selected count type, set the count type to the first available
            
            if(!DomainStatsConstants.STATS_VIEWS_BY_CATEGORY.get(currentCategory).contains(currentCountType))
            {
                currentCountType = DomainStatsConstants.STATS_VIEWS_BY_CATEGORY.get(currentCategory).get(0);
            }
            
            // Load the stats data from the server
            
            context.getDataService().getDomainStatsSeries(currentCategory, 0L, System.currentTimeMillis(), new AsyncCallback<DomainStatsSeries>(){
                public void onFailure(Throwable e)
                {
                    context.getErrorHandler().handleError(new Exception("Error loading stats data.", e));
                }
                
                public void onSuccess(DomainStatsSeries series)
                {
                    currentSeriesData = series;
                    categoryStates = new HashMap<String, CategoryState>();
                    
                    // Render chart
                    
                    renderChart();
                }
            });
        }
        
        public void renderChart()
        {
            updateNavigationURL();
            renderDataSelectionButtons();
            renderCountTypeButtons();
            renderCategoryStructureButtons();
            
            categoryIdList = new ArrayList<String>();
            
            // Build category ID list and sort by Y index value (larger to smaller)
            
            for(DomainStats stats: currentSeriesData.getStatsMap().values())
            {
                for(String categoryId: stats.getCategoryToCountsMap().keySet())
                {
                    if(!categoryIdList.contains(categoryId))
                    {
                        categoryIdList.add(categoryId);
                    }
                }
            }
            
            List<String> categoriesInOther = new ArrayList<String>();
            HashMap<String, Integer> counts = new HashMap<String, Integer>();
            int overallCount = 0;
            
            for(String categoryId: categoryIdList)
            {
                int totalCount = 0;
                
                for(Long time: currentSeriesData.getStatsMap().keySet())
                {
                    DomainStatsCounts count = currentSeriesData.getStatsMap().get(time).getCategoryToCountsMap().get(categoryId);
                    Long countValue;
                    
                    if(count!=null)
                    {
                        countValue = count.getCountsMap().get(currentCountType);
                        
                        if(countValue==null)
                        {
                            countValue = 0L;
                        }
                    }
                    else
                    {
                        countValue = 0L;
                    }
                    
                    totalCount = totalCount+countValue.intValue();
                }
                
                counts.put(categoryId, totalCount);
                overallCount = overallCount+totalCount;
            }
            
            Collections.sort(categoryIdList, new Comparator<String>(){
                public int compare(String a, String b)
                {
                    return -Integer.compare(counts.get(a), counts.get(b));
                }
            });
            
            // Summarise categories (if selected)
            
            double omitPercentage = 0d;
            
            if(currentCategoryStructure.equals(CATEGORY_LIST__OCCLUDE_LOWEST_1P.getKey()))
            {
                omitPercentage = 0.01d;
            }
            if(currentCategoryStructure.equals(CATEGORY_LIST__OCCLUDE_LOWEST_10P.getKey()))
            {
                omitPercentage = 0.1d;
            }
            
            if(omitPercentage>0d)
            {
                for(int i=categoryIdList.size()-1; i>=0; i--)
                {
                    String categoryId = categoryIdList.get(i);
                    double percentage = ((double)counts.get(categoryId))/(double)overallCount;
                    
                    if(percentage<=omitPercentage)
                    {
                        categoriesInOther.add(categoryId);
                    }
                }
                if(!categoriesInOther.isEmpty())
                {
                    for(String categoryId: categoriesInOther)
                    {
                        categoryIdList.remove(categoryId);
                    }
                    
                    categoryIdList.add(CATEGORY_ENTITY__OTHER);
                }
            }
            if(currentCategoryStructure.equals(CATEGORY_LIST__SHOW_TOP_20.getKey()))
            {
                while(categoryIdList.size()>20)
                {
                    categoriesInOther.add(categoryIdList.remove(categoryIdList.size()-1));
                }
                if(!categoriesInOther.isEmpty())
                {
                    categoryIdList.add(CATEGORY_ENTITY__OTHER);
                }
            }
            
            // Add category states (for retaining visibility status)
            
            for(int i=0; i<categoryIdList.size(); i++)
            {
                String categoryId = categoryIdList.get(i);
                
                if(!categoryStates.containsKey(categoryId))
                {
                    categoryStates.put(categoryId, new CategoryState(categoryId, i, !hiddenCategories.contains(categoryId)));
                }
            }
            
            // Build the bar chart datasets
            
            DateTimeFormat timeFormat = DateTimeFormat.getFormat("MM-yyyy");
            List<Long> timeList = new ArrayList<Long>(currentSeriesData.getStatsMap().keySet());
            Collections.sort(timeList);
            
            BarChartDataset[] datasets = new BarChartDataset[categoryIdList.size()];
            String[] colors = generateColors(datasets.length, 220, 360);
            
            for(int i=0; i<datasets.length; i++)
            {
                String categoryId = categoryIdList.get(i);
                String color = colors[i];
                BarChartData[] data = new BarChartData[currentSeriesData.getStatsMap().size()];
                
                for(int t=0; t<timeList.size(); t++)
                {
                    Long time = timeList.get(t);
                    int categoryCount = 0;
                    
                    List<String> dataCategories = categoryId.equals(CATEGORY_ENTITY__OTHER)?(categoriesInOther):(Arrays.asList(categoryId));
                    
                    for(String dataCategoryId: dataCategories)
                    {
                        DomainStatsCounts count = currentSeriesData.getStatsMap().get(time).getCategoryToCountsMap().get(dataCategoryId);
                        Long countValue;
                        
                        if(count!=null)
                        {
                            countValue = count.getCountsMap().get(currentCountType);
                            
                            if(countValue==null)
                            {
                                countValue = 0L;
                            }
                        }
                        else
                        {
                            countValue = 0L;
                        }
                        
                        categoryCount = categoryCount+countValue.intValue();
                    }
                    
                    data[t] = new BarChartData(timeFormat.format(new Date(time)), categoryCount);
                }
                
                datasets[i] = new BarChartDataset(categoryId, color, color, "black", data);
            }
            
            // Specify the legend listener
            
            ChartLegendClickListener legendClickListener = new ChartLegendClickListener(){
                public void onLegendItemClicked(int index)
                {
                    categoryStates.get(categoryIdList.get(index)).setVisible(currentBarChart.isDatasetVisible(index));
                    updateNavigationURL();
                }
            };
            
            try
            {
                // Render the bar chart
                
                currentBarChart = new BarChart();
                currentBarChart.setClassNames("chart-bar");
                canvasContainer.clear();
                canvasContainer.add(FlexFlow.create().setAdditionalClassNames("growing").directionDown().with(FlexFlow.create().directionRight().with(currentBarChart)));
                
                ChartOptions options = ChartOptions.create().legendPositionRight().textColor("ghostwhite");
                currentBarChart.render(options, legendClickListener, datasets); 
                
                for(int i=0; i<categoryIdList.size(); i++)
                {
                    String categoryId = categoryIdList.get(i);
                    
                    if(!categoryStates.get(categoryId).isVisible())
                    {
                        currentBarChart.hideDataset(i);
                    }
                }
                
                currentBarChart.update();
            }
            catch(Exception e)
            {
                context.getErrorHandler().handleError(new Exception("Error rendering data.", e));
            }
        }
        
        private void renderDataSelectionButtons()
        {
            dataSelectionPanel.clear();
            
            Element hFlow = FlexFlow.create().directionRight();
            dataSelectionPanel.add(hFlow, Spacer.tall("5em"));
            
            for(StringPair category: DomainStatsConstants.STATS_VIEW_CATEGORIES)
            {
                Button button = new Button("option center "+(category.getKey().equals(currentCategory)?"selected":""), new Label(category.getValue()), new Runnable(){
                    public void run()
                    {
                        // Temporarily disable the 'BY ESTIMATED LOCATION' button until implemented
                        
                        if(category.getKey().equals(DomainStatsConstants.STATS_VIEW_CATEGORY__BY_GEOLOCATION))
                        {
                            context.getErrorHandler().handleError(new Exception("Not yet implemented."));
                            return;
                        }
                        
                        //
                        
                        currentCategory = category.getKey();
                        hiddenCategories.clear();
                        refreshData();
                    }
                });
                
                hFlow.add(button);
            }
        }
        
        private void renderCountTypeButtons()
        {
            countTypePanel.clear();
            
            for(StringPair countType: DomainStatsConstants.STATS_VIEW_COUNT_TYPES)
            {
                if(DomainStatsConstants.STATS_VIEWS_BY_CATEGORY.get(currentCategory).contains(countType.getKey()))
                {
                    Button button = new Button("option left "+(countType.getKey().equals(currentCountType)?"selected":""), new Label(countType.getValue()), new Runnable(){
                        public void run()
                        {
                            currentCountType = countType.getKey();
                            renderChart();
                        }
                    });
                    
                    countTypePanel.add(button);
                }
            }
        }
        
        private void renderCategoryStructureButtons()
        {
            categoryPanel.clear();
            
            for(StringPair option: CATEGORY_LIST_OPTIONS)
            {
                Button button = new Button("option right "+(currentCategoryStructure.equals(option.getKey())?"selected":""), new Label(option.getValue()), new Runnable(){
                    public void run()
                    {
                        currentCategoryStructure = option.getKey();
                        renderChart();
                    }
                });
                
                categoryPanel.add(button);
            }
        }
        
        private void updateNavigationURL()
        {
            // Updates the navigation URL based on the currently selected filters and category groupings so that the URL can re-used to link to this exact state
            
            context.getNavigator().updateURL(getId(), 
                    new StringPair(ARG_CATEGORY, currentCategory),
                    new StringPair(ARG_COUNT_TYPE, currentCountType),
                    new StringPair(ARG_CATEGORY_STRUCTURE, currentCategoryStructure),
                    new StringPair(ARG_HIDDEN_CATEGORIES, CommonUtils.printArray(hiddenCategories, HIDDEN_CATEGORY_DELIM)));
        }
        
        private String[] generateColors(int count, int startingHue, int maxHue)
        {
            // Generate pastel colors
            
            String[] colors = new String[count];
            String light = "75%";
            String sat = "50%";
            double hue = startingHue;
            int interval = maxHue/count;
            
            for(int i=0; i<count; i++)
            {
                colors[i] = "hsl("+hue+","+sat+","+light+")";
                hue = hue+interval;
                
                while(hue>360)
                {
                    hue = hue-360;
                }
            }
            
            return colors;
        }
        
        public void disposeScreen()
        {
            // Nothing to clean up
        }
        
        private class CategoryState
        {
            private String categoryId;
            private int index;
            private boolean visible;
            
            public CategoryState(String categoryId, int index, boolean visible)
            {
                this.categoryId = categoryId;
                this.index = index;
                this.visible = visible;
            }

            public boolean isVisible()
            {
                return visible;
            }

            public void setVisible(boolean visible)
            {
                this.visible = visible;
                
                if(visible && hiddenCategories.contains(categoryId))
                {
                    hiddenCategories.remove(categoryId);
                }
                if(!visible && !hiddenCategories.contains(categoryId))
                {
                    hiddenCategories.add(categoryId);
                }
            }

            public int getIndex()
            {
                return index;
            }
        }
    }
}