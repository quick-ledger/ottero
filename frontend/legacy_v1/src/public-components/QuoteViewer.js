import React, { useEffect, useState } from 'react';
import { Button, Table } from 'reactstrap';
import axios from 'axios';
import CustomModal from '../helpers/CustomModal';
import { useLocation } from 'react-router-dom';
import ToastNotification from '../helpers/ToastNotification';


const QuoteViewer = () => {
  const location = useLocation();
  const [quote, setQuote] = useState({});
  const [token, setToken] = useState('');//token from KC
  const [validationResponse, setValidationResponse] = useState({});
  const [incomingToken, setIncomingToken] = useState('');
  const [disableButtons, setDisableButtons] = useState(false);

  const [modalProperties, setModalProperties] = useState({
    title: '',
    body: '',
    okTitle: '',
    okHandler: null,
    showModal: false
  });

  //this should be outside of the state object!
  const toggleToast = () => {
    setToastProperties(prevState => ({
      ...prevState,
      isToastOpen: !prevState.isToastOpen
    }));
  };

  const [toastProperties, setToastProperties] = useState({
    toastTitle: '',
    toastBody: '',
  });


  /**
  1- get the token from url (token_1) - this token contains quote id
  2- get a new token from KC (token_2)
  3- send token from url (token_1) to the validate api (using token_2)
  4- get quote (using token_2)
  5- PUT quote (using token_2) for approval/rejection
  */


  useEffect(() => {
    const queryParams = new URLSearchParams(location.search);
    const token = queryParams.get('paramToken');
    console.log('Token from URL:', token);

    setIncomingToken(token);
  }, []);//empty dependency to make it run only once

  useEffect(() => {
    if (incomingToken) getServerToken();
  }, [incomingToken]); //only run when incomingToken changes

  useEffect(() => {
    if (token) validateIncomingToken();
  }, [token]);

  useEffect(() => {
    if (validationResponse) fetchQuote(validationResponse);
  }, [validationResponse]);


  useEffect(() => {
    if (quote && (quote.status === 'ACCEPTED' || quote.status === 'REJECTED')) {
      setToastProperties({
        toastTitle: 'Quote already actioned',
        toastBody: 'The quote has already been ' + quote.status + '.',
        isToastOpen: true,
      });
      setDisableButtons(true);
    }
  }, [quote]);


  async function getServerToken() {// this is the token to interact with BE
    console.log('Getting a server token...');
    const params = new URLSearchParams();
    // params.append('client_id', process.env.REACT_APP_CLIENT_ID);
    // params.append('client_secret', process.env.REACT_APP_CLIENT_SECRET);
    // params.append('username', process.env.REACT_APP_USERNAME);
    // params.append('password', process.env.REACT_APP_PASSWORD);
    // params.append('grant_type', process.env.REACT_APP_GRANT_TYPE);

    params.append('client_id', 'admin-cli');
    params.append('client_secret', 'm9xcmg7JAQmKyUhmRxliorL1dXTrNMiS');
    params.append('username', 'mani.hosseini@hotmail.com');//TODO REMOVE these from here.
    params.append('password', '0tter020@$!');
    params.append('grant_type', 'password');

    const tokenUrl = '/in-ottero/realms/QuickLedger/protocol/openid-connect/token';

    try {
      const response = await axios.post(tokenUrl, params, {
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
      });
      console.log('Server token response:', response.data);
      setToken(response.data);
    } catch (error) {
      console.error('Error fetching token:', error);
    }
  };

  //this is more of a decoding + validation
  async function validateIncomingToken() {
    try {
      console.log('Validating incoming token...');
      const response = await axios.post("/api/quotes/validate-token", incomingToken, {
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token.access_token}`,
        },
      });

      let validationResponse = response.data;
      console.log('Validation response:', validationResponse);
      setValidationResponse(validationResponse);

      // {
      //   "companyId": 12,
      //   "clientId": 1,
      //   "userId": 1,
      //   "quoteId": 73,
      //   "sub": "quote-approval",
      //   "iat": 1724886509,
      //   "exp": 1725491309
      // }


      return true;

    } catch (error) {
      console.error('Error fetching token:', error);
      return null;
    }

  }

  async function fetchQuote() {
    try {

      //TODO if quote is already rejected or accepted, show a message and return

      console.log('Fetching quote');
      const response = await axios.get(`/api/companies/${validationResponse.companyId}/quotes/${validationResponse.quoteId}`, {
        headers: {
          Authorization: `Bearer ${token.access_token}`,
        },
      });
      console.log('Quote response:', response.data);
      setQuote(response.data);
    } catch (error) {
      console.error('Error fetching quote:', error);
    }
  };

  const handleAccept = () => {
    setModalProperties({
      title: 'Confirm Quote Acceptance',
      body: 'Are you sure you want to ACCEPT this quote?',
      okTitle: 'Accept',
      okHandler: sendAccept,
      showModal: true
    });

  };

  const actionQuote = async (action) => {
    try {
      const response = await axios.post(`/api/companies/${validationResponse.companyId}/quotes/${validationResponse.quoteId}/approval`, { ...quote, status: action }, {
        headers: {
          Authorization: `Bearer ${token.access_token}`,
        },
      });
      console.log('action response:', response.data);

      setToastProperties({
        toastTitle: 'Quote ' + action,
        toastBody: 'The quote has been ' + action + ' successfully.',
        isToastOpen: true,
      });
    } catch (error) {
      setToastProperties({
        toastTitle: 'Failed',
        toastBody: error.response.data,
        isToastOpen: true,
        isAutoHideEnabled: false
      });
      console.error('Error fetching quote:', error);
    }
  }
  const sendAccept = async () => {
    setModalProperties({ setShowModal: false });
    await actionQuote('ACCEPTED');
  };

  const sendReject = async () => {
    setModalProperties({ setShowModal: false });
    await actionQuote('REJECTED');
  };

  const handleReject = () => {
    setModalProperties({
      title: 'Confirm Quote Rejection',
      body: 'Are you sure you want to REJECT this quote?',
      okTitle: 'Reject',
      okHandler: sendReject,
      showModal: true
    });

  };

  const getStatusColor = (status) => {
  switch (status) {
    case 'ACCEPTED':
      return 'green';
    case 'REJECTED':
      return 'red';
    default:
      return 'black';
  }
};

  return (
    <div>
      <CustomModal
        show={modalProperties.showModal}
        handleClose={() => setModalProperties({ showModal: false })}
        handleOk={modalProperties.okHandler}
        title={modalProperties.title}
        okTitle={modalProperties.okTitle}
      >
        {modalProperties.body}
      </CustomModal>

      <ToastNotification
        toastTitle={toastProperties.toastTitle}
        toastBody={toastProperties.toastBody}
        toggleToast={toggleToast}
        isToastOpen={toastProperties.isToastOpen}
        autoHideEnabled={toastProperties.autoHideEnabled}
      />


      <h1>Quote Approval</h1>
      <Table>
        <tbody>
          <tr>
            <td><b>To</b></td>
            <td>{quote.clientFirstname}</td>
            <td><b>From</b></td>
            <td>company name and address</td>
          </tr>
          <tr>
            <td><b>Quote Number</b></td>
            <td>{quote.quoteNumber}</td>
            <td><b>ABN</b></td>
            <td>abn number</td>
          </tr>
          <tr>
            <td><b>Issue date</b></td>
            <td>{quote.quoteDate}</td>
            <td><b>Status</b></td>
            <td><b style={{ color: getStatusColor(quote.status) }}>{quote.status}</b></td>
          </tr>
          <tr>
            <td><b>Due date</b></td>
            <td>{quote.expiryDate}</td>
          </tr>
        </tbody>
      </Table>

      <Table>
        <thead>
          <tr>
            <th>#</th>
            <th>Description</th>
            <th>Price</th>
            <th>Quantity</th>
            <th>GST</th>
            <th>Total</th>
          </tr>
        </thead>
        <tbody>
          {(quote.quoteItems || []).map((item, index) => (
            <tr key={index}>
              <td>{index + 1}</td>
              <td>{item.itemDescription}</td>
              <td>{item.price}</td>
              <td>{item.quantity}</td>
              <td>{item.gst}</td>
              <td>{item.total}</td>
            </tr>
          ))}
        </tbody>
      </Table>

      <Table>
        <tbody>
          <tr>
            <td>Subtotal</td>
            <td>{quote.subtotal}</td>
          </tr>
          <tr>
            <td>GST Applied</td>
            <td>{quote.gst}</td>
          </tr>
          <tr>
            <td>Discount</td>
            <td>{quote.discountValue}</td>
          </tr>
          <tr>
            <td><b>Total</b></td>
            <td><b>{quote.totalPrice}</b></td>
          </tr>

        </tbody>
      </Table>

      <p>
        <label htmlFor="clientNotes">Your Note:</label>
        <textarea
          id="clientNotes"
          value={quote.clientNotes}
          onChange={(e) => setQuote({ ...quote, clientNotes: e.target.value })}
        />
      </p>

      <Button color="primary" onClick={handleAccept} disabled={disableButtons}>Accept Quote</Button>
      <Button color="danger" onClick={handleReject} disabled={disableButtons}>Reject Quote</Button>
    </div>
  );
};

export default QuoteViewer;
