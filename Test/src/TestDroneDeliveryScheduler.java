import java.text.ParseException;
import java.util.logging.Logger;

import com.bridenstine.walmartlabs.interview.DroneDeliveryScheduler;
import com.bridenstine.walmartlabs.interview.Order;

public class TestDroneDeliveryScheduler {
	
	private final static Logger LOGGER = Logger.getLogger(Order.class.getName());
	
	public static void main(String[] args) throws ParseException {
		
		LOGGER.info("Scheduling orders");
		
		DroneDeliveryScheduler droneDeliveryScheduler = new DroneDeliveryScheduler();
		droneDeliveryScheduler.processFiles();

		LOGGER.info("Finished scheduling orders");
	}
}
