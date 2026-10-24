/**
 * Lớp Relation trong Room kết hợp thông tin bản ghi Bảng lương (Salary)
 * và thông tin Nhân viên (Employee) tương ứng để hiển thị trên giao diện Admin.
 */
package com.example.test2.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class SalaryWithEmployee(
    @Embedded val salary: Salary,
    @Relation(
        parentColumn = "employeeId",
        entityColumn = "id"
    )
    val employee: Employee
)