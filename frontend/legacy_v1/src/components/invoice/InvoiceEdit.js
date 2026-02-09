import React, {useEffect, useState} from 'react';
import {Link, useLocation, useNavigate, useParams} from 'react-router-dom';
import {Button, Col, Container, Input, Label, Row} from 'reactstrap';
import {useApi} from '../../helpers/api';

import {useSelector} from 'react-redux';
import '../../css/QuoteEdit.css';
import SearchCustomer from '../customer/SearchCustomer';
import ToastNotification from '../../helpers/ToastNotification';
import CustomModal from '../../helpers/CustomModal';
import QuoteInvoiceItems from "../quote/QuoteInvoiceItems";


const today = new Date();
const thirtyDaysFromNow = new Date();
thirtyDaysFromNow.setDate(today.getDate() + 30);


const InvoiceEdit = () => {

    const emptyInvoice = {
        invoiceNumber: '',
        invoiceDate: today.toISOString().substr(0, 10),
        dueDate: thirtyDaysFromNow.toISOString().substr(0, 10),
        discountType: 'DOLLAR',
        discountValue: 0,
        totalPrice: 0,
        gst: 0,
        clientId: '',
        clientFirstname: '',
        clientLastname: '',
        clientEntityName: '',
        clientPhone: '',
        clientEmail: '',
        status: 'DRAFT',
        notes: '',
        invoiceItems: []
    };

    const toggleToastVisibility = () => {
        setToastProperties(prevState => ({
            ...prevState,
            isToastOpen: !prevState.isToastOpen
        }));
    };

    const [invoice, setInvoice] = useState(emptyInvoice);
    
    const location = useLocation();
    const [toastProperties, setToastProperties] = useState({
        toastTitle: '',
        toastBody: '',
    });

    const [modalProperties, setModalProperties] = useState({
        showModal: false,
        title: '',
        body: '',
        okTitle: '',
        okHandler: null
    });

    

    const [disabledSave, setDisabledSave] = useState(false);
    const [items, setItems] = useState([]);
    const navigate = useNavigate();
    const api = useApi();
    const selectedCompanyId = useSelector(state => {
        return state.app.selectedCompanyId;
    });

    const { id } = useParams();

    useEffect(() => {
        const fetchData = async () => {
            if (id !== 'new') {
                const invoice = await api.get(`/api/companies/${selectedCompanyId}/invoices/${id}`);
                setInvoice(invoice.data);
                setItems(invoice.data.invoiceItems);
            }else if (location.state && location.state.copyInvoice) {//this is the route for copy invoice
                setInvoice(location.state.copyInvoice);
                setItems(location.state.copyInvoice.invoiceItems);
            }
        };
        fetchData();
    }, [id]);

    useEffect(() => {
        if(invoice.status !== "DRAFT"){
            setDisabledSave(true);
        }
    }, [invoice]);


    const handleInvoiceChange = (event) => {
        console.log("invoice change", event.target.name, event.target.value);
        const { name, value } = event.target;
        setInvoice({ ...invoice, [name]: value });
    }

    const handleSubmit = async event => {
        event.preventDefault();
        invoice.companyId = selectedCompanyId;
        console.log(invoice);
        let response;

        try {
            if (invoice.id) {
                response = await api.put(`/api/companies/${selectedCompanyId}/invoices/` + invoice.id, invoice);
                handleInvoiceChange(response.data);
            } else {
                invoice.status = 'DRAFT';
                response = await api.post(`/api/companies/${selectedCompanyId}/invoices`, invoice);
                handleInvoiceChange(response.data);
            }
            setToastProperties({
                toastTitle: 'Success',
                toastBody: 'Invoice saved successfully',
                isToastOpen: true
            });
        } catch (e) {
            console.error("Error saving invoice", e);
            setToastProperties({
                toastTitle: 'Error',
                toastBody: 'Error saving invoice',
                isToastOpen: true
            });
        }

    };

    const sendEmail = async event => {
        const response = await api.post(`/api/companies/${selectedCompanyId}/invoices/${invoice.id}/send-invoice`);
    };

    const handleNewCustomer = async event => {
        navigate('/customers/new');

    }

    const handleDelete = () => {
        setModalProperties({
            showModal: true,
            title: 'DANGER!',
            body: 'Are you sure you want to delete this revision?',
            okTitle: 'Delete',
            okHandler: () => deleteRevision()
        });
    }

    const deleteRevision = async event => {
        try {
            const response = await api.delete(`/api/companies/${selectedCompanyId}/invoices/${invoice.id}`);
            setInvoice(response.data);

            setToastProperties({
                toastTitle: 'Success',
                toastBody: 'Invoice deleted successfully',
                isToastOpen: true
            });
            navigate('/invoices');
        } catch {
            setToastProperties({
                toastTitle: 'Error',
                toastBody: 'Error deleting invoice',
                isToastOpen: true
            });
            return;
        }
    };

    //this is to pass to the search component and get back the selected client
    const handleSelectedClient = (client) => {
            //setSelectedClient(client);
        console.log("selected client", client);
        setInvoice(prev => ({ ...prev, clientId: client.id, clientFirstname: client.firstName, clientLastname: client.lastName, clientEntityName: client.clientEntityName, clientPhone: client.phone, clientEmail: client.email }));
    }

    return (
        <Container>
            <h1>New Invoice</h1>

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



            <Row>
                <Col>
                    <SearchCustomer handleSelectedClient={handleSelectedClient} selectedCompanyId={selectedCompanyId} />
                </Col>
                <Col><Button color="primary" type="submit" onClick={handleNewCustomer}>Add New Customer</Button>{' '}</Col>
            </Row>

            <Row>
                {invoice.clientId && (
                    <div>
                        <Col><b>Invoice To: </b>
                            {invoice.clientFirstname}  {invoice.clientLastname}  {invoice.clientEntityName}
                        </Col>
                    </div>)
                }
            </Row>

            <Row>
                <Col md={3}>
                    <Label htmlFor="invoiceNumber">Invoice Number</Label>
                    <Input type="text" name="invoiceNumber" id="invoiceNumber" value={invoice.invoiceNumber || ''}
                        placeholder='will be generate after save' disabled
                        onChange={handleInvoiceChange} autoComplete="invoiceNumber" />
                </Col>
                <Col md={3}>
                    <Label htmlFor="invoiceDate">Date</Label>
                    <Input type="date" name="invoiceDate" id="invoiceDate" value={invoice.invoiceDate || today}
                        onChange={handleInvoiceChange} autoComplete="invoiceDate" />

                </Col>
                <Col md={3}>
                    <Label htmlFor="dueDate">Due Date</Label>
                    <Input type="date" name="dueDate" id="dueDate" value={invoice.dueDate || ''}
                        onChange={handleInvoiceChange} autoComplete="dueDate" />

                </Col>
            </Row>
            <Col>
                <Label htmlFor="dueDate">Notes</Label>
                <Input type="text" name="notes" id="notes" value={invoice.notes || ''}
                    onChange={handleInvoiceChange} autoComplete="notes" />
            </Col>

            <Col>
                <Button color="primary" type="submit" onClick={handleSubmit}>Save</Button>{' '}
                <Button color="secondary" tag={Link} to="/invoices">Close</Button> {' '}
                <Button onClick={sendEmail} color="warning" type="submit" disabled={disabledSave}>Send to Customer</Button> {' '}
                <Button onClick={handleDelete} color="danger" type="submit" disabled={disabledSave}>Delete</Button>

            </Col>

            <QuoteInvoiceItems document={invoice}  setDocument={setInvoice} items={items} setItems={setItems}  handleDocumentChange={handleInvoiceChange} type={"invoice"} />

        </Container>
    );
};

export default InvoiceEdit;