package com.example.utils;

public class UserContext {
    private static final ThreadLocal<Integer> currentUser = new ThreadLocal<>();

    public static void setCurrentUserId(Integer userId) {
        currentUser.set(userId);
    }

    public static Integer getCurrentUserId() {
        return currentUser.get();
    }
    public static void clear() {
        currentUser.remove();
    }
}