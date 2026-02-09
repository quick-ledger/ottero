import React, {useEffect, useState} from 'react';
import {Link, useParams} from 'react-router-dom';
import {Button, Container, Form, FormGroup, Input, Label} from 'reactstrap';

const EmployeeEdit = () => {
    const emptyItem = {
        name: '',
        email: ''
    };

    const [item, setItem] = useState(emptyItem);
    const { id } = useParams();

    useEffect(() => {
        const fetchData = async () => {
            if (id !== 'new') {
                const employee = await (await fetch(`/employee/${id}`)).json();
                setItem(employee);
            }
        };

        fetchData();
    }, [id]);

    const handleChange = (event) => {
        const target = event.target;
        const value = target.value;
        const name = target.name;
        //let item = {...this.state.item}; //TODO replace with useState hook's state
        item[name] = value;
        setItem(item);
    }


    const handleSubmit = async event => {
        //event.preventDefault();
        // Handle form submission here

        event.preventDefault();
        const {item} = this.state;
    
        const response = await fetch('/employee' + (item.id ? '/' + item.id : ''), {
            method: (item.id) ? 'PUT' : 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(item),
        });

        //history.push('/invoices'); // Navigate back to the invoices list after saving

        //const result = await response.json()
        
    };

    return (
        <div>
            <Container>
                <Form onSubmit={handleSubmit}>
                    <FormGroup>
                        <Label htmlFor="name">Name</Label>
                        <Input type="text" name="name" id="name" value={item.name || ''}
                               onChange={handleChange} autoComplete="name"/>
                    </FormGroup>
                    <FormGroup>
                        <Label htmlFor="email">Email</Label>
                        <Input type="text" name="email" id="email" value={item.email || ''}
                               onChange={handleChange} autoComplete="email"/>
                    </FormGroup>
                    <FormGroup>
                        <Button color="primary" type="submit">Save</Button>{' '}
                        <Button color="secondary" tag={Link} to="/employee">Cancel</Button>
                    </FormGroup>
                </Form>
            </Container>
        </div>

    );
};

export default EmployeeEdit;