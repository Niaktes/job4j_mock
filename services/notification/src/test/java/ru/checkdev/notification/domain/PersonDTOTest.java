package ru.checkdev.notification.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

class PersonDTOTest {

    private PersonDTO person;

    @BeforeEach
    public void setUp() {
        List<RoleDTO> roles = new ArrayList<>();
        roles.add(new RoleDTO(1));
        Calendar created = new Calendar.Builder()
                .setDate(2023, 10, 23)
                .setTimeOfDay(20, 20, 20)
                .build();
        person = new PersonDTO(0, "email", "password", true, roles, created);
    }

    @Test
    void testGetEmail() {
        assertThat("email", is(person.getEmail()));
    }

    @Test
    void testGetPassword() {
        assertThat("password", is(person.getPassword()));
    }

    @Test
    void testGetPrivacy() {
        assertThat(true, is(person.isPrivacy()));
    }

    @Test
    void testGetRoles() {
        List<RoleDTO> roles = new ArrayList<>();
        roles.add(new RoleDTO(1));
        assertThat(roles, is(person.getRoles()));
    }

    @Test
    void testGetCreated() {
        Calendar created = new Calendar.Builder()
                .setDate(2023, 10, 23)
                .setTimeOfDay(20, 20, 20)
                .build();
        assertThat(created, is(person.getCreated()));
    }

}