package com.driver.services;

import java.util.List;

import com.driver.model.Admin;
import com.driver.model.Customer;
import com.driver.model.Driver;
import org.springframework.data.domain.Page;

public interface AdminService {

	public void adminRegister(Admin admin);

	public Admin updatePassword(Integer adminId, String password);

	public void deleteAdmin(int adminId);

	List<Driver> getListOfDrivers(int page, int size);


	public List<Customer> getListOfCustomers();
}
