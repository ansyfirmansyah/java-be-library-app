package com.ansyporto.auth.repository;

public class UserSql {
    public static final String FIND_BY_EMAIL = "SELECT * FROM users WHERE email = :email";
}
