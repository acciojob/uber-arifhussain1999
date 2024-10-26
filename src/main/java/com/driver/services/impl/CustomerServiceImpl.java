package com.driver.services.impl;

import com.driver.model.*;
import com.driver.services.CustomerService;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.driver.repository.CustomerRepository;
import com.driver.repository.DriverRepository;
import com.driver.repository.TripBookingRepository;

import java.util.*;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerRepository customerRepository2;

	@Autowired
	DriverRepository driverRepository2;

	@Autowired
	TripBookingRepository tripBookingRepository2;

	@Override
	public void register(Customer customer) {
		//Save the customer in database
		customerRepository2.save(customer);
	}

	@Override
	public void deleteCustomer(Integer customerId) {
		// Delete customer without using deleteById function
		customerRepository2.deleteById(customerId);
	}

	@Override
	public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception{
		//Book the driver with lowest driverId who is free (cab available variable is Boolean.TRUE). If no driver is available, throw "No cab available!" exception
		//Avoid using SQL query
		List<Driver> drivers = driverRepository2.findAll();
		Driver currDriver = null;

		for (Driver driver : drivers) {
			if (driver.getCab().getAvailable()) {
				currDriver = driver;
				break;
			}
		}

		// If no driver is available, throw an exception
		if (currDriver == null) {
			throw new Exception("No cab available!");
		}

		// Fetch customer by ID
		Optional<Customer> customerOptional = customerRepository2.findById(customerId);
		if (!customerOptional.isPresent()) {
			throw new Exception("Customer is not present!");
		}

		// Set the driver's cab to unavailable
		currDriver.getCab().setAvailable(false);
		Customer currCustomer = customerOptional.get();

		// Create new trip booking
		TripBooking tripBooking = new TripBooking();
		tripBooking.setFromLocation(fromLocation);
		tripBooking.setToLocation(toLocation);
		tripBooking.setDistanceInKm(distanceInKm);
		tripBooking.setBill(0);
		tripBooking.setStatus(TripStatus.CONFIRMED);
		tripBooking.setDriver(currDriver);
		tripBooking.setCustomer(currCustomer);

		// Add tripBooking to customer and driver
		currDriver.getTripBookingList().add(tripBooking);
		currCustomer.getTripBookingList().add(tripBooking);

		// Save the tripBooking only
		return tripBookingRepository2.save(tripBooking);
	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly
		Optional<TripBooking> tripBookingOptional = tripBookingRepository2.findById(tripId);

		if(!tripBookingOptional.isPresent()){
			return;
		}

		TripBooking tripBooking = tripBookingOptional.get();
		tripBooking.getDriver().getCab().setAvailable(true);
		tripBooking.setBill(0);
		tripBooking.setStatus(TripStatus.CANCELED);

		tripBookingRepository2.save(tripBooking);
	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly
		Optional<TripBooking> tripBookingOptional = tripBookingRepository2.findById(tripId);

		if(!tripBookingOptional.isPresent()){
			return;
		}

		TripBooking tripBooking = tripBookingOptional.get();
		tripBooking.setBill(tripBooking.getDriver().getCab().getPerKmRate() * tripBooking.getDistanceInKm());
		tripBooking.getDriver().getCab().setAvailable(true);
		tripBooking.setStatus(TripStatus.COMPLETED);

		tripBookingRepository2.save(tripBooking);
	}
}
