package com.foodorderingsystem.service;

import com.foodorderingsystem.model.user.Address;
import java.util.List;

public interface AddressService {
    List<Address> getAddressesByUserId(Long userId);
    Address getAddressByIdAndUserId(Long addressId, Long userId);
    Address saveAddress(Address address);
    void deleteAddress(Long addressId, Long userId);
    Address setDefaultAddress(Long addressId, Long userId);
}