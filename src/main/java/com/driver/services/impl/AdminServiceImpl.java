package com.driver.services.impl;

import java.util.List;
import java.util.Optional;

import com.driver.services.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.driver.model.Admin;
import com.driver.model.Customer;
import com.driver.model.Driver;
import com.driver.repository.AdminRepository;
import com.driver.repository.CustomerRepository;
import com.driver.repository.DriverRepository;
import org.springframework.cache.annotation.Cacheable;


@Service
public class AdminServiceImpl implements AdminService {

	@Autowired
	AdminRepository adminRepository1;

	@Autowired
	DriverRepository driverRepository1;

	@Autowired
	CustomerRepository customerRepository1;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Override
	public void adminRegister(Admin admin) {
		//Save the admin in the database
		String encodedPassword = passwordEncoder.encode(admin.getPassword());
		admin.setPassword(encodedPassword);
		adminRepository1.save(admin);
	}

	@Override
	public Admin updatePassword(Integer adminId, String password) {
		//Update the password of admin with given id
		Optional<Admin> adminOptional = adminRepository1.findById(adminId);
		if(!adminOptional.isPresent()){
			return null;
		}

		Admin admin = adminOptional.get();
		String encodedPassword = passwordEncoder.encode(password);
		admin.setPassword(encodedPassword);
		adminRepository1.save(admin);
		return admin;
	}

	@Override
	public void deleteAdmin(int adminId){
		// Delete admin without using deleteById function
		adminRepository1.deleteById(adminId);
	}

	@Override
	@Cacheable(value = "drivers")
	public List<Driver> getListOfDrivers(int page, int size) {
		System.out.println("Fetching drivers from database with pagination...");
		Pageable pageable = PageRequest.of(page, size, Sort.by("driverId").ascending());
		return driverRepository1.findAll(pageable).getContent();
	}


	@Override
	@Cacheable(value = "customers")
	public List<Customer> getListOfCustomers() {
		//Find the list of all customers
		System.out.println("Fetching customers from database...");
		return customerRepository1.findAll();
	}

}
