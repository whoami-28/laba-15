package com.library;

import com.library.service.LibraryService;
import com.library.service.LibraryServiceImpl;
import com.library.ui.ConsoleMenu;

public class Main {
    public static void main(String[] args) {
        System.out.println("Инициализация системы управления библиотечным фондом...");

        LibraryService libraryService = null;
        try {
            libraryService = new LibraryServiceImpl();
            libraryService.seedInitialDataIfEmpty();
            System.out.println("База данных библиотеки успешно загружена.");
        } catch (Exception e) {
            System.err.println("Предупреждение при инициализации базы данных: " + e.getMessage());
            if (libraryService == null) {
                libraryService = new LibraryServiceImpl();
            }
        } finally {
            System.out.println("Готово к работе.\n");
        }

        ConsoleMenu menu = new ConsoleMenu(libraryService);
        menu.run();
    }
}
