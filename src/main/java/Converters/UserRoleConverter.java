/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Converters;

import Enums.UserRole;
import jakarta.persistence.AttributeConverter;

/**
 *
 * @author LENOVO
 */
public class UserRoleConverter implements AttributeConverter<UserRole, String> {

    @Override
    public String convertToDatabaseColumn(UserRole role) {
        return (role != null) ? role.name().toLowerCase() : null;
    }

    @Override
    public UserRole convertToEntityAttribute(String dbValue) {
        return (dbValue != null) ? UserRole.fromString(dbValue) : null;
    }

}
