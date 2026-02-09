import React from 'react';
import CompanyEdit from '../components/company/CompanyEdit';
const Company = () => {

 return (
   <div>

<div class="alert alert-success">
  Complete your business or company profile. We will use this information to build your quotes and invoices.
</div>

                  <CompanyEdit />

<p><a href="#" class="link-primary link-offset-2 link-underline-opacity-25 link-underline-opacity-100-hover">Add services your business provides</a></p>

<p><a href="#" class="link-primary link-offset-2 link-underline-opacity-25 link-underline-opacity-100-hover">Add your employees</a></p>



   </div>
 );
};

export default Company;