package com.acme;

public class App {
    private final String value;

    public App(String value) {
        this.value = value;
    }

    public String getGreeting() {
        return value;
    }

    public static void main(String[] args) {
        System.out.println(new App("Hello").getGreeting());
    }
}
