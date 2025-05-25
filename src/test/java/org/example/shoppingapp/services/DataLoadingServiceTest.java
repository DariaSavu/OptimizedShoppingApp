package org.example.shoppingapp.services;

import org.example.shoppingapp.model.Discount;
import org.example.shoppingapp.model.PriceEntry;
import org.example.shoppingapp.model.Product;
import org.example.shoppingapp.model.User;
import org.example.shoppingapp.repository.interfaces.DiscountRepository;
import org.example.shoppingapp.repository.interfaces.PriceEntryRepository;
import org.example.shoppingapp.repository.interfaces.ProductRepository;
import org.example.shoppingapp.repository.interfaces.UserRepository;
import org.example.shoppingapp.service.DataLoadingService;
import org.example.shoppingapp.utils.CsvDataParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataLoadingServiceTest {

    @Mock
    private CsvDataParser mockCsvDataParser;
    @Mock
    private ProductRepository mockProductRepository;
    @Mock
    private PriceEntryRepository mockPriceEntryRepository;
    @Mock
    private DiscountRepository mockDiscountRepository;
    @Mock
    private UserRepository mockUserRepository;

    @InjectMocks
    private DataLoadingService dataLoadingService;

    private Path tempDir;
    private Path pricesFile;
    private Path discountsFile;
    private Path usersFile;
    private Path otherFile;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("testDataDir");
        pricesFile = Files.createFile(tempDir.resolve("storeA_2023-01-01.csv"));
        discountsFile = Files.createFile(tempDir.resolve("storeA_discounts_2023-01-01.csv"));
        usersFile = Files.createFile(tempDir.resolve("users.csv"));
        otherFile = Files.createFile(tempDir.resolve("notes.txt"));

        ReflectionTestUtils.setField(dataLoadingService, "dataDirectoryPath", tempDir.toString());

        lenient().when(mockCsvDataParser.parseAllDataFromFile(any(Path.class)))
                .thenReturn(new CsvDataParser.ParsedFileData(Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
        lenient().when(mockCsvDataParser.parseUsersFile(any(Path.class)))
                .thenReturn(Collections.emptyList());
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(pricesFile);
        Files.deleteIfExists(discountsFile);
        Files.deleteIfExists(usersFile);
        Files.deleteIfExists(otherFile);
        Files.walk(tempDir)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(java.io.File::delete);
    }

    @Test
    @DisplayName("loadInitialData processes CSV files and saves data")
    void loadInitialData_ProcessesFiles_AndSavesData() throws IOException {
        Product p1 = new Product("P1", "Prod1", "Cat1", "B1", 1, "kg");
        PriceEntry pe1 = new PriceEntry(p1, "StoreA", LocalDate.parse("2023-01-01"), 10.0, "RON");
        Discount d1 = new Discount(p1, "StoreA", LocalDate.parse("2023-01-01"), LocalDate.parse("2023-01-07"), 10, LocalDate.parse("2023-01-01"));
        User u1 = new User(1, "user1", "Test", "User");
        User u2 = new User(2, "user2", "Another", "User");

        CsvDataParser.ParsedFileData pricesParsedData = new CsvDataParser.ParsedFileData(
                List.of(p1), List.of(pe1), Collections.emptyList()
        );
        CsvDataParser.ParsedFileData discountsParsedData = new CsvDataParser.ParsedFileData(
                List.of(p1), Collections.emptyList(), List.of(d1)
        );

        when(mockCsvDataParser.parseAllDataFromFile(pricesFile)).thenReturn(pricesParsedData);
        when(mockCsvDataParser.parseAllDataFromFile(discountsFile)).thenReturn(discountsParsedData);
        when(mockCsvDataParser.parseUsersFile(usersFile)).thenReturn(List.of(u1, u2));

        when(mockUserRepository.count()).thenReturn(0L);
        when(mockUserRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mockUserRepository.findById(anyInt())).thenReturn(Optional.empty());
        when(mockUserRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        dataLoadingService.loadInitialData();

        verify(mockCsvDataParser).parseAllDataFromFile(pricesFile);
        verify(mockCsvDataParser).parseAllDataFromFile(discountsFile);
        verify(mockCsvDataParser).parseUsersFile(usersFile);

        verify(mockPriceEntryRepository).saveAll(List.of(pe1));
        verify(mockDiscountRepository).saveAll(List.of(d1));
        verify(mockUserRepository, times(2)).save(any(User.class));
    }

    @Test
    @DisplayName("loadInitialData handles IOException from CSV parsing")
    void loadInitialData_HandlesIOExceptionFromCsvParsing() throws IOException {
        when(mockCsvDataParser.parseAllDataFromFile(pricesFile)).thenThrow(new IOException("Test CSV price parse error"));
        when(mockCsvDataParser.parseUsersFile(usersFile)).thenThrow(new IOException("Test user parse error"));

        assertDoesNotThrow(() -> dataLoadingService.loadInitialData());

        verify(mockProductRepository, never()).saveAll(any());
        verify(mockPriceEntryRepository, never()).saveAll(any());
        verify(mockDiscountRepository, never()).saveAll(any());
        verify(mockUserRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("loadInitialData skips non-CSV files effectively")
    void loadInitialData_SkipsNonCsvFiles() throws IOException {
        dataLoadingService.loadInitialData();
        verify(mockCsvDataParser, never()).parseAllDataFromFile(otherFile);
    }

    @Test
    @DisplayName("loadInitialData does nothing if data directory does not exist")
    void loadInitialData_NoDataDirectory_DoesNothing() throws IOException {
        ReflectionTestUtils.setField(dataLoadingService, "dataDirectoryPath", tempDir.resolve("non_existent_dir").toString());

        assertDoesNotThrow(() -> dataLoadingService.loadInitialData());

        verify(mockCsvDataParser, never()).parseAllDataFromFile(any(Path.class));
        verify(mockCsvDataParser, never()).parseUsersFile(any(Path.class));
    }

    @Test
    @DisplayName("reloadAllData should clear existing data and load new data")
    void reloadAllData_ClearsAndLoads() throws IOException {
        Product pNew1 = new Product("P_NEW1", "New Prod 1", "Cat", "B", 1, "kg");
        PriceEntry peNew1 = new PriceEntry(pNew1, "StoreNew", LocalDate.now(), 1.0, "RON");
        User userReload1 = new User(10, "reloadUser1", "Reload", "Test");

        CsvDataParser.ParsedFileData dataFromFile1 = new CsvDataParser.ParsedFileData(
                List.of(pNew1), List.of(peNew1), Collections.emptyList()
        );

        when(mockCsvDataParser.parseAllDataFromFile(pricesFile)).thenReturn(dataFromFile1);
        when(mockCsvDataParser.parseAllDataFromFile(discountsFile))
                .thenReturn(new CsvDataParser.ParsedFileData(Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
        when(mockCsvDataParser.parseUsersFile(usersFile)).thenReturn(List.of(userReload1));

        when(mockUserRepository.count()).thenReturn(0L);
        when(mockUserRepository.save(any(User.class))).thenReturn(userReload1);
        when(mockUserRepository.findById(anyInt())).thenReturn(Optional.empty());
        when(mockUserRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        dataLoadingService.reloadAllData();

        verify(mockProductRepository).deleteAll();
        verify(mockPriceEntryRepository).deleteAll();
        verify(mockDiscountRepository).deleteAll();

        verify(mockProductRepository).saveAll(List.of(pNew1));
        verify(mockPriceEntryRepository).saveAll(List.of(peNew1));
        verify(mockUserRepository).save(userReload1);
    }

    @Test
    @DisplayName("loadUsersFromCsv adds users if repository is empty")
    void loadUsersFromCsv_AddsUsersWhenEmpty() throws IOException {
        User u1 = new User(1, "user1", "Test", "User");
        User u2 = new User(2, "user2", "Another", "User");
        when(mockCsvDataParser.parseUsersFile(usersFile)).thenReturn(List.of(u1, u2));
        when(mockUserRepository.findById(anyInt())).thenReturn(Optional.empty());
        when(mockUserRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(mockUserRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReflectionTestUtils.invokeMethod(dataLoadingService, "loadUsersFromCsv");

        verify(mockUserRepository, times(2)).save(any(User.class));
    }

    @Test
    @DisplayName("loadUsersFromCsv does not add users if they already exist by ID")
    void loadUsersFromCsv_DoesNotAddUsersWhenIdExists() throws IOException {
        User u1 = new User(1, "user1", "Test", "User");
        when(mockCsvDataParser.parseUsersFile(usersFile)).thenReturn(List.of(u1));
        when(mockUserRepository.findById(1)).thenReturn(Optional.of(u1));

        ReflectionTestUtils.invokeMethod(dataLoadingService, "loadUsersFromCsv");
        verify(mockUserRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("loadUsersFromCsv does not add users if users.csv does not exist")
    void loadUsersFromCsv_NoFile_DoesNotAddUsers() throws IOException {
        Files.deleteIfExists(usersFile);
        ReflectionTestUtils.invokeMethod(dataLoadingService, "loadUsersFromCsv");
        verify(mockUserRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("loadUsersFromCsv should skip existing users by username")
    void loadUsersFromCsv_SkipsExistingUsersByUsername() throws IOException {
        User u1Existing = new User(1, "user1", "Existing", "One");
        User u2New = new User(2, "user2new", "New", "Two");
        when(mockCsvDataParser.parseUsersFile(usersFile)).thenReturn(List.of(u1Existing, u2New));

        when(mockUserRepository.findById(1)).thenReturn(Optional.empty());
        when(mockUserRepository.findByUsername("user1")).thenReturn(Optional.of(u1Existing));
        when(mockUserRepository.findById(2)).thenReturn(Optional.empty());
        when(mockUserRepository.findByUsername("user2new")).thenReturn(Optional.empty());
        when(mockUserRepository.save(u2New)).thenReturn(u2New);

        ReflectionTestUtils.invokeMethod(dataLoadingService, "loadUsersFromCsv");

        verify(mockUserRepository, times(1)).save(u2New);
        verify(mockUserRepository, never()).save(u1Existing);
    }
}