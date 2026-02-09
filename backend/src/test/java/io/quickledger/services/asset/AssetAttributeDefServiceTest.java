package io.quickledger.services.asset;

import io.quickledger.dto.asset.SelectableDto;
import io.quickledger.dto.asset.SelectableItemDto;
import io.quickledger.entities.asset.*;
import io.quickledger.mappers.asset.AssetAttributeDefinitionMapper;
import io.quickledger.mappers.asset.AssetAttributeValueMapper;
import io.quickledger.mappers.asset.AssetMapperImpl;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AssetAttributeDefServiceTest {

    @Autowired
    AssetDefinitionService assetAttributeDefinitionService;
    @Autowired
    AssetAttributeValueService assetAttributeValueService;

    @Autowired
    SelectableService selectableService;
    @Autowired
    private AssetMapperImpl assetMapperImpl;

    @Autowired
    private AssetAttributeDefinitionMapper assetAttributeDefinitionMapper;

    @Autowired
    private AssetAttributeValueMapper assetAttributeValueMapper;

    private static final Logger logger = LoggerFactory.getLogger(AssetAttributeDefServiceTest.class);

    @Test
    public void createSelectableUnit() {
        SelectableDto selectable = new SelectableDto();
        selectable.setName("Weight");
        selectable.setType(Selectable.SelectableType.UNIT);
        selectable.getSelectableItems().add(new SelectableItemDto("KG"));
        selectable.getSelectableItems().add(new SelectableItemDto("G"));

        selectableService.saveSelectables(selectable);
    }

    @Test
    public void createSelectableValues() {
        SelectableDto selectable = new SelectableDto();
        selectable.setName("Fridge Colors");
        selectable.setType(Selectable.SelectableType.FREE_SELECTABLE);
        selectable.getSelectableItems().add(new SelectableItemDto("Blue"));
        selectable.getSelectableItems().add(new SelectableItemDto("Red"));

        selectableService.saveSelectables(selectable);
    }


    @Test
    public void getAllSelectables() {
        selectableService.getAllSelectables(Selectable.SelectableType.UNIT);
    }

    @Test
    public void saveAssetDef_text_required_with_default() {
        AssetAttributeDefinition assetAttributeDefinition = new AssetAttributeDefinition();
        assetAttributeDefinition.setName("color");
        assetAttributeDefinition.setDefaultValue("Red");
        assetAttributeDefinition.setFieldValueType(AssetAttributeDefinition.ValueType.TEXT);
        assetAttributeDefinition.setRequired(true);

        assetAttributeDefinitionService.save(assetAttributeDefinition, 0L);
    }

    @Test
    public void saveAssetDef_text_required_fails_validation() {
        AssetAttributeDefinition assetAttributeDefinition = new AssetAttributeDefinition();
        assetAttributeDefinition.setName("color"+System.currentTimeMillis());
        assetAttributeDefinition.setDefaultValue("Red");
        assetAttributeDefinition.setFieldValueType(AssetAttributeDefinition.ValueType.NUMBER);
        assetAttributeDefinition.setRequired(true);
        assetAttributeDefinitionService.save(assetAttributeDefinition, 0L);
    }

    @Test
    public void saveAssetDef_with_unit() {
        AssetAttributeDefinition assetAttributeDefinition = new AssetAttributeDefinition();
        assetAttributeDefinition.setName("weight"+System.currentTimeMillis());
        assetAttributeDefinition.setFieldValueUnit(new Selectable(2L));
        assetAttributeDefinition.setDefaultValue("33");
        assetAttributeDefinition.setFieldValueType(AssetAttributeDefinition.ValueType.NUMBER);
        assetAttributeDefinitionService.save(assetAttributeDefinition, 0L);
    }

    @Test
    public void saveAssetDef_with_selectable_value() {
        AssetAttributeDefinition assetAttributeDefinition = new AssetAttributeDefinition();
        assetAttributeDefinition.setName("color"+System.currentTimeMillis());
        //assetAttributeDefinition.setFieldValueUnit(new Selectable(2L));
        //assetAttributeDefinition.setDefaultValue("33");
        assetAttributeDefinition.setFieldValueType(AssetAttributeDefinition.ValueType.SELECTABLE);
        assetAttributeDefinition.setFieldSelectableValue(new Selectable(3L));
        assetAttributeDefinitionService.save(assetAttributeDefinition, 0L);
    }

    @Test
    public void renameAttributeDefName(){
        AssetAttributeDefinition def = assetAttributeDefinitionService.findById(4L);
        def.setName("weight-" + System.currentTimeMillis());
        assetAttributeDefinitionService.save(def );
    }

    @Test
    public void saveAttributeValue_simple(){
        AssetAttributeDefinition assetAttributeDefinition = new AssetAttributeDefinition();
        assetAttributeDefinition.setName("color"+System.currentTimeMillis());
        assetAttributeDefinition.setDefaultValue("Red");
        assetAttributeDefinition.setFieldValueType(AssetAttributeDefinition.ValueType.TEXT);
        assetAttributeDefinition.setRequired(true);
        assetAttributeDefinition = assetAttributeDefinitionService.save(assetAttributeDefinition, 0L);
        AssetAttributeValue assetAttributeValue1 = new AssetAttributeValue();
        assetAttributeValue1.setValue("Red2");
        assetAttributeValue1.setDefinition(assetAttributeDefinition);
        assetAttributeValueService.saveAttributeValue(assetAttributeValue1);
    }

    @Test
    public void saveAttributeValue_selectable(){
        AssetAttributeDefinition assetAttributeDefinition = new AssetAttributeDefinition();
        assetAttributeDefinition.setName("color"+System.currentTimeMillis());
        assetAttributeDefinition.setFieldValueType(AssetAttributeDefinition.ValueType.SELECTABLE);
        assetAttributeDefinition.setRequired(true);
        assetAttributeDefinition = assetAttributeDefinitionService.save(assetAttributeDefinition, 0L);

        AssetAttributeValue assetAttributeValue1 = new AssetAttributeValue();
        assetAttributeValue1.setSelectedValueItem(new SelectableItem(3L));
        assetAttributeValue1.setDefinition(assetAttributeDefinition);
        assetAttributeValueService.saveAttributeValue(assetAttributeValue1);
    }

}
