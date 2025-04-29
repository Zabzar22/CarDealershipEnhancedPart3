package org.example;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class XMLFileHandlerTest {
    @Test
    void testImportXML() {
        XMLFileHandler handler = new XMLFileHandler();
        // Use a sample XML file path for testing
        File xmlFile = new File("src/test/resources/sample_inventory.xml");
        // This test will pass if the method does not throw and returns a list
        List<Vehicle> vehicles = handler.importXML(xmlFile);
        assertNotNull(vehicles);
        // Optionally, check for expected size or contents if you have a known sample file
        // assertEquals(expectedCount, vehicles.size());
    }
}
