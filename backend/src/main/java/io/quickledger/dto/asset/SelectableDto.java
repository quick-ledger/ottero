package io.quickledger.dto.asset;

import io.quickledger.entities.asset.Selectable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SelectableDto {
    private Long id;
    private String name;
    private List<SelectableItemDto> selectableItems = new ArrayList<>();
    private Selectable.SelectableType type;

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

    public List<SelectableItemDto> getSelectableItems() {
        return selectableItems;
    }

    public void setSelectableItems(List<SelectableItemDto> selectableItems) {
        this.selectableItems = selectableItems;
    }

    public Selectable.SelectableType getType() {
        return type;
    }

    public void setType(Selectable.SelectableType type) {
        this.type = type;
    }
}
