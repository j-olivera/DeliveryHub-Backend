package com.deliveryhub.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class AddressTest {

    @Test
    @DisplayName("Should create a valid address")
    void shouldCreateValidAddress() {
        Address address = new Address("Main St 123", "Springfield", 40.7128, -74.0060);
        
        assertEquals("Main St 123", address.street());
        assertEquals("Springfield", address.city());
        assertEquals(40.7128, address.latitude());
        assertEquals(-74.0060, address.longitude());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "  "})
    @DisplayName("Should throw exception when street is blank")
    void shouldThrowExceptionWhenStreetIsBlank(String street) {
        assertThrows(IllegalArgumentException.class, () -> 
            new Address(street, "Springfield", 40.7128, -74.0060));
    }

    @Test
    @DisplayName("Should throw exception when street is null")
    void shouldThrowExceptionWhenStreetIsNull() {
        assertThrows(IllegalArgumentException.class, () -> 
            new Address(null, "Springfield", 40.7128, -74.0060));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "  "})
    @DisplayName("Should throw exception when city is blank")
    void shouldThrowExceptionWhenCityIsBlank(String city) {
        assertThrows(IllegalArgumentException.class, () -> 
            new Address("Main St 123", city, 40.7128, -74.0060));
    }

    @Test
    @DisplayName("Should throw exception when city is null")
    void shouldThrowExceptionWhenCityIsNull() {
        assertThrows(IllegalArgumentException.class, () -> 
            new Address("Main St 123", null, 40.7128, -74.0060));
    }

    @ParameterizedTest
    @ValueSource(doubles = {-90.1, 90.1})
    @DisplayName("Should throw exception when latitude is out of range")
    void shouldThrowExceptionWhenLatitudeIsOutOfRange(double latitude) {
        assertThrows(IllegalArgumentException.class, () -> 
            new Address("Main St 123", "Springfield", latitude, -74.0060));
    }

    @ParameterizedTest
    @ValueSource(doubles = {-180.1, 180.1})
    @DisplayName("Should throw exception when longitude is out of range")
    void shouldThrowExceptionWhenLongitudeIsOutOfRange(double longitude) {
        assertThrows(IllegalArgumentException.class, () -> 
            new Address("Main St 123", "Springfield", 40.7128, longitude));
    }
}
