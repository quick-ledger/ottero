package io.quickledger.entities.asset;

import io.quickledger.entities.BaseEntity;
import jakarta.persistence.*;

import java.util.List;

@Entity
public class Selectable extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Enumerated(EnumType.STRING)
    private SelectableType type;

    @OneToMany(mappedBy = "selectable", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<SelectableItem> items;


    public enum SelectableType {
        FREE_SELECTABLE,
        UNIT
    }

    public Selectable(Long id) {
        this.id = id;
    }

    public Selectable() {
    }

    public List<SelectableItem> getItems() {
        return items;
    }

    public void setItems(List<SelectableItem> items) {
        this.items = items;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SelectableType getType() {
        return type;
    }

    public void setType(SelectableType type) {
        this.type = type;
    }
}
