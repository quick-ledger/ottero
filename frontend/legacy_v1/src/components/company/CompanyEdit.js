import React, {useEffect, useState} from 'react';
import {Link, useNavigate, useParams} from 'react-router-dom';
import {Alert, Button, Col, Form, FormGroup, Input, Label, Row} from 'reactstrap';
import UploadComponent from '../../helpers/Upload';
import {useApi} from '../../helpers/api';
import CustomModal from "../../helpers/CustomModal";

const CompanyEdit = (props) => {
    const initialItemState = {
        name: '',
        summary: '',
        id: null
    };


    const navigate = useNavigate();
    const [logo, setLogo] = useState(null);
    const [error, setError] = useState(null);
    const [item, setCompany] = useState(initialItemState);
    const {id} = useParams();
    const api = useApi();
    const [modalProperties, setModalProperties] = useState({
        showModal: false,
        title: '',
        body: '',
        okTitle: '',
        okHandler: null
    });

    const fetchLogo = async () => {

        try {
            const response = await api.get(`/api/companies/${id}/images`, {responseType: 'arraybuffer'});
            if (response.status !== 200) {
                throw new Error('HTTP status ' + response.status);
            }
            const base64 = btoa(
                new Uint8Array(response.data).reduce(
                    (data, byte) => data + String.fromCharCode(byte),
                    '',
                ),
            );
            const imageUrl = `data:image/jpeg;base64,${base64}`;

            setLogo(imageUrl);
        } catch (error) {
            console.error('Failed to fetch company:', error);
        }
    };

    useEffect(() => {
        if (id === 'new') {
            // This is a new company, no need to fetch data
            return;
        }

        const fetchCompany = async () => {
            try {
                const response = await api.get(`/api/companies/${id}`);
                if (response.status !== 200) {
                    throw new Error('HTTP status ' + response.status);
                }
                setCompany(response.data);
            } catch (error) {
                console.error('Failed to fetch company:', error);
            }
        };
        fetchCompany();
        fetchLogo();
    }, [id]);


    const handleSubmit = async (event) => {
        event.preventDefault();
        let response;
        try {
            if (item.id) {
                response = await api.put(`/api/companies/${item.id}`, item);
            } else {
                response = await api.post('/api/companies', item);
                setModalProperties({
                    showModal: true,
                    title: 'Company Created',
                    body: 'Now consider adding sequences for Quote and Invoices',
                    okTitle: 'Add Sequence',
                    okHandler: () => navigate('/quotes/number')
                });
            }
        } catch (error) {
            setError(error.response.data);
        }
    }


    const handleChange = (event) => {
        const {name, value} = event.target;
        setCompany({...item, [name]: value});

    };

    const handleImageSubmit = async (event) => {
        event.preventDefault();
        try {
            const formData = new FormData();
            formData.append('file', event.target.files[0]);

            const response = await api.patch(`/api/companies/${item.id}/images`, formData, {
                headers: {
                    'Content-Type': 'multipart/form-data',
                },
            });


            if (!response.status === 200) {
                throw new Error(response.status);
            }

            fetchLogo();
        } catch (error) {
            setError(error.message);
        }
    };


    return (


        <div>
            {error && <Alert color="danger">{error}</Alert>}
            <h1>Company Details</h1>

            <CustomModal
                show={modalProperties.showModal}
                handleOk={modalProperties.okHandler}
                title={modalProperties.title}
                okTitle={modalProperties.okTitle}
                handleClose={()=> navigate("/companies")}>
                {modalProperties.body}
            </CustomModal>


            <Form onSubmit={handleSubmit}>

                <Row>
                    <Col>
                        <Label htmlFor="name">Business Name*</Label>
                        <Input type="text" name="name" id="name" value={item.name || ''}
                               onChange={handleChange} autoComplete="name" required/>
                    </Col>
                    <Col>
                        <Label htmlFor="abn">ABN*</Label>
                        <Input type="text" name="abn" id="abn" value={item.abn || ''}
                               onChange={handleChange} autoComplete="abn"/>
                    </Col>
                </Row>

                <Row>
                    <Col>
                        <Label htmlFor="phone">Phone</Label>
                        <Input type="text" name="phone" id="acn" value={item.phone || ''}
                               onChange={handleChange} autoComplete="phone"/>
                    </Col>
                    <Col>
                        <Label htmlFor="bsb">BSB</Label>
                        <Input type="text" name="bsb" id="bsb" value={item.bank_bsb || ''}
                               onChange={handleChange} autoComplete="bank_bsb"/>
                    </Col>
                    <Col>
                        <Label htmlFor="bank_account">Bank Account</Label>
                        <Input type="text" name="bank_account" id="bank_account" value={item.bank_account || ''}
                               onChange={handleChange} autoComplete="bank_account"/>
                    </Col>
                </Row>

                <Row>
                    <Col>
                        <Label htmlFor="email">Business Email</Label>
                        <Input type="email" name="email" id="email" value={item.email || ''}
                               onChange={handleChange} autoComplete="email"/>
                    </Col>
                    <Col>
                        <Label htmlFor="address">Website</Label>
                        <Input type="text" name="url" id="url" value={item.website || ''}
                               onChange={handleChange} autoComplete="website"/>
                    </Col>
                </Row>


                <Row>
                    <Col>
                        <Label htmlFor="address">Address</Label>
                        <Input type="text" name="address" id="address" value={item.address || ''}
                               onChange={handleChange} autoComplete="address"/>
                    </Col>
                </Row>


                <FormGroup>
                    <Button color="primary" type="submit">Save</Button>{' '}
                    <Button color="secondary" tag={Link} to="/companies">Close</Button>
                </FormGroup>
            </Form>


            {item.id && <UploadComponent logo={logo} handleLogoChange={handleImageSubmit}/>}


        </div>
    );


};

export default CompanyEdit;



