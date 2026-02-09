import React, {useEffect, useState} from 'react';
import {Button, ButtonGroup, Container, Table} from 'reactstrap';
import {Link} from "react-router-dom";
//import { AppProvider, useAppContext } from '../helpers/AppContext';
import {useApi} from '../../helpers/api';

const AssetList = () => {
  
  // react hook - It takes one argument, which is the initial state, 
  // and returns an array with two elements. The first element is the current state, 
  // and the second element is a function to update that state.
  //note that calling a `setState` function will trigger a re-render of the component.

  const [assets, setAssets] = useState([]);
  const api = useApi();

  const fetchAssets = async () => {
    try {
      const response = await api.get('/api/assets');
      setAssets(response.data);
      // Do something with response.data
    } catch (error) {

      console.error('Failed to fetch assets:', error);
    }
  };

  useEffect(() => {
    fetchAssets();
  }, []);

  const remove = async (id) => {
    await api.delete(`/api/assets/${id}`)
    let updatedQuotes = [...assets].filter(i => i.id !== id);
    setAssets(updatedQuotes);
  };



  return (

    <div style={{clear:'both'}}>
    <Container>

        <h3>List of Assets</h3>
        <Table className="mt-4">
            <thead>
            <tr>
                <th width="30%">Name</th>
                <th width="30%">Description</th>
            </tr>
            </thead>
            <tbody>
            {assets.map(asset => {
                return <tr key={asset.id}>
                    <td style={{whiteSpace: 'nowrap'}}>{asset.name}</td>
                    <td>{asset.description}</td>
                    <td>
                        <ButtonGroup>
                            <Button size="sm" color="primary" tag={Link} to={"/assets/" + asset.id}>Edit</Button>
                            <Button size="sm" color="danger" onClick={() => remove(asset.id)}>Delete</Button>
                        </ButtonGroup>
                    </td>
                    <td>
                    
                    </td>
                </tr>
            })}
            </tbody>
        </Table>
    </Container>

    <Button color="success" tag={Link} to="/assets/new">Add Asset</Button>

</div>


  )

};

export default AssetList;