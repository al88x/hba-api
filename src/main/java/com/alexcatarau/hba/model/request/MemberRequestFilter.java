package com.alexcatarau.hba.model.request;

public class MemberRequestFilter {

    private Integer page;
    private Integer pageSize;

    public MemberRequestFilter() {
        this.page = 1;
        this.pageSize = 10;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getOffset() {
        return (page - 1) * pageSize;
    }
}
