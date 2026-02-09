import React, {useEffect, useState} from 'react';
import {Button, Container, Dropdown, DropdownItem, DropdownMenu, DropdownToggle, Table} from 'reactstrap';
import {useApi} from '../../helpers/api';
import {useSelector} from 'react-redux';
import {Link, useNavigate} from 'react-router-dom';
import '../../css/CommonCSS.css';
import CustomModal from '../../helpers/CustomModal';
import ToastNotification from '../../helpers/ToastNotification';
import Pagination from '../../helpers/Pagination';
import "../../css/CustomerList.css";


const CustomerList = () => {
	const navigate = useNavigate();

	const [customers, setCustomers] = useState([]);
	const api = useApi();
	const selectedCompanyId = useSelector(state => { return state.app.selectedCompanyId; });
	const [refresh, setRefresh] = useState(false);
	const [modalProperties, setModalProperties] = useState({
		title: '',
		body: '',
		okTitle: '',
		okHandler: null,
		showModal: false
	});

	const [toastProperties, setToastProperties] = useState({
		toastTitle: '',
		toastBody: '',
	});

	const [page, setPage] = useState(0); // Current page number
	const [pageSize, setPageSize] = useState(10); // Number of items per page
	const [totalPages, setTotalPages] = useState(0); // Total number of pages

	const toggleToastVisibility = () => {
		setToastProperties(prevState => ({
			...prevState,
			isToastOpen: !prevState.isToastOpen
		}));
	};

	useEffect(() => {
		const fetchData = async () => {
			try {
				let response = await api.get(`/api/companies/${selectedCompanyId}/clients`, {
					params: {
						page: page,
						size: pageSize
					}
				});
				setCustomers(response.data.content);
				setTotalPages(response.data.totalPages);
			} catch (error) {
				console.error("Error fetching customers:", error);
				// Optionally, set an error state or handle the error appropriately
			}
		};

		fetchData();
	}, [refresh, page, pageSize]);

	const remove = async () => {
		try {
			setModalProperties({ showModal: false });

			await api.delete(`/api/companies/${selectedCompanyId}/clients/${selectedCustomer.id}`);
			setToastProperties({
				toastTitle: 'Customer Deleted',
				toastBody: 'The customer was successfully deleted.'
			});
			setRefresh(!refresh);
		} catch (error) {
			console.error("Error deleting customer:", error);
			setToastProperties({
				toastTitle: 'Error Deleting Customer',
				toastBody: 'An error occurred while deleting the customer.'
			});
		}
	};


	//---Actions dropdown 
	const [selectedCustomer, setSelectedCustomer] = useState({});
	const [dropdownOpen, setDropdownOpen] = useState(false);

	const toggleDropdown = () => setDropdownOpen(prevState => !prevState);

	const handleSelectCustomer = (customer) => {
		setSelectedCustomer(customer);
	};

	const handleDeleteSelected = () => {
		setModalProperties({
			title: 'Delete Selected Customers',
			body: 'Are you sure you want to delete the selected customers?',
			okTitle: 'Delete',
			okHandler: remove,
			showModal: true
		});
	};

	return (
		<Container>


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


			<div className="inline-container">
				<h3>List of Customers</h3>
				<Button color="success" size="sm" tag={Link} to="/customers/new">+ Customer</Button>
			</div>
			<Table>
				<thead>
					<tr>

						<th>Name</th>
						<th>Email</th>
						<th>
							<Dropdown isOpen={dropdownOpen} toggle={toggleDropdown}>
								<DropdownToggle caret>
									Actions
								</DropdownToggle>
								<DropdownMenu>
									<DropdownItem onClick={handleDeleteSelected} disabled={selectedCustomer.length === 0}>
										Delete Selected
									</DropdownItem>
									{/* Add more actions here */}
								</DropdownMenu>
							</Dropdown>
						</th>
					</tr>
				</thead>
				<tbody>
					{(customers || []).map(customer => (
						<tr key={customer.id}>
							<td>
								<a href={`customers/${customer.id}`}>
									{customer.firstName} {customer.lastName}
								</a>


							</td>
							<td>
							{customer.email}</td>
							<td>
								<input
									type="radio"
									name="SingleName"
									onChange={() => handleSelectCustomer(customer)}
								/>
							</td>

						</tr>
					))}
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

export default CustomerList;