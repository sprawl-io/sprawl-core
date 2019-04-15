package org.thomaschen.sprawl.security;

public enum Role {

    USER(0),
    ADMIN(1);

    private final int value;

    /**
     * Retrieve the string name of the Role
     * @return the string name
     */
    public String getText() {
        return this.name();
    }

    /**
     * Retrieve integer value of Role.
     * @return the integer corresponding to the Role
     */
    public int getValue() {
        return this.value;
    }

    /**
     * Private constructor for enumeration Role.
     * @param value the value of the Role
     */
    private Role(int value) {
        this.value = value;
    }
}
