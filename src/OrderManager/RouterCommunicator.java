package OrderManager;

import OrderRouter.Router;
import Ref.Instrument;

import java.io.Serializable;

public class RouterCommunicator implements Serializable {

    private Router.api orderType;
    private int orderId;
    private int sliceID;
    private long sizeRemaining;
    private Instrument instrument;
}
