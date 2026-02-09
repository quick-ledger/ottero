import React, {useEffect, useState} from 'react';
import {useParams} from 'react-router-dom';
import {Container} from 'reactstrap';
import {useApi} from '../../helpers/api';
import {useSelector} from 'react-redux';


import Form from '@rjsf/core';
import validator from '@rjsf/validator-ajv8';

const AssetEdit = (props) => {

  const [assetDefs, setAssetDefs] = useState([{ id: null, name: '', productAttributes: [] }]);
  const [selectedAssetDef, setSelectedAssetDef] = useState({});

  const { id } = useParams();
  const api = useApi();
  const selectedCompanyName = useSelector(state => {
    return state.app.selectedCompanyName;
  });

  const selectedCompanyId = useSelector(state => {
    return state.app.selectedCompanyId;
  });


  useEffect(() => {
    // if (id === 'new') {
    //   return;
    // }

    const fetchAssetDefs = async () => {
      try {
        //list for drop down
        const response = await api.get(`/api/companies/${selectedCompanyId}/product-definition`);

        if (response.status !== 200) {
          throw new Error('HTTP status ' + response.status);
        }

        setAssetDefs(response.data);
      } catch (error) {
        console.error('Failed to fetch asset:', error);
      }
    };

    fetchAssetDefs();
  }, [id]);

  const schema2 = {

    "type": "object",
    "title": "Mani-write",
    "$schema": "https://json-schema.org/draft/2020-12/schema",
    "required": [
      "Length",
      "roll weight"
    ],
    "properties": {
      "Length": {
        "type": "object",
        "required": [
          "value",
          "unit"
        ],
        "properties": {
          "unit": {
            "enum": [
              "m",
              "cm",
              "mm"
            ],
            "type": "string"
          },
          "value": {
            "type": "number",
            "description": "length"
          }
        },
        "description": "length"
      },
      "roll weight": {
        "type": "object",
        "required": [
          "value",
          "unit"
        ],
        "properties": {
          "unit": {
            "enum": [
              "gr",
              "kg"
            ],
            "type": "string"
          },
          "value": {
            "type": "number",
            "description": "roll weight"
          }
        },
        "description": "roll weight"
      },
      "specification url": {
        "type": "object",
        "properties": {
          "value": {
            "type": "string",
            "description": "spec url"
          }
        },
        "description": "spec url"
      }
    },
    "description": "awesome write"


  };
  const schema = {
    type: "object",
    title: "ManiWire",
    description: "this is some test description that can be used in future like add assetId here or something usefull for our internal",
    properties: {
      wireLength: {
        type: "object",
        description: "Length of the wire including the unit of measurement",
        pattern: "^[a-zA-Z]+$",
        properties: {
          value: {
            type: "number",
            description: "Numerical value of the length"
          },
          unit: {
            type: "string",
            description: "Unit of measurement for the wire length",
            enum: ["cm", "mm", "m"],
            pattern: "^(cm|mm|m)$"
          }
        },
        required: ["value", "unit"]
      },
      operatingTemperature: {
        type: "object",
        description: "Operating temperature range of the device",
        properties: {
          minTemperature: {
            type: "number",
            description: "Minimum operating temperature",
            default: 0
          },
          maxTemperature: {
            type: "number",
            description: "Maximum operating temperature",
            default: 100
          },
          unit: {
            type: "string",
            description: "Unit of measurement for temperature",
            enum: ["C", "F"]
          }
        },
        required: ["minTemperature", "maxTemperature", "unit"]
      }
    },
    required: ["wireLength", "operatingTemperature"]
  };


  const handleSubmit = (event) => {
    console.log(JSON.stringify(event.formData, null, 2));
  };

  const handleSelectChange = (event) => {
    console.log(JSON.stringify(assetDefs[event.target.value].productAttributes[0].schema, null, 2));
    console.log(assetDefs[event.target.value].productAttributes[0].schema);
    //event.target.value is the index
    setSelectedAssetDef(assetDefs[event.target.value].productAttributes[0].schema);
  };

  const generateUiSchema = (schema) => {
    const uiSchema = {};

    // Object.keys(schema.properties).forEach((key) => {
    //  uiSchema[key] = {"ui:field": "description",
    //  "ui:options": {
    //    label: false
    //  }};


    //  if (key === "value") {
    //   uiSchema[key] = { 
    //     "ui:field": "description",
    //     "ui:options": {
    //       label: false
    //     }
    //   };
    // }

    // });

    return uiSchema;
  };

  const uiSchema = generateUiSchema(selectedAssetDef);



  return (
    <Container>
      <label>Select an asset definition:</label>
      <select onChange={handleSelectChange}>
        <option>choose one</option>

        {assetDefs.map((assetDef, index) => (
          <option key={index} value={index}>
            {assetDef.name}-{assetDef.productDescription}
          </option>
        ))}
      </select>


      {/* 
          https://rjsf-team.github.io/react-jsonschema-form/docs/advanced-customization/custom-templates 
          https://jsonforms.io/docs/uischema/
          https://rjsf-team.github.io/react-jsonschema-form/
          

      */}
      {/* when list is empty do not show the submit button */}
      {selectedAssetDef && Object.keys(selectedAssetDef).length > 0 && (
        <Form
          schema={selectedAssetDef}
          uiSchema={uiSchema}

          validator={validator}
          onSubmit={handleSubmit}
        />
      )}
    </Container>

  );

};

export default AssetEdit;