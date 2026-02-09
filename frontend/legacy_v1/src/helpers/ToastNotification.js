/* 
This is a toast component that can be used to show notifications. it fades out after 5 seconds.
You can pass the title and body of the notification as props to the component.
See an example of how to use this component in the QuoteList.js and NumberBuilder 

you have to pass both isToastOpen and toggleToast as props
*/


import React, { useEffect } from 'react';
import { Toast, ToastBody, ToastHeader } from 'reactstrap';
import '../css/ToastNotification.css'; // Import the CSS file


const ToastNotification = ({
    toastTitle = '',
    toastBody,
    isToastOpen = false,
    toggleToast,
    autoHideDuration = 5000,
    autoHideEnabled = true
}) => {

    useEffect(() => {
        let timeoutId;
        if (isToastOpen) {
            timeoutId = setTimeout(() => {
                toggleToast();
            }, autoHideEnabled ? autoHideDuration : null);
        }
        return () => clearTimeout(timeoutId);
    }, [isToastOpen, toggleToast, autoHideDuration, autoHideEnabled]);


    return (
        <div className="toast-container">

            <Toast isOpen={isToastOpen}>
                <ToastHeader toggle={toggleToast}>
                    {toastTitle}
                </ToastHeader>
                <ToastBody>
                    {toastBody}
                </ToastBody>
            </Toast>
        </div>
    );
};

export default ToastNotification;
