package com.bridenstine.walmartlabs.interview;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DroneDeliveryScheduler {
	
	private final static Logger LOGGER = Logger.getLogger(Order.class.getName());
	private final static DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");

	public DroneDeliveryScheduler() {
		
	}
	
	/**
	 * 
	 * @param orderLine - e.g., WM001 N11W5 05:11:50
	 * @return an Optional<Order> and if it's invalid return an empty Optional
	 */
	private Order createOrder(String orderLine) {
		
		if(orderLine == null || orderLine.isEmpty()) {
			LOGGER.severe("This line contains an empty order");
			return null;
		}
		
		String[] fields = orderLine.split(" ");
		
		if(fields.length != 3) {
			LOGGER.severe("This order is not in the valid format: " + orderLine);
			return null;
		}
		
		String orderIdentifier = fields[0];
		String gridCoordinate = fields[1];
		String orderTimestamp = fields[2];
		
		// Valid format is WM####
		if(!orderIdentifier.matches("[W][M]\\d\\d\\d")) {
			LOGGER.severe("Invalid Order Identifier: " + orderIdentifier);
			return null;
		}
		
		// Valid format is e.g., N1W2 - https://regexr.com/4ejd7
		if(!gridCoordinate.matches("[nNsS](\\d)+[eEwW](\\d)+")) {
			LOGGER.severe("Invalid Grid Coordinate: " + gridCoordinate);
			return null;
		}
		
		// Valid format is e.g., 07:02:55 - (Date format HH:MM:SS)
		if(!orderTimestamp.matches("\\d\\d[:]\\d\\d[:]\\d\\d")) {
			LOGGER.severe("Invalid Order Identifier: " + orderTimestamp);
			return null;
		}
		
		return new Order(orderIdentifier, gridCoordinate, orderTimestamp);
	}
	
	private List<Order> getOrdersFromFile(String fileName) throws IOException {

		//read file into stream, try-with-resources
		try (Stream<String> stream = Files.lines(Paths.get(fileName))) {

			// Using the file stream create a list of Order objects and filter out any invalid order lines
			return stream.map(line -> createOrder(line) ).filter(Objects::nonNull).collect(Collectors.toList());
		}
	}
	
	private List<Order> sortOrdersForDelivery(List<Order> orders) {
		
		Collections.sort(orders, (Order orderA, Order orderB) -> orderA.getRoundTripLength().compareTo(orderB.getRoundTripLength()));
		return orders;
	}
	
	private List<Order> processDepartureTimes(List<Order> orders, Date departureTime) {
		
		for(int i=0; i<orders.size(); i++) {
			
			Order currentNode = orders.get(i);
			Optional<Order> nextNode = Optional.empty();
			
			// Do we have a next node?
			if(orders.size() < i+1) {
				
				nextNode = Optional.of(orders.get(i+1));
			}
			
			if(i==0) {
				
				currentNode.setDepartureTimestamp(departureTime);
				
				if(nextNode.isPresent()) {
					
					nextNode.get().setDepartureTimestamp(getDepartureTime(currentNode.getDepartureTimestamp(), currentNode.getRoundTripLength()));
				}
				
			} else {
				
				Order previousNode = orders.get(i-1);
				
				currentNode.setDepartureTimestamp(getDepartureTime(previousNode.getDepartureTimestamp(), previousNode.getRoundTripLength()));
				
				if(nextNode.isPresent()) {
					
					nextNode.get().setDepartureTimestamp(getDepartureTime(currentNode.getDepartureTimestamp(), currentNode.getRoundTripLength()));
				}
			}
		}
		
		return orders;
	}
	
	private Date getDepartureTime(Date startTime, Double minutes) {
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(startTime);

		// Round minutes to the nearest whole number
		cal.add(Calendar.MINUTE, Integer.valueOf((int) Math.round(minutes)));

		return cal.getTime();
	}
	
	private void createOrdersScheduleFile(List<Order> orders, String fileName) {
		
		 try(Writer w = 
		            new BufferedWriter(
		            new OutputStreamWriter(
		            new FileOutputStream(fileName)))) {
			 
			 orders.stream().map(Order::toFileString).forEach(str -> {

				 try {
					w.write(str + System.lineSeparator());
				} catch (IOException e) {
					 LOGGER.severe("Could not write to the scheduled file: " + fileName);
				}
			});
			 
		 } catch(IOException e) {
			 LOGGER.severe("Could not open the scheduled file: " + fileName);
		 }
	}
	
	public void processFiles() {
		
		String fileName = "/Users/kylebridenstine/walmart-order-batch.txt";
		String scheduledFileName = "/Users/kylebridenstine/walmart-order-batch-scheduled.txt";

		try {
			
			List<Order> orders = getOrdersFromFile(fileName);
			orders = sortOrdersForDelivery(orders);
			orders = processDepartureTimes(orders, DATE_FORMAT.parse("06:00:00"));
			createOrdersScheduleFile(orders, scheduledFileName);
			
		} catch (IOException e) {
			LOGGER.severe("Processing failed! Please ensure the file is present and that the contents match the required format.");
		} catch (ParseException e) {
			LOGGER.severe("Invalid initial departure date.");
		}
		
	}
	
}
