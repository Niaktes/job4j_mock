package ru.checkdev.notification.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class TgUserTest {

    private TgUser tgUser;

    @BeforeEach
    public void setUp() {
        tgUser = new TgUser(0, "username", "email", 1L, false, 1);
    }

    @Test
    void testGetUsername() {
        assertThat(tgUser.getUsername(), is("username"));
    }

    @Test
    void testGetEmail() {
        assertThat(tgUser.getEmail(), is("email"));
    }

    @Test
    void testGetChatId() {
        assertThat(tgUser.getChatId(), is(1L));
    }

    @Test
    void testIsSubscribed() {
        assertThat(tgUser.isSubscribed(), is(false));
    }

    @Test
    void testGetUserId() {
        assertThat(tgUser.getUserId(), is(1));
    }

}