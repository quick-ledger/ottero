/**
 * i dont think combining toast and modal confirmation is a good idea
 * 
 */
import React, { useState } from 'react';
import { Modal, ModalHeader, ModalBody, ModalFooter, Button, Toast, ToastBody, ToastHeader } from 'reactstrap';

const ConfirmationWithToast = (handleConfirm) => {
  const [modalOpen, setModalOpen] = useState(false);
  const [toastVisible, setToastVisible] = useState(false);
  const [toastMessage, setToastMessage] = useState('');
  const [autoHideEnabled, setAutoHideEnabled] = useState(true);

  const toggleModal = () => setModalOpen(!modalOpen);
  const showToast = (message) => {
    setToastMessage(message);
    setToastVisible(true);
    if (autoHideEnabled) {
      setTimeout(() => setToastVisible(false), 3000); // Auto-hide after 3 seconds
    }
  };

  const handleSubmit = () => {
    // Handle form submission or confirmation action
    toggleModal();
    handleConfirm();
    showToast('Your action was successful!');
  };

  return (
    <div>
      <Button color="primary" onClick={toggleModal}>Open Modal</Button>

      <Modal isOpen={modalOpen} toggle={toggleModal}>
        <ModalHeader toggle={toggleModal}>Modal Title</ModalHeader>
        <ModalBody>
          {/* Form or content goes here */}
          Are you sure you want to proceed?
        </ModalBody>
        <ModalFooter>
          <Button color="primary" onClick={handleSubmit}>Confirm</Button>
          <Button color="secondary" onClick={toggleModal}>Cancel</Button>
        </ModalFooter>
      </Modal>

      <div className="p-3 my-2 rounded">
        <Toast isOpen={toastVisible}>
          <ToastHeader icon="success">
            Notification
          </ToastHeader>
          <ToastBody>
            {toastMessage}
          </ToastBody>
        </Toast>
      </div>
    </div>
  );
};

export default ConfirmationWithToast;