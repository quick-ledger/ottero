import React, {useEffect, useState} from 'react';
import {Button, Container, Dropdown, DropdownItem, DropdownMenu, DropdownToggle, Table} from 'reactstrap';
import {Link, useNavigate} from "react-router-dom";
//import { AppProvider, useAppContext } from '../helpers/AppContext';
import {useDispatch, useSelector} from 'react-redux';

import CustomModal from '../../helpers/CustomModal';
import {useApi} from '../../helpers/api';
import ToastNotification from '../../helpers/ToastNotification';
import '../../css/CommonCSS.css';

const ServiceList = () => {

  const [services, setServices] = useState([]);
  const [selectedServiceId, setSelectedServiceId] = useState([]);

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
    const fetchServices = async () => {
      try {
        const response = await api.get('/api/companies/' + selectedCompanyId + '/service_items');
        setServices(response.data);
        // Do something with response.data
      } catch (error) {

        console.error('Failed to fetch companies:', error);
      }
    };
    fetchServices();
  }, []);






  const doDelete = async (id) => {
    try {
      await api.delete(`/api/companies/${selectedCompanyId}/service_items/${id}`);
      setServices(services.filter(company => company.id !== id));
      setModalProperties({ showModal: false });
      setToastProperties({
        toastTitle: 'Service Deleted',
        toastBody: 'Service has been deleted successfully',
        isToastOpen: true
      });
    } catch (error) {
      console.error('Failed to delete Service:', error);
    }

  }

  const handleDelete = (id) => {
    setModalProperties({
      title: 'Delete Service',
      body: 'Are you sure you want to delete this Service?',
      okTitle: 'Delete',
      okHandler: () => doDelete(id),
      showModal: true
    });
  }

  const handleSelectedService = (id) => {
    setSelectedServiceId(id);
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
          <h3>List of Services</h3>
          <Button color="success" size="sm" tag={Link} to="/services/new">+ Service</Button>
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
                      <DropdownItem onClick={() => handleDelete(selectedServiceId)}>
                        Delete service
                      </DropdownItem>
                    </DropdownMenu>
                  </Dropdown>
                </div>

              </th>
            </tr>
          </thead>
          <tbody>
            {services.map(service => {
              return <tr key={service.id}>
                <td style={{ whiteSpace: 'nowrap' }}>

                  <a href="" onClick={() => navigate("/services/" + service.id)}>{service.name}</a>

                </td>
                <td>{service.code}</td>
                <td>{service.price}</td>
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
                    onChange={() => handleSelectedService(service.id)}
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

export default ServiceList;