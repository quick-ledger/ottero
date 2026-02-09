import React, {useEffect, useState} from 'react';
import {
    Button,
    Card,
    CardBody,
    Collapse,
    Container,
    Dropdown,
    DropdownItem,
    DropdownMenu,
    DropdownToggle,
    Table
} from 'reactstrap';
import {useApi} from '../../helpers/api';

import {useSelector} from 'react-redux';
import {Link, useNavigate} from 'react-router-dom';

import ToastNotification from '../../helpers/ToastNotification'; // Import the ToastNotification component
import CustomModal from '../../helpers/CustomModal';
import Pagination from '../../helpers/Pagination';


const InvoiceList = () => {
    const navigate = useNavigate();
    const api = useApi();
    const selectedCompanyId = useSelector(state => {
        return state.app.selectedCompanyId;
    });
    const [invoices, setInvoices] = useState([]);
    const [refresh, setRefresh] = useState(false); // State variable to trigger useEffect

    const [page, setPage] = useState(0); // Current page number
    const [pageSize] = useState(10); // Number of items per page
    const [totalPages, setTotalPages] = useState(0); // Total number of pages


    const [isToastVisible, setIsToastVisible] = useState(false);
    const [showModal, setShowModal] = useState(false);

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
    const toggleToastVisibility = () => {
        setToastProperties(prevState => ({
            ...prevState,
            isToastOpen: !prevState.isToastOpen
        }));
    };    // const toggleToast = (title) => {
    //     setToastTitle(title);
    //     toggleToastVisibility();
    // }
    const [isOpenRevision, setIsOpenRevision] = useState(false);
    const toggleRevision = () => setIsOpenRevision(!isOpenRevision);
    //---Actions dropdown
    const [selectedInvoice, setSelectedInvoice] = useState({});
    const [dropdownOpen, setDropdownOpen] = useState(false);
    const toggleDropdown = () => setDropdownOpen(prevState => !prevState);


    useEffect(() => {
        const fetchData = async () => {

            try {
                const invoices = await api.get(`/api/companies/${selectedCompanyId}/invoices?lazy=true`, {
                    params: {
                        page: page,
                        size: pageSize
                    }

                });
                setInvoices(invoices.data.content);
                setTotalPages(invoices.data.totalPages); // Assuming the response has a 'totalPages' field
                //setOriginalQuotes(invoices.data.content);
            } catch (e) {
                console.error("Error fetching invoices:", e);
                setToastProperties({
                    toastTitle: 'Failed',
                    toastBody: "Error fetching quotes. Please try again later.",
                    isToastOpen: true,
                });
            }
        };

        fetchData();
    }, []);


    const handleSelectedInvoice = (invoice) => {
        setSelectedInvoice(invoice);
    }

    const remove = async (id) => {
        //await api.delete(`/api/companies/${selectedCompanyId}/invoices/${id}`);
        setShowModal(false);

        toggleToastVisibility();
    };

    const handleDeleteSelected = () => {
        setShowModal(true);
    };

    const handleEditSelected = () => {
        navigate(`/invoices/${selectedInvoice.id}`);
    };

    const handleRevisionSelected = () => {
        //selectedInvoice.forEach(customerId => remove(customerId));
        setSelectedInvoice([]);
    };

    const handleCopySelected = () => {
        
        console.log(selectedInvoice);

        //set IDs to null before moving to the edit page. This is an empty invoice.
        const copyInvoice = { ...selectedInvoice, id: '', invoiceNumber: '', status: 'DRAFT' ,
            invoiceItems: selectedInvoice.invoiceItems.map(item => ({ ...item, id: '' })) };
        navigate(`/invoices/new`, { state: { copyInvoice } });
    };

    //edit the whole row go into edit page
    const handleEditRow = (id) => {
        navigate(`/invoices/${id}`);
    }

    return (
        <Container>
            <Button color="success" tag={Link} to="/invoices/new"> + Add Invoice</Button>

            <ToastNotification
                toastBody="Invoice Deleted"
                isToastOpen={isToastVisible}
                toggleToast={toggleToastVisibility}
            />

            <CustomModal show={showModal} handleClose={() => setShowModal(false)} handleOk={remove} okTitle={"Delete"}
                title="Confirm invoice delete">
                Do you really want to delete this Invoice?
            </CustomModal>


            <Table className="table-hover">
                <thead>
                    <th>Invoice Number</th>
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
                                    <DropdownItem onClick={handleDeleteSelected} disabled={selectedInvoice.length === 0}>
                                        Delete
                                    </DropdownItem>
                                    <DropdownItem onClick={handleCopySelected} disabled={selectedInvoice.length === 0}>
                                        Duplicate
                                    </DropdownItem>
                                    <DropdownItem onClick={handleEditSelected} disabled={selectedInvoice.length === 0}>
                                        Edit
                                    </DropdownItem>
                                    {/* Add more actions here */}
                                </DropdownMenu>
                            </Dropdown>
                        </div>
                    </th>
                </thead>
                <tbody>

                    {(invoices||[]).map(invoice => {
                        return <tr key={invoice.id}>
                            <td >
                                <a href="" onClick={() => handleEditRow(invoice.id)}>{invoice.invoiceNumber}</a> 
                            </td>
                            <td style={{ whiteSpace: 'nowrap' }}>{invoice.clientFirstname}-{invoice.clientLastname}-{invoice.clientEntityName}</td>
                            <td>{invoice.status}</td>
                            <td>{invoice.totalPrice}</td>
                            <td>
                                <input
                                    type="radio"

                                    onChange={() => handleSelectedInvoice(invoice)}
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

export default InvoiceList;