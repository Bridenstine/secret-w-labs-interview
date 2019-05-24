package com.bridenstine.walmartlabs.interview;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

public class Order {
	
	private final static Logger LOGGER = Logger.getLogger(Order.class.getName());
	private final static DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");

	private final String orderIdentifier;
	private final String gridCoordinate;
	private final String orderTimestamp;
	private Date orderTimestampDate;
	private Double roundTripLength;
	private Date departureTimestamp;

	public Order(String orderIdentifier, String gridCoordinate, String orderTimestamp) {
		
		this.orderIdentifier = orderIdentifier;
		this.gridCoordinate = gridCoordinate;
		this.orderTimestamp = orderTimestamp;
		
		try {
			orderTimestampDate = DATE_FORMAT.parse(orderTimestamp);
		} catch (ParseException e) {
			LOGGER.severe("The order's timestamp is not in the required format HH:MM:SS - " + orderTimestamp);
		}
		
		roundTripLength = getDroneDeliveryRoundTripLength(gridCoordinate);
	}
	
	private double getHypotenuse(double x, double y) {
		return Math.hypot(x, y);
	}
	
	/**
	 * 
	 * @param coordinate - Format is e.g., N1W2 - https://regexr.com/4ejd7
	 * @return the length of the total round trip for these coordinates
	 * 
	 * The length of the round trip is determined by getting the hypotenuse
	 * of the latitude and longitude coordinates and then multiplying
	 * that by two since the drone has to fly there and back.
	 */
	private double getDroneDeliveryRoundTripLength(String coordinate) {

		String[] coordinateDigits = coordinate.split("[nNsSeEwW]");
		
		Double longitude = Double.parseDouble(coordinateDigits[1]);
		Double latitude = Double.parseDouble(coordinateDigits[2]);
			
		return getHypotenuse(longitude, latitude) * 2;
	}
	
	public String getOrderIdentifier() {
		return orderIdentifier;
	}

	public String getGridCoordinate() {
		return gridCoordinate;
	}

	public String getOrderTimestamp() {
		return orderTimestamp;
	}

	public Date getOrderTimestampDate() {
		return orderTimestampDate;
	}

	public Double getRoundTripLength() {
		return roundTripLength;
	}
	
	public Date getDepartureTimestamp() {
		return departureTimestamp;
	}

	public void setDepartureTimestamp(Date departureTimestamp) {
		this.departureTimestamp = departureTimestamp;
	}
	
	public String toFileString() {
		return orderIdentifier + " " + DATE_FORMAT.format(departureTimestamp);
	}

	@Override
	public String toString() {
		return "Order [orderIdentifier=" + orderIdentifier + ", gridCoordinate=" + gridCoordinate + ", orderTimestamp="
				+ orderTimestamp + ", orderTimestampDate=" + orderTimestampDate + ", roundTripLength=" + roundTripLength
				+ ", departureTimestamp=" + departureTimestamp + "]";
	}
}
