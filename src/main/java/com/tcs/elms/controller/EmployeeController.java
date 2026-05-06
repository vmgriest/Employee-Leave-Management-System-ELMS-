package com.tcs.elms.controller;

import com.tcs.elms.dto.ApiResponse;
import com.tcs.elms.dto.EmployeeDto;
import com.tcs.elms.entity.Employee;
import com.tcs.elms.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@Tag(name = "Employee Management", description = "APIs for managing employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    @Operation(summary = "Create a new employee")
    public ResponseEntity<ApiResponse<Employee>> createEmployee(@Valid @RequestBody EmployeeDto dto) {
        Employee created = employeeService.createEmployee(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Employee created successfully", created));
    }

    @GetMapping
    @Operation(summary = "Get all employees")
    public ResponseEntity<ApiResponse<List<Employee>>> getAllEmployees() {
        List<Employee> employees = employeeService.getAllEmployees();
        return ResponseEntity.ok(ApiResponse.success("Employees fetched successfully", employees));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get employee by database ID")
    public ResponseEntity<ApiResponse<Employee>> getEmployeeById(@PathVariable Long id) {
        Employee employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(ApiResponse.success("Employee fetched successfully", employee));
    }

    @GetMapping("/emp-id/{employeeId}")
    @Operation(summary = "Get employee by employee ID (e.g. TCS001)")
    public ResponseEntity<ApiResponse<Employee>> getEmployeeByEmployeeId(
            @PathVariable String employeeId) {
        Employee employee = employeeService.getEmployeeByEmployeeId(employeeId);
        return ResponseEntity.ok(ApiResponse.success("Employee fetched successfully", employee));
    }

    @GetMapping("/department/{department}")
    @Operation(summary = "Get all employees in a department")
    public ResponseEntity<ApiResponse<List<Employee>>> getEmployeesByDepartment(
            @PathVariable String department) {
        List<Employee> employees = employeeService.getEmployeesByDepartment(department);
        return ResponseEntity.ok(ApiResponse.success("Employees fetched successfully", employees));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing employee")
    public ResponseEntity<ApiResponse<Employee>> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeDto dto) {
        Employee updated = employeeService.updateEmployee(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Employee updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an employee")
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok(ApiResponse.success("Employee deleted successfully", null));
    }

    @GetMapping("/{id}/leave-balance")
    @Operation(summary = "Get remaining leave balance for an employee")
    public ResponseEntity<ApiResponse<Double>> getLeaveBalance(@PathVariable Long id) {
        double balance = employeeService.getEmployeeLeaveBalance(id);
        return ResponseEntity.ok(ApiResponse.success("Leave balance fetched successfully", balance));
    }
}
