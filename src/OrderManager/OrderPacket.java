package OrderManager;

import java.io.Serializable;

public class OrderPacket implements Serializable {
    public long packetOrderId;
    public Order packetOrder;
    public Object packetMethod;

    public OrderPacket(long packetOrderId, Order packetOrder, Object packetMethod) {
        this.packetOrderId = packetOrderId;
        this.packetOrder = packetOrder;
        this.packetMethod = packetMethod;
    }

    public long getOrderId() {
        return packetOrderId;
    }

    public void setOrderId(int packetOrderId) {
        this.packetOrderId = packetOrderId;
    }

    public Order getOrder() {
        return packetOrder;
    }

    public void setOrder(Order packetOrder) {
        this.packetOrder = packetOrder;
    }

    public Object getMethod() {
        return packetMethod;
    }

    public void setMethod(Object packetMethod) {
        this.packetMethod = packetMethod;
    }
}