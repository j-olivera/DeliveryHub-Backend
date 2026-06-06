package com.deliveryhub.domain.model;

public record Address(
    String street,
    String number,
    String city,
    double latitude,
    double longitude
) {
    public Address {
        validateBlank(street, "Street cannot be blank");
        validateBlank(number, "Number cannot be blank");
        validateBlank(city, "City cannot be blank");
        validateRange(latitude, -90, 90, "Latitude must be between -90 and 90");
        validateRange(longitude, -180, 180, "Longitude must be between -180 and 180");
    }

    private void validateBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    private void validateRange(double value, double min, double max, String message) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(message);
        }
    }
}
