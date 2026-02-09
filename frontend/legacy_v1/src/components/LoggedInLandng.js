import React from 'react';
import {Button, Card, CardBody, CardSubtitle, CardText, CardTitle} from 'reactstrap';
import {useNavigate} from 'react-router-dom';

const LoggedInLanding = () => {
    const navigate = useNavigate();
    return (
        <div className="d-flex flex-column align-items-center">
            <Card className="mb-4" style={{ width: '18rem' }}>
                <CardBody>
                    <CardTitle tag="h5">
                        Create Quote
                    </CardTitle>
                    <CardSubtitle className="mb-2 text-muted" tag="h6">
                        Create a new quote and send to your customers
                    </CardSubtitle>
                    <CardText>
                    </CardText>
                    <Button onClick={()=> navigate("/quotes/new")}>
                        Go 
                    </Button>
                </CardBody>
            </Card>
            <Card className="mb-4" style={{ width: '18rem' }}>
                <CardBody>
                    <CardTitle tag="h5">
                        Create Invoice
                    </CardTitle>
                    <CardSubtitle className="mb-2 text-muted" tag="h6">
                        Create a new invoice and send to your customers
                    </CardSubtitle>
                    <CardText>
                    </CardText>
                    <Button onClick={()=> navigate("/invoices/new")}>
                        Go
                    </Button>
                </CardBody>
            </Card>
            <Card className="mb-4" style={{ width: '18rem' }}>
                <CardBody>
                    <CardTitle tag="h5">
                        Add a new client
                    </CardTitle>
                    <CardSubtitle className="mb-2 text-muted" tag="h6">
                        Create a new client to send quotes and invoices to
                    </CardSubtitle>
                    <CardText>
                    </CardText>
                    <Button onClick={()=> navigate("/client/new")}>
                        Go
                    </Button>
                </CardBody>
            </Card>
        </div>
    );
};

export default LoggedInLanding;