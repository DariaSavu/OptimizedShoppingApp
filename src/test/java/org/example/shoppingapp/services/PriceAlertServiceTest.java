package org.example.shoppingapp.services;

import org.example.shoppingapp.model.PriceAlert;
import org.example.shoppingapp.model.PriceEntry;
import org.example.shoppingapp.model.Product;
import org.example.shoppingapp.model.User;
import org.example.shoppingapp.model.dto.TriggeredAlertDTO;
import org.example.shoppingapp.repository.interfaces.PriceEntryRepository;
import org.example.shoppingapp.repository.interfaces.ProductRepository;
import org.example.shoppingapp.repository.interfaces.UserRepository;
import org.example.shoppingapp.service.PriceAlertService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.lenient;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PriceAlertServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private PriceEntryRepository priceEntryRepository;

    @InjectMocks
    private PriceAlertService priceAlertService;

    private User user1;
    private Product p1, p2;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        today = LocalDate.now();
        user1 = new User(1, "testuser", "Test", "User");
        p1 = new Product("P001", "Lapte", "Lactate", "BrandA", 1.0, "l");
        p2 = new Product("P002", "Pâine", "Panificație", "BrandB", 0.5, "kg");

        lenient().when(userRepository.findById(1)).thenReturn(Optional.of(user1));
        lenient().when(productRepository.findById("P001")).thenReturn(Optional.of(p1));
        lenient().when(productRepository.findById("P002")).thenReturn(Optional.of(p2));
    }

    @Test
    @DisplayName("SetAlert successfully creates an alert")
    void setAlert_Successful() {
        boolean result = priceAlertService.setAlert(1, "P001", 4.50);
        assertTrue(result);
        List<PriceAlert> alerts = priceAlertService.getAlertsForUser(1);
        assertEquals(1, alerts.size());
        assertEquals("P001", alerts.get(0).getProduct().getProductId());
        assertEquals(4.50, alerts.get(0).getTargetPrice());
    }

    @Test
    @DisplayName("SetAlert fails if user not found")
    void setAlert_UserNotFound_Fails() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());
        boolean result = priceAlertService.setAlert(99, "P001", 4.50);
        assertFalse(result);
    }

    @Test
    @DisplayName("SetAlert fails if product not found")
    void setAlert_ProductNotFound_Fails() {
        when(productRepository.findById("P999")).thenReturn(Optional.empty());
        boolean result = priceAlertService.setAlert(1, "P999", 4.50);
        assertFalse(result);
    }

    @Test
    @DisplayName("SetAlert fails for non-positive target price")
    void setAlert_NonPositiveTargetPrice_Fails() {
        boolean result = priceAlertService.setAlert(1, "P001", 0.0);
        assertFalse(result);
        result = priceAlertService.setAlert(1, "P001", -1.0);
        assertFalse(result);
    }

    @Test
    @DisplayName("SetAlert overwrites existing alert for same user and product")
    void setAlert_OverwritesExisting() {
        priceAlertService.setAlert(1, "P001", 5.00);
        priceAlertService.setAlert(1, "P001", 4.50);
        List<PriceAlert> alerts = priceAlertService.getAlertsForUser(1);
        assertEquals(1, alerts.size());
        assertEquals(4.50, alerts.get(0).getTargetPrice());
    }
    
    @Test
    @DisplayName("GetAlertsForUser returns correct alerts")
    void getAlertsForUser_ReturnsCorrectly() {
        priceAlertService.setAlert(1, "P001", 4.50);
        priceAlertService.setAlert(1, "P002", 3.00);
        
        List<PriceAlert> alertsUser1 = priceAlertService.getAlertsForUser(1);
        assertEquals(2, alertsUser1.size());
        
        List<PriceAlert> alertsUser2 = priceAlertService.getAlertsForUser(2);
        assertTrue(alertsUser2.isEmpty());
    }


    @Test
    @DisplayName("CheckTriggeredAlerts finds alerts when price is at or below target")
    void checkTriggeredAlerts_FindsTriggered() {
        priceAlertService.setAlert(1, "P001", 5.00);
        PriceEntry peP001Lidl = new PriceEntry(p1, "Lidl", today, 4.80, "RON");
        PriceEntry peP001Kauf = new PriceEntry(p1, "Kaufland", today, 5.10, "RON");
        when(priceEntryRepository.findByProductId("P001")).thenReturn(Arrays.asList(peP001Lidl, peP001Kauf));

        List<TriggeredAlertDTO> triggered = priceAlertService.checkTriggeredAlerts();
        assertEquals(1, triggered.size());
        TriggeredAlertDTO alert = triggered.get(0);
        assertEquals(1, alert.getUserId());
        assertEquals("P001", alert.getProductId());
        assertEquals("Lidl", alert.getStoreName());
        assertEquals(0, BigDecimal.valueOf(4.80).compareTo(alert.getCurrentPrice()));
        assertEquals(0, BigDecimal.valueOf(5.00).compareTo(alert.getTargetPriceSetByUser()));
    }

    @Test
    @DisplayName("CheckTriggeredAlerts finds no alerts if price is above target")
    void checkTriggeredAlerts_NoTriggerIfPriceAboveTarget() {
        priceAlertService.setAlert(1, "P001", 4.00);
        PriceEntry peP001Lidl = new PriceEntry(p1, "Lidl", today, 4.80, "RON");
        when(priceEntryRepository.findByProductId("P001")).thenReturn(List.of(peP001Lidl));

        List<TriggeredAlertDTO> triggered = priceAlertService.checkTriggeredAlerts();
        assertTrue(triggered.isEmpty());
    }
    @Test
    @DisplayName("RemoveAlert successfully removes an active alert")
    void removeAlert_Successful() {
        priceAlertService.setAlert(1, "P001", 4.50);
        assertTrue(priceAlertService.getAlertsForUser(1).stream().anyMatch(a -> a.getProduct().getProductId().equals("P001")));
        
        boolean removed = priceAlertService.removeAlert(1, "P001");
        assertTrue(removed);
        assertTrue(priceAlertService.getAlertsForUser(1).isEmpty());
    }

    @Test
    @DisplayName("RemoveAlert returns false if alert not found")
    void removeAlert_NotFound_ReturnsFalse() {
        boolean removed = priceAlertService.removeAlert(1, "P999");
        assertFalse(removed);
        
        priceAlertService.setAlert(1, "P001", 4.50);
        boolean removedOtherUser = priceAlertService.removeAlert(2, "P001");
        assertFalse(removedOtherUser);
        assertFalse(priceAlertService.getAlertsForUser(1).isEmpty());
    }
}