package odin.stats;

import loki.StringPair;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
* A set of constants used by the application.
*/

public class DomainStatsConstants
{
    public static final String STATS_VIEW_CATEGORY__BY_TLD = "by_tld";
    public static final String STATS_VIEW_CATEGORY__BY_GEOLOCATION = "by_geolocation";
    public static final String STATS_VIEW_CATEGORY__BY_MIME_TYPE = "by_mime_type";
    
    public static final StringPair[] STATS_VIEW_CATEGORIES = new StringPair[]{
           new StringPair(STATS_VIEW_CATEGORY__BY_TLD, "BY DOMAIN SUFFIX"),
           new StringPair(STATS_VIEW_CATEGORY__BY_MIME_TYPE, "BY MIME TYPE"),
           new StringPair(STATS_VIEW_CATEGORY__BY_GEOLOCATION, "BY ESTIMATED LOCATION"),
    };
    public static final String STATS_VIEW_COUNT_TYPE__DOMAIN_COUNT = "domain_count";
    public static final String STATS_VIEW_COUNT_TYPE__URL_COUNT = "url_count";
    public static final String STATS_VIEW_COUNT_TYPE__MB_SIZE = "mb_size";
    
    public static final StringPair[] STATS_VIEW_COUNT_TYPES = new StringPair[]{
            new StringPair(STATS_VIEW_COUNT_TYPE__DOMAIN_COUNT, "DOMAIN COUNT"),
            new StringPair(STATS_VIEW_COUNT_TYPE__URL_COUNT, "URL COUNT"),
            new StringPair(STATS_VIEW_COUNT_TYPE__MB_SIZE, "MB DATA"),
    };
    
    public static final HashMap<String, List<String>> STATS_VIEWS_BY_CATEGORY = new HashMap<String, List<String>>();
    
    static
    {
        STATS_VIEWS_BY_CATEGORY.put(STATS_VIEW_CATEGORY__BY_TLD, Arrays.asList(
                STATS_VIEW_COUNT_TYPE__DOMAIN_COUNT,
                STATS_VIEW_COUNT_TYPE__URL_COUNT,
                STATS_VIEW_COUNT_TYPE__MB_SIZE
        ));
        
        STATS_VIEWS_BY_CATEGORY.put(STATS_VIEW_CATEGORY__BY_GEOLOCATION, Arrays.asList(
                STATS_VIEW_COUNT_TYPE__DOMAIN_COUNT,
                STATS_VIEW_COUNT_TYPE__URL_COUNT,
                STATS_VIEW_COUNT_TYPE__MB_SIZE
        ));
        
        STATS_VIEWS_BY_CATEGORY.put(STATS_VIEW_CATEGORY__BY_MIME_TYPE, Arrays.asList(
                STATS_VIEW_COUNT_TYPE__URL_COUNT,
                STATS_VIEW_COUNT_TYPE__MB_SIZE
        ));
    }
}