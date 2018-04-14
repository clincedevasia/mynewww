package com.statoil.reinvent.queries;


import com.day.cq.wcm.api.Page;

import java.util.List;

public class TagQueryResult {
    public final boolean hasMore;
    public final List<Page> results;

    public TagQueryResult(List<Page> results, boolean hasMore) {
        this.hasMore = hasMore;
        this.results = results;
    }
}
