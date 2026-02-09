package io.quickledger.dto.asset;

import java.io.Serializable;

public class SelectableItemDto implements Serializable {
    private Long id;
    private String name;

    public SelectableItemDto() {
    }
    public SelectableItemDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }
    public SelectableItemDto( String name) {
        this.name = name;
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
