package com.tcs.elms.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tcs.elms.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "employees")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Employee ID must not be blank")
    @Column(unique = true, nullable = false, length = 20)
    private String employeeId;

    @NotBlank(message = "Name must not be blank")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email must be valid")
    @Column(unique = true, nullable = false)
    private String email;

    @Column
    private String department;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    @Builder.Default
    private double availableLeaves = 20.0;

    /**
     * References the id of the employee's direct manager.
     * Stored as a plain Long to avoid circular JPA relationships.
     */
    @Column
    private Long managerId;
}
