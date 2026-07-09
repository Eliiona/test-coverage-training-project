package lv.bootcamp.shelter.service;

import lv.bootcamp.shelter.audit.AuditLogger;
import lv.bootcamp.shelter.audit.RejectionReason;
import lv.bootcamp.shelter.client.NotificationClient;
import lv.bootcamp.shelter.model.*;
import lv.bootcamp.shelter.repository.AdopterRepository;
import lv.bootcamp.shelter.repository.AnimalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Write tests for AdoptionEligibilityService.
 * The class and mocks are set up — the rest is yours.
 */
@ExtendWith(MockitoExtension.class)
class AdoptionEligibilityServiceTest {

    @Mock
    private AdopterRepository adopterRepository;

    @Mock
    private AnimalRepository animalRepository;

    @Mock
    private NotificationClient notificationClient;

    @Mock
    private AuditLogger auditLogger;

    @InjectMocks
    private AdoptionEligibilityService service;

    @Test
    void shouldRejectWhenAdopterDoesNotExist() {

        when(adopterRepository.findById(1L))
                .thenReturn(Optional.empty());

        AdoptionResult result = service.evaluateAdoption(1L, 10L);

        assertFalse(result.approved());
        assertEquals(RejectionReasons.ADOPTER_NOT_FOUND, result.reason());

        verifyNoInteractions(notificationClient, auditLogger);
    }

    @Test
    void shouldRejectWhenAnimalDoesNotExist() {

        when(adopterRepository.findById(1L))
                .thenReturn(Optional.of(adopter()));

        when(animalRepository.findById(10L))
                .thenReturn(Optional.empty());

        AdoptionResult result =
                service.evaluateAdoption(1L, 10L);

        assertFalse(result.approved());
        assertEquals(RejectionReasons.ANIMAL_NOT_FOUND, result.reason());
    }

    @Test
    void shouldRejectUnavailableAnimal() {

        when(adopterRepository.findById(1L))
                .thenReturn(Optional.of(adopter()));

        when(animalRepository.findById(10L))
                .thenReturn(Optional.of(animal(AnimalStatus.ADOPTED)));

        AdoptionResult result = service.evaluateAdoption(1L, 10L);

        assertFalse(result.approved());
        assertEquals(RejectionReasons.ANIMAL_NOT_AVAILABLE, result.reason());
    }

    @Test
    void shouldRejectUnderageAdopter() {

        Adopter adopter = adopter();
        adopter.setAge(17);

        when(adopterRepository.findById(1L))
                .thenReturn(Optional.of(adopter));

        when(animalRepository.findById(10L))
                .thenReturn(Optional.of(animal(AnimalStatus.AVAILABLE)));

        AdoptionResult result = service.evaluateAdoption(1L, 10L);

        assertFalse(result.approved());
        assertEquals(RejectionReasons.UNDERAGE, result.reason());

        verify(auditLogger).logRejection(1L, 10L, RejectionReason.UNDERAGE);
    }

    @Test
    void shouldRejectWhenPetLimitReached() {

        Adopter adopter = adopter();
        adopter.setCurrentPetCount(3);

        when(adopterRepository.findById(1L))
                .thenReturn(Optional.of(adopter));

        when(animalRepository.findById(10L))
                .thenReturn(Optional.of(animal(AnimalStatus.AVAILABLE)));

        AdoptionResult result = service.evaluateAdoption(1L, 10L);

        assertFalse(result.approved());
        assertEquals(RejectionReasons.PET_LIMIT_REACHED, result.reason());
    }

    @Test
    void shouldApproveValidAdoption() {

        Adopter adopter = adopter();

        Animal animal = animal(AnimalStatus.AVAILABLE);
        animal.setName("Buddy");

        when(adopterRepository.findById(1L))
                .thenReturn(Optional.of(adopter));

        when(animalRepository.findById(10L))
                .thenReturn(Optional.of(animal));

        AdoptionResult result = service.evaluateAdoption(1L, 10L);

        assertTrue(result.approved());
        assertEquals("Approved", result.reason());

        verify(auditLogger).logApproval(eq(1L), eq(10L), anyInt());
        verify(notificationClient).sendApprovalNotification(adopter.getEmail(), "Buddy");
    }

    @Test
    void shouldRejectExoticAnimalWithoutPermit() {
        
        Adopter adopter = adopter();
        adopter.setExoticPermit(false);

        Animal animal = exoticAnimal();

        when(adopterRepository.findById(1L))
                .thenReturn(Optional.of(adopter));

        when(animalRepository.findById(20L))
                .thenReturn(Optional.of(animal));

        AdoptionResult result = service.evaluateAdoption(1L, 20L);

        assertFalse(result.approved());
        assertEquals(RejectionReasons.EXOTIC_PERMIT_REQUIRED, result.reason()
        );

        verify(auditLogger).logRejection(1L, 20L, RejectionReason.NO_EXOTIC_PERMIT);
        verifyNoInteractions(notificationClient);
    }

    private Adopter adopter() {
        return new Adopter(1L, "John", "john@test.com", 30, 0,
                0, false, true);
    }

    private Animal animal(AnimalStatus status) {
        return new Animal(10L, "Max", AnimalType.DOG, "Labrador", 5,
                "Friendly", status);
    }

    private Animal exoticAnimal() {
        return new Animal(20L, "Kiwi", AnimalType.BIRD, "African Grey Parrot",
                3, "Talkative", AnimalStatus.AVAILABLE);
    }
}
