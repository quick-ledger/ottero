import React, {useEffect, useState} from 'react';
import {Link, useNavigate, useParams} from 'react-router-dom';
import {Alert, Button, Col, Input, Label, Row} from 'reactstrap';
import {useApi} from '../../helpers/api';
import {errorMessages} from '../../helpers/errorMessages';
import {useSelector} from 'react-redux';


const ServiceEdit = () => {
    const navigate = useNavigate();
    const [error, setError] = useState(null);
    const [service, setService] = useState({});
    const { id } = useParams();
    const api = useApi();
    const selectedCompanyId = useSelector(state => { return state.app.selectedCompanyId; });

  // const fetchLogo = async () => {

  //   try {
  //     const response = await api.get(`/api/companies/${id}/images`, { responseType: 'arraybuffer' });
  //     if (response.status !== 200) {
  //       throw new Error('HTTP status ' + response.status);
  //     }
  //     const base64 = btoa(
  //       new Uint8Array(response.data).reduce(
  //         (data, byte) => data + String.fromCharCode(byte),
  //         '',
  //       ),
  //     );
  //     const imageUrl = `data:image/jpeg;base64,${base64}`;
      
  //     setLogo(imageUrl);
  //   } catch (error) {
  //     console.error('Failed to fetch company:', error);
  //   }
  // };
  
  useEffect(() => {
    if (id === 'new') {
      // This is a new company, no need to fetch data
      return;
    }

    const fetchProduct = async () => {
      try {
        const response = await api.get(`/api/companies/${selectedCompanyId}/service_items/${id}`);
        setService(response.data);
      } catch (error) {
        console.error('Failed to fetch Service:', error);
      }
    };
    fetchProduct();
    //fetchLogo();
  }, [id]);




  const handleSubmit = async () => {
    try {
      let response;

      if (service.id) {
        response = await api.put(`/api/companies/${selectedCompanyId}/service_items`, service);
      } else {
        response = await api.post(`/api/companies/${selectedCompanyId}/service_items`, service);
      }
      navigate('/services');

    } catch (error) {
      if (error.response && error.response.status === 500) {
        setError(errorMessages.SERVER_ERROR_500);
      } else {
        setError(error.message);
      }
    }
  };


  const handleChange = (event) => {
    const { name, value } = event.target;
    setService({ ...service, [name]: value });

  };


  const handleImageSubmit = async (event) => {
    event.preventDefault();
    try {
      const formData = new FormData();
      formData.append('files', event.target.files[0]);

      const response = await api.patch(`/api/companies/${selectedCompanyId}/images`, formData,{
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });


      if (!response.status === 200) {
        throw new Error(response.status);
      }

      //fetchLogo();
    } catch (error) {
      setError(error.message);
    }
  };



  return (


    <div>
      <h2>{service.id ? 'Edit Service' : 'Add Service'}</h2>

      {error && <Alert color="danger">{error}</Alert>}
      <Row>
        <Col>
        <Label htmlFor="name">Name*</Label>
          <Input type="text" name="name" id="name" value={service.name || ''}
            onChange={handleChange} autoComplete="name" required />
        </Col>
        <Col>
        <Label htmlFor="code">Code</Label>
          <Input type="text" name="code" id="code" value={service.code || ''}
            onChange={handleChange} autoComplete="code" />
        </Col>
      </Row>

      <Row>
        <Col>
        <Label htmlFor="itemDescription">Description</Label>
          <Input type="text" name="itemDescription" id="itemDescription" value={service.itemDescription || ''}
            onChange={handleChange} autoComplete="itemDescription" />
        </Col>
        <Col>
        <Label htmlFor="quantity">Quanitity</Label>
          <Input type="number" name="quantity" id="quantity" value={service.quantity || ''}
            onChange={handleChange} autoComplete="quantity" />
        </Col>
      </Row>

      <Row>
        <Col>
        <Label htmlFor="price">Price $</Label>
          <Input type="number" name="price" id="price" value={service.price || ''}
            onChange={handleChange} autoComplete="price" />
        </Col>
        <Col>
        <Label htmlFor="item_tax">Tax $</Label>
          <Input type="number" name="item_tax" id="item_tax" value={service.item_tax || ''}
            onChange={handleChange} autoComplete="item_tax" />
        </Col>
      </Row>



          <Button color="primary" onClick={()=>handleSubmit()}>Save</Button>{' '}
          <Button color="secondary" tag={Link} to="/services">Cancel</Button>





      {/* {item.id && <UploadComponent logo={logo} handleLogoChange={handleImageSubmit} />} */}


    </div>
  );
}
  





export default ServiceEdit;



