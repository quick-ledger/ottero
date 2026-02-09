import React, {useEffect, useState} from 'react';
import {Link, useLocation, useNavigate, useParams} from 'react-router-dom';
import {Button, Col, Container, Input, Label, Row} from 'reactstrap';
import {useApi} from '../../helpers/api';

import {useSelector} from 'react-redux';
import '../../css/QuoteEdit.css';
import SearchCustomer from '../customer/SearchCustomer';
import QuoteInvoiceItems from './QuoteInvoiceItems';
import ToastNotification from '../../helpers/ToastNotification';
import CustomModal from '../../helpers/CustomModal';


const today = new Date();
const thirtyDaysFromNow = new Date();
thirtyDaysFromNow.setDate(today.getDate() + 30);


const QuoteEdit = () => {

    const emptyQuote = {
        quoteNumber: '',
        quoteDate: today.toISOString().substr(0, 10),
        expiryDate: thirtyDaysFromNow.toISOString().substr(0, 10),
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
        quoteRevision: 0,
        quoteItems: []
    };

    const toggleToastVisibility = () => {
        setToastProperties(prevState => ({
            ...prevState,
            isToastOpen: !prevState.isToastOpen
        }));
    };

    const [quote, setQuote] = useState(emptyQuote);
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
                const quote = await api.get(`/api/companies/${selectedCompanyId}/quotes/${id}`);
                setQuote(quote.data);
                setItems(quote.data.quoteItems);
            } else if (location.state && location.state.copyQuote) {//this is the route for copy quote
                setQuote(location.state.copyQuote);
                setItems(location.state.copyQuote.quoteItems);
            }
        };

        fetchData();
    }, [id]);


    useEffect(() => {
        if(quote.status !== "DRAFT"){
            setDisabledSave(true);
        }
    }, [quote]);

    const handleQuoteChange = (event) => {
        console.log("quote change", event.target.name, event.target.value);
        const { name, value } = event.target;
        setQuote({ ...quote, [name]: value });
    }

    const handleSubmit = async event => {
        event.preventDefault();
        quote.companyId = selectedCompanyId;
        let response;
        if (quote.id) {
            try {
                response = await api.put(`/api/companies/${selectedCompanyId}/quotes/` + quote.id, quote);
                setQuote(response.data);
                setToastProperties({
                    toastTitle: 'Success',
                    toastBody: 'Quote saved successfully',
                    isToastOpen: true
                });
            } catch {
                setToastProperties({
                    toastTitle: 'Error',
                    toastBody: 'Error saving quote',
                    isToastOpen: true
                });
                return;
            }


        } else {
            quote.status = 'DRAFT';
            try {
                response = await api.post(`/api/companies/${selectedCompanyId}/quotes`, quote);
                setQuote(response.data);    
                setToastProperties({
                    toastTitle: 'Success',
                    toastBody: 'Quote saved successfully',
                    isToastOpen: true
                });
            } catch {
                setToastProperties({
                    toastTitle: 'Error',
                    toastBody: 'Error saving quote',
                    isToastOpen: true
                });
            }
        }
    };

    const sendEmail = async event => {
        //TODO add a confirmation dialog or something
        try {
            const response = await api.post(`/api/companies/${selectedCompanyId}/quotes/${quote.id}/send-quote`);
            setQuote(response.data);
            setToastProperties({
                toastTitle: 'Success',
                toastBody: 'Quote sent to customer successfully',
                isToastOpen: true
            });
        } catch {
            setToastProperties({
                toastTitle: 'Error',
                toastBody: 'Error sending quote',
                isToastOpen: true
            });
            return;
        }
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
            const response = await api.delete(`/api/companies/${selectedCompanyId}/quotes/${quote.id}`);
            setQuote(response.data);
            
            setToastProperties({
                toastTitle: 'Success',
                toastBody: 'Quote deleted successfully',
                isToastOpen: true
            });
            navigate('/quotes');
        } catch {
            setToastProperties({
                toastTitle: 'Error',
                toastBody: 'Error deleting quote',
                isToastOpen: true
            });
            return;
        }
    };




    //this is to pass to the search component and get back the selected client
    const handleSelectedClient = (client) => {
        //setSelectedClient(client);
        setQuote(prev => ({ ...prev, clientId: client.id, clientFirstname: client.firstName, clientLastname: client.lastName, clientEntityName: client.clientEntityName, clientPhone: client.phone, clientEmail: client.email }));
    }

    return (
        <Container>
            <h1>New Quote</h1>

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
 

                    <Col>
                        
                        {(quote.clientId && (
                            <>
                            <b>Quote To: </b>
                                {quote.clientFirstname} {quote.clientLastname} {quote.clientEntityName}
                            </>
                        ))} 
                    </Col>
          
     
            </Row>

            <Row>
                <Col md={3}>
                    <Label htmlFor="quoteNumber">Quote Number</Label>
                    <Input type="text" name="quoteNumber" id="quoteNumber" value={quote.quoteNumber || ''}
                        placeholder='will be generate after save' disabled
                        onChange={handleQuoteChange} autoComplete="quoteNumber" />
                </Col>
                <Col md={2}>
                    <Label htmlFor="quoteRevision">Revision</Label>
                    <Input type="text" name="quoteRevision" id="quoteRevision" value={quote.quoteRevision || 0}
                        placeholder='' disabled
                        onChange={handleQuoteChange} autoComplete="quoteRevision" />
                </Col>
                <Col md={3}>
                    <Label htmlFor="quoteDate">Date</Label>
                    <Input type="date" name="quoteDate" id="quoteDate" value={quote.quoteDate || today}
                        onChange={handleQuoteChange} autoComplete="quoteDate" />

                </Col>
                <Col md={3}>
                    <Label htmlFor="expiryDate">Expiry Date</Label>
                    <Input type="date" name="expiryDate" id="expiryDate" value={quote.expiryDate || ''}
                        onChange={handleQuoteChange} autoComplete="expiryDate" />

                </Col>
            </Row>
            <Row>
            <Col>
                <Label htmlFor="expiryDate">Notes</Label>
                <Input type="text" name="notes" id="notes" value={quote.notes || ''}
                    onChange={handleQuoteChange} autoComplete="notes" />
            </Col>
            <Col>
            {quote.status && (
                <span>
                   <b> {"Status: " + quote.status}</b>
                </span>
            )}
            </Col>

            </Row>
            <Col>
                <Button color="primary" type="submit" onClick={handleSubmit} disabled={disabledSave}>Save</Button>{' '}
                <Button color="secondary" tag={Link} to="/quotes">Close</Button> {' '}
                <Button onClick={sendEmail} color="warning" type="submit" disabled={disabledSave}>Send to Customer</Button> {' '}
                <Button onClick={handleDelete} color="danger" type="submit" disabled={disabledSave}>Delete</Button>
            </Col>

            <QuoteInvoiceItems setDocument={setQuote} setItems={setItems} items={items} document={quote} handleDocumentChange={handleQuoteChange} type={"quote"} />

        </Container>
    );
};

export default QuoteEdit;