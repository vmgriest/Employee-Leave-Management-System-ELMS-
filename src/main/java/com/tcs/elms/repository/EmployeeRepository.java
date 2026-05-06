package com.tcs.elms.repository;

import com.tcs.elms.entity.Employee;
import com.tcs.elms.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmployeeId(String employeeId);

    List<Employee> findByDepartment(String department);

    List<Employee> findByRole(Role role);

    boolean existsByEmployeeId(String employeeId);

    boolean existsByEmail(String email);
}
