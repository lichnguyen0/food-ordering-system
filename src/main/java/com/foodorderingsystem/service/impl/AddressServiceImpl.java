package com.foodorderingsystem.service.impl;

import com.foodorderingsystem.model.user.Address;
import com.foodorderingsystem.model.user.User;
import com.foodorderingsystem.repository.user.AddressRepository;
import com.foodorderingsystem.repository.user.UserRepository;
import com.foodorderingsystem.service.AddressService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public AddressServiceImpl(AddressRepository addressRepository, UserRepository userRepository) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<Address> getAddressesByUserId(Long userId) {
        return addressRepository.findByUser_UserId(userId);
    }

    @Override
    public Address getAddressByIdAndUserId(Long addressId, Long userId) {
        return addressRepository.findById(addressId)
                .filter(address -> address.getUser().getUserId().equals(userId))
                .orElseThrow(() -> new IllegalArgumentException("Address not found or does not belong to the user"));
    }

    @Override
    public Address saveAddress(Address address) {
        // Ensure the user exists
        User user = userRepository.findById(address.getUser().getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        address.setUser(user);
        return addressRepository.save(address);
    }

    @Override
    public void deleteAddress(Long addressId, Long userId) {
        Address address = getAddressByIdAndUserId(addressId, userId);
        addressRepository.delete(address);
    }

    @Override
    public Address setDefaultAddress(Long addressId, Long userId) {
        // First, unset any existing default address for this user
        List<Address> addresses = getAddressesByUserId(userId);
        for (Address addr : addresses) {
            if (addr.isDefault()) {
                addr.setDefault(false);
                addressRepository.save(addr);
            }
        }

        // Set the selected address as default
        Address address = getAddressByIdAndUserId(addressId, userId);
        address.setDefault(true);
        return addressRepository.save(address);
    }
}