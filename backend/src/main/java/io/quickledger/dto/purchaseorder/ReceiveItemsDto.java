package io.quickledger.dto.purchaseorder;

import java.util.List;

public class ReceiveItemsDto {

    private Long purchaseOrderId;
    private List<ReceiveItemDto> items;
    private String notes;

    public static class ReceiveItemDto {
        private Long purchaseOrderItemId;
        private int quantityReceived;

        public Long getPurchaseOrderItemId() {
            return purchaseOrderItemId;
        }

        public void setPurchaseOrderItemId(Long purchaseOrderItemId) {
            this.purchaseOrderItemId = purchaseOrderItemId;
        }

        public int getQuantityReceived() {
            return quantityReceived;
        }

        public void setQuantityReceived(int quantityReceived) {
            this.quantityReceived = quantityReceived;
        }
    }

    // Getters and Setters

    public Long getPurchaseOrderId() {
        return purchaseOrderId;
    }

    public void setPurchaseOrderId(Long purchaseOrderId) {
        this.purchaseOrderId = purchaseOrderId;
    }

    public List<ReceiveItemDto> getItems() {
        return items;
    }

    public void setItems(List<ReceiveItemDto> items) {
        this.items = items;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
