package com.driver.services.impl;

import com.driver.model.Cab;
import com.driver.repository.CabRepository;
import com.driver.services.DriverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import com.driver.model.Driver;
import com.driver.repository.DriverRepository;

import java.util.Optional;

@Service
public class DriverServiceImpl implements DriverService {

	@Autowired
	DriverRepository driverRepository3;

	@Autowired
	CabRepository cabRepository3;

	@Override
	@CacheEvict(value = "drivers", allEntries = true)
	public void register(String mobile, String password){
		//Save a driver in the database having given details and a cab with ratePerKm as 10 and availability as True by default.

		Driver driver = new Driver();
		driver.setMobile(mobile);
		driver.setPassword(password);

		Cab cab = new Cab();
		cab.setAvailable(true);
		cab.setPerKmRate(10);

		cab.setDriver(driver);
		driver.setCab(cab);
		driverRepository3.save(driver);
	}

	@Override
	@CacheEvict(value = "drivers", allEntries = true)
	public void removeDriver(int driverId){
		// Delete driver without using deleteById function
		driverRepository3.deleteById(driverId);
	}

	@Override
	public void updateStatus(int driverId){
		//Set the status of respective car to unavailable

		Optional<Driver> optionalDriver = driverRepository3.findById(driverId);

		if(optionalDriver.isPresent()){
			Driver driver = optionalDriver.get();
			driver.getCab().setAvailable(false);

			driverRepository3.save(driver);
		}


	}
}
