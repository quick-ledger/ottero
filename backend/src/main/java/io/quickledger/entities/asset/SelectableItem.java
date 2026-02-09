package io.quickledger.entities.asset;

import io.quickledger.entities.BaseEntity;
import jakarta.persistence.*;

@Entity
public class SelectableItem extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Selectable selectable;
    private String name;

    public SelectableItem(Long id) {
        this.id = id;
    }

    public SelectableItem() {
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Selectable getSelectable() {
        return selectable;
    }

    public void setSelectable(Selectable selectable) {
        this.selectable = selectable;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }




}
