package com.coding.exercise.bankapp.domain;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CustomerDetails {

    private String firstName;

    private String lastName;

    private String middleName;

    private Long customerNumber;

    private String status;

    private AddressDetails customerAddress;

    private ContactDetails contactDetails;

    public boolean isValidCustomerDetails() {
        // Add conditions for valid customer details
        // Example: Check if required fields are not null or empty
        return isNotEmpty(firstName)
                && isNotEmpty(lastName)
                && isNotNull(customerNumber)
                && isNotEmpty(status)
                && isValidAddressDetails(customerAddress)
                && isValidContactDetails(contactDetails);
    }

    // Additional helper methods

    private boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private boolean isNotNull(Object value) {
        return value != null;
    }

    private boolean isValidAddressDetails(AddressDetails addressDetails) {
        // Add conditions for valid address details
        // Example: Check if required fields in AddressDetails are not null
        return addressDetails != null
                && isNotEmpty(addressDetails.getAddress1())
                && isNotEmpty(addressDetails.getCity())
                && isNotEmpty(addressDetails.getState())
                && isNotEmpty(addressDetails.getZip())
                && isNotEmpty(addressDetails.getCountry());
    }

    private boolean isValidContactDetails(ContactDetails contactDetails) {
        // Add conditions for valid contact details
        // Example: Check if required fields in ContactDetails are not null or empty
        return contactDetails != null
                && (isNotEmpty(contactDetails.getEmailId())
                || isNotEmpty(contactDetails.getHomePhone())
                || isNotEmpty(contactDetails.getWorkPhone()));
    }

}
