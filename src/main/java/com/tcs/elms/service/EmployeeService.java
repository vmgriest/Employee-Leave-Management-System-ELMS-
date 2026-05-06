package com.tcs.elms.service;

import com.tcs.elms.dto.EmployeeDto;
import com.tcs.elms.entity.Employee;
import com.tcs.elms.exception.BusinessException;
import com.tcs.elms.exception.ResourceNotFoundException;
import com.tcs.elms.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    // -------------------------------------------------------------------------
    // Create
    // -------------------------------------------------------------------------

    @Transactional
    public Employee createEmployee(EmployeeDto dto) {
        if (employeeRepository.existsByEmployeeId(dto.getEmployeeId())) {
            throw new BusinessException(
                    "Employee with ID '" + dto.getEmployeeId() + "' already exists");
        }
        if (employeeRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessException(
                    "Employee with email '" + dto.getEmail() + "' already exists");
        }

        double leaves = (dto.getAvailableLeaves() != null) ? dto.getAvailableLeaves() : 20.0;

        Employee employee = Employee.builder()
                .employeeId(dto.getEmployeeId())
                .name(dto.getName())
                .email(dto.getEmail())
                .department(dto.getDepartment())
                .role(dto.getRole())
                .availableLeaves(leaves)
                .managerId(dto.getManagerId())
                .build();

        return employeeRepository.save(employee);
    }

    // -------------------------------------------------------------------------
    // Read
    // -------------------------------------------------------------------------

    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
    }

    public Employee getEmployeeByEmployeeId(String employeeId) {
        return employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "employeeId", employeeId));
    }

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public List<Employee> getEmployeesByDepartment(String department) {
        return employeeRepository.findByDepartment(department);
    }

    public double getEmployeeLeaveBalance(Long id) {
        Employee employee = getEmployeeById(id);
        return employee.getAvailableLeaves();
    }

    // -------------------------------------------------------------------------
    // Update
    // -------------------------------------------------------------------------

    @Transactional
    public Employee updateEmployee(Long id, EmployeeDto dto) {
        Employee existing = getEmployeeById(id);

        // Validate uniqueness only if the values are actually changing
        if (!existing.getEmployeeId().equals(dto.getEmployeeId())
                && employeeRepository.existsByEmployeeId(dto.getEmployeeId())) {
            throw new BusinessException(
                    "Employee with ID '" + dto.getEmployeeId() + "' already exists");
        }
        if (!existing.getEmail().equals(dto.getEmail())
                && employeeRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessException(
                    "Employee with email '" + dto.getEmail() + "' already exists");
        }

        existing.setEmployeeId(dto.getEmployeeId());
        existing.setName(dto.getName());
        existing.setEmail(dto.getEmail());
        existing.setDepartment(dto.getDepartment());
        existing.setRole(dto.getRole());
        existing.setManagerId(dto.getManagerId());
        if (dto.getAvailableLeaves() != null) {
            existing.setAvailableLeaves(dto.getAvailableLeaves());
        }

        return employeeRepository.save(existing);
    }

    // -------------------------------------------------------------------------
    // Delete
    // -------------------------------------------------------------------------

    @Transactional
    public void deleteEmployee(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Employee", "id", id);
        }
        employeeRepository.deleteById(id);
    }
}
