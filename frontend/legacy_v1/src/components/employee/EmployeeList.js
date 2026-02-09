import React, {Component} from 'react';
import {Button, ButtonGroup, Container, Table} from 'reactstrap';
//import { Link } from 'react-router-dom';
import {Link} from "react-router-dom";

class EmployeeList extends Component {

    constructor(props) {
        super(props);
        this.state = {employees: []};
        this.remove = this.remove.bind(this);
    }

    componentDidMount() {
        fetch('/employees')
            .then(response => response.json())
            .then(data => this.setState({employees: data}));
    }

    async remove(id) {
        await fetch(`/employees/${id}`, {
            method: 'DELETE',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            }
        }).then(() => {
            let updatedemployees = [...this.state.employees].filter(i => i.id !== id);
            this.setState({employees: updatedemployees});
        });
    }

    render() {
        const {employees} = this.state;

        const employeeList = employees.map(employee => {
            return <tr key={employee.id}>
                <td style={{whiteSpace: 'nowrap'}}>{employee.summary}</td>
                <td>{employee.summary}</td>
                <td>
                    <ButtonGroup>
                        <Button size="sm" color="primary" tag={Link} to={"/employees/" + employee.id}>Edit</Button>
                        <Button size="sm" color="danger" onClick={() => this.remove(employee.id)}>Delete</Button>
                    </ButtonGroup>
                </td>
            </tr>
        });

        return (
            <div>
                <Container fluid>
                    <div className="float-right">
                        <Button color="success" tag={Link} to="/employees/new">Add employee</Button>
                    </div>
                    <h3>List of employees</h3>
                    <Table className="mt-4">
                        <thead>
                        <tr>
                            <th width="30%">Name</th>
                            <th width="30%">Email</th>
                            <th width="40%">Actions</th>
                        </tr>
                        </thead>
                        <tbody>
                        {employeeList}
                        </tbody>
                    </Table>
                </Container>
            </div>
        );
    }
}

export default EmployeeList;