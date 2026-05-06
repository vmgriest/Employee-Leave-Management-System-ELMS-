package com.tcs.elms.service;

import com.tcs.elms.dto.EmployeeDto;
import com.tcs.elms.entity.Employee;
import com.tcs.elms.enums.Role;
import com.tcs.elms.exception.BusinessException;
import com.tcs.elms.exception.ResourceNotFoundException;
import com.tcs.elms.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private EmployeeDto validDto;
    private Employee savedEmployee;

    @BeforeEach
    void setUp() {
        validDto = EmployeeDto.builder()
                .employeeId("TCS001")
                .name("John Doe")
                .email("john.doe@tcs.com")
                .department("Engineering")
                .role(Role.EMPLOYEE)
                .build();

        savedEmployee = Employee.builder()
                .id(1L)
                .employeeId("TCS001")
                .name("John Doe")
                .email("john.doe@tcs.com")
                .department("Engineering")
                .role(Role.EMPLOYEE)
                .availableLeaves(20.0)
                .build();
    }

    // -------------------------------------------------------------------------
    // createEmployee tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("createEmployee: duplicate employeeId should throw BusinessException")
    void createEmployee_WhenDuplicateEmployeeId_ShouldThrowBusinessException() {
        when(employeeRepository.existsByEmployeeId("TCS001")).thenReturn(true);

        assertThrows(BusinessException.class,
                () -> employeeService.createEmployee(validDto));

        verify(employeeRepository, never()).save(any());
    }

    @Test
    @DisplayName("createEmployee: valid employee should be saved and returned")
    void createEmployee_WhenValid_ShouldReturnEmployee() {
        when(employeeRepository.existsByEmployeeId("TCS001")).thenReturn(false);
        when(employeeRepository.existsByEmail("john.doe@tcs.com")).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(savedEmployee);

        Employee result = employeeService.createEmployee(validDto);

        assertThat(result).isNotNull();
        assertThat(result.getEmployeeId()).isEqualTo("TCS001");
        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getAvailableLeaves()).isEqualTo(20.0);
        verify(employeeRepository).save(any(Employee.class));
    }

    // -------------------------------------------------------------------------
    // getEmployeeById tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getEmployeeById: non-existent id should throw ResourceNotFoundException")
    void getEmployeeById_WhenNotFound_ShouldThrowResourceNotFoundException() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> employeeService.getEmployeeById(999L));
    }

    @Test
    @DisplayName("getEmployeeById: existing id should return the employee")
    void getEmployeeById_WhenFound_ShouldReturnEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(savedEmployee));

        Employee result = employeeService.getEmployeeById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmployeeId()).isEqualTo("TCS001");
    }
}
