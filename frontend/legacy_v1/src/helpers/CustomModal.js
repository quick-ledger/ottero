import React from 'react';
import { Modal, Button } from 'react-bootstrap';

const CustomModal = ({ show, handleClose, handleOk, title, okTitle, children }) => {
    return (
        <Modal show={show} onHide={handleClose}>
            <Modal.Header closeButton>
                <Modal.Title>{title}</Modal.Title>
            </Modal.Header>
            <Modal.Body>{children}</Modal.Body>
            <Modal.Footer>

                <Button variant="secondary" onClick={handleClose}>Close</Button>
                <Button variant="primary" onClick={handleOk}>{okTitle}</Button>

            </Modal.Footer>
        </Modal>
    );
};

export default CustomModal;