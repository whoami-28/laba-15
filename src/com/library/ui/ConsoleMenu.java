package com.library.ui;

import com.library.exception.BorrowLimitExceededException;
import com.library.exception.LibraryException;
import com.library.exception.MaterialNotAvailableException;
import com.library.exception.MaterialNotFoundException;
import com.library.exception.ValidationException;
import com.library.model.BorrowRecord;
import com.library.model.LibraryMaterial;
import com.library.model.Reader;
import com.library.service.LibraryService;
import com.library.test.LibraryTestSuite;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class ConsoleMenu {
    private final LibraryService libraryService;
    private final Scanner scanner;

    public ConsoleMenu(LibraryService libraryService) {
        this.libraryService = libraryService;
        this.scanner = new Scanner(System.in);
    }

    public void run() {
        boolean running = true;
        while (running) {
            printMainMenu();
            String choice = promptString("Выберите пункт меню: ");
            System.out.println();

            if (choice.isEmpty() && !scanner.hasNextLine()) {
                break;
            }

            try {
                switch (choice) {
                    case "1":
                        showAllMaterials();
                        break;
                    case "2":
                        addPrintedBook();
                        break;
                    case "3":
                        addElectronicBook();
                        break;
                    case "4":
                        searchMaterials();
                        break;
                    case "5":
                        registerReader();
                        break;
                    case "6":
                        showAllReaders();
                        break;
                    case "7":
                        borrowMaterial();
                        break;
                    case "8":
                        returnMaterial();
                        break;
                    case "9":
                        showBorrowsReport();
                        break;
                    case "10":
                        removeMaterial();
                        break;
                    case "11":
                        saveData();
                        break;
                    case "12":
                        loadData();
                        break;
                    case "13":
                        runTestSuite();
                        break;
                    case "0":
                        System.out.println("Сохранение данных перед выходом...");
                        try {
                            libraryService.saveData();
                            System.out.println("Данные успешно сохранены.");
                        } catch (Exception e) {
                            System.err.println("Ошибка сохранения при выходе: " + e.getMessage());
                        }
                        System.out.println("Завершение работы программы. До свидания!");
                        running = false;
                        break;
                    default:
                        System.out.println("Неверный пункт меню. Пожалуйста, введите цифру от 0 до 13.");
                }
            } catch (Exception e) {
                System.err.println("Произошла ошибка: " + e.getMessage());
            }

            if (running) {
                System.out.println("\nНажмите Enter для продолжения...");
                if (scanner.hasNextLine()) {
                    scanner.nextLine();
                } else {
                    break;
                }
            }
        }
    }

    private void printMainMenu() {
        System.out.println();
        System.out.println("===================================================================");
        System.out.println("      СИСТЕМА УПРАВЛЕНИЯ БИБЛИОТЕЧНЫМ ФОНДОМ (ВАРИАНТ №3)          ");
        System.out.println("===================================================================");
        System.out.println(" 1. Просмотр всех материалов библиотечного фонда");
        System.out.println(" 2. Зарегистрировать новую печатную книгу");
        System.out.println(" 3. Зарегистрировать новое электронное издание");
        System.out.println(" 4. Поиск материалов (по названию / автору / доступности)");
        System.out.println(" 5. Регистрация нового читателя");
        System.out.println(" 6. Просмотр списка читателей");
        System.out.println(" 7. Выдача материала читателю");
        System.out.println(" 8. Возврат материала (с расчётом штрафа за просрочку)");
        System.out.println(" 9. Отчёт по активным выдачам и задолженностям");
        System.out.println("10. Удаление материала из библиотечного фонда");
        System.out.println("11. Сохранить все данные в файлы");
        System.out.println("12. Загрузить (восстановить) данные из файлов");
        System.out.println("13. Запуск встроенных автоматических тестов");
        System.out.println(" 0. Выход из программы");
        System.out.println("===================================================================");
    }

    private void showAllMaterials() {
        System.out.println("--- КАТАЛОГ БИБЛИОТЕЧНЫХ МАТЕРИАЛОВ ---");
        List<LibraryMaterial> materials = libraryService.getAllMaterials();
        if (materials.isEmpty()) {
            System.out.println("Фонд библиотеки пуст.");
            return;
        }
        for (int i = 0; i < materials.size(); i++) {
            System.out.printf("%2d. %s\n", (i + 1), materials.get(i).getFormattedSummary(true));
        }
    }

    private void addPrintedBook() throws ValidationException {
        System.out.println("--- РЕГИСТРАЦИЯ ПЕЧАТНОЙ КНИГИ ---");
        String id = promptString("Уникальный шифр/ID (напр., PB-105): ");
        String title = promptString("Название книги: ");
        String author = promptString("Автор: ");
        int year = promptInt("Год издания: ");
        int totalCopies = promptInt("Количество экземпляров: ");
        int pages = promptInt("Количество страниц: ");
        String shelf = promptString("Место на полке (напр., B-3-12): ");
        double fine = promptDouble("Ставка штрафа за день просрочки (руб., по умолчанию 50): ", 50.0);
        int loanDays = promptInt("Срок выдачи в днях (по умолчанию 14): ", 14);

        libraryService.registerPrintedBook(id, title, author, year, totalCopies, pages, shelf, fine, loanDays);
        System.out.println("Печатная книга успешно зарегистрирована в фонде!");
    }

    private void addElectronicBook() throws ValidationException {
        System.out.println("--- РЕГИСТРАЦИЯ ЭЛЕКТРОННОГО ИЗДАНИЯ ---");
        String id = promptString("Уникальный шифр/ID (напр., EB-205): ");
        String title = promptString("Название издания: ");
        String author = promptString("Автор: ");
        int year = promptInt("Год публикации: ");
        int totalCopies = promptInt("Количество одновременных лицензий: ");
        String format = promptString("Формат файла (PDF, EPUB, FB2): ");
        double sizeMb = promptDouble("Размер файла (МБ): ");
        String url = promptString("URL-ссылка или путь к файлу: ");

        libraryService.registerElectronicBook(id, title, author, year, totalCopies, format, sizeMb, url);
        System.out.println("Электронное издание успешно зарегистрировано в фонде!");
    }

    private void searchMaterials() {
        System.out.println("--- ПОИСК МАТЕРИАЛОВ ---");
        String query = promptString("Введите строку поиска (название, автор или ID, пусто - показать все): ");
        String onlyAvailInput = promptString("Искать только доступные для выдачи? (д/н): ");
        boolean onlyAvailable = onlyAvailInput.equalsIgnoreCase("д") || onlyAvailInput.equalsIgnoreCase("y");

        List<LibraryMaterial> results = libraryService.searchMaterials(query, onlyAvailable);
        if (results.isEmpty()) {
            System.out.println("Материалы по заданным критериям не найдены.");
            return;
        }

        System.out.printf("Найдено результатов: %d\n", results.size());
        for (int i = 0; i < results.size(); i++) {
            System.out.printf("%2d. %s\n", (i + 1), results.get(i).getFormattedSummary(true));
        }
    }

    private void registerReader() throws ValidationException {
        System.out.println("--- РЕГИСТРАЦИЯ НОВОГО ЧИТАТЕЛЯ ---");
        String id = promptString("Номер читательского билета (напр., R-004): ");
        String name = promptString("ФИО читателя: ");
        String email = promptString("Электронная почта: ");
        String phone = promptString("Контактный телефон: ");
        int limit = promptInt("Максимальное число одновременно взятых печатных книг (по умолчанию 3): ", 3);

        libraryService.registerReader(id, name, email, phone, limit);
        System.out.println("Читатель успешно зарегистрирован!");
    }

    private void showAllReaders() {
        System.out.println("--- СПИСОК ЧИТАТЕЛЕЙ БИБЛИОТЕКИ ---");
        List<Reader> readers = libraryService.getAllReaders();
        if (readers.isEmpty()) {
            System.out.println("В библиотеке нет зарегистрированных читателей.");
            return;
        }
        for (int i = 0; i < readers.size(); i++) {
            System.out.printf("%2d. %s\n", (i + 1), readers.get(i));
        }
    }

    private void borrowMaterial() {
        System.out.println("--- ВЫДАЧА МАТЕРИАЛА ЧИТАТЕЛЮ ---");
        String materialId = promptString("Введите ID материала: ");
        String readerId = promptString("Введите номер билета читателя: ");
        String customDaysInput = promptString("Указать нестандартный срок выдачи? (Enter - использовать стандартный, либо введите дни): ");

        try {
            BorrowRecord record;
            if (customDaysInput.trim().isEmpty()) {
                record = libraryService.borrowMaterial(materialId, readerId);
            } else {
                int days = Integer.parseInt(customDaysInput.trim());
                record = libraryService.borrowMaterial(materialId, readerId, days);
            }

            System.out.println("Материал успешно выдан!");
            System.out.println("Запись выдачи: " + record);
        } catch (NumberFormatException e) {
            System.err.println("Некорректный ввод количества дней.");
        } catch (MaterialNotFoundException e) {
            System.err.println("Ошибка поиска: " + e.getMessage());
        } catch (MaterialNotAvailableException e) {
            System.err.println("Отказ в выдаче (нет копий): " + e.getMessage());
        } catch (BorrowLimitExceededException e) {
            System.err.println("Отказ в выдаче (лимит/задолженность): " + e.getMessage());
        } catch (ValidationException e) {
            System.err.println("Ошибка валидации: " + e.getMessage());
        }
    }

    private void returnMaterial() {
        System.out.println("--- ВОЗВРАТ МАТЕРИАЛА В БИБЛИОТЕКУ ---");
        List<BorrowRecord> active = libraryService.getActiveBorrows();
        if (active.isEmpty()) {
            System.out.println("В данный момент нет активных выдач.");
            return;
        }

        System.out.println("Активные выдачи:");
        for (BorrowRecord r : active) {
            System.out.println("  " + r);
        }

        String recordId = promptString("Введите ID записи выдачи (напр., BR-1001): ");
        String dateMode = promptString("Дата возврата: 1 - Сегодня (" + LocalDate.now() + 
                "), 2 - Указать дату вручную (для проверки штрафа): ");

        LocalDate returnDate = LocalDate.now();
        if ("2".equals(dateMode.trim())) {
            String dateStr = promptString("Введите дату возврата (ГГГГ-ММ-ДД, напр. 2026-11-20): ");
            try {
                returnDate = LocalDate.parse(dateStr.trim());
            } catch (DateTimeParseException e) {
                System.err.println("Некорректный формат даты! Возврат отменён.");
                return;
            }
        }

        try {
            BorrowRecord closed = libraryService.returnMaterial(recordId, returnDate);
            System.out.println("Материал успешно возвращен в фонд!");
            System.out.println("Дата возврата: " + closed.getReturnDate());
            if (closed.getFineCharged() > 0) {
                System.out.printf("ВНИМАНИЕ! Зафиксирована просрочка. Начислен штраф: %.2f руб.\n", closed.getFineCharged());
            } else {
                System.out.println("Возврат выполнен вовремя (или на электронное издание). Штраф: 0.00 руб.");
            }
        } catch (LibraryException e) {
            System.err.println("Ошибка при возврате: " + e.getMessage());
        }
    }

    private void showBorrowsReport() {
        System.out.println("--- ОТЧЁТ ПО ВЫДАЧАМ И ЗАДОЛЖЕННОСТЯМ ---");
        List<BorrowRecord> all = libraryService.getAllBorrowRecords();
        if (all.isEmpty()) {
            System.out.println("История выдач пуста.");
            return;
        }

        LocalDate today = LocalDate.now();
        List<BorrowRecord> overdue = libraryService.getOverdueBorrows(today);

        System.out.printf("Всего записей в истории: %d | Активных: %d | Просроченных: %d\n\n",
                all.size(), libraryService.getActiveBorrows().size(), overdue.size());

        if (!overdue.isEmpty()) {
            System.out.println("!!! ВНИМАНИЕ: СПИСОК ПРОСРОЧЕННЫХ ВЫДАЧ !!!");
            for (BorrowRecord r : overdue) {
                int days = r.getOverdueDays(today);
                double fine = libraryService.calculateFine(r, today);
                System.out.printf("  [ДОЛЖНИК] ID: %s | Читатель: %s | Материал: \"%s\" | Срок был: %s | Просрочено: %d дн. | Текущий штраф: %.2f руб.\n",
                        r.getId(), r.getReaderName(), r.getMaterialTitle(), r.getDueDate(), days, fine);
            }
            System.out.println();
        }

        System.out.println("Полный список выдач:");
        for (BorrowRecord r : all) {
            System.out.println("  " + r);
        }
    }

    private void removeMaterial() {
        System.out.println("--- УДАЛЕНИЕ МАТЕРИАЛА ИЗ ФОНДА ---");
        String id = promptString("Введите ID удаляемого материала: ");
        try {
            boolean removed = libraryService.removeMaterial(id);
            if (removed) {
                System.out.println("Материал с ID " + id + " успешно списан и удален из фонда.");
            }
        } catch (LibraryException e) {
            System.err.println("Ошибка удаления: " + e.getMessage());
        }
    }

    private void saveData() {
        System.out.println("--- СОХРАНЕНИЕ ДАННЫХ В ФАЙЛЫ ---");
        try {
            libraryService.saveData();
            System.out.println("Все данные успешно сохранены в каталог 'data/'.");
        } catch (Exception e) {
            System.err.println("Ошибка сохранения: " + e.getMessage());
        }
    }

    private void loadData() {
        System.out.println("--- ВОССТАНОВЛЕНИЕ ДАННЫХ ИЗ ФАЙЛОВ ---");
        try {
            libraryService.loadData();
            System.out.println("Данные успешно загружены из каталога 'data/'.");
        } catch (Exception e) {
            System.err.println("Ошибка загрузки: " + e.getMessage());
        }
    }

    private void runTestSuite() {
        System.out.println("--- ЗАПУСК ВСТРОЕННОГО НАБОРА ТЕСТОВ ---");
        LibraryTestSuite suite = new LibraryTestSuite();
        boolean ok = suite.runAllTests();
        if (ok) {
            System.out.println("Все проверки успешно пройдены! Архитектура и ограничения соответствуют требованиям.");
        } else {
            System.err.println("Внимание! Были обнаружены ошибки при выполнении тестов.");
        }
    }

    private String promptString(String prompt) {
        System.out.print(prompt);
        System.out.flush();
        if (scanner.hasNextLine()) {
            return scanner.nextLine().trim();
        }
        return "";
    }

    private int promptInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            System.out.flush();
            if (!scanner.hasNextLine()) {
                return 0;
            }
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите целое число.");
            }
        }
    }

    private int promptInt(String prompt, int defaultValue) {
        while (true) {
            System.out.print(prompt);
            System.out.flush();
            if (!scanner.hasNextLine()) {
                return defaultValue;
            }
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return defaultValue;
            }
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите целое число или нажмите Enter для значения по умолчанию.");
            }
        }
    }

    private double promptDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            System.out.flush();
            if (!scanner.hasNextLine()) {
                return 0.0;
            }
            String input = scanner.nextLine().trim().replace(',', '.');
            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите число (например, 12.5).");
            }
        }
    }

    private double promptDouble(String prompt, double defaultValue) {
        while (true) {
            System.out.print(prompt);
            System.out.flush();
            if (!scanner.hasNextLine()) {
                return defaultValue;
            }
            String input = scanner.nextLine().trim().replace(',', '.');
            if (input.isEmpty()) {
                return defaultValue;
            }
            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите число или нажмите Enter для значения по умолчанию.");
            }
        }
    }
}
