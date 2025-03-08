package com.driver.services.impl;

import com.driver.model.*;
import com.driver.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.driver.repository.CustomerRepository;
import com.driver.repository.DriverRepository;
import com.driver.repository.TripBookingRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@Transactional
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerRepository customerRepository2;

	@Autowired
	DriverRepository driverRepository2;

	@Autowired
	TripBookingRepository tripBookingRepository2;

	@Override
	@CacheEvict(value = "customers", allEntries = true)
	public void register(Customer customer) {
		//Save the customer in database
		customerRepository2.save(customer);
	}

	@Override
	@CacheEvict(value = "customers", allEntries = true)
	public void deleteCustomer(Integer customerId) {
		// Delete customer without using deleteById function
		customerRepository2.deleteById(customerId);
	}

	@Async
	public CompletableFuture<Driver> findAvailableDriver() {
		List<Driver> drivers = driverRepository2.findAll();
		for (Driver driver : drivers) {
			if (driver.getCab().getAvailable()) {
				return CompletableFuture.completedFuture(driver);
			}
		}
		return CompletableFuture.failedFuture(new Exception("No cab available!"));
	}

	@Async
	public CompletableFuture<Customer> findCustomerById(int customerId) {
		Optional<Customer> customerOptional = customerRepository2.findById(customerId);
		if (!customerOptional.isPresent()) {
			return CompletableFuture.failedFuture(new Exception("Customer is not present!"));
		}
		return CompletableFuture.completedFuture(customerOptional.get());
	}

	@Override
	public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception{
		//Book the driver with lowest driverId who is free (cab available variable is Boolean.TRUE). If no driver is available, throw "No cab available!" exception
		//Avoid using SQL query

		CompletableFuture<Driver> driverFuture = findAvailableDriver();
		CompletableFuture<Customer> customerFuture = findCustomerById(customerId);

		// Combine both async calls and wait for results
		CompletableFuture.allOf(driverFuture, customerFuture).join();

		Driver currDriver = driverFuture.get();
		Customer currCustomer = customerFuture.get();

		currDriver.getCab().setAvailable(false);

		TripBooking tripBooking = new TripBooking();
		tripBooking.setFromLocation(fromLocation);
		tripBooking.setToLocation(toLocation);
		tripBooking.setDistanceInKm(distanceInKm);
		tripBooking.setBill(0);
		tripBooking.setStatus(TripStatus.CONFIRMED);
		tripBooking.setDriver(currDriver);
		tripBooking.setCustomer(currCustomer);

		currDriver.getTripBookingList().add(tripBooking);
		currCustomer.getTripBookingList().add(tripBooking);

		return tripBookingRepository2.save(tripBooking);
	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip id and update TripBooking attributes accordingly
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
		//Complete the trip having given trip id and update TripBooking attributes accordingly
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
