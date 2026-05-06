package com.tcs.elms.service;

import com.tcs.elms.dto.LeaveRequestDto;
import com.tcs.elms.entity.Employee;
import com.tcs.elms.entity.LeaveRequest;
import com.tcs.elms.enums.LeaveStatus;
import com.tcs.elms.enums.LeaveType;
import com.tcs.elms.enums.Role;
import com.tcs.elms.exception.AccessDeniedException;
import com.tcs.elms.exception.BusinessException;
import com.tcs.elms.exception.ResourceNotFoundException;
import com.tcs.elms.repository.EmployeeRepository;
import com.tcs.elms.repository.LeaveRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveServiceTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private LeaveService leaveService;

    private Employee employee;
    private Employee manager;
    private LeaveRequestDto validDto;

    @BeforeEach
    void setUp() {
        manager = Employee.builder()
                .id(10L)
                .employeeId("MGR001")
                .name("Alice Manager")
                .email("alice@tcs.com")
                .role(Role.MANAGER)
                .availableLeaves(20.0)
                .build();

        employee = Employee.builder()
                .id(1L)
                .employeeId("EMP001")
                .name("Bob Employee")
                .email("bob@tcs.com")
                .role(Role.EMPLOYEE)
                .availableLeaves(20.0)
                .managerId(10L)
                .build();

        validDto = LeaveRequestDto.builder()
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(3))
                .leaveType(LeaveType.CASUAL)
                .reason("Family vacation")
                .build();
    }

    // -------------------------------------------------------------------------
    // applyForLeave tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("applyLeave: employee with existing PENDING leave should throw BusinessException")
    void applyLeave_WhenEmployeeHasPendingLeave_ShouldThrowBusinessException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        LeaveRequest existingPending = LeaveRequest.builder()
                .id(99L)
                .employee(employee)
                .status(LeaveStatus.PENDING)
                .build();
        when(leaveRequestRepository.findByEmployeeIdAndStatus(1L, LeaveStatus.PENDING))
                .thenReturn(List.of(existingPending));

        assertThrows(BusinessException.class,
                () -> leaveService.applyForLeave(1L, validDto));

        verify(leaveRequestRepository, never()).save(any());
    }

    @Test
    @DisplayName("applyLeave: insufficient leave balance should throw BusinessException")
    void applyLeave_WhenInsufficientBalance_ShouldThrowBusinessException() {
        employee.setAvailableLeaves(1.0);  // only 1 day left; request is for 3 days

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveRequestRepository.findByEmployeeIdAndStatus(1L, LeaveStatus.PENDING))
                .thenReturn(Collections.emptyList());

        assertThrows(BusinessException.class,
                () -> leaveService.applyForLeave(1L, validDto));

        verify(leaveRequestRepository, never()).save(any());
    }

    @Test
    @DisplayName("applyLeave: valid request should save and return the leave request")
    void applyLeave_WhenValid_ShouldReturnSavedRequest() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveRequestRepository.findByEmployeeIdAndStatus(1L, LeaveStatus.PENDING))
                .thenReturn(Collections.emptyList());

        LeaveRequest savedRequest = LeaveRequest.builder()
                .id(5L)
                .employee(employee)
                .startDate(validDto.getStartDate())
                .endDate(validDto.getEndDate())
                .leaveType(validDto.getLeaveType())
                .reason(validDto.getReason())
                .status(LeaveStatus.PENDING)
                .appliedAt(LocalDateTime.now())
                .build();

        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(savedRequest);

        LeaveRequest result = leaveService.applyForLeave(1L, validDto);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(LeaveStatus.PENDING);
        assertThat(result.getId()).isEqualTo(5L);
        verify(leaveRequestRepository).save(any(LeaveRequest.class));
    }

    // -------------------------------------------------------------------------
    // approveLeave tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("approveLeave: non-manager attempting approval should throw AccessDeniedException")
    void approveLeave_WhenNotManager_ShouldThrowAccessDeniedException() {
        // The "manager" has EMPLOYEE role — should be denied
        Employee notAManager = Employee.builder()
                .id(20L)
                .employeeId("EMP002")
                .name("Charlie Employee")
                .email("charlie@tcs.com")
                .role(Role.EMPLOYEE)
                .availableLeaves(20.0)
                .build();

        LeaveRequest pendingRequest = LeaveRequest.builder()
                .id(1L)
                .employee(employee)
                .status(LeaveStatus.PENDING)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(3))
                .build();

        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(pendingRequest));
        when(employeeRepository.findById(20L)).thenReturn(Optional.of(notAManager));

        assertThrows(AccessDeniedException.class,
                () -> leaveService.approveLeave(1L, 20L, "looks good"));
    }

    @Test
    @DisplayName("approveLeave: manager who is not the direct manager should throw AccessDeniedException")
    void approveLeave_WhenNotTeamMember_ShouldThrowAccessDeniedException() {
        // employee.managerId = 10, but approver has id 99
        Employee wrongManager = Employee.builder()
                .id(99L)
                .employeeId("MGR002")
                .name("Dave Manager")
                .email("dave@tcs.com")
                .role(Role.MANAGER)
                .availableLeaves(20.0)
                .build();

        LeaveRequest pendingRequest = LeaveRequest.builder()
                .id(1L)
                .employee(employee)
                .status(LeaveStatus.PENDING)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(3))
                .build();

        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(pendingRequest));
        when(employeeRepository.findById(99L)).thenReturn(Optional.of(wrongManager));

        assertThrows(AccessDeniedException.class,
                () -> leaveService.approveLeave(1L, 99L, "approved"));
    }

    @Test
    @DisplayName("approveLeave: valid approval should set status APPROVED and deduct balance")
    void approveLeave_WhenValid_ShouldApproveAndDeductBalance() {
        LeaveRequest pendingRequest = LeaveRequest.builder()
                .id(1L)
                .employee(employee)
                .status(LeaveStatus.PENDING)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(3)) // 3 days inclusive
                .build();

        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(pendingRequest));
        when(employeeRepository.findById(10L)).thenReturn(Optional.of(manager));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        LeaveRequest approvedRequest = LeaveRequest.builder()
                .id(1L)
                .employee(employee)
                .status(LeaveStatus.APPROVED)
                .startDate(pendingRequest.getStartDate())
                .endDate(pendingRequest.getEndDate())
                .reviewedAt(LocalDateTime.now())
                .build();
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(approvedRequest);

        LeaveRequest result = leaveService.approveLeave(1L, 10L, "approved");

        assertThat(result.getStatus()).isEqualTo(LeaveStatus.APPROVED);
        // Balance should have been deducted: 20 - 3 = 17
        assertThat(employee.getAvailableLeaves()).isEqualTo(17.0);
        verify(employeeRepository).save(employee);
        verify(leaveRequestRepository).save(any(LeaveRequest.class));
    }
}
