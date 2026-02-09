import React, {useEffect, useState} from 'react';
import {Button, ButtonGroup, Container, Input, Modal, Table} from 'reactstrap';

import '../../css/QuoteEdit.css';
import ProductServiceSelect from '../product/ProductServiceSelect';

//document is either quote or invoice
const QuoteInvoiceEditItems = ({ setDocument, setItems, items, document, handleDocumentChange, type }) => {
    const [subTotal, setSubTotal] = useState(0);
    const [modalIsOpen, setModalIsOpen] = useState(false);

    const openModal = () => {
        setModalIsOpen(true);
    };

    const closeModal = () => {
        setModalIsOpen(false);
    };


    const emptyRow = {
        itemDescription: '',
        quantity: 1,
        price: 0.00,
        total: 0,
        gst: 0,
        itemOrder: 1,
        serviceItemId: null,
        productItemId: null

    };

    useEffect(() => {
        performItemCalculations();//side effect of changes in quote

        //provide an empty row at the beginning
        if (items && items.length === 0) {
            setItems([emptyRow]);
        }

    }, [document, items]);

    const handleAddRow = () => {
        setItems(prevItems => {
            // Get the latest item
            const latestItem = prevItems[prevItems.length - 1];
            // Calculate the new itemOrder
            const newItemOrder = latestItem ? latestItem.itemOrder + 1 : 1;
            // Define a new empty row with the new itemOrder
            const newRow = { ...emptyRow, itemOrder: newItemOrder };
            // Add the new empty row to the existing items array
            return [...prevItems, newRow];
        });
    }


    //In JavaScript, array indices start from 0. If your index starts from 1, you need to adjust it to match the array's 0-based indexing. This can be done by subtracting 1 from the index when accessing the array.
    //setItems method is VERY async. if you need to perform some action after the state update you can use useEffect
    const handleItemChange = (event, index) => {
        console.log("in item change", event.target.name, event.target.value);
        const { name, value } = event.target;
        setItems(prevItems => {
            const items = [...prevItems];
            items[index - 1] = { ...items[index - 1], [name]: value };
            return items;
        });
    }

    /*
    Your base price x 1.1 = GST inclusive price
    since this changes items which is part of component state then we have to make sure to update the state.
    it is either with useEffect or setItems
    */

    function performItemCalculations() {
        let total = 0;
        let gstApplied = 0;
        const updatedItems = items.map(item => {
            let total;
            if (Number(item.gst) === 0) {
                total = Number(item.quantity) * Number(item.price);
            } else if (Number(item.gst) === 10) {
                total = Number(item.quantity) * Number(item.price) * 1.1;
                gstApplied += (total - Number(item.quantity) * Number(item.price));
            } else {
                total = item.total;
            }
            total += total;
            return { ...item, total: total.toFixed(2) }; // Return a new item object with the updated total
        });
        if (document.discountType === 'DOLLAR') {
            total = total - document.discountValue;
        } else if (document.discountType === 'PERCENT') {
            total = total - (total * document.discountValue / 100);
        }

        //we compare the new items with the old items to avoid unnecessary re-renders. otherwise it will call useEffect and infinite loop
        if (JSON.stringify(updatedItems) !== JSON.stringify(items)) {
            setItems(updatedItems); // Update the state with the new items array
            if (type === 'invoice'){
                setDocument({ ...document, invoiceItems: updatedItems });
            }else {
                setDocument({ ...document, quoteItems: items });
            }
        }

        if (document.totalPrice !== total || document.gst !== gstApplied.toFixed(2)) {
            if(type === 'invoice'){
                setDocument({ ...document, totalPrice: total, gst: gstApplied.toFixed(2),
                    invoiceItems: updatedItems }); // Update the quote state with the new total
            }else {
                setDocument({
                    ...document, totalPrice: total, gst: gstApplied.toFixed(2),
                    quoteItems: updatedItems
                }); // Update the quote state with the new total
            }
        }

        setSubTotal(total - gstApplied);
    }

    const handleRemoveRow = async (index) => {
        if (index > 0) { // Adjusted to account for index starting from 1
            // Remove the row and re-arrange itemOrder
            setItems(prevItems => {
                // Remove the item at the specified index (adjusting for 1-based index)
                const updatedItems = prevItems.filter((_, i) => i !== index - 1);
                // Re-arrange itemOrder if there are remaining items
                if (updatedItems.length > 0) {
                    return updatedItems.map((item, i) => ({
                        ...item,
                        itemOrder: i + 1
                    }));
                } else {
                    return [];
                }
            });
        }
    };

    const handleProductSelect = (itemOrder, product) => {
        setItems(prevItems => {
            const items = [...prevItems];
            items[itemOrder - 1] = { ...items[itemOrder - 1], itemDescription: product.name, productItemId: product.id, price: product.price };
            return items;
        });
        closeModal();
    }

    const handleServiceSelect = (itemOrder, service) => {
        setItems(prevItems => {
            const items = [...prevItems];
            items[itemOrder - 1] = { ...items[itemOrder - 1], itemDescription: service.name, serviceItemId: service.id, price: service.price };
            return items;
        });
        closeModal();
    }

    return (
        <Container>
            <Table>
                <thead>
                    <tr>
                        <th>#</th>
                        <th style={{ width: "40%" }}>Description</th>
                        <th> </th>
                        <th>Price</th>
                        <th>Quanity</th>
                        <th>GST</th>
                        <th>Total</th>
                    </tr>
                </thead>
                <tbody>

                    {(items).map((item) => (
                        <tr key={item.itemOrder}>
                            <td>
                                <span>{item.itemOrder}</span>
                            </td>
                            <td>
                                <Input
                                    disabled={item.serviceItemId || item.productItemId}
                                    required
                                    type="textarea"
                                    name="itemDescription"
                                    value={item.itemDescription}
                                    onChange={event => handleItemChange(event, item.itemOrder)}
                                    style={{ height: 'auto' }} // Adjust the height to auto
                                />
                            </td>
                            <td>
                                <ButtonGroup>
                                    <Button size="sm" color="primary" onClick={openModal}>+</Button>
                                </ButtonGroup>
                                <Modal
                                    isOpen={modalIsOpen}
                                    onRequestClose={closeModal} >

                                    <button onClick={closeModal}>Close</button>
                                    <ProductServiceSelect
                                        handleProductSelect={handleProductSelect}
                                        handleServiceSelect={handleServiceSelect}
                                        itemOrder={item.itemOrder} />
                                </Modal>
                            </td>
                            <td>
                                <Input
                                    type="text"
                                    name="price"
                                    value={item.price}
                                    onChange={event => handleItemChange(event, item.itemOrder)}
                                    onBlur={event => handleItemChange(event, item.itemOrder)}
                                />
                            </td>
                            <td>

                                <Input
                                    type="text"
                                    name="quantity"
                                    value={item.quantity}
                                    onChange={event => handleItemChange(event, item.itemOrder)}
                                    onBlur={event => handleItemChange(event, item.itemOrder)}
                                />
                            </td>
                            <td>
                                <select name="gst" id="gst" value={item.gst}
                                    onChange={event => handleItemChange(event, item.itemOrder)}>
                                    <option value="0">0%</option>
                                    <option value="10">10%</option>
                                </select>
                            </td>
                            <td>
                                {item.total}
                            </td>

                            <td>
                                <ButtonGroup>
                                    <Button size="sm" color="danger" onClick={() => handleRemoveRow(item.itemOrder)}>Delete</Button>
                                </ButtonGroup>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </Table>

            <Button onClick={handleAddRow}>Add Row</Button>

            <Table style={{ border: 'none', width: '30%', float: 'right' }}>
                <tbody>
                    <tr>
                        <td>Subtotal</td>
                        <td> $</td>
                        <td disabled>
                            {subTotal}
                        </td>
                    </tr>
                    <tr>
                        <td>GST Applied</td>
                        <td> $</td>
                        <td disabled>
                            {document.gst}
                        </td>
                    </tr>
                    <tr>
                        <td>Discount</td>
                        <td>
                            <select name="discountType" id="discountType" value={document.discountType}
                                onChange={handleDocumentChange}>
                                <option value="DOLLAR">$</option>
                                <option value="PERCENT">%</option>
                            </select>
                        </td>
                        <td>
                            <Input type="text" name="discountValue" id="discountValue" value={document.discountValue}
                                onChange={handleDocumentChange}
                                onBlur={handleDocumentChange}
                            />
                        </td>
                    </tr>
                    <tr>
                        <td>Total</td>
                        <td>$</td>
                        <td>{document.totalPrice}</td>
                    </tr>

                </tbody>
            </Table>
        </Container>
    );
};

export default QuoteInvoiceEditItems;