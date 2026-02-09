import React, {useEffect, useState} from 'react';
import {Button, Container, Dropdown, DropdownItem, DropdownMenu, DropdownToggle, Table} from 'reactstrap';
import {Link, useNavigate} from "react-router-dom";
//import { AppProvider, useAppContext } from '../helpers/AppContext';
import {useDispatch, useSelector} from 'react-redux';
import {setCompanyId, setCompanyName} from '../../helpers/redux-action';

import CustomModal from '../../helpers/CustomModal';
import {useApi} from '../../helpers/api';
import ToastNotification from '../../helpers/ToastNotification';
import '../../css/CommonCSS.css';

const CompanyList = () => {

  const [companies, setCompanies] = useState([]);
  const api = useApi();
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const toggleDropdown = () => setDropdownOpen(prevState => !prevState);
  const [selectedCompanyId, setSelectedCompanyId] = useState({});
  const [defaultCompany, setDefaultCompany] = useState({});
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

  const selectedToken = useSelector(state => {
    return state.app.selectedToken;
  });


  const toggleToastVisibility = () => {
    setToastProperties(prevState => ({
      ...prevState,
      isToastOpen: !prevState.isToastOpen
    }));
  };

  useEffect(() => {
    const fetchCompanies = async () => {
      try {
        const response = await api.get('/api/companies');
        setCompanies(response.data);
        // Do something with response.data
      } catch (error) {

        console.error('Failed to fetch companies:', error);
      }
    };
    fetchCompanies();
    getDefaultCompany();
  }, []);

  const handleSetDefault = async (companyId) => {
    //event.preventDefault();
    try {
      let user;
      user = await api.put(`/api/users/${selectedToken?.databaseId}`, { defaultCompany: { id: companyId } });
      setToastProperties({
        toastTitle: 'Default Company Set',
        toastBody: 'The default company will be used to generate quotes and invoices.',
        isToastOpen: true
      });

      dispatch(setCompanyName(user.data.defaultCompany.name));
      dispatch(setCompanyId(user.data.defaultCompany.id));
      setDefaultCompany(user.data.defaultCompany);
      // dispatch(setCompanyName(user.defaultCompany?.name));
      // dispatch(setCompanyId(user.defaultCompany?.id));
      // setSelectedCompanyName(user.defaultCompany?.name);


    } catch (error) {
      if (error.response && error.response.status === 500) {
        //setError(errorMessages.SERVER_ERROR_500);
      } else {
        // setError(error.message);
      }
    }
  }

  const getDefaultCompany = async () => {
    //event.preventDefault();
    try {
      let response;
      response = await api.get(`/api/users/${selectedToken?.databaseId}`);
      dispatch(setCompanyName(response.data && response.data.defaultCompany.name));
      dispatch(setCompanyId(response.data && response.data.defaultCompany.id));
      setDefaultCompany(response.data.defaultCompany);
    } catch (error) {
      if (error.response && error.response.status === 500) {
      } else {
      }
    }
  }

  const selectedCompanyName = useSelector(state => {
    return state.app.selectedCompanyName;
  });

  const doDelete = async (id) => {
    try {
      await api.delete(`/api/companies/${id}`);
      setCompanies(companies.filter(company => company.id !== id));
      setModalProperties({ showModal: false });
      setToastProperties({
        toastTitle: 'Company Deleted',
        toastBody: 'Company has been deleted successfully',
        isToastOpen: true
      });
    } catch (error) {
      console.error('Failed to delete company:', error);
    }

  }

  const handleDelete = (id) => {
    setModalProperties({
      title: 'Delete Company',
      body: 'Are you sure you want to delete this company?',
      okTitle: 'Delete',
      okHandler: () => doDelete(id),
      showModal: true
    });
  }

  const handleSelectedCompany = (id) => {
    setSelectedCompanyId(id);
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


        <p>Default company is set to: <b>{defaultCompany?.name}</b></p>
        <div className="inline-container">
            <h3>List of Companies</h3>
            <Button color="success" size='sm' tag={Link} to="/companies/new">+ Company</Button>
        </div>

        
        <Table className="mt-4">
          <thead>
            <tr>
              <th width="30%">Name</th>
              <th width="30%">Email</th>
              <th width="40%">ABN</th>
              <th width="40%">
                <div>
                  <Dropdown isOpen={dropdownOpen} toggle={toggleDropdown}>
                    <DropdownToggle caret>
                      Actions
                    </DropdownToggle>
                    <DropdownMenu>
                      <DropdownItem onClick={() => handleSetDefault(selectedCompanyId)} >
                        Set as default company
                      </DropdownItem>
                      <DropdownItem onClick={() => handleDelete(selectedCompanyId)}>
                        Delete company
                      </DropdownItem>
                    </DropdownMenu>
                  </Dropdown>
                </div>
              </th>
            </tr>
          </thead>
          <tbody>
            {companies.map(company => {
              return <tr key={company.id}>
                <td style={{ whiteSpace: 'nowrap' }}>
                  <a href="" onClick={() => navigate("/companies/" + company.id)}>{company.name}</a>
                  {company.name === defaultCompany?.name ? " (Default)" : ""}
                </td>
                <td>{company.email}</td>
                <td>{company.abn}</td>
                <td>
                  <td>
                    <input
                      type="radio"
                      name="quoteSelectionSingleName"
                      onChange={() => handleSelectedCompany(company.id)}
                    />
                  </td>
                </td>
              </tr>
            })}
          </tbody>
        </Table>

        <p> Define Quote and Invoice sequence numbers for your companies&nbsp;
          <a href={"/quotes/number"}>here</a>

        </p>
      </Container>


    </div>


  )

};

export default CompanyList;