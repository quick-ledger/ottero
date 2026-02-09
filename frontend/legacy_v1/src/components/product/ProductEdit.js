import React, {useEffect, useState} from 'react';
import {Link, useNavigate, useParams} from 'react-router-dom';
import {Alert, Button, Col, Input, Label, Row} from 'reactstrap';
import {useApi} from '../../helpers/api';
import {errorMessages} from '../../helpers/errorMessages';
import {useSelector} from 'react-redux';


const ProductEdit = () => {
    const navigate = useNavigate();
    const [error, setError] = useState(null);
    const [product, setProduct] = useState({});
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
        const response = await api.get(`/api/companies/${selectedCompanyId}/products/${id}`);
        setProduct(response.data);
      } catch (error) {
        console.error('Failed to fetch product:', error);
      }
    };
    fetchProduct();
    //fetchLogo();
  }, [id]);




  const handleSubmit = async () => {
    try {
      let response;

      if (product.id) {
        response = await api.put(`/api/companies/${selectedCompanyId}/products`, product);
      } else {
        response = await api.post(`/api/companies/${selectedCompanyId}/products`, product);
      }
      navigate('/products');

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
    setProduct({ ...product, [name]: value });

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
      <h2>{product.id ? 'Edit Product' : 'Add Product'}</h2>

      {error && <Alert color="danger">{error}</Alert>}
      <Row>
        <Col>
        <Label htmlFor="name">Name*</Label>
          <Input type="text" name="name" id="name" value={product.name || ''}
            onChange={handleChange} autoComplete="name" required />
        </Col>
        <Col>
        <Label htmlFor="code">Code</Label>
          <Input type="text" name="code" id="code" value={product.code || ''}
            onChange={handleChange} autoComplete="code" />
        </Col>
      </Row>

      <Row>
        <Col>
        <Label htmlFor="productDescription">Description</Label>
          <Input type="text" name="productDescription" id="productDescription" value={product.productDescription || ''}
            onChange={handleChange} autoComplete="productDescription" />
        </Col>
        <Col>
        <Label htmlFor="quantity">Quanitity</Label>
          <Input type="number" name="quantity" id="quantity" value={product.quantity || ''}
            onChange={handleChange} autoComplete="quantity" />
        </Col>
      </Row>

      <Row>
        <Col>
        <Label htmlFor="price">Price $</Label>
          <Input type="number" name="price" id="price" value={product.price || ''}
            onChange={handleChange} autoComplete="price" />
        </Col>
        <Col>
        <Label htmlFor="item_tax">Tax $</Label>
          <Input type="number" name="item_tax" id="item_tax" value={product.item_tax || ''}
            onChange={handleChange} autoComplete="item_tax" />
        </Col>
      </Row>



          <Button color="primary" onClick={()=>handleSubmit()}>Save</Button>{' '}
          <Button color="secondary" tag={Link} to="/products">Cancel</Button>





      {/* {item.id && <UploadComponent logo={logo} handleLogoChange={handleImageSubmit} />} */}


    </div>
  );
}
  





export default ProductEdit;



