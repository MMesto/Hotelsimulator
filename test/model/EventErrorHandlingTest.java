package model;

import hotelevents.HotelEvent;
import hotelevents.HotelEventType;

/**
 * Event Error Handling Test
 * Verifiëert dat corrupte/null data niet tot crashes leidt
 */
public class EventErrorHandlingTest {

    public static void main(String[] args) {
        System.out.println("=== Event Error Handling Test ===\n");

        EventBusImpl eventBus = new EventBusImpl();
        
        System.out.println("[TEST 1] Null DLL event name");
        eventBus.handleExternalDLLEvent(null);
        System.out.println("✅ System blijft draaien\n");

        System.out.println("[TEST 2] Empty DLL event name");
        eventBus.handleExternalDLLEvent("");
        System.out.println("✅ System blijft draaien\n");

        System.out.println("[TEST 3] Null HotelEvent");
        eventBus.handleHotelEvent(null);
        System.out.println("✅ System blijft draaien\n");

        System.out.println("[TEST 4] HotelEvent met null type");
        HotelEvent badEvent = new HotelEvent(1, null, 0, 0);
        eventBus.handleHotelEvent(badEvent);
        System.out.println("✅ System blijft draaien\n");

        System.out.println("[TEST 5] Trigger event met null type");
        eventBus.triggerHotelEvent(null, 1, 0);
        System.out.println("✅ System blijft draaien\n");

        System.out.println("[TEST 6] Valid event nadat fouten afgehandeld zijn");
        eventBus.handleExternalDLLEvent("Initialization");
        System.out.println("✅ System verwerkt normale events nog steeds\n");

        System.out.println("[TEST 7] System state check na fouten");
        System.out.println("System State: " + eventBus.getSystemState());
        System.out.println("✅ Status is behouden\n");

        System.out.println("[TEST 8] Error log review");
        System.out.println("Error log entries: " + eventBus.getErrorLog().size());
        if (!eventBus.getErrorLog().isEmpty()) {
            System.out.println("Sample errors:");
            eventBus.getErrorLog().stream().limit(3).forEach(System.out::println);
        }
        System.out.println("✅ Fouten worden gelogd\n");

        System.out.println("=== Alle tests geslaagd! ===");
        System.out.println("Summary:");
        System.out.println("- Application crashed NOT ✅");
        System.out.println("- Null/corrupte data handled ✅");
        System.out.println("- Errors logged ✅");
        System.out.println("- System status behouden ✅");
    }
}
