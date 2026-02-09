import React, {useEffect, useState} from 'react';
import {Link} from 'react-router-dom';
import {Button, ButtonGroup, Table} from 'reactstrap';
import {useApi} from '../../helpers/api';
import {useDispatch, useSelector} from 'react-redux';
import {setCompanyId, setCompanyName} from '../../helpers/redux-action';


const AssetDefList = () => {

  const [productDefs, setProductDefs] = useState([]);
  const api = useApi();
  const dispatch = useDispatch();


  const selectedCompanyId = useSelector(state => {
    return state.app.selectedCompanyId;
  });

  const selectedCompanyName = useSelector(state => {
    return state.app.selectedCompanyName;
  });


  const remove = async (id) => {
    await api.delete(`/api/companies/${selectedCompanyId}/product-definition/${id}`)
    let updatedList = [...productDefs].filter(i => i.id !== id);
    setProductDefs(updatedList);
  };


  const fetchProductDefs = async () => {
    try {

      const response = await api.get(`/api/companies/${selectedCompanyId}/product-definition/tabular`);
      setProductDefs(response.data);
    } catch (error) {

      console.error('Failed to fetch product defs:', error);
    }
  };

  useEffect(() => {
    fetchProductDefs();
    getDefaultCompany();
  }, []);

  const selectedToken = useSelector(state => {
    return state.app.selectedToken;
  });

  const getDefaultCompany = async () => {
    //event.preventDefault();
    try {
      let response;
      response = await api.get(`/api/users/${selectedToken?.databaseId}`);
      dispatch(setCompanyName(response.data && response.data.defaultCompany.name));
      dispatch(setCompanyId(response.data && response.data.defaultCompany.id));

    } catch (error) {
      if (error.response && error.response.status === 500) {
      } else {
      }
    }
  }



  return (
    <div style={{ clear: 'both' }}>


      <h3>List of Asset Definitions</h3>
      <p>For {selectedCompanyName} company. Change the current company <a href="/companies">here</a>.</p>

      <Table className="mt-4">
        <thead>
          <tr>
            <th width="30%">Title</th>
            <th width="30%">Description</th>
            <th width="30%">Fields</th>
            <th width="10%">Action</th>
          </tr>
        </thead>
        <tbody>
          {productDefs.map(def => {
            return <tr key={def.id}>
              <td style={{ whiteSpace: 'nowrap' }}>{def.name}</td>
              <td>{def.productDescription}</td>
              <td>
                {def.rows && def.rows.map(row => row.name).join(', ')}
              </td>
              <td>
                <ButtonGroup>
                  <Button size="sm" color="primary" tag={Link} to={"/assets/def/" + def.id}>Edit</Button>
                  <Button size="sm" color="danger" onClick={() => remove(def.id)}>Delete</Button>
                </ButtonGroup>
              </td>

            </tr>
          })}
        </tbody>
      </Table>

      <Button color="success" tag={Link} to="/assets/def/new">Add New Asset Definition</Button>

    </div>


  );



};

export default AssetDefList;


