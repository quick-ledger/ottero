// UploadComponent.js
import React, { useRef } from 'react';
import { Button } from 'reactstrap';
import "../css/Upload.css";

const UploadComponent = ({ logo, handleLogoChange }) => {
  const fileInputRef = useRef(null);

  const handleButtonClick = () => {
    fileInputRef.current.click();
  };
  
  return (
    <div className='upload-container'>
      {logo ? (
        <img src={logo} alt="Logo" className='logo-image'/>
      ) : (
        <label className='upload-label'>
          Click to upload your logo
        </label>
      )}
      <input
        type="file"
        onChange={handleLogoChange}
        className="file-input"
        ref={fileInputRef}
        style={{ display: 'none' }}
      />
      <Button className="custom-upload-button" onClick={handleButtonClick}>
        Choose file
      </Button>
    </div>
  );
};

export default UploadComponent;