package com.assignment.java_assignment.controller;

import com.assignment.java_assignment.entity.Customer;
import com.assignment.java_assignment.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sunbase/portal/api")
public class CustomerController {
    private final CustomerService customerService;

    @Autowired
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/assignment_auth.jsp")
    public ResponseEntity<String> authenticateUser(@RequestBody Map<String, String> credentials) {
        String authApiUrl = "https://qa2.sunbasedata.com/sunbase/portal/api/assignment_auth.jsp";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create the request body with login_id and password
        String requestBody = "{\"login_id\":\"" + credentials.get("login_id") + "\", \"password\":\"" + credentials.get("password") + "\"}";

        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        RestTemplate restTemplate = new RestTemplate();

        try {
            // Make the POST request to the authentication API
            ResponseEntity<String> response = restTemplate.exchange(authApiUrl, HttpMethod.POST, requestEntity, String.class);

            // Check the response status
            if (response.getStatusCode() == HttpStatus.OK) {
                // Extract the bearer token from the response
                String token = response.getBody();

                // Optionally, you can store the token for further API calls
                // You can also implement a token cache to reuse the token until it expires

                // Return the token as the response
                return ResponseEntity.ok(token);
            } else {
                // Handle other possible error scenarios, e.g., invalid credentials, server errors, etc.
                return ResponseEntity.status(response.getStatusCode()).body("Authentication failed: " + response.getBody());
            }
        } catch (Exception e) {
            // Handle any exceptions that may occur during the API call
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred during authentication: " + e.getMessage());
        }
    }

    @PostMapping("/assignment.jsp")
    public ResponseEntity<String> createCustomer(@RequestBody Customer customer) {
        if (customer.getFirstName() == null || customer.getLastName() == null) {
            return ResponseEntity.badRequest().body("First Name or Last Name is missing");
        }
        Customer createdCustomer = customerService.createCustomer(customer);
        if (createdCustomer != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body("Successfully Created");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create customer");
        }
    }

    @GetMapping("/assignment.jsp")
    public ResponseEntity<List<Customer>> getCustomerList() {
        List<Customer> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }

    @DeleteMapping("/assignment.jsp")
    public ResponseEntity<String> deleteCustomer(@RequestParam("cmd") String command, @RequestParam("uuid") Long customerId) {
        if (!command.equals("delete")) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Invalid Command");
        }
        try {
            customerService.deleteCustomer(customerId);
            return ResponseEntity.ok("Successfully deleted");
        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.badRequest().body("UUID not found");
        }
    }

    @PostMapping("/assignment_update.jsp")
    public ResponseEntity<String> updateCustomer(@RequestParam("cmd") String command, @RequestParam("uuid") Long customerId, @RequestBody Customer customer) {
        if (!command.equals("update")) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Invalid Command");
        }
        Customer updatedCustomer = customerService.updateCustomer(customerId, customer);
        if (updatedCustomer != null) {
            return ResponseEntity.ok("Successfully Updated");
        } else {
            return ResponseEntity.badRequest().body("UUID not found");
        }
    }
}
