package org.thomaschen.sprawl.model;

import org.springframework.security.crypto.bcrypt.BCrypt;

import javax.persistence.AttributeConverter;

public class PasswordConverter implements AttributeConverter<String, String> {

    @Override
    public String convertToDatabaseColumn(String s) {
        String pw_hash = BCrypt.hashpw(s, BCrypt.gensalt());
        return pw_hash;
    }

    @Override
    public String convertToEntityAttribute(String s) {
        return s;
    }
}
