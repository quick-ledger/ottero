// Pagination.js
import React from 'react';
import '../css/Pagination.css'; // Import the CSS file

const Pagination = ({ currentPage, totalPages, onPageChange }) => {
    const handleNextPage = () => {
        if (currentPage < totalPages - 1) {
            onPageChange(currentPage + 1);
        }
    };

    const handlePreviousPage = () => {
        if (currentPage > 0) {
            onPageChange(currentPage - 1);
        }
    };

    return (
        <div className="pagination-container">
            <button
                className="pagination-button"
                onClick={handlePreviousPage}
                disabled={currentPage === 0}
            >
                Previous
            </button>
            <span className="pagination-info">
                Page {currentPage + 1} of {totalPages}
            </span>
            <button
                className="pagination-button"
                onClick={handleNextPage}
                disabled={currentPage === totalPages - 1}
            >
                Next
            </button>
        </div>
    );
};

export default Pagination;