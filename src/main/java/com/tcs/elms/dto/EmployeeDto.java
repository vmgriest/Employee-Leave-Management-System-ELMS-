package com.tcs.elms.dto;

import com.tcs.elms.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDto {

    @NotBlank(message = "Employee ID must not be blank")
    private String employeeId;

    @NotBlank(message = "Name must not be blank")
    private String name;

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email must be a valid address")
    private String email;

    private String department;

    @NotNull(message = "Role must not be null")
    private Role role;

    /** Leave balance — defaults to 20.0 when null/not supplied. */
    private Double availableLeaves;

    /** ID of the employee's direct manager (nullable). */
    private Long managerId;
}
