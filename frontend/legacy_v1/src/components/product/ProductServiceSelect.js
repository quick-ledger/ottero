import React, {useEffect, useState} from 'react';
import {useSelector} from 'react-redux';
import {Container, Nav, NavItem, NavLink, TabContent, Table, TabPane} from 'reactstrap';
import '../../css/QuoteEdit.css';
import {useApi} from '../../helpers/api';

const ProductServiceSelect = ({ itemOrder, handleProductSelect, handleServiceSelect }) => {
    const api = useApi();
    const [services, setServices] = useState([]);
    const [products, setProducts] = useState([]);
    const selectedCompanyId = useSelector(state => {
        return state.app.selectedCompanyId;
    });

    useEffect(() => {
        const fetchData = async () => {
            let services = await api.get(`/api/companies/${selectedCompanyId}/service_items`);
            let products = await api.get(`/api/companies/${selectedCompanyId}/products`);
            setServices(services.data);
            setProducts(products.data);
        };
        fetchData();
    }, []);

    const [activeTab, setActiveTab] = useState('1');

    const toggleTab = (tab) => {
        if (activeTab !== tab) {
            setActiveTab(tab);
        }
    };

    return (
        <Container>
            <Nav tabs>
                <NavItem>
                    <NavLink
                        className={activeTab === '1' ? 'active' : ''}
                        onClick={() => toggleTab('1')}
                    >
                        Products
                    </NavLink>
                </NavItem>
                <NavItem>
                    <NavLink
                        className={activeTab === '2' ? 'active' : ''}
                        onClick={() => toggleTab('2')}
                    >
                        Services
                    </NavLink>
                </NavItem>
            </Nav>

            <TabContent activeTab={activeTab}>
                <TabPane tabId="1">
                    <Table>
                        <thead>
                            <tr>
                                <th>Service Name</th>
                                <th>Code</th>
                            </tr>

                        </thead>

                        <tbody>
                            {(products || []).map(product => {
                                return <tr key={product.id} onClick={() => handleServiceSelect(itemOrder, product)}>
                                    <td>{product.name}</td>
                                    <td>{product.code}</td>
                                </tr>
                            })}
                        </tbody>
                    </Table>
                </TabPane>

                <TabPane tabId="2">
                    <Table>
                        <thead>
                            <tr>
                                <th>Service Name</th>
                                <th>Code</th>
                            </tr>

                        </thead>

                        <tbody>
                            {(services || []).map(service => {
                                return <tr key={service.id} onClick={() => handleProductSelect(itemOrder, service)}>
                                    <td>{service.name}</td>
                                    <td>{service.code}</td>
                                </tr>
                            })}
                        </tbody>
                    </Table>
                </TabPane>

            </TabContent>
        </Container>
    );
};
export default ProductServiceSelect;