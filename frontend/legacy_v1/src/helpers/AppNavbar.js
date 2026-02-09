import React, { useState , useEffect} from 'react';
import {
  Navbar, NavbarBrand, UncontrolledDropdown,
  DropdownToggle,
  DropdownMenu,
  DropdownItem,
  NavbarText, NavItem, Nav, NavbarToggler, Collapse
} from 'reactstrap';
import { useKeycloak } from "@react-keycloak/web";
import { useDispatch } from 'react-redux';
import { setCompanyName } from './redux-action';
import { setCompanyId } from './redux-action';
import { useApi } from './api';
import { useNavigate } from 'react-router-dom';

const AppNavbar = () => {

  const { keycloak, initialized } = useKeycloak();
  const [isOpen, setIsOpen] = useState(false);
  const api = useApi();
  const [selectedToken, setSelectedToken] = useState(null);
  const [selectedCompanyName, setSelectedCompanyName] = useState(null);
  const dispatch = useDispatch();
  const [loading, setLoading] = useState(true);

  const toggle = () => {
    setIsOpen(!isOpen);
  }

  useEffect(() => {
    if(initialized && keycloak.authenticated) {
      setSelectedToken(keycloak.token);
      api.get(`/api/users/${keycloak.tokenParsed.databaseId}`).then(response => {
        const user = response.data;
        dispatch(setCompanyName(user.defaultCompany?.name));
        dispatch(setCompanyId(user.defaultCompany?.id));
        setSelectedCompanyName(user.defaultCompany?.name);
        //navigate('/landing'); // Navigate to the /landing route after login
        setLoading(false);
      }).catch(error => {
        console.error('Error fetching user data:', error);
      });
  }
  }, [keycloak, initialized]);

  return (
    <div>
      <Navbar color="dark" dark expand="md">
        <NavbarBrand to="/">

        <img src="/Logo-04.png" alt="Logo"
             onClick={() => window.location.href = '/'}
             style={{ height: '40px', marginRight: '10px' }} />

        
        </NavbarBrand>
        
        <NavbarToggler onClick={toggle} />
        <Collapse isOpen={isOpen} navbar>
          <Nav className="ml-auto" navbar fill>
            <UncontrolledDropdown nav inNavbar>
              <DropdownToggle nav caret>
                Invoices
              </DropdownToggle>
              <DropdownMenu end>
                <DropdownItem href='/invoices/new'>New Invoice</DropdownItem>
                <DropdownItem href='/invoices'>List Invoice</DropdownItem>
              </DropdownMenu>
            </UncontrolledDropdown>


            <UncontrolledDropdown nav inNavbar>
              <DropdownToggle nav caret>
                Quotes
              </DropdownToggle>
              <DropdownMenu end>
                <DropdownItem href='/quotes/new'>New Quote</DropdownItem>
                <DropdownItem href='/quotes'>List Quotes</DropdownItem>
              </DropdownMenu>
            </UncontrolledDropdown>

            <UncontrolledDropdown nav inNavbar>
              <DropdownToggle nav caret>
                Your Business
              </DropdownToggle>
              <DropdownMenu end>
                <DropdownItem href='/companies'>Companies</DropdownItem>
                {/* not for mvp <DropdownItem href='/employees'>Employees</DropdownItem> */}
                <DropdownItem href='/customers'>Customers</DropdownItem>
                <DropdownItem href='/products'>Products</DropdownItem>
                <DropdownItem href='/services'>Servcies</DropdownItem>
                <DropdownItem href='/quotes/number'>Quote/Invoice Sequence Builder</DropdownItem>
              </DropdownMenu>
            </UncontrolledDropdown>

            <UncontrolledDropdown nav inNavbar>
              <DropdownToggle nav caret>
                Account
              </DropdownToggle>

              <DropdownMenu end>
                <DropdownItem href=''>Profile</DropdownItem>
                <DropdownItem href=''>Change Password</DropdownItem>
                <DropdownItem onClick={() => {
                  keycloak.logout({ redirectUri: window.location.origin + '/logout' });
                    }}>Logout {!!keycloak.authenticated && keycloak.tokenParsed.preferred_username}</DropdownItem>
              </DropdownMenu>
            </UncontrolledDropdown>

                <NavItem>
                  {!keycloak.authenticated && (
                    <button
                      type="button"
                      className="text-blue-800"
                      onClick={() => keycloak.login({ redirectUri: window.location.origin + '/landing' })}
                    >
                      Login
                    </button>
                  )}
                </NavItem>

                <NavItem>
                  {!keycloak.authenticated && (
                    <button
                      type="button"
                      className="text-blue-800"
                      onClick={() => window.location.href = 'https://auth.quickledger.net/realms/QuickLedger/protocol/openid-connect/registrations?client_id=QuickLedger&redirect_uri=http%3A%2F%2Flocalhost%3A8085%2F&response_mode=query&response_type=code&scope=openid&state=random-state&nonce=random-nonce'}
                    >
                      Signup
                    </button>
                  )}
                </NavItem>


            {/* {!!keycloak.authenticated && (
              <NavItem>
                <button
                  type="button"
                  onClick={() => {
                    keycloak.logout();

                  }}
                >
                  Logout ({keycloak.tokenParsed.preferred_username})

                </button>
              </NavItem>


            )} */}

            <NavItem className="align-items-center d-flex">
              <NavbarText>&nbsp; {selectedToken?'Hello ' + keycloak.tokenParsed.given_name:''} {selectedCompanyName?'@'+selectedCompanyName:''}  </NavbarText>
            </NavItem>

          </Nav>
        </Collapse>
      </Navbar>
      <br />
    </div>

  );
}

export default AppNavbar;