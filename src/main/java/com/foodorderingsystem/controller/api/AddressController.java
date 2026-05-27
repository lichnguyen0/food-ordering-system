package com.foodorderingsystem.controller.api;

import com.foodorderingsystem.model.user.Address;
import com.foodorderingsystem.model.user.User;
import com.foodorderingsystem.model.user.AddressZone;
import com.foodorderingsystem.security.CustomUserDetails;
import com.foodorderingsystem.service.AddressService;
import com.foodorderingsystem.service.DistanceService;
import com.foodorderingsystem.repository.user.AddressZoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressService addressService;
    private final AddressZoneRepository addressZoneRepository;
    private final DistanceService distanceService;

    @Autowired
    public AddressController(AddressService addressService,
                              AddressZoneRepository addressZoneRepository,
                              DistanceService distanceService) {
        this.addressService = addressService;
        this.addressZoneRepository = addressZoneRepository;
        this.distanceService = distanceService;
    }

    @GetMapping
    public ResponseEntity<List<Address>> getAddresses(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userDetails.getUser();
        List<Address> addresses = addressService.getAddressesByUserId(user.getUserId());
        return ResponseEntity.ok(addresses);
    }

    @PostMapping
    public ResponseEntity<Address> createAddress(@RequestBody Address address,
                                                 @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userDetails.getUser();
        address.setUser(user);
        Address savedAddress = addressService.saveAddress(address);
        return ResponseEntity.ok(savedAddress);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Address> updateAddress(@PathVariable Long id,
                                                 @RequestBody Address addressDetails,
                                                 @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userDetails.getUser();
        Address address = addressService.getAddressByIdAndUserId(id, user.getUserId());
        // Update fields
        address.setLabel(addressDetails.getLabel());
        address.setRecipientName(addressDetails.getRecipientName());
        address.setPhoneNumber(addressDetails.getPhoneNumber());
        address.setAddressLine(addressDetails.getAddressLine());
        address.setWard(addressDetails.getWard());
        address.setDistrict(addressDetails.getDistrict());
        address.setCity(addressDetails.getCity());
        address.setZipCode(addressDetails.getZipCode());
        address.setDefault(addressDetails.isDefault());
        Address updatedAddress = addressService.saveAddress(address);
        return ResponseEntity.ok(updatedAddress);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long id,
                                              @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userDetails.getUser();
        addressService.deleteAddress(id, user.getUserId());
        return ResponseEntity.noContent().build();
    }

@PutMapping("/{id}/default")
    public ResponseEntity<Address> setDefaultAddress(@PathVariable Long id,
                                                      @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userDetails.getUser();
        Address address = addressService.setDefaultAddress(id, user.getUserId());
        return ResponseEntity.ok(address);
    }

    @GetMapping("/{id}/delivery-fee")
    public ResponseEntity<Double> getDeliveryFee(@PathVariable Long id,
                                                    @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userDetails.getUser();
        Address address = addressService.getAddressByIdAndUserId(id, user.getUserId());
        if (address == null) {
            return ResponseEntity.notFound().build();
        }

        Double fee = 16000.0;
        if (address.getWard() != null && address.getCity() != null) {
            Optional<AddressZone> zone = addressZoneRepository.findByWardAndCity(address.getWard(), address.getCity());
            if (zone.isPresent()) {
                fee = zone.get().getBaseFee();
            }
        }
        return ResponseEntity.ok(fee);
    }
}