package com.alexcatarau.hba.model.response;

import com.alexcatarau.hba.model.request.MemberRequestFilter;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

public abstract class ListResponseModel<TItem, TFilter extends MemberRequestFilter> {
    private final List<TItem> items;
    private final TFilter filter;
    private final int totalNumberOfItems;

    public ListResponseModel(List<TItem> items, TFilter filter, int totalNumberOfItems) {
        this.items = items;
        this.filter = filter;
        this.totalNumberOfItems = totalNumberOfItems;
    }

    protected abstract String getRootUrl();

    public List<TItem> getItems() {
        return items;
    }

    public int getTotalNumberOfItems() {
        return totalNumberOfItems;
    }

    public URI getPreviousPage() {
        if (filter.getPage() <= 1) {
            return null;
        }

        UriBuilder builder = UriComponentsBuilder.fromPath("/suspects")
                .queryParam("page", filter.getPage() - 1)
                .queryParam("pageSize", filter.getPageSize());

        return builder.build();
    }

    public URI getNextPage() {
        if (filter.getPage() * filter.getPageSize() >= totalNumberOfItems) {
            return null;
        }

        UriBuilder builder = UriComponentsBuilder.fromPath(getRootUrl())
                .queryParam("page", filter.getPage() + 1)
                .queryParam("pageSize", filter.getPageSize());

        return builder.build();
    }
}
