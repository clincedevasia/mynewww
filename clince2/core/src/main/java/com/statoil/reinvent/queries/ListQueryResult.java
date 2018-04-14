package com.statoil.reinvent.queries;

import com.statoil.reinvent.components.list.ListItem;

import java.util.List;

/**
 * Created by andreas on 13/10/15.
 */
public class ListQueryResult {

    public final boolean hasMore;
    public final List<ListItem> results;
    public final Integer firstYear;

    public ListQueryResult(List<ListItem> results, boolean hasMore, Integer firstYear) {
        this.hasMore = hasMore;
        this.results = results;
        this.firstYear = firstYear;
    }
}
