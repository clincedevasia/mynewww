"use strict";

var global = this;

use(["ResourceUtils.js", "StringUtils.js", "DateUtils.js", "/libs/sightly/js/3rd-party/q.js"], function (ResourceUtils, StringUtils, DateUtils, Q) {
    var ListItemUtils = {};

    ListItemUtils.addSimpleSearchItems = function (listConfig) {
        var resultPromise = Q.defer();
        if (!global.Packages) {
            console.warn("Did not detect AEM platform, simple search mode of list component not available!");
        }

        if (listConfig.query && global.Packages) {
            var resourceResolver = resource.getResourceResolver();
            var search = resource.adaptTo(global.Packages.com.day.cq.search.SimpleSearch);
            search.setQuery(listConfig.query);
            search.setSearchIn(listConfig.startIn);
            var pagePredicate = new global.Packages.com.day.cq.search.Predicate("type", "type");
            pagePredicate.set("type", "cq:Page");
            search.addPredicate(pagePredicate);
            search.setHitsPerPage(100000);
            var searchResult = search.getResult();
            if (searchResult && searchResult.getHits) {

                var hitsIterator = searchResult.getHits().iterator();

                _deferredCollectSearchItems(hitsIterator).then(function (itemList) {
                    _addItems(resultPromise, itemList, listConfig);
                });
            }
        }

        return resultPromise.promise;
    };

    ListItemUtils.addFixedPathsItems = function (resultList, listConfig) {
        var resultPromise = Q.defer();

        var pagePaths = granite.resource.properties["pages"]
            || [];

        if (typeof pagePaths.length == "function") {
            // got only one string, convert to an array with one item
            var tmp = pagePaths;
            pagePaths = [tmp];
        }

        log.debug("Got list with " + pagePaths.length + " fixed page items");
        var iterableArray = {
            store: pagePaths,
            currentIdx: -1,
            next: function () {
                this.currentIdx++;
                return this.store[this.currentIdx];
            },
            hasNext: function () {
                return this.currentIdx + 1 < this.store.length;
            }
        };

        _deferredCollectSearchItems(iterableArray).then(function (itemList) {
            _addItems(resultPromise, itemList, listConfig);
        });

        return resultPromise.promise;
    };

    ListItemUtils.addQueryBuilderSearchItems = function (listConfig) {
         var resultPromise = Q.defer();
         if (!global.Packages) {
             console.warn("Did not detect AEM platform, advanced search mode of list component not available!");
         } else {
             var resourceResolver = resource.getResourceResolver();
             var session = resourceResolver.adaptTo(global.Packages.javax.jcr.Session);
             var queryBuilder = resourceResolver.adaptTo(global.Packages.com.day.cq.search.QueryBuilder);
             var query = queryBuilder.loadQuery(resource.getPath() + "/" + "savedquery", session);
             if (query) {
                 query.setHitsPerPage(listConfig.limit);
                 var searchResult = query.getResult();
                 if (searchResult && searchResult.getHits) {
                     var hitsIterator = searchResult.getHits().iterator();

                     _deferredCollectSearchItems(hitsIterator).then(function (itemList) {
                         _addItems(resultPromise, itemList, listConfig);
                     });
                 }
             }
         }

         return resultPromise.promise;
     };

    ListItemUtils.addChildItems = function (listConfig) {
         // get parent path, default to current page
         var resultPromise = Q.defer();
         var parentPath = granite.resource.properties["parentPage"]
                 || granite.resource.path;

         log.debug("Listing child items of " + parentPath);
         ResourceUtils.getResource(parentPath)
             .then(function (startResource) {
                 ResourceUtils.getContainingPage(startResource).then(function (startPage) {
                     log.debug("Adding children of page " + startPage.path + " to the list component");
                     startPage.getChildren().then(function (childList) {
                         _addItems(resultPromise, childList, listConfig, undefined);
                     });
                 });
             });

         return resultPromise.promise;
     };

    var test = function (listConfig, root) {
        // get parent path, default to current page
        var resultPromise = Q.defer();
        var parentPath = granite.resource.properties["parentPage"]
            || granite.resource.path;

        log.debug("Listing child items of " + parentPath);
        ResourceUtils.getResource(parentPath)
            .then(function (startResource) {
                ResourceUtils.getContainingPage(startResource).then(function (startPage) {
                    log.debug("Adding children of page " + startPage.path + " to the list component");
                    startPage.getChildren().then(function (childList) {
                        _addItems(resultPromise, childList, listConfig, undefined);
                    });
                });
            });

        return resultPromise.promise;
    };


    var _unique = function(arr) {
        var hash = {}, result = [];
        for (var i = 0, l = arr.length; i < l; ++i) {
            if (!hash.hasOwnProperty(arr[i])) { //it works with objects! in FF, at least
                hash[arr[i]] = true;
                result[result.length] = arr[i];
            }
        }
        return result;
    }
     /**
     * Collects search result items returned through an iterator
     * Returns a promise that will be resolved asynchronous
     */
    var _deferredCollectSearchItems = function (itemIterator, itemPromise, itemList) {
        if (!itemPromise) {
            itemPromise = Q.defer();
            log.debug("Deferred collecting items from an iterator");
        }

        if (!itemList) {
            itemList = [];
        }

        if (itemIterator.hasNext()) {
            var resPath = "";
            try {
                var nextItem = itemIterator.next();
                if (nextItem.getResource) {
                    resPath = nextItem.getResource().getPath();
                } else if (nextItem.getPath) {
                    resPath = nextItem.getPath();
                } else {
                    log.warn("Can't determine search item path " + nextItem);
                    resPath = nextItem;
                }
            } catch (e) {
                log.error(e);
            }
            log.debug("Deferred collecting item with path " + resPath);
            granite.resource.resolve(resPath).then(function (hitResource) {
                itemList.push(hitResource);
                _deferredCollectSearchItems(itemIterator, itemPromise, itemList);
            });
        } else {
            log.debug("Iterator has no more items, resolving collection with " + itemList.length);
            itemPromise.resolve(itemList);
        }

        return itemPromise.promise;
    };

    var _addItems = function (resultPromise, itemList, listConfig) {
        if (!itemList) {
            return;
        }

        var destination = [];

        var collectItems = function (allItems, currentIndex, collector, collectPromise) {
            if (!collectPromise) {
                collectPromise = Q.defer();
                log.debug("Collecting all " + allItems.length + " result items");
            }

            if (!collector) {
                collector = [];
            }

            if (allItems.length == 0) {
                log.debug("Resolving collection, total " + collector.length + " items");
                collectPromise.resolve(collector);
                return collectPromise.promise;
            }

            var item = allItems[currentIndex];
            log.debug("Collecting item " + item.path);
            ResourceUtils.getContainingPage(item)
                .then(function (pageContainingItem) {
                    log.debug("Collecting item's page " + pageContainingItem.path);
                    // need to set item.title to jcr:title of jcr:content node
                    ResourceUtils.getPageProperties(pageContainingItem)
                        .then(function (pageProps) {
                            pageContainingItem.title = pageProps["jcr:title"];
                            var desctext = pageProps["jcr:description"];
                            var subtitle = pageProps["subtitle"];
                            if(desctext)
                                desctext = StringUtils.truncate(desctext, 25);
                            pageContainingItem.description = desctext;
                            pageContainingItem.orderByData = pageProps[listConfig.orderBy];
                            pageContainingItem.tags = pageProps["cq:tags"];
                            pageContainingItem.subtitle = subtitle;
                            collector.push(pageContainingItem);

                            if (currentIndex + 1 <= allItems.length -1) {
                                log.debug("Collecting next item in the list");
                                collectItems(allItems, currentIndex + 1, collector, collectPromise);
                            } else {
                                log.debug("Resolving collection, total " + collector.length + " items");
                                collectPromise.resolve(collector);
                            }
                        });
                });

            return collectPromise.promise;
        };

        collectItems(itemList, 0)
            .then(function (fullList) {
                log.debug("Collected " + fullList.length + " total items for the list");
                // apply ordering, if any
                if (listConfig.orderBy) {
                    log.debug("Need to order the list by [" + listConfig.orderBy + "]");
                    fullList.sort(function (page1, page2) {
                        var prop1 = page1.orderByData
                            || page2.orderByData
                            || "";
                        var prop2 = page2.orderByData
                            || page1.orderByData
                            || "";
                        log.debug("[Sorting list] Comparing " + prop1 + " with " + prop2);
                        if (prop1.compareTo) {
                            log.debug("[Sorting list] Using 'compareTo' method for comparison");
                            return prop1.compareTo(prop2)
                        } else if (prop1.localeCompare) {
                            log.debug("[Sorting list] Using 'localeCompare' method for comparison");
                            return prop1.localeCompare(prop2);
                        }
                        return -1;
                    });
                    log.debug("Sorting finished!");
                }

                var currentIndex = -1;
                var count = 0;
                var hasMore = false;
                var allTags = [];
                for (var currentIndex = 0 ; currentIndex < fullList.length ; currentIndex++) {
                    // skip items until start index or stop if global limit is reached
                    if (currentIndex >= listConfig.limit) {
                        break;
                    }
                    if (listConfig.pageStart >= 0 && currentIndex < listConfig.pageStart) {
                        continue;
                    }

                    // stop when reached page maximum
                    if (listConfig.pageMax > 0 && count >= listConfig.pageMax ) {
                        hasMore = true;
                        break;
                    }

                    count++;
                    var currentTags=fullList[currentIndex].tags;
                    for(var tag in currentTags){
                        allTags.push(currentTags[tag]);
                    }


                    destination.push({
                        item: fullList[currentIndex],
                        itemName: fullList[currentIndex].title,
                        itemDescription: fullList[currentIndex].description,
                        itemSubtitle:  fullList[currentIndex].subtitle,
                        modifiedDate: DateUtils.getModifiedDate(fullList[currentIndex]),
                        hasimage: ResourceUtils.hasImage(fullList[currentIndex]),
                        currentTags: currentTags
                    });
                }

                // apply limit
                destination = destination.slice(0, listConfig.limit)
                var resultingTags = _unique(allTags)
                resultPromise.resolve({
                    renderList: destination,
                    hasMore: hasMore,
                    allTags: resultingTags
                });
            });
    }

    return ListItemUtils;
});