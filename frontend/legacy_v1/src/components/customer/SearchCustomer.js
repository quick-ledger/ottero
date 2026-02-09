import {useCallback, useEffect, useState} from 'react';
import {debounce} from 'lodash';
import {useApi} from '../../helpers/api';
import {Container, Input, Label} from 'reactstrap';

const SearchCustomer = ({ handleSelectedClient, selectedCompanyId }) => {
    const [clients, setClients] = useState([]);
    const api = useApi();
    const [showDropdown, setShowDropdown] = useState(false);

    // Debounce function to delay execution for the search of client
    const debouncedSearch = useCallback(
        debounce(async (searchTerm) => {
            console.log("searching for: " + selectedCompanyId);
            const clients = await api.get(`/api/companies/${selectedCompanyId}/clients/search?searchTerm=${searchTerm}`);
            //console.log("number of clients returned: " + clients.data.length);
            setClients(clients.data);
        }, 500),
        [selectedCompanyId]
    );

    const handleSearchChange = (event) => {
        const { value } = event.target;
        debouncedSearch(value);
    };

    const handleClientSelection = (client) => {
        handleSelectedClient(client);
        setShowDropdown(false); // Hide the dropdown after selection
    };

    useEffect(() => {
        const handleKeyDown = (event) => {
            if (event.key === 'Escape') {
                setShowDropdown(false); // Hide the dropdown when Escape is pressed
            }
        };

        document.addEventListener('keydown', handleKeyDown);

        // Cleanup the event listener on component unmount
        return () => {
            document.removeEventListener('keydown', handleKeyDown);
        };
    }, []);
    
    useEffect(() => {
        return () => {
            debouncedSearch.cancel();
        };
    }, [debouncedSearch]);

    useEffect(() => {
        if (clients && clients.length > 0) {
            setShowDropdown(true);
        } else {
            setShowDropdown(false);
        }
    }, [clients]);

    return (
        <Container>
            <Label htmlFor="search">Search Customer</Label>
            <Input
                type="text"
                name="search"
                id="search"
                placeholder="by name, phone, email"
                onChange={handleSearchChange}
                onFocus={handleSearchChange}
                autoComplete="search"
            />
            {showDropdown && (
                <ul style={{ position: 'absolute', zIndex: 1, backgroundColor: 'lightgray', listStyle: 'none', padding: 0 }}>
                    {clients.map((client, index) => (
                        <li key={index} onClick={() => handleClientSelection(client)} className="quote-edit">
                            {client.firstName} {client.lastName} {client.email} {client.phone}
                        </li>
                    ))}
                </ul>
            )}
        </Container>
    );
};

export default SearchCustomer;
