/**
 * TODO warn user on editing but do not block them
 * 
 * 
 * 
 */
import React, {useEffect, useState} from 'react';
import {Button, Input, Label, Table} from 'reactstrap';
import {useApi} from '../../helpers/api';
import ToastNotification from '../../helpers/ToastNotification'; // Import the ToastNotification component
import CustomModal from '../../helpers/CustomModal';
import {useNavigate} from 'react-router-dom';


const QuoteNumberBuilder = () => {

    const api = useApi();
    const [companies, setCompanies] = useState([]);
    const [selectedCompanyId, setSelectedCompanyId] = useState();
    const navigate = useNavigate();

    const [quoteConfig, setQuoteConfig] = useState({});
    const [invConfig, setInvConfig] = useState({});

    const [isToastVisible, setIsToastVisible] = useState(false);
    const [toastTitle, setToastTitle] = useState('');
    const [showModal, setShowModal] = useState(false);

    useEffect(() => {
        const fetchCompanies = async () => {
            const companies = await api.get(`/api/companies`);
            setCompanies(companies.data);
            setSelectedCompanyId( companies.data[0].id);
        };

        fetchCompanies();
        //fetchData();
    }, []);


    const handleCompanyChange = async (event) => {
        const { name, value } = event.target;
        setSelectedCompanyId(value);

        const qConfig = await api.get(`/api/companies/${value}/sequence-configs/type/QUOTE`);
        setQuoteConfig(qConfig.data );

        const iConfig = await api.get(`/api/companies/${value}/sequence-configs/type/INVOICE`);
        setInvConfig(iConfig.data );
    }


    const handleQuoteChange = (event) => {
        const { name, value } = event.target;
        setQuoteConfig(prevSeqConfig => ({
            ...prevSeqConfig,
            [name]: name === 'currentNumber' ? parseInt(value, 10) : value

    }));
    }

    const handleInvChange = (event) => {
        const { name, value } = event.target;
        console.log(name, value);
        setInvConfig(prevSeqConfig => ({
            ...prevSeqConfig,
            [name]: name === 'currentNumber' ? parseInt(value, 10) : value
        }));
    }


    const handleSubmit = async event => {
        setShowModal(false);

        event.preventDefault();
        const xx = await api.put(`/api/companies/${selectedCompanyId}/sequence-configs`, quoteConfig);
        const xxx = await api.put(`/api/companies/${selectedCompanyId}/sequence-configs`, invConfig);

        setToastTitle("Sequence Configurations Saved");
        setIsToastVisible(true);

    };

    return (
        <div>

            <h1>Quote and Invoice number sequence format</h1>
            <p>
                Define how you want your quote and invoice numbers to look like, i.e. having optional prefix and postfix, e.g., QT-1001 or INV-1000-G.
                <br />
                Also specify what number you want your first quote / invoice to start from.<br />
                If you need a dash (-) you should specify that.

                Make sure the changes do not interfere with the existing quotes or invoices you already have.
            </p>
                <ToastNotification
                    toastTitle={toastTitle}
                    isToastOpen={isToastVisible}
                    toggleToast={() => setIsToastVisible(!isToastVisible)}
                />

                <CustomModal
                    show={showModal}
                    handleClose={() => setShowModal(false)}
                    handleOk={handleSubmit}
                    title="Confirm Quote accept" okTitle={"Go Ahead!"}>
                    Make sure the changes do not interfere with the existing quotes or invoices you already have.
                </CustomModal>





            <Label for="companySelect">Select Company</Label>
            <div style={{maxWidth: '30%'}}>
                <Input type="select" name="companySelect" id="companySelect" onChange={handleCompanyChange}>
                {companies.map(company => (
                    <option key={company.id} value={company.id}>
                        {company.name}
                    </option>
                ))}
            </Input>
            </div>
            <form>
                <Table>
                    <thead>
                        <tr>
                            <th></th>
                            <th>Prefix</th>
                            <th className={"w-50"}>Current Sequence #</th>
                            <th>Postfix</th>
                            <th>Example</th>
                        </tr>

                    </thead>
                    <tbody>
                        <tr>
                            <td><b>Quote:</b></td>
                            <td>
                                <Input type="text" name="prefix" id="prefix" value={quoteConfig.prefix} onChange={handleQuoteChange} placeholder="prefix e.g. QT-" />
                            </td>
                            <td>
                                <Input type="number" name="currentNumber" id="currentNumber" value={quoteConfig.currentNumber} onChange={handleQuoteChange} placeholder="sequence number starting from" />
                            </td>
                            <td>
                                <Input type="text" name="postfix" id="postfix" value={quoteConfig.postfix} onChange={handleQuoteChange} placeholder="postfix e.g. -abc" />
                            </td>
                            <td>
                                <Input type="text" disabled name="quote_example" id="quote_example" value={quoteConfig.prefix + quoteConfig.currentNumber + quoteConfig.postfix} />
                            </td>
                        </tr>
                        <tr>
                            <td><b>Invoice:</b></td>
                            <td>
                                <Input type="text" name="prefix" id="prefix" value={invConfig.prefix} onChange={handleInvChange} placeholder="prefix e.g. INV-" />
                            </td>
                            <td>
                                <Input type="number" name="currentNumber" id="currentNumber" value={invConfig.currentNumber} onChange={handleInvChange} placeholder="sequence number starting from" />
                            </td>
                            <td>
                                <Input type="text" name="postfix" id="postfix" value={invConfig.postfix} onChange={handleInvChange} placeholder="postfix e.g. -XXX " />
                            </td>
                            <td>
                                <Input type="text" disabled name="inv_example" id="inv_example" value={invConfig.prefix + invConfig.currentNumber + invConfig.postfix} />
                            </td>
                        </tr>
                    </tbody>
                </Table>

                <Button onClick={()=>setShowModal(true)}>save</Button> &nbsp;
                <Button onClick={()=>navigate("/quotes")}>Close</Button>

            </form>

        </div>

    );
};

export default QuoteNumberBuilder;