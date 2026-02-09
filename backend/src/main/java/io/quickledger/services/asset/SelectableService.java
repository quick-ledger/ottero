package io.quickledger.services.asset;

import io.quickledger.dto.asset.SelectableDto;
import io.quickledger.dto.asset.SelectableItemDto;
import io.quickledger.entities.asset.Selectable;
import io.quickledger.entities.asset.SelectableItem;
import io.quickledger.repositories.asset.SelectableItemRepository;
import io.quickledger.repositories.asset.SelectableRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SelectableService {
    private static final Logger logger = LoggerFactory.getLogger(SelectableService.class);

    private final SelectableRepository selectableRepository;
    private final SelectableItemRepository selectableItemRepository;

    public SelectableService(SelectableRepository selectableRepository, SelectableItemRepository selectableItemRepository) {
        this.selectableRepository = selectableRepository;
        this.selectableItemRepository = selectableItemRepository;
    }


    //this should take care of both selectable and unit
    public void saveSelectables(SelectableDto selectableDto) {
        Selectable selectable = new Selectable();
        selectable.setId(selectableDto.getId());
        selectable.setName(selectableDto.getName());
        selectable.setType(selectableDto.getType());
        selectableRepository.save(selectable);

        //loop through the selectable items and save them
        selectableDto.getSelectableItems().stream().forEach(selectableItem -> {
            SelectableItem item = new SelectableItem();
            item.setId(selectableItem.getId());
            item.setName(selectableItem.getName());
            item.setSelectable(selectable);
            selectableItemRepository.save(item);
        });
    }

    public List<SelectableDto> getAllSelectables(Selectable.SelectableType type) {
        List<Selectable> selectables = selectableRepository.findAllByType(type);

        //rewrite below with iterator
        List<SelectableDto> selectableDtos = new ArrayList<>();
        selectables.forEach(selectable -> {
            SelectableDto selectableDto = new SelectableDto();
            selectableDto.setId(selectable.getId());
            selectableDto.setName(selectable.getName());
            selectableDto.setType(selectable.getType());
            selectable.getItems().stream().forEach(selectableItem -> {
                SelectableItemDto selectableItemDto = new SelectableItemDto();
                selectableItemDto.setId(selectableItem.getId());
                selectableItemDto.setName(selectableItem.getName());
                selectableDto.getSelectableItems().add(selectableItemDto);
            });
        });
        return selectableDtos;
    }

}
