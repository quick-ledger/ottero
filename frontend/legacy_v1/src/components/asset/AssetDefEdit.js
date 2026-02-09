import React, {useEffect, useState} from 'react';
import {Link, useNavigate, useParams} from 'react-router-dom';
import {Alert, Button, ButtonGroup, Form, FormGroup, Input, Label, Table, Tooltip} from 'reactstrap';
import {useApi} from '../../helpers/api';
import {errorMessages} from '../../helpers/errorMessages';
import questionCircle from '../../img/question-circle.svg';
import {useSelector} from 'react-redux';


const AssetDefEdit = () => {

  const selectedCompanyName = useSelector(state => {
    return state.app.selectedCompanyName;
  });

  const selectedCompanyId = useSelector(state => {
    return state.app.selectedCompanyId;
  });

  const navigate = useNavigate();
  const [error, setError] = useState(null);
  const { id } = useParams();
  const api = useApi();
  const [tooltipOpen, setTooltipOpen] = useState(false);
  const toggleTooltip = () => {
    setTooltipOpen(!tooltipOpen);
  }

  const initialItemState = {
    name: '',
    productDescription: '',
    id: null,
    rows: [{
      name: '',
      description: '',
      required: 'yes',
      valueType: 'string',
      defaultValue: '',
      unit: ''
    }]
  };
  const [item, setItem] = useState(initialItemState);
  const [numExistingRows, setNumExistingRows] = useState(0);

  useEffect(() => {
    if (id === 'new') {
      // This is a new asset, no need to fetch data
      return;
    }

    const fetchAsset = async () => {
      try {
        const response = await api.get(`/api/companies/${selectedCompanyId}/product-definition/${id}/tabular`);

        if (response.status !== 200) {
          throw new Error('HTTP status ' + response.status);
        }
        setItem(response.data);
        console.log(response.data);

        setNumExistingRows(response.data.rows.length);

      } catch (error) {
        console.error('Failed to fetch asset:', error);
      }
    };



    fetchAsset();
  }, [id]);




  const handleSubmit = async (event) => {
    event.preventDefault();
    try {
      let response;

      if (item.id) {
        response = await api.put(`/api/companies/` + selectedCompanyId + `/product-definition`, item);
      } else {
        response = await api.post(`/api/companies/` + selectedCompanyId + `/product-definition`, item);
      }
      navigate('/assets/def');

    } catch (error) {
      if (error.response && error.response.status === 500) {
        setError(errorMessages.SERVER_ERROR_500);
      } else {
        setError(error.message);
      }
    }
  }




  const handleAddRow = () => {
    setItem(prevItem => ({
      ...prevItem,
      rows: [...prevItem.rows, { name: '', description: '', required: 'yes', valueType: 'string', unit: '', defaultValue: '' }]
    }));
  }

  const handleAttributeChange = (event, index) => {
    const { name, value } = event.target;

    setItem(prevItem => {
      const newAttributes = [...prevItem.rows];
      newAttributes[index][name] = value;
      return { ...prevItem, rows: newAttributes };
    });
  }

  const handleItemChange = (event) => {
    const { name, value } = event.target;

    setItem(item => ({
      ...item,
      [name]: value
    }));
  }

  const remove = async (index) => {
    // setToBeDeleted(id); 
    // setShowModal(true);
    if (index >= numExistingRows) {
      //remove the row
      setItem(prevItem => ({
        ...prevItem,
        rows: prevItem.rows.filter((_, i) => i !== index)
      }));
    }
  };


  return (
    <div>
      {error && <Alert color="danger">{error}</Alert>}

      {/*
      If your asset belongs to a group or category, you may define those first: <Button color="success" tag={Link} to="/category/new">Add Category</Button>

      <br />
      Define Asset Fields or Attribute. All your assets will inherit these fields.
  */}

      <p>Define asset fields for: {selectedCompanyName} company.</p>

      <Form onSubmit={handleSubmit}>
        <FormGroup className='form-group col-md-6'>
          <Label htmlFor="name">Asset Title*</Label>
          <Input type="text" name="name" id="name" value={item.name || ''}
            onChange={handleItemChange} autoComplete="name" required />
        </FormGroup>
        <FormGroup className='form-group col-md-6'>
          <Label htmlFor="productDescription">Description</Label>
          <Input type="text" name="productDescription" id="productDescription" value={item.productDescription || ''}
            onChange={handleItemChange} autoComplete="productDescription" />
        </FormGroup>
        <h2 htmlFor="phone">Define Extra Asset Attributes</h2>

        <FormGroup>
          <Label htmlFor="attributes"></Label>
          <Table>
            <thead>
              <tr>
                <th>#</th>
                <th>Name</th>
                <th>Description</th>
                <th>Required</th>
                <th>Value Type</th>
                <th>Default Value</th>
                <th>Unit <span><img id="xx" alt={"xx"} src={questionCircle}></img> <Tooltip placement="top" target="xx" isOpen={tooltipOpen} toggle={toggleTooltip}>Measure unit of this field. If you want this to be a selection, use a comma separated value.</Tooltip> </span> </th>
              </tr>
            </thead>
            <tbody>

              {(item.rows || []).map((attribute, index) => (
                <tr key={index}>
                  <td>{index + 1}</td>
                  <td>
                    {index < numExistingRows ? (
                      <Input
                        type="text"
                        name="name"
                        value={attribute.name}
                        disabled />
                    ) : (
                      <Input
                        type="text"
                        placeholder='e.g. Color, Size, Weight'
                        name="name"
                        value={attribute.name}
                        onChange={event => handleAttributeChange(event, index)}
                        required />
                    )}
                  </td>
                  <td>
                    {index < numExistingRows ? (
                      <Input
                        type="text"
                        name="description"
                        value={attribute.description}
                        disabled
                      />
                    ) : (
                      <Input
                        type="text"
                        placeholder='e.g. Color of the asset'
                        name="description"
                        value={attribute.description}
                        onChange={event => handleAttributeChange(event, index)}
                      />
                    )}
                  </td>
                  <td>
                    {index < numExistingRows ? (
                      <select disabled
                        name="required"
                        value={attribute.required}>
                        <option value="yes">Yes</option>
                        <option value="no">No</option>
                      </select>
                    ) : (
                      <select
                        name="required"
                        value={attribute.required}
                        title='You cannot have a required attribute when editing an existing asset definition.'
                      >
                        <option value="no">No</option>
                      </select>
                    )}
                  </td>
                  <td>
                    {index < numExistingRows ? (

                      <select disabled
                        name="valueType"
                        value={attribute.valueType}
                      >
                        <option value="string">String</option>
                        <option value="number">Number</option>
                      </select>
                    ) : (
                      <select
                        name="valueType"
                        value={attribute.valueType}
                        onChange={event => handleAttributeChange(event, index)}
                      >
                        <option value="string">String</option>
                        <option value="number">Number</option>
                      </select>
                    )}
                  </td>
                  <td>
                    {index < numExistingRows ? (
                      <Input disabled
                        type="text"
                        placeholder=''
                        name="defaultValue"
                        value={attribute.defaultValue || ''}
                      />
                    ) : (
                      <Input
                        type="text"
                        placeholder=''
                        name="defaultValue"
                        value={attribute.defaultValue || ''}
                        onChange={event => handleAttributeChange(event, index)}
                      />
                    )}
                  </td>
                  <td>
                    {index < numExistingRows ? (
                      <Input disabled
                        type="text"
                        name="unit"
                        value={attribute.unit || ''}
                      />
                    ) : (
                      <Input
                        type="text"
                        placeholder='e.g. Kg, cm, mm, mL'
                        name="unit"
                        value={attribute.unit || ''}
                        onChange={event => handleAttributeChange(event, index)}
                      />
                    )}
                  </td>
                  <ButtonGroup>
                    <Button size="sm" color="danger" onClick={() => remove(index)}>Delete</Button>
                  </ButtonGroup>
                </tr>
              ))}


            </tbody>



          </Table>
          <Button onClick={handleAddRow}>Add Row</Button>
        </FormGroup>




        <FormGroup>
          <Button color="primary" type="submit">Save</Button>{' '}
          <Button color="secondary" tag={Link} to="/assets/def">Cancel</Button>
        </FormGroup>
      </Form>






    </div>
  );



};

export default AssetDefEdit;



