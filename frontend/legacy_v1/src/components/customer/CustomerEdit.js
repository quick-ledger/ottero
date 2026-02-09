import React, {useEffect, useState} from 'react';
import {Link, useNavigate, useParams} from 'react-router-dom';
import {Alert, Button, Col, Container, Input, Label, Row} from 'reactstrap';
import {useApi} from '../../helpers/api';
import {useSelector} from 'react-redux';

const QuoteEdit = () => {
    const emptyClient = {
        firstName: '',
        lastName: '',
        email: '',
        phone: '',
        address: '',
        entityName: '',
        accountNumber: '',

    };

    const selectedCompanyId = useSelector(state => { return state.app.selectedCompanyId; });
    const navigate = useNavigate();
    const [client, setClient] = useState(emptyClient);
    const { id } = useParams();
    const api = useApi();
    const [error, setError] = useState(null);
    const [alert, setAlert] = useState({ message: '', color: '' });

    useEffect(() => {
        const fetchData = async () => {
            if (id !== 'new') {
                const client = await api.get(`/api/companies/${selectedCompanyId}/clients/${id}`);
                setClient(client.data);
            }
        };

        fetchData();
    }, [id]);

    // const handleChange = (event) => {
    //     const { name, value } = event.target;
    //     let item = { ...this.state.client };
    //     item[name] = value;
    //     this.setState({ item });
    // }


    const handleChange = (event) => {
        const { name, value } = event.target;
        setClient(prevClient => ({
            ...prevClient,
            [name]: value
        }));
    };


    const handleSubmit = async event => {
        event.preventDefault();
        try {
            if (client.id) {
                await api.put(`/api/companies/` + selectedCompanyId + `/clients`, client);
            } else {
                await api.post(`/api/companies/` + selectedCompanyId + `/clients`, client);
            }
            navigate('/customers');
        } catch (error) {
            setAlert({ message: error.message, color: 'danger' });
        }
    };

    return (
        <div>
            {alert && <Alert color={alert.color}>{alert.message}</Alert>}
            <Container>
                <Row>
                    <Col>
                        <Label htmlFor="lastname">Firstname</Label>
                        <Input type="text" name="firstName" id="firstName" value={client.firstName}
                            onChange={handleChange} autoComplete="name" />
                    </Col>

                    <Col>
                        <Label htmlFor="lastname">Lastname</Label>
                        <Input type="text" name="lastName" id="lastName" value={client.lastName}
                            onChange={handleChange} autoComplete="lastname" />
                    </Col>

                    <Col>
                        <Label htmlFor="email">Email</Label>
                        <Input type="text" name="email" id="email" value={client.email}
                            onChange={handleChange} autoComplete="email" />
                    </Col>


                </Row>

                <Row>
                    <Col>
                        <Label htmlFor="phone">Phone</Label>
                        <Input type="text" name="phone" id="phone" value={client.phone}
                            onChange={handleChange} autoComplete="phone" />
                    </Col>


                    <Col>
                        <Label htmlFor="address">Address</Label>
                        <Input type="text" name="address" id="address" value={client.address}
                            onChange={handleChange} autoComplete="sumaddressmary" />
                    </Col>
                    <Col>
                        <Label htmlFor="accountNumber">Reference Numer</Label>
                        <Input type="text" name="accountNumber" id="accountNumber" value={client.accountNumber}
                            onChange={handleChange} autoComplete="accountNumber" />
                    </Col>
                </Row>


                <Button color="primary" type="submit" onClick={handleSubmit}>Save</Button>{' '}
                <Button color="secondary" tag={Link} to="/customers">Close</Button>
            </Container>
        </div>

    );
};

export default QuoteEdit;