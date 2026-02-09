import React, {useEffect, useState} from 'react';
import {
    Button,
    Col,
    Collapse,
    Container,
    Dropdown,
    DropdownItem,
    DropdownMenu,
    DropdownToggle,
    Input,
    Label,
    Row,
    Table
} from 'reactstrap';
import {useApi} from '../../helpers/api';

import {useSelector} from 'react-redux';
import {Link, useNavigate} from 'react-router-dom';

import ToastNotification from '../../helpers/ToastNotification'; // Import the ToastNotification component
import CustomModal from '../../helpers/CustomModal';
import Pagination from '../../helpers/Pagination';

const QuoteList = () => {
    const navigate = useNavigate();
    const api = useApi();
    const selectedCompanyId = useSelector(state => {
        return state.app.selectedCompanyId;
    });
    const [quotes, setQuotes] = useState([]);
    const [originalQuotes, setOriginalQuotes] = useState([]);
    const [searchValue, setSearchValue] = useState('');
    const [openRevisions, setOpenRevisions] = useState([]);
    const [revisions, setRevisions] = useState([]);
    const [refresh, setRefresh] = useState(false); // State variable to trigger useEffect

    const [page, setPage] = useState(0); // Current page number
    const [pageSize] = useState(10); // Number of items per page
    const [totalPages, setTotalPages] = useState(0); // Total number of pages


    const [toastProperties, setToastProperties] = useState({
        toastTitle: '',
        toastBody: '',
        //isToastOpen: false,
        // autoHideEnabled: true
    });

    const [modalProperties, setModalProperties] = useState({
        title: '',
        body: '',
        okTitle: '',
        okHandler: null,
        showModal: false
    });


    const toggleRevision = (quoteNumber) => {
        setOpenRevisions(prevState => ({
            ...prevState,
            [quoteNumber]: !prevState[quoteNumber]
        }));

        getRevisions(quoteNumber);
    };

    const toggleToastVisibility = () => {
        setToastProperties(prevState => ({
            ...prevState,
            isToastOpen: !prevState.isToastOpen
        }));
    };

    //const [isOpenRevision, setIsOpenRevision] = useState(false);
    //const toggleRevision = () => setIsOpenRevision(!isOpenRevision);


    //---Actions dropdown
    const [selectedQuote, setSelectedQuote] = useState({});
    const [dropdownOpen, setDropdownOpen] = useState(false);
    const toggleDropdown = () => setDropdownOpen(prevState => !prevState);

    useEffect(() => {
        const fetchData = async () => {
            try {
                const response = await api.get(`/api/companies/${selectedCompanyId}/quotes/quotenumbers`,{
                    params: {
                        page: page,
                        size: pageSize
                    }
                });
                //if we use java pagination then response is wrapped arount an object calle content
                setQuotes(response.data.content);
                setTotalPages(response.data.totalPages); // Assuming the response has a 'totalPages' field
                setOriginalQuotes(response.data.content);
            } catch (error) {
                console.error('Error fetching quotes:', error);
                setQuotes([]); // Set quotes to an empty array in case of error
                setToastProperties({
                    toastTitle: 'Failed',
                    toastBody: "Error fetching quotes. Please try again later.",
                    isToastOpen: true,
                });
            };
        };
        fetchData();
    }, [selectedCompanyId,refresh, page, pageSize]);

    const handleSelectedQuote = (quote) => {
        setSelectedQuote(quote);
    }

    const remove = async (id) => {
        console.log('Deleting quote:', id);
        setModalProperties({ showModal: false });
        try {
            await api.delete(`/api/companies/${selectedCompanyId}/quotes/${id}`);
            setRefresh(!refresh); // Toggle refresh state to trigger useEffect
            setToastProperties({
                toastTitle: 'Quote Delete',
                toastBody: 'Quote has been deleted successfully',
                isToastOpen: true,
            });

        } catch (error) {
            console.error('Error deleting quote:', error);
            setToastProperties({
                toastTitle: 'Failed',
                toastBody: error.response.data,
                isToastOpen: true,
            });
        }
    };

    const getRevisions = async (quoteNumber) => {
        try {
            const revs = await api.get(`/api/companies/${selectedCompanyId}/quotes/quotenumbers/${quoteNumber}/revisions`);
            console.log("revisions: " + revs.data);
            setRevisions(prevRevisions => ({
                ...prevRevisions,
                [quoteNumber]: revs.data
            }));
        } catch (error) {
            console.error('Error fetching revisions:', error);
        }

    }

    const getLatestRevision = async (quoteNumber) => {
        try {
            const response = await api.get(`/api/companies/${selectedCompanyId}/quotes/quotenumbers/${quoteNumber}/revisions/latest`);
            navigate(`/quotes/${response.data.id}`);
        } catch (error) {
            console.error('Error fetching latest revision:', error);
            setToastProperties({
                toastTitle: 'Failed',
                toastBody: error.response.data,
                isToastOpen: true,
            });
        }
    }


    const handleRevisionSelected = async (quoteNumber) => {
        if (selectedQuote.status === 'DRAFT') {
            setToastProperties({    
                toastTitle: 'Not Allowed',
                toastBody: 'Cannot create a revision for a draft quote. You can copy it instead into a new quote.',
                isToastOpen: true,
            });

            return;
        }

        const response = await api.get(`/api/companies/${selectedCompanyId}/quotes/quotenumbers/${quoteNumber}/revisions/latest`);
        const quote = response.data;
        const copyQuote = {
            ...quote, id: '', status: 'DRAFT', quoteRevision: quote.quoteRevision + 1,
            quoteItems: quote.quoteItems.map(item => ({ ...item, id: '' }))
        };
        navigate(`/quotes/new`, { state: { copyQuote } });
    };

    const handleCopySelected = async (quoteNumber) => {
        const response = await api.get(`/api/companies/${selectedCompanyId}/quotes/quotenumbers/${quoteNumber}/revisions/latest`);
        const quote = response.data;

        //set IDs to null before moving to the edit page. This is an empty quote.
        const copyQuote = {
            ...quote, id: '', quoteNumber: '', status: 'DRAFT',
            quoteItems: quote.quoteItems.map(item => ({ ...item, id: '' }))
        };
        navigate(`/quotes/new`, { state: { copyQuote } });
    };


    const handleDeleteSelected = () => {
        setModalProperties({
            showModal: true,
            title: 'DANGER!',
            body: 'Are you sure you want to delete this latest revision?',
            okTitle: 'Delete',
            okHandler: () => remove(selectedQuote.id)
        });
    }

    const handleSearch = async (searchValue) => {
        console.log("searching");
        if (searchValue.trim() === "") {
            setQuotes(originalQuotes);
            return;
        }
        try {
            let response = await api.get(`/api/companies/${selectedCompanyId}/quotes/search?searchTerm=${searchValue}`);
            setQuotes(response.data);
        } catch (error) {
            console.error("Error fetching quotes:", error);
            setQuotes([]); // Set quotes to an empty array in case of error
        }
    };


    const handleKeyDown = (event) => {
        if (event.key === 'Enter') {
            handleSearch(event.target.value);
        }
    };

    return (
        <Container>
            <Button color="success" tag={Link} to="/quotes/new"> + Add Quote</Button>

            <br />
            <br />
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

                <Col sm="3"><Label htmlFor="search">Search Quotes</Label></Col>
                <Col>
                    <Input sm="3"
                        type="text"
                        name="search"
                        id="search"
                        placeholder="by quote number or customer"
                        autoComplete="search"
                        onChange={(e) => setSearchValue(e.target.value)}
                        onKeyDown={(e) => handleKeyDown(e)}

                    />
                </Col>
                <Col>
                    <Button onClick={() => handleSearch(searchValue)}>
                        Search
                    </Button>
                </Col>
            </Row>




            <Table className="table-hover">
                <thead>
                    <tr>
                        <th>Quote Number</th>
                        <th>Revision</th>
                        <th>Bill To</th>
                        <th>Status</th>
                        <th>Total Price</th>
                        <th>
                            <div>
                                <Dropdown isOpen={dropdownOpen} toggle={toggleDropdown}>
                                    <DropdownToggle caret>
                                        Actions
                                    </DropdownToggle>
                                    <DropdownMenu>
                                        <DropdownItem onClick={handleDeleteSelected} disabled={selectedQuote.length === 0}>
                                            Delete
                                        </DropdownItem>
                                        <DropdownItem onClick={() => handleCopySelected(selectedQuote.quoteNumber)} disabled={selectedQuote.length === 0}>
                                            Duplicate
                                        </DropdownItem>
                                        <DropdownItem onClick={() => handleRevisionSelected(selectedQuote.quoteNumber)} disabled={selectedQuote.length === 0}>
                                            Make a Revision
                                        </DropdownItem>
                                        <DropdownItem onClick={() => { navigate(`/quotes/${selectedQuote.id}`); }} disabled={selectedQuote.length === 0}>
                                            Edit
                                        </DropdownItem>
                                        {/* Add more actions here */}
                                    </DropdownMenu>
                                </Dropdown>
                            </div>
                        </th>
                    </tr>
                </thead>
                <tbody>

                    {quotes.map(quote => {
                        return <tr key={quote.quoteNumber}>
                            <td >
                                <button
                                    onClick={() => getLatestRevision(quote.quoteNumber)}
                                    className="btn btn-link"
                                >
                                    {quote.quoteNumber}
                                </button>
                                <br />
                                {quote.quoteRevision > 0 &&
                                <a href="#" color="primary" onClick={() => toggleRevision(quote.quoteNumber)} style={{ fontSize: '0.6em' }}>Revisions</a>
                                }
                                <Collapse isOpen={openRevisions[quote.quoteNumber]}>
                                    {(revisions[quote.quoteNumber] || []).map((quote, index) => (
                                        <span key={quote.id}>
                                            <a href={`/quotes/${quote.id}`}> {quote.quoteRevision}</a>
                                            {index < revisions[quote.quoteNumber].length - 1 && ', '}
                                        </span>
                                    ))}
                                </Collapse>
                            </td>
                            <td>{quote.quoteRevision}</td>

                            <td style={{ whiteSpace: 'nowrap' }}>{quote.clientFirstname} {quote.clientLastname} {quote.clientEntityName}</td>
                            <td>{quote.status}</td>
                            <td>{quote.totalPrice}</td>
                            <td>
                                <input
                                    type="radio"
                                    name="quoteSelectionSingleName"
                                    onChange={() => handleSelectedQuote(quote)}
                                />
                            </td>
                        </tr>
                    })}
                </tbody>
            </Table>

            <Pagination
                currentPage={page}
                totalPages={totalPages}
                onPageChange={setPage}
            />
        </Container>
    );
};

export default QuoteList;