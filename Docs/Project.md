Project: EMPLOYEE WORKING HOURS AND SALARY MANAGEMENT
Create a project with the following 4 tables:
•	Account(id, username, password, role)
•	Employee(id, code, fullname, email, phone, hourlyRate, accountId)
•	WorkLog(id, employeeId, workDate, checkInTime, checkOutTime, totalHours, status)
•	Salary(id, employeeId, month, year, totalHours, overtimeHours, totalSalary, status)
And required functionalities:
•	Login: Implement a login function with two roles: Admin and Employee, and provide the corresponding functions for each role.
•	Login Account Storage: Store login account information using SharedPreferences.
•	Data Storage: Store the tables using SQLite or Room Database.
•	Employee Management: Allow administrators to add, update, delete, search, and view employee information, including hourly rates.
•	Attendance Management: Allow employees to check in and check out daily. Record the working date, check-in time, and check-out time.
•	Working Hours Calculation: Automatically calculate the total working hours for each employee based on check-in and check-out times.
•	Salary Calculation: Automatically calculate monthly salaries based on total working hours and hourly rates. Support overtime calculations based on predefined overtime rates.
•	Salary Management: Allow employees to view their monthly working hours, overtime hours, and salary details. Allow administrators to view and manage salary records.
•	Attendance History: Allow employees to view their daily attendance records and monthly working-hour summaries.
•	Data Validation: Prevent duplicate check-ins, invalid check-out times, and incomplete attendance records.
•	Version Control and Collaboration: Work together and manage the project code using GitHub or GitLab.
•	Scoring: The score is based on the time taken to complete the project. The less time a student takes, the higher the score.

