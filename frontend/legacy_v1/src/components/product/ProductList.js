import React, {useEffect, useState} from 'react';
import {Button, Container, Dropdown, DropdownItem, DropdownMenu, DropdownToggle, Table} from 'reactstrap';
import {Link, useNavigate} from "react-router-dom";
//import { AppProvider, useAppContext } from '../helpers/AppContext';
import {useDispatch, useSelector} from 'react-redux';
import CustomModal from '../../helpers/CustomModal';
import {useApi} from '../../helpers/api';
import ToastNotification from '../../helpers/ToastNotification';
import '../../css/CommonCSS.css';

const ProductList = () => {

  const [products, setProducts] = useState([]);
  const [selectedProductId, setSelectedProductId] = useState([]);

  const api = useApi();

  const dispatch = useDispatch();
  const navigate = useNavigate();
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const toggleDropdown = () => setDropdownOpen(prevState => !prevState);
  const selectedCompanyId = useSelector(state => { return state.app.selectedCompanyId; });

  const [modalProperties, setModalProperties] = useState({
    title: '',
    body: '',
    okTitle: '',
    okHandler: null,
    showModal: false
  });

  const [toastProperties, setToastProperties] = useState({
    toastTitle: '',
    toastBody: '',
  });


  const toggleToastVisibility = () => {
    setToastProperties(prevState => ({
      ...prevState,
      isToastOpen: !prevState.isToastOpen
    }));
  };

  useEffect(() => {
    const fetchProducts = async () => {
      try {
        const response = await api.get('/api/companies/' + selectedCompanyId + '/products');
        setProducts(response.data);
        // Do something with response.data
      } catch (error) {

        console.error('Failed to fetch companies:', error);
      }
    };
    fetchProducts();
  }, []);






  const doDelete = async (id) => {
    try {
      await api.delete(`/api/companies/${selectedCompanyId}/products/${id}`);
      setProducts(products.filter(company => company.id !== id));
      setModalProperties({ showModal: false });
      setToastProperties({
        toastTitle: 'Product Deleted',
        toastBody: 'Product has been deleted successfully',
        isToastOpen: true
      });
    } catch (error) {
      console.error('Failed to delete Product:', error);
    }

  }

  const handleDelete = (id) => {
    setModalProperties({
      title: 'Delete Product',
      body: 'Are you sure you want to delete this product?',
      okTitle: 'Delete',
      okHandler: () => doDelete(id),
      showModal: true
    });
  }

  const handleSelectedProduct = (id) => {
    setSelectedProductId(id);
  }

  return (

    <div style={{ clear: 'both' }}>

      <Container>

        <CustomModal
          show={modalProperties.showModal}
          handleClose={() => setModalProperties({ showModal: false })}
          handleOk={modalProperties.okHandler}
          title={modalProperties.title}
          okTitle={modalProperties.okTitle}>
          {modalProperties.body}
        </CustomModal>


        <ToastNotification
          toastTitle={toastProperties.toastTitle}
          toastBody={toastProperties.toastBody}
          toggleToast={toggleToastVisibility}
          isToastOpen={toastProperties.isToastOpen}
        />

        <div className="inline-container">
          <h3>List of Products</h3>
          <Button color="success" size="sm" tag={Link} to="/products/new">+ Product</Button>
        </div>
        <Table className="mt-4">
          <thead>
            <tr>
              <th width="30%">Name</th>
              <th width="30%">Code</th>
              <th width="40%">Price</th>
              <th width="40%">
                <div>
                  <Dropdown isOpen={dropdownOpen} toggle={toggleDropdown}>
                    <DropdownToggle caret>
                      Actions
                    </DropdownToggle>
                    <DropdownMenu>
                      <DropdownItem onClick={() => handleDelete(selectedProductId)}>
                        Delete product
                      </DropdownItem>
                    </DropdownMenu>
                  </Dropdown>
                </div>

              </th>
            </tr>
          </thead>
          <tbody>
            {products.map(product => {
              return <tr key={product.id}>
                <td style={{ whiteSpace: 'nowrap' }}>

                  <a href="" onClick={() => navigate("/products/" + product.id)}>{product.name}</a>

                </td>
                <td>{product.code}</td>
                <td>{product.price}</td>
                {/* <td>
                  <ButtonGroup>
                    <Button size="sm" color="danger" onClick={() => doModal(company.id)}>Delete</Button>
                  </ButtonGroup>
                </td>
                <td>
                  <Button size="sm" color="info" onClick={() => setDefaultCompany(company.id)}>Set Default</Button>

                </td> */}
                <td>
                    <input
                      type="radio"
                      name="quoteSelectionSingleName"
                      onChange={() => handleSelectedProduct(product.id)}
                    />
                </td>
              </tr>
            })}
          </tbody>
        </Table>
      </Container>


    </div>


  )

};

export default ProductList;