package com.employee.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import com.employee.dto.AddressInfoDTO;
import com.employee.dto.AgreementInfoDTO;
import com.employee.dto.BankInfoDTO;
import com.employee.dto.BasicInfoDTO;
import com.employee.dto.CategoryInfoDTO;
import com.employee.dto.DocumentDTO;
import com.employee.dto.EmployeeOnboardingDTO;
import com.employee.dto.FamilyInfoDTO;
import com.employee.dto.PreviousEmployerInfoDTO;
import com.employee.dto.QualificationDTO;
import com.employee.entity.BankDetails;
import com.employee.entity.EmpChequeDetails;
import com.employee.entity.EmpaddressInfo;
import com.employee.entity.EmpDetails;
import com.employee.entity.EmpDocuments;
import com.employee.entity.EmpExperienceDetails;
import com.employee.entity.EmpFamilyDetails;
import com.employee.entity.EmpPfDetails;
import com.employee.entity.EmpQualification;
import com.employee.entity.Employee;
import com.employee.entity.EmployeeCheckListStatus;
import com.employee.entity.Campus;
import com.employee.exception.ResourceNotFoundException;
import com.employee.repository.BloodGroupRepository;
import com.employee.repository.CampusRepository;
import com.employee.repository.CasteRepository;
import com.employee.repository.CategoryRepository;
import com.employee.repository.CityRepository;
import com.employee.repository.CountryRepository;
import com.employee.repository.DepartmentRepository;
import com.employee.repository.DesignationRepository;
import com.employee.repository.DistrictRepository;
import com.employee.repository.EmpaddressInfoRepository;
import com.employee.repository.EmpChequeDetailsRepository;
import com.employee.repository.EmpDetailsRepository;
import com.employee.repository.EmpDocTypeRepository;
import com.employee.repository.EmpDocumentsRepository;
import com.employee.repository.EmpExperienceDetailsRepository;
import com.employee.repository.EmpFamilyDetailsRepository;
import com.employee.repository.EmpPfDetailsRepository;
import com.employee.repository.EmpQualificationRepository;
import com.employee.repository.EmpSubjectRepository;
import com.employee.repository.EmpPaymentTypeRepository;
import com.employee.repository.EmployeeCheckListStatusRepository;
import com.employee.repository.EmployeeRepository;
import com.employee.repository.EmployeeTypeRepository;
import com.employee.repository.GenderRepository;
import com.employee.repository.JoiningAsRepository;
import com.employee.repository.MaritalStatusRepository;
import com.employee.repository.ModeOfHiringRepository;
import com.employee.repository.QualificationDegreeRepository;
import com.employee.repository.QualificationRepository;
import com.employee.repository.RelationRepository;
import com.employee.repository.RelegionRepository;
import com.employee.repository.StateRepository;
import com.employee.repository.WorkingModeRepository;
import com.employee.repository.BankDetailsRepository;
import com.employee.repository.OrgBankRepository;
import com.employee.repository.OrgBankBranchRepository;

import com.employee.repository.SubjectRepository;
import com.employee.repository.SkillTestDetlRepository;
import com.employee.entity.EmpSubject;
import com.employee.entity.EmpPaymentType;
import com.employee.entity.OrgBank;
import com.employee.entity.OrgBankBranch;
import com.employee.entity.subject;

@Service
@Transactional
public class EmployeeService {

	private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);

	@Autowired
	private EmployeeRepository employeeRepository;

	@Autowired
	private EmpDetailsRepository empDetailsRepository;

	@Autowired
	private EmpPfDetailsRepository empPfDetailsRepository;

	@Autowired
	private EmpaddressInfoRepository empaddressInfoRepository;

	@Autowired
	private EmpFamilyDetailsRepository empFamilyDetailsRepository;

	@Autowired
	private EmpExperienceDetailsRepository empExperienceDetailsRepository;

	@Autowired
	private EmpQualificationRepository empQualificationRepository;

	@Autowired
	private EmpDocumentsRepository empDocumentsRepository;

	@Autowired
	private BankDetailsRepository bankDetailsRepository;
	
	@Autowired
	private DepartmentRepository departmentRepository;

	@Autowired
	private GenderRepository genderRepository;

	@Autowired
	private BloodGroupRepository bloodGroupRepository;

	@Autowired
	private CasteRepository casteRepository;

	@Autowired
	private RelegionRepository relegionRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private DesignationRepository designationRepository;

	@Autowired
	private EmployeeTypeRepository employeeTypeRepository;

	@Autowired
	private QualificationRepository qualificationRepository;

	@Autowired
	private QualificationDegreeRepository qualificationDegreeRepository;

	@Autowired
	private WorkingModeRepository workingModeRepository;

	@Autowired
	private JoiningAsRepository joiningAsRepository;

	@Autowired
	private ModeOfHiringRepository modeOfHiringRepository;

	@Autowired
	private EmployeeCheckListStatusRepository employeeCheckListStatusRepository;

	@Autowired
	private MaritalStatusRepository maritalStatusRepository;

	@Autowired
	private CountryRepository countryRepository;

	@Autowired
	private StateRepository stateRepository;

	@Autowired
	private CityRepository cityRepository;

	@Autowired
	private DistrictRepository districtRepository;

	@Autowired
	private RelationRepository relationRepository;

	@Autowired
	private EmpDocTypeRepository empDocTypeRepository;

	@Autowired
	private EmpSubjectRepository empSubjectRepository;
	
	@Autowired
	private SubjectRepository subjectRepository;
	
	@Autowired
	private SkillTestDetlRepository skillTestDetlRepository;
	
	@Autowired
	private CampusRepository campusRepository;

	@Autowired
	private EmpPaymentTypeRepository empPaymentTypeRepository;

	@Autowired
	private OrgBankRepository orgBankRepository;
	
	@Autowired
	private EmpChequeDetailsRepository empChequeDetailsRepository;
	
	@Autowired
	private OrgBankBranchRepository orgBankBranchRepository;

	@Autowired
	private ModelMapper modelMapper;

	@PersistenceContext
	private EntityManager entityManager;

	/**
	 * Main onboarding method - handles all 8 tabs in one transaction
	 * Supports both INSERT (new employee) and UPDATE (existing employee) operations
	 * 
	 * Flow:
	 * 1. Check if employee exists by temp_payroll_id
	 * 2. If exists: UPDATE existing employee and all child tables
	 * 3. If not exists: INSERT new employee (creates new emp_id)
	 * 
	 * IMPORTANT: Sequence Number Skipping in PostgreSQL
	 * ===================================================
	 * PostgreSQL sequences operate OUTSIDE of transactions. When nextval() is called,
	 * the sequence number is consumed even if the transaction rolls back.
	 * 
	 * To MINIMIZE sequence skipping:
	 * 1. Comprehensive validation happens BEFORE any database save operations (validateOnboardingData)
	 * 2. All foreign key existence checks happen BEFORE employee.save()
	 * 3. If validation fails, NO emp_id is consumed
	 * 
	 * However, if an error occurs AFTER employee.save() (step 1), the transaction rolls back
	 * but the sequence number is already consumed. This is NORMAL PostgreSQL behavior and cannot
	 * be completely prevented. The gaps are harmless and don't affect functionality.
	 * 
	 * @param onboardingDTO Contains all data from all 8 tabs
	 * @return Same DTO structure that was posted, with emp_id added
	 */
	public EmployeeOnboardingDTO onboardEmployee(EmployeeOnboardingDTO onboardingDTO) {
		
		try {
			validateOnboardingData(onboardingDTO);
			performPreFlightChecks(onboardingDTO);
		} catch (Exception e) {
			logger.error("❌ ERROR: Employee onboarding failed DURING VALIDATION. NO ID consumed. Error will be thrown before employee.save(). Root cause: {}", 
					e.getMessage(), e);
			throw e;
		}
		
		Employee employee = null;
		boolean isUpdate = false;
		
		// Check if employee already exists by temp_payroll_id
		if (onboardingDTO.getBasicInfo() != null && 
				onboardingDTO.getBasicInfo().getTempPayrollId() != null && 
				!onboardingDTO.getBasicInfo().getTempPayrollId().trim().isEmpty()) {
			Optional<Employee> existingEmployee = employeeRepository.findByTemp_payroll_id(
					onboardingDTO.getBasicInfo().getTempPayrollId().trim());
			if (existingEmployee.isPresent()) {
				employee = existingEmployee.get();
				isUpdate = true;
				logger.info("🔄 UPDATE MODE: Employee with temp_payroll_id '{}' already exists (emp_id: {}). Updating existing employee and child tables.", 
						onboardingDTO.getBasicInfo().getTempPayrollId(), employee.getEmp_id());
				
				// Check if status is "Back to Campus" - update to "Pending at DO" and clear remarks
				if (employee.getEmp_check_list_status_id() != null) {
					String currentStatus = employee.getEmp_check_list_status_id().getCheck_app_status_name();
					if ("Back to Campus".equals(currentStatus)) {
						EmployeeCheckListStatus pendingAtDOStatus = employeeCheckListStatusRepository.findByCheck_app_status_name("Pending at DO")
								.orElseThrow(() -> new ResourceNotFoundException("EmployeeCheckListStatus with name 'Pending at DO' not found"));
						employee.setEmp_check_list_status_id(pendingAtDOStatus);
						// Clear remarks when re-forwarding from "Back to Campus" to "Pending at DO"
						employee.setRemarks(null);
						logger.info("✅ Updated status from 'Back to Campus' to 'Pending at DO' and cleared remarks for employee (emp_id: {})", employee.getEmp_id());
					}
				}
				
				// Note: We will UPDATE existing child entities instead of deleting them
				// This preserves data integrity and avoids deletion issues
			}
		}
		
		try {
			if (!isUpdate) {
				// INSERT MODE: Create new employee
			employee = prepareEmployeeEntity(onboardingDTO.getBasicInfo());
				logger.info("➕ INSERT MODE: Creating new employee with temp_payroll_id: {}", 
						onboardingDTO.getBasicInfo() != null ? onboardingDTO.getBasicInfo().getTempPayrollId() : "N/A");
			} else {
				// UPDATE MODE: Update existing employee fields
				updateEmployeeEntity(employee, onboardingDTO.getBasicInfo());
				logger.info("🔄 UPDATE MODE: Updating existing employee (emp_id: {})", employee.getEmp_id());
			}
			// Only previous UAN and previous ESI numbers are stored at HR level (not current PF/ESI/UAN)
			EmpPfDetails empPfDetails = prepareEmpPfDetailsEntity(onboardingDTO.getBasicInfo(), employee);
			EmpDetails empDetails = prepareEmpDetailsEntity(onboardingDTO.getBasicInfo(), onboardingDTO.getAddressInfo(), employee);
			java.util.List<EmpaddressInfo> addressEntities = prepareAddressEntities(onboardingDTO.getAddressInfo(), employee);
			java.util.List<EmpFamilyDetails> familyEntities = prepareFamilyEntities(onboardingDTO.getFamilyInfo(), employee);
			java.util.List<EmpExperienceDetails> experienceEntities = prepareExperienceEntities(onboardingDTO.getPreviousEmployerInfo(), employee);
			java.util.List<EmpQualification> qualificationEntities = prepareQualificationEntities(onboardingDTO.getQualification(), employee);
			java.util.List<EmpDocuments> documentEntities = prepareDocumentEntities(onboardingDTO.getDocuments(), employee);
			java.util.List<BankDetails> bankEntities = prepareBankEntities(onboardingDTO.getBankInfo(), employee);
			prepareCategoryInfoUpdates(onboardingDTO.getCategoryInfo(), employee);
			validatePreparedEntities(employee, empDetails, empPfDetails, addressEntities, familyEntities, 
					experienceEntities, qualificationEntities, documentEntities, bankEntities);
			
			if (!isUpdate) {
				// INSERT MODE: Save new employee (generates new emp_id)
			employee = employeeRepository.save(employee);
			logger.info("✅ Employee ID {} generated and consumed from sequence - proceeding with child entity saves", employee.getEmp_id());
			} else {
				// UPDATE MODE: Save existing employee (emp_id already exists)
				employee = employeeRepository.save(employee);
				logger.info("✅ Employee ID {} updated - proceeding with child entity saves", employee.getEmp_id());
			}
			
			// Only previous UAN and previous ESI numbers are stored (not current PF/ESI/UAN)
			if (empPfDetails != null) {
				empPfDetails.setEmployee_id(employee);
				// Update existing or create new
				if (isUpdate) {
					Optional<EmpPfDetails> existingPfDetails = empPfDetailsRepository.findByEmployeeId(employee.getEmp_id());
					if (existingPfDetails.isPresent()) {
						// Update existing record
						EmpPfDetails existing = existingPfDetails.get();
						existing.setPre_uan_no(empPfDetails.getPre_uan_no());
						existing.setPre_esi_no(empPfDetails.getPre_esi_no());
						existing.setIs_active(empPfDetails.getIs_active());
						empPfDetailsRepository.save(existing);
						logger.info("Updated existing EmpPfDetails for employee (emp_id: {})", employee.getEmp_id());
					} else {
						// Create new record
				empPfDetailsRepository.save(empPfDetails);
						logger.info("Created new EmpPfDetails for employee (emp_id: {})", employee.getEmp_id());
					}
				} else {
					// INSERT MODE: Create new
					empPfDetailsRepository.save(empPfDetails);
				}
			}
			
			// Update or create EmpDetails
			empDetails.setEmployee_id(employee);
			if (isUpdate) {
				// First, try to find by emp_id
				Optional<EmpDetails> existingDetails = empDetailsRepository.findById(employee.getEmp_id());
				
				if (existingDetails.isPresent()) {
					// Update existing record - copy all fields from new to existing
					EmpDetails existing = existingDetails.get();
					updateEmpDetailsFields(existing, empDetails);
					empDetailsRepository.save(existing);
					logger.info("Updated existing EmpDetails for employee (emp_id: {})", employee.getEmp_id());
				} else {
					// Not found by emp_id - check by email if email is provided
					if (empDetails.getPersonal_email() != null && !empDetails.getPersonal_email().trim().isEmpty()) {
						Optional<EmpDetails> existingByEmail = empDetailsRepository.findByPersonal_email(empDetails.getPersonal_email().trim());
						
						if (existingByEmail.isPresent()) {
							// Email already exists - update that record (preserve email, update other fields)
							EmpDetails existing = existingByEmail.get();
							// Update all fields except email (preserve existing email to avoid unique constraint violation)
							updateEmpDetailsFieldsExceptEmail(existing, empDetails);
							// Update employee_id to point to current employee
							existing.setEmployee_id(employee);
							empDetailsRepository.save(existing);
							logger.info("Updated existing EmpDetails found by email for employee (emp_id: {}), email: {}", 
									employee.getEmp_id(), empDetails.getPersonal_email());
						} else {
							// Email doesn't exist - create new record
							empDetailsRepository.save(empDetails);
							logger.info("Created new EmpDetails for employee (emp_id: {})", employee.getEmp_id());
						}
					} else {
						// No email provided - create new record
						empDetailsRepository.save(empDetails);
						logger.info("Created new EmpDetails for employee (emp_id: {})", employee.getEmp_id());
					}
				}
			} else {
				// INSERT MODE: Check if email already exists before creating
				if (empDetails.getPersonal_email() != null && !empDetails.getPersonal_email().trim().isEmpty()) {
					Optional<EmpDetails> existingByEmail = empDetailsRepository.findByPersonal_email(empDetails.getPersonal_email().trim());
					
					if (existingByEmail.isPresent()) {
						// Email already exists - update that record (preserve email, update other fields)
						EmpDetails existing = existingByEmail.get();
						// Update all fields except email (preserve existing email to avoid unique constraint violation)
						updateEmpDetailsFieldsExceptEmail(existing, empDetails);
						// Update employee_id to point to current employee
						existing.setEmployee_id(employee);
						empDetailsRepository.save(existing);
						logger.info("Updated existing EmpDetails found by email during INSERT for employee (emp_id: {}), email: {}", 
								employee.getEmp_id(), empDetails.getPersonal_email());
					} else {
						// Email doesn't exist - create new record
						empDetailsRepository.save(empDetails);
						logger.info("Created new EmpDetails for employee (emp_id: {})", employee.getEmp_id());
					}
				} else {
					// No email provided - create new record
					empDetailsRepository.save(empDetails);
					logger.info("Created new EmpDetails for employee (emp_id: {})", employee.getEmp_id());
				}
			}
			
			// Update or create list entities (UPDATE mode: overwrite existing, INSERT mode: create new)
			if (isUpdate) {
				// UPDATE MODE: Overwrite existing records
				updateOrCreateAddressEntities(addressEntities, employee, onboardingDTO.getAddressInfo());
				updateOrCreateFamilyEntities(familyEntities, employee);
				updateOrCreateExperienceEntities(experienceEntities, employee);
				updateOrCreateQualificationEntities(qualificationEntities, employee);
				updateOrCreateDocumentEntities(documentEntities, employee);
				updateOrCreateBankEntities(bankEntities, employee);
			} else {
				// INSERT MODE: Create new records
			for (EmpaddressInfo addr : addressEntities) {
				addr.setEmp_id(employee);
					addr.setIs_active(1);
				empaddressInfoRepository.save(addr);
			}
			
			for (EmpFamilyDetails family : familyEntities) {
				family.setEmp_id(employee);
					family.setIs_active(1);
				empFamilyDetailsRepository.save(family);
			}
			
			for (EmpExperienceDetails exp : experienceEntities) {
				try {
					exp.setEmployee_id(employee);
						exp.setIs_active(1);
					empExperienceDetailsRepository.save(exp);
				} catch (Exception e) {
					logger.error("❌ CRITICAL: Failed to save EmpExperienceDetails. Employee ID {} was already consumed. Company: {}, Error: {}", 
							employee.getEmp_id(), exp.getPre_organigation_name(), e.getMessage(), e);
					throw new ResourceNotFoundException("Failed to save Experience Details for company: " + 
							(exp.getPre_organigation_name() != null ? exp.getPre_organigation_name() : "Unknown") + 
							". Error: " + e.getMessage() + ". Note: Employee ID " + employee.getEmp_id() + " was already consumed.");
				}
			}
			
			for (EmpQualification qual : qualificationEntities) {
				qual.setEmp_id(employee);
					qual.setIs_active(1);
				empQualificationRepository.save(qual);
			}
			
			for (EmpDocuments doc : documentEntities) {
				doc.setEmp_id(employee);
					doc.setIs_active(1);
				empDocumentsRepository.save(doc);
				}
				
				for (BankDetails bank : bankEntities) {
					bank.setEmpId(employee);
					bank.setIsActive(1);
					bankDetailsRepository.save(bank);
				}
			}
			
			// Save Family Group Photo as a document if provided
			if (onboardingDTO.getFamilyInfo() != null && onboardingDTO.getFamilyInfo().getFamilyGroupPhotoPath() != null 
					&& !onboardingDTO.getFamilyInfo().getFamilyGroupPhotoPath().trim().isEmpty()) {
				EmpDocuments familyPhotoDoc = createFamilyGroupPhotoDocument(onboardingDTO.getFamilyInfo().getFamilyGroupPhotoPath(), employee);
				empDocumentsRepository.save(familyPhotoDoc);
				logger.info("✅ Family Group Photo saved as document for Employee ID: {}", employee.getEmp_id());
			}
			
			if (onboardingDTO.getQualification() != null && onboardingDTO.getQualification().getQualifications() != null) {
				// Note: Validation for isHighest already done in validateOnboardingData()
				// Find the qualification marked as highest and update Employee.qualification_id
				Integer highestQualificationId = null;
				for (QualificationDTO.QualificationDetailsDTO qualDTO : onboardingDTO.getQualification().getQualifications()) {
					if (Boolean.TRUE.equals(qualDTO.getIsHighest())) {
						highestQualificationId = qualDTO.getQualificationId();
						break;
					}
				}
				if (highestQualificationId != null) {
					employee.setQualification_id(qualificationRepository.findById(highestQualificationId)
							.orElseThrow(() -> new ResourceNotFoundException("Qualification not found")));
					employeeRepository.save(employee);
				}
			}
			
			// Handle EmpSubject - update existing or create new
			if (onboardingDTO.getCategoryInfo() != null && onboardingDTO.getCategoryInfo().getSubjectId() != null 
					&& onboardingDTO.getCategoryInfo().getSubjectId() > 0
					&& onboardingDTO.getCategoryInfo().getAgreedPeriodsPerWeek() != null) {
				if (isUpdate) {
					// Update existing or create new
					int empId = employee.getEmp_id();
					List<EmpSubject> existingEmpSubjects = empSubjectRepository.findAll().stream()
							.filter(es -> es.getEmp_id() != null && es.getEmp_id().getEmp_id() == empId)
							.collect(Collectors.toList());
					
					if (!existingEmpSubjects.isEmpty()) {
						// Update first existing record
						EmpSubject existing = existingEmpSubjects.get(0);
						existing.setSubject_id(subjectRepository.findById(onboardingDTO.getCategoryInfo().getSubjectId())
								.orElseThrow(() -> new ResourceNotFoundException("Subject not found with ID: " + onboardingDTO.getCategoryInfo().getSubjectId())));
						existing.setAgree_no_period(onboardingDTO.getCategoryInfo().getAgreedPeriodsPerWeek());
						existing.setIs_active(1);
						empSubjectRepository.save(existing);
						logger.info("Updated existing EmpSubject for employee (emp_id: {})", employee.getEmp_id());
						
						// Mark other existing records as inactive if there are multiple
						if (existingEmpSubjects.size() > 1) {
							for (int i = 1; i < existingEmpSubjects.size(); i++) {
								existingEmpSubjects.get(i).setIs_active(0);
								empSubjectRepository.save(existingEmpSubjects.get(i));
							}
							logger.info("Marked {} additional EmpSubject records as inactive for employee (emp_id: {})", 
									existingEmpSubjects.size() - 1, employee.getEmp_id());
						}
					} else {
						// Create new record
				EmpSubject empSubject = new EmpSubject();
				empSubject.setEmp_id(employee);
				empSubject.setSubject_id(subjectRepository.findById(onboardingDTO.getCategoryInfo().getSubjectId())
						.orElseThrow(() -> new ResourceNotFoundException("Subject not found with ID: " + onboardingDTO.getCategoryInfo().getSubjectId())));
				empSubject.setAgree_no_period(onboardingDTO.getCategoryInfo().getAgreedPeriodsPerWeek());
				empSubject.setClass_id(null);
				empSubject.setIs_active(1);
				empSubjectRepository.save(empSubject);
						logger.info("Created new EmpSubject for employee (emp_id: {})", employee.getEmp_id());
					}
				} else {
					// INSERT MODE: Create new
					EmpSubject empSubject = new EmpSubject();
					empSubject.setEmp_id(employee);
					empSubject.setSubject_id(subjectRepository.findById(onboardingDTO.getCategoryInfo().getSubjectId())
							.orElseThrow(() -> new ResourceNotFoundException("Subject not found with ID: " + onboardingDTO.getCategoryInfo().getSubjectId())));
					empSubject.setAgree_no_period(onboardingDTO.getCategoryInfo().getAgreedPeriodsPerWeek());
					empSubject.setClass_id(null);
					empSubject.setIs_active(1);
					empSubjectRepository.save(empSubject);
				}
			}
			
			
			employeeRepository.save(employee);
			
			// Save Agreement Info and Cheque Details (after employee is saved to get emp_id)
			saveAgreementInfo(onboardingDTO.getAgreementInfo(), employee);
			
			// Save employee again to persist agreement fields
			employeeRepository.save(employee);
			
			if (onboardingDTO.getBasicInfo() != null) {
				onboardingDTO.getBasicInfo().setEmpId(employee.getEmp_id());
			}
			
			return onboardingDTO;
			
		} catch (Exception e) {
			if (employee != null && employee.getEmp_id() > 0) {
				logger.error("❌ ERROR: Employee onboarding failed AFTER ID consumption. Employee ID {} was consumed but transaction rolled back. Root cause: {}", 
						employee.getEmp_id(), e.getMessage(), e);
			} else {
				logger.error("❌ ERROR: Employee onboarding failed DURING PREPARATION. NO ID consumed. Error will be thrown before employee.save(). Root cause: {}", 
						e.getMessage(), e);
			}
			throw e;
		}
	}

	/**
	 * Pre-flight checks: Validate data formats, lengths, and constraints that could cause runtime errors
	 * This catches issues that might cause failures AFTER employee.save() (which would consume emp_id)
	 * Called AFTER validateOnboardingData but BEFORE any database saves
	 */
	private void performPreFlightChecks(EmployeeOnboardingDTO onboardingDTO) {
		if (onboardingDTO == null || onboardingDTO.getBasicInfo() == null) {
			return;
		}
		
		BasicInfoDTO basicInfo = onboardingDTO.getBasicInfo();
		
		if (basicInfo.getFirstName() != null && basicInfo.getFirstName().length() > 50) {
			throw new ResourceNotFoundException("First Name cannot exceed 50 characters");
		}
		if (basicInfo.getLastName() != null && basicInfo.getLastName().length() > 50) {
			throw new ResourceNotFoundException("Last Name cannot exceed 50 characters");
		}
		// Email validation - email goes to EmpDetails.personal_email only (not Employee entity)
		if (basicInfo.getEmail() != null && basicInfo.getEmail().length() > 50) {
			throw new ResourceNotFoundException("Email cannot exceed 50 characters");
		}
		
		String username = (basicInfo.getFirstName() + "." + basicInfo.getLastName()).toLowerCase();
		if (username.length() > 50) {
			username = username.substring(0, 50);
		}
		
		if (onboardingDTO.getAddressInfo() != null) {
			if (onboardingDTO.getAddressInfo().getCurrentAddress() != null) {
				AddressInfoDTO.AddressDTO current = onboardingDTO.getAddressInfo().getCurrentAddress();
				if (current.getPin() != null && current.getPin().length() > 10) {
					throw new ResourceNotFoundException("PIN code cannot exceed 10 characters");
				}
				if (current.getName() != null && current.getName().length() > 50) {
					throw new ResourceNotFoundException("Address name cannot exceed 50 characters");
				}
			}
			if (onboardingDTO.getAddressInfo().getPermanentAddress() != null) {
				AddressInfoDTO.AddressDTO permanent = onboardingDTO.getAddressInfo().getPermanentAddress();
				if (permanent.getPin() != null && permanent.getPin().length() > 10) {
					throw new ResourceNotFoundException("PIN code cannot exceed 10 characters");
				}
				if (permanent.getName() != null && permanent.getName().length() > 50) {
					throw new ResourceNotFoundException("Address name cannot exceed 50 characters");
				}
			}
		}
		
		if (onboardingDTO.getBankInfo() != null) {
			if (onboardingDTO.getBankInfo().getPersonalAccount() != null 
					&& onboardingDTO.getBankInfo().getPersonalAccount().getAccountNo() != null) {
				try {
					Long.parseLong(onboardingDTO.getBankInfo().getPersonalAccount().getAccountNo());
				} catch (NumberFormatException e) {
					throw new ResourceNotFoundException("Personal Account Number must be numeric");
				}
			}
			if (onboardingDTO.getBankInfo().getSalaryAccount() != null 
					&& onboardingDTO.getBankInfo().getSalaryAccount().getAccountNo() != null) {
				try {
					Long.parseLong(onboardingDTO.getBankInfo().getSalaryAccount().getAccountNo());
				} catch (NumberFormatException e) {
					throw new ResourceNotFoundException("Salary Account Number must be numeric");
				}
			}
		}
	}


	/**
	 * Validate all onboarding data BEFORE any database operation
	 * This prevents ID consumption when validation fails
	 * Call this method BEFORE saveBasicInfo to avoid consuming emp_id
	 */
	private void validateOnboardingData(EmployeeOnboardingDTO onboardingDTO) {
		if (onboardingDTO == null) {
			throw new ResourceNotFoundException("Employee onboarding data is required");
		}

		BasicInfoDTO basicInfo = onboardingDTO.getBasicInfo();
		if (basicInfo == null) {
			throw new ResourceNotFoundException("Basic Info is required");
		}

		if (basicInfo.getFirstName() == null || basicInfo.getFirstName().trim().isEmpty()) {
			throw new ResourceNotFoundException("First Name is required");
		}
		if (basicInfo.getLastName() == null || basicInfo.getLastName().trim().isEmpty()) {
			throw new ResourceNotFoundException("Last Name is required");
		}
		if (basicInfo.getDateOfJoin() == null) {
			throw new ResourceNotFoundException("Date of Join is required");
		}
		if (basicInfo.getPrimaryMobileNo() == null || basicInfo.getPrimaryMobileNo() == 0) {
			throw new ResourceNotFoundException("Primary Mobile Number is required");
		}
		// Email is optional - goes to EmpDetails.personal_email only (not Employee entity)
		// Email validation removed - personal_email is nullable in database

		
		if (basicInfo.getGenderId() != null) {
			genderRepository.findById(basicInfo.getGenderId())
				.orElseThrow(() -> new ResourceNotFoundException("Gender not found with ID: " + basicInfo.getGenderId()));
		} else {
			throw new ResourceNotFoundException("Gender ID is required (NOT NULL column)");
		}

		if (basicInfo.getDesignationId() != null) {
			designationRepository.findById(basicInfo.getDesignationId())
				.orElseThrow(() -> new ResourceNotFoundException("Designation not found with ID: " + basicInfo.getDesignationId()));
		} else {
			throw new ResourceNotFoundException("Designation ID is required (NOT NULL column)");
		}

		if (basicInfo.getDepartmentId() != null) {
			departmentRepository.findById(basicInfo.getDepartmentId())
				.orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + basicInfo.getDepartmentId()));
		} else {
			throw new ResourceNotFoundException("Department ID is required (NOT NULL column)");
		}

		if (basicInfo.getCategoryId() != null) {
			categoryRepository.findById(basicInfo.getCategoryId())
				.orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + basicInfo.getCategoryId()));
		} else {
			throw new ResourceNotFoundException("Category ID is required (NOT NULL column)");
		}

		if (basicInfo.getReferenceEmpId() != null && basicInfo.getReferenceEmpId() > 0) {
			employeeRepository.findByIdAndIs_active(basicInfo.getReferenceEmpId(), 1)
				.orElseThrow(() -> new ResourceNotFoundException("Active Reference Employee not found with ID: " + basicInfo.getReferenceEmpId()));
		}

		if (basicInfo.getHiredByEmpId() != null && basicInfo.getHiredByEmpId() > 0) {
			employeeRepository.findByIdAndIs_active(basicInfo.getHiredByEmpId(), 1)
				.orElseThrow(() -> new ResourceNotFoundException("Active Hired By Employee not found with ID: " + basicInfo.getHiredByEmpId()));
		}

		if (basicInfo.getManagerId() != null && basicInfo.getManagerId() > 0) {
			employeeRepository.findByIdAndIs_active(basicInfo.getManagerId(), 1)
				.orElseThrow(() -> new ResourceNotFoundException("Active Manager not found with ID: " + basicInfo.getManagerId()));
		}

		if (basicInfo.getReportingManagerId() != null && basicInfo.getReportingManagerId() > 0) {
			employeeRepository.findByIdAndIs_active(basicInfo.getReportingManagerId(), 1)
				.orElseThrow(() -> new ResourceNotFoundException("Active Reporting Manager not found with ID: " + basicInfo.getReportingManagerId()));
		}

		if (basicInfo.getReplacedByEmpId() != null && basicInfo.getReplacedByEmpId() > 0) {
			employeeRepository.findByIdAndIs_active(basicInfo.getReplacedByEmpId(), 0)
				.orElseThrow(() -> new ResourceNotFoundException("Inactive Replacement Employee not found with ID: " + basicInfo.getReplacedByEmpId() + ". Only inactive employees (is_active = 0) can be used as replacement."));
		}

		if (basicInfo.getCampusId() != null && basicInfo.getCampusId() > 0) {
			campusRepository.findByCampusIdAndIsActive(basicInfo.getCampusId(), 1)
				.orElseThrow(() -> new ResourceNotFoundException("Active Campus not found with ID: " + basicInfo.getCampusId()));
		}

		if (basicInfo.getEmpTypeId() != null) {
			employeeTypeRepository.findById(basicInfo.getEmpTypeId())
				.orElseThrow(() -> new ResourceNotFoundException("Employee Type not found with ID: " + basicInfo.getEmpTypeId()));
		}

		// Note: qualificationId is NOT in basicInfo DTO - it will be set automatically from qualification tab's highest qualification

		if (basicInfo.getEmpWorkModeId() != null) {
			workingModeRepository.findById(basicInfo.getEmpWorkModeId())
				.orElseThrow(() -> new ResourceNotFoundException("Working Mode not found with ID: " + basicInfo.getEmpWorkModeId()));
		}

		if (basicInfo.getJoinTypeId() != null) {
			joiningAsRepository.findById(basicInfo.getJoinTypeId())
				.orElseThrow(() -> new ResourceNotFoundException("Join Type not found with ID: " + basicInfo.getJoinTypeId()));
			
			// If joinTypeId = 3 (Replacement), replacedByEmpId is MANDATORY
			if (basicInfo.getJoinTypeId() == 3) {
				if (basicInfo.getReplacedByEmpId() == null || basicInfo.getReplacedByEmpId() <= 0) {
					throw new ResourceNotFoundException(
							"replacedByEmpId is required when joinTypeId is 3 (Replacement). Please provide a valid replacement employee ID.");
				}
			}
		}

		if (basicInfo.getModeOfHiringId() != null) {
			modeOfHiringRepository.findById(basicInfo.getModeOfHiringId())
				.orElseThrow(() -> new ResourceNotFoundException("Mode of Hiring not found with ID: " + basicInfo.getModeOfHiringId()));
		}

		// Validate tempPayrollId against SkillTestDetl table if provided
		if (basicInfo.getTempPayrollId() != null && !basicInfo.getTempPayrollId().trim().isEmpty()) {
			skillTestDetlRepository.findByTempPayrollId(basicInfo.getTempPayrollId())
				.orElseThrow(() -> new ResourceNotFoundException("Temp Payroll ID not found in Skill Test Details: " + basicInfo.getTempPayrollId() + ". Please provide a valid temp payroll ID."));
			
			// Note: We no longer throw error if temp_payroll_id already exists
			// The onboardEmployee method will handle both INSERT (new employee) and UPDATE (existing employee) cases
		}

		if (onboardingDTO.getAddressInfo() != null) {
			AddressInfoDTO addressInfo = onboardingDTO.getAddressInfo();
			
			if (addressInfo.getCurrentAddress() != null) {
				if (addressInfo.getCurrentAddress().getCityId() != null) {
					cityRepository.findById(addressInfo.getCurrentAddress().getCityId())
						.orElseThrow(() -> new ResourceNotFoundException("City not found with ID: " + addressInfo.getCurrentAddress().getCityId()));
				}
				if (addressInfo.getCurrentAddress().getStateId() != null) {
					stateRepository.findById(addressInfo.getCurrentAddress().getStateId())
						.orElseThrow(() -> new ResourceNotFoundException("State not found with ID: " + addressInfo.getCurrentAddress().getStateId()));
				}
				if (addressInfo.getCurrentAddress().getCountryId() != null) {
					countryRepository.findById(addressInfo.getCurrentAddress().getCountryId())
						.orElseThrow(() -> new ResourceNotFoundException("Country not found with ID: " + addressInfo.getCurrentAddress().getCountryId()));
				}
			}
		}

		if (onboardingDTO.getAddressInfo() != null && onboardingDTO.getAddressInfo().getPermanentAddress() != null) {
			AddressInfoDTO addressInfo = onboardingDTO.getAddressInfo();
			if (addressInfo.getPermanentAddress().getCityId() != null) {
				cityRepository.findById(addressInfo.getPermanentAddress().getCityId())
					.orElseThrow(() -> new ResourceNotFoundException("Permanent Address City not found with ID: " + addressInfo.getPermanentAddress().getCityId()));
			}
			if (addressInfo.getPermanentAddress().getStateId() != null) {
				stateRepository.findById(addressInfo.getPermanentAddress().getStateId())
					.orElseThrow(() -> new ResourceNotFoundException("Permanent Address State not found with ID: " + addressInfo.getPermanentAddress().getStateId()));
			}
			if (addressInfo.getPermanentAddress().getCountryId() != null) {
				countryRepository.findById(addressInfo.getPermanentAddress().getCountryId())
					.orElseThrow(() -> new ResourceNotFoundException("Permanent Address Country not found with ID: " + addressInfo.getPermanentAddress().getCountryId()));
			}
		}

		// Validate Family Group Photo document type if provided
		if (onboardingDTO.getFamilyInfo() != null && onboardingDTO.getFamilyInfo().getFamilyGroupPhotoPath() != null 
				&& !onboardingDTO.getFamilyInfo().getFamilyGroupPhotoPath().trim().isEmpty()) {
			// Validate that Family Group Photo document type exists by name
			empDocTypeRepository.findByDocNameAndIsActive("Family Group Photo", 1)
				.orElseThrow(() -> new ResourceNotFoundException("Family Group Photo document type not found or inactive in document type master. Please ensure the document type is configured and active."));
		}

		if (onboardingDTO.getFamilyInfo() != null && onboardingDTO.getFamilyInfo().getFamilyMembers() != null) {
			for (FamilyInfoDTO.FamilyMemberDTO member : onboardingDTO.getFamilyInfo().getFamilyMembers()) {
				if (member == null) continue;
				
				if (member.getRelationId() == null) {
					throw new ResourceNotFoundException("Relation ID is required for family member: " + member.getFirstName());
				}
				relationRepository.findById(member.getRelationId())
					.orElseThrow(() -> new ResourceNotFoundException("Relation not found with ID: " + member.getRelationId() + " for family member: " + member.getFirstName()));
				
				// Gender validation: For Father (relationId=1) and Mother (relationId=2), gender is auto-set by backend
				// For other relations, genderId is required from DTO
				if (member.getRelationId() != 1 && member.getRelationId() != 2) {
					if (member.getGenderId() == null) {
						throw new ResourceNotFoundException("Gender ID is required for family member: " + member.getFirstName() + 
								" (relationId: " + member.getRelationId() + "). Gender is only auto-set for Father and Mother.");
					}
					genderRepository.findById(member.getGenderId())
						.orElseThrow(() -> new ResourceNotFoundException("Gender not found with ID: " + member.getGenderId() + " for family member: " + member.getFirstName()));
				}
				// Note: For Father (relationId=1) and Mother (relationId=2), genderId from DTO is ignored - backend auto-sets it
				
				if (member.getBloodGroupId() == null) {
					throw new ResourceNotFoundException("Blood Group ID is required for family member: " + member.getFirstName());
				}
				bloodGroupRepository.findByIdAndIsActive(member.getBloodGroupId(), 1)
					.orElseThrow(() -> new ResourceNotFoundException("Active Blood Group not found with ID: " + member.getBloodGroupId() + " for family member: " + member.getFirstName()));
				
				if (member.getNationality() == null || member.getNationality().trim().isEmpty()) {
					throw new ResourceNotFoundException("Nationality is required for family member: " + member.getFirstName());
				}
				
				if (member.getOccupation() == null || member.getOccupation().trim().isEmpty()) {
					throw new ResourceNotFoundException("Occupation is required for family member: " + member.getFirstName());
				}
			}
		}

		if (onboardingDTO.getPreviousEmployerInfo() != null && onboardingDTO.getPreviousEmployerInfo().getPreviousEmployers() != null) {
			for (PreviousEmployerInfoDTO.EmployerDetailsDTO employer : onboardingDTO.getPreviousEmployerInfo().getPreviousEmployers()) {
				if (employer == null) continue;
				
				if (employer.getCompanyName() == null || employer.getCompanyName().trim().isEmpty()) {
					throw new ResourceNotFoundException("Company Name is required for previous employer");
				}
				if (employer.getFromDate() == null) {
					throw new ResourceNotFoundException("From Date is required for previous employer: " + employer.getCompanyName());
				}
				if (employer.getToDate() == null) {
					throw new ResourceNotFoundException("To Date is required for previous employer: " + employer.getCompanyName());
				}
				if (employer.getDesignation() == null || employer.getDesignation().trim().isEmpty()) {
					throw new ResourceNotFoundException("Designation is required for previous employer: " + employer.getCompanyName());
				}
				if (employer.getLeavingReason() == null || employer.getLeavingReason().trim().isEmpty()) {
					throw new ResourceNotFoundException("Leaving Reason is required for previous employer: " + employer.getCompanyName());
				}
				if (employer.getNatureOfDuties() == null || employer.getNatureOfDuties().trim().isEmpty()) {
					throw new ResourceNotFoundException("Nature of Duties is required for previous employer: " + employer.getCompanyName());
				}
				if (employer.getCompanyAddressLine1() == null || employer.getCompanyAddressLine1().trim().isEmpty()) {
					throw new ResourceNotFoundException("Company Address Line 1 is required for previous employer: " + employer.getCompanyName());
				}
			}
		}

		if (onboardingDTO.getQualification() != null && onboardingDTO.getQualification().getQualifications() != null) {
			// Validate: Only ONE qualification should have isHighest = true
			long highestCount = onboardingDTO.getQualification().getQualifications().stream()
					.filter(qual -> qual != null && Boolean.TRUE.equals(qual.getIsHighest()))
					.count();
			
			if (highestCount > 1) {
				throw new IllegalArgumentException(
						"Only one qualification can be marked as highest. Found " + highestCount + " qualifications with isHighest = true.");
			}
			
			for (QualificationDTO.QualificationDetailsDTO qual : onboardingDTO.getQualification().getQualifications()) {
				if (qual == null) continue;
				
				if (qual.getQualificationId() != null) {
					qualificationRepository.findById(qual.getQualificationId())
						.orElseThrow(() -> new ResourceNotFoundException("Qualification not found with ID: " + qual.getQualificationId()));
				}
				if (qual.getQualificationDegreeId() != null) {
					qualificationDegreeRepository.findById(qual.getQualificationDegreeId())
						.orElseThrow(() -> new ResourceNotFoundException("Qualification Degree not found with ID: " + qual.getQualificationDegreeId()));
				}
			}
		}

		if (onboardingDTO.getDocuments() != null && onboardingDTO.getDocuments().getDocuments() != null) {
			for (DocumentDTO.DocumentDetailsDTO doc : onboardingDTO.getDocuments().getDocuments()) {
				if (doc == null) continue;
				
				if (doc.getDocTypeId() == null) {
					throw new ResourceNotFoundException("Document Type ID is required for document");
				}
				empDocTypeRepository.findById(doc.getDocTypeId())
					.orElseThrow(() -> new ResourceNotFoundException("Document Type not found with ID: " + doc.getDocTypeId()));
			}
		}

		if (onboardingDTO.getCategoryInfo() != null) {
			CategoryInfoDTO categoryInfo = onboardingDTO.getCategoryInfo();
			
			if (categoryInfo.getEmployeeTypeId() != null) {
				employeeTypeRepository.findById(categoryInfo.getEmployeeTypeId())
					.orElseThrow(() -> new ResourceNotFoundException("Employee Type not found with ID: " + categoryInfo.getEmployeeTypeId()));
			}
			if (categoryInfo.getDepartmentId() != null) {
				departmentRepository.findById(categoryInfo.getDepartmentId())
					.orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + categoryInfo.getDepartmentId()));
			}
			if (categoryInfo.getDesignationId() != null) {
				designationRepository.findById(categoryInfo.getDesignationId())
					.orElseThrow(() -> new ResourceNotFoundException("Designation not found with ID: " + categoryInfo.getDesignationId()));
			}
			if (categoryInfo.getSubjectId() != null && categoryInfo.getSubjectId() > 0) {
				subjectRepository.findById(categoryInfo.getSubjectId())
					.orElseThrow(() -> new ResourceNotFoundException("Subject not found with ID: " + categoryInfo.getSubjectId()));
			}
		}

		if (onboardingDTO.getBankInfo() != null) {
			BankInfoDTO bankInfo = onboardingDTO.getBankInfo();
			
			if (bankInfo.getPaymentTypeId() != null && bankInfo.getPaymentTypeId() > 0) {
				empPaymentTypeRepository.findByIdAndIsActive(bankInfo.getPaymentTypeId(), 1)
					.orElseThrow(() -> new ResourceNotFoundException("Active Payment Type not found with ID: " + bankInfo.getPaymentTypeId()));
			}
			
			if (bankInfo.getPersonalAccount() != null) {
				if (bankInfo.getPersonalAccount().getAccountNo() == null || bankInfo.getPersonalAccount().getAccountNo().trim().isEmpty()) {
					throw new ResourceNotFoundException("Personal Account Number is required");
				}
				try {
					Long.parseLong(bankInfo.getPersonalAccount().getAccountNo());
				} catch (NumberFormatException e) {
					throw new ResourceNotFoundException("Personal Account Number must be numeric");
				}
				if (bankInfo.getPersonalAccount().getIfscCode() == null || bankInfo.getPersonalAccount().getIfscCode().trim().isEmpty()) {
					throw new ResourceNotFoundException("Personal Account IFSC Code is required");
				}
				if (bankInfo.getPersonalAccount().getAccountHolderName() == null || bankInfo.getPersonalAccount().getAccountHolderName().trim().isEmpty()) {
					throw new ResourceNotFoundException("Personal Account Holder Name is required");
				}
			}
			
			if (bankInfo.getSalaryAccount() != null) {
				if (bankInfo.getBankId() != null && bankInfo.getBankId() > 0) {
					orgBankRepository.findById(bankInfo.getBankId())
						.orElseThrow(() -> new ResourceNotFoundException("Organization Bank not found with ID: " + bankInfo.getBankId()));
				}
				if (bankInfo.getBankBranchId() != null && bankInfo.getBankBranchId() > 0) {
					orgBankBranchRepository.findById(bankInfo.getBankBranchId())
						.orElseThrow(() -> new ResourceNotFoundException("Organization Bank Branch not found with ID: " + bankInfo.getBankBranchId()));
				}
				if (bankInfo.getSalaryAccount().getAccountNo() == null || bankInfo.getSalaryAccount().getAccountNo().trim().isEmpty()) {
					throw new ResourceNotFoundException("Salary Account Number is required");
				}
				try {
					Long.parseLong(bankInfo.getSalaryAccount().getAccountNo());
				} catch (NumberFormatException e) {
					throw new ResourceNotFoundException("Salary Account Number must be numeric");
				}
				if (bankInfo.getSalaryAccount().getIfscCode() == null || bankInfo.getSalaryAccount().getIfscCode().trim().isEmpty()) {
					throw new ResourceNotFoundException("Salary Account IFSC Code is required");
				}
				if (bankInfo.getSalaryAccount().getAccountHolderName() == null || bankInfo.getSalaryAccount().getAccountHolderName().trim().isEmpty()) {
					throw new ResourceNotFoundException("Salary Account Holder Name is required");
				}
			}
		}
	}

	// ============================================================================
	// PREPARATION METHODS: Create entities WITHOUT saving (to prevent ID consumption)
	// ============================================================================
	
	/**
	 * Prepare Employee entity WITHOUT saving (no ID consumed)
	 * This allows validation before ID generation
	 */
	private Employee prepareEmployeeEntity(BasicInfoDTO basicInfo) {
		if (basicInfo == null) {
			throw new ResourceNotFoundException("Basic Info is required");
		}

		Employee employee = new Employee();
		employee.setFirst_name(basicInfo.getFirstName());
		employee.setLast_name(basicInfo.getLastName());
		employee.setDate_of_join(basicInfo.getDateOfJoin());
		employee.setPrimary_mobile_no(basicInfo.getPrimaryMobileNo());
		// Email is not set to Employee entity - it goes to EmpDetails.personal_email only
		employee.setEmail(null);
		
		// String username = (basicInfo.getFirstName() + "." + basicInfo.getLastName()).toLowerCase();
		// if (username.length() > 50) {
		// 	username = username.substring(0, 50);
		// }
		// employee.setUser_name(username);
		// employee.setPassword("Temp@123");
		// employee.setPassout_year(0);
		
		if (basicInfo.getTotalExperience() != null) {
			employee.setTotal_experience(basicInfo.getTotalExperience().doubleValue());
		}
		
		employee.setIs_active(1);
		employee.setStatus("ACTIVE");

		// Set tempPayrollId if provided (validated in validateOnboardingData)
		if (basicInfo.getTempPayrollId() != null && !basicInfo.getTempPayrollId().trim().isEmpty()) {
			employee.setTemp_payroll_id(basicInfo.getTempPayrollId());
		}

		if (basicInfo.getCreatedBy() != null && basicInfo.getCreatedBy() > 0) {
			employee.setCreated_by(basicInfo.getCreatedBy());
		} else {
			employee.setCreated_by(1);
		}
		if (basicInfo.getCampusId() != null) {
			employee.setCampus_id(campusRepository.findByCampusIdAndIsActive(basicInfo.getCampusId(), 1)
					.orElseThrow(() -> new ResourceNotFoundException("Active Campus not found with ID: " + basicInfo.getCampusId())));
		}

		if (basicInfo.getGenderId() != null) {
			employee.setGender(genderRepository.findByIdAndIsActive(basicInfo.getGenderId(), 1)
					.orElseThrow(() -> new ResourceNotFoundException("Active Gender not found")));
		}
		if (basicInfo.getDesignationId() != null) {
			employee.setDesignation(designationRepository.findByIdAndIsActive(basicInfo.getDesignationId(), 1)
					.orElseThrow(() -> new ResourceNotFoundException("Active Designation not found")));
		}
		if (basicInfo.getDepartmentId() != null) {
			employee.setDepartment(departmentRepository.findByIdAndIsActive(basicInfo.getDepartmentId(), 1)
					.orElseThrow(() -> new ResourceNotFoundException("Active Department not found")));
		}
		if (basicInfo.getCategoryId() != null) {
			employee.setCategory(categoryRepository.findByIdAndIsActive(basicInfo.getCategoryId(), 1)
					.orElseThrow(() -> new ResourceNotFoundException("Active Category not found")));
		}
		if (basicInfo.getEmpTypeId() != null) {
			employee.setEmployee_type_id(employeeTypeRepository.findByIdAndIsActive(basicInfo.getEmpTypeId(), 1)
					.orElseThrow(() -> new ResourceNotFoundException("Active EmployeeType not found")));
		}
		if (basicInfo.getEmpWorkModeId() != null) {
			employee.setWorkingMode_id(workingModeRepository.findByIdAndIsActive(basicInfo.getEmpWorkModeId(), 1)
					.orElseThrow(() -> new ResourceNotFoundException("Active WorkingMode not found")));
		}
		if (basicInfo.getJoinTypeId() != null) {
			employee.setJoin_type_id(joiningAsRepository.findByIdAndIsActive(basicInfo.getJoinTypeId(), 1)
					.orElseThrow(() -> new ResourceNotFoundException("Active JoiningAs not found")));
			
			// If joinTypeId = 3 (Replacement), replacedByEmpId is MANDATORY
			if (basicInfo.getJoinTypeId() == 3) {
				if (basicInfo.getReplacedByEmpId() == null || basicInfo.getReplacedByEmpId() <= 0) {
					throw new ResourceNotFoundException(
							"replacedByEmpId is required when joinTypeId is 3 (Replacement). Please provide a valid replacement employee ID.");
				}
			}
			
			if (basicInfo.getJoinTypeId() == 4) {
				if (basicInfo.getContractStartDate() != null) {
					employee.setContract_start_date(basicInfo.getContractStartDate());
				} else {
					employee.setContract_start_date(basicInfo.getDateOfJoin());
				}
				
				if (basicInfo.getContractEndDate() != null) {
					employee.setContract_end_date(basicInfo.getContractEndDate());
				} else {
					java.sql.Date startDate = basicInfo.getContractStartDate() != null ? 
							basicInfo.getContractStartDate() : basicInfo.getDateOfJoin();
					long oneYearInMillis = 365L * 24 * 60 * 60 * 1000;
					java.util.Date endDateUtil = new java.util.Date(startDate.getTime() + oneYearInMillis);
					employee.setContract_end_date(new java.sql.Date(endDateUtil.getTime()));
				}
			}
		}
		if (basicInfo.getModeOfHiringId() != null) {
			employee.setModeOfHiring_id(modeOfHiringRepository.findByIdAndIsActive(basicInfo.getModeOfHiringId(), 1)
					.orElseThrow(() -> new ResourceNotFoundException("Active ModeOfHiring not found")));
		}
		
		// Note: qualification_id is NOT set from basicInfo - it will be set automatically from qualification tab's highest qualification
		// See lines 287-302 where it's set based on isHighest flag
		
		// IMPORTANT: Set initial app status to "Pending at DO" when employee is onboarded
		// This is the ONLY status that should be set during onboarding - no other status is allowed
		// All new employees must start with "Pending at DO" status
		EmployeeCheckListStatus pendingAtDOStatus = employeeCheckListStatusRepository.findByCheck_app_status_name("Pending at DO")
				.orElseThrow(() -> new ResourceNotFoundException("EmployeeCheckListStatus with name 'Pending at DO' not found"));
		employee.setEmp_check_list_status_id(pendingAtDOStatus);
		logger.info("Set initial app status to 'Pending at DO' (ID: {}) for new employee (emp_id: {}) - This is the only status allowed during onboarding", 
				pendingAtDOStatus.getEmp_app_status_id(), employee.getEmp_id());
		if (basicInfo.getReferenceEmpId() != null) {
			employee.setEmployee_reference(employeeRepository.findByIdAndIs_active(basicInfo.getReferenceEmpId(), 1)
					.orElseThrow(() -> new ResourceNotFoundException("Active Reference Employee not found with ID: " + basicInfo.getReferenceEmpId())));
		}
		if (basicInfo.getHiredByEmpId() != null) {
			employee.setEmployee_hired(employeeRepository.findByIdAndIs_active(basicInfo.getHiredByEmpId(), 1)
					.orElseThrow(() -> new ResourceNotFoundException("Active Hired By Employee not found with ID: " + basicInfo.getHiredByEmpId())));
		}
		if (basicInfo.getManagerId() != null) {
			employee.setEmployee_manager_id(employeeRepository.findByIdAndIs_active(basicInfo.getManagerId(), 1)
					.orElseThrow(() -> new ResourceNotFoundException("Active Manager not found with ID: " + basicInfo.getManagerId())));
		}
		if (basicInfo.getReportingManagerId() != null) {
			employee.setEmployee_reporting_id(employeeRepository.findByIdAndIs_active(basicInfo.getReportingManagerId(), 1)
					.orElseThrow(() -> new ResourceNotFoundException("Active Reporting Manager not found with ID: " + basicInfo.getReportingManagerId())));
		}
		// Handle replacedByEmpId - mandatory if joinTypeId = 3, optional otherwise
		// Only inactive employees (is_active = 0) can be used as replacement
		if (basicInfo.getJoinTypeId() != null && basicInfo.getJoinTypeId() == 3) {
			// For joinTypeId = 3 (Replacement), replacedByEmpId is MANDATORY
			if (basicInfo.getReplacedByEmpId() == null || basicInfo.getReplacedByEmpId() <= 0) {
				throw new ResourceNotFoundException(
						"replacedByEmpId is required when joinTypeId is 3 (Replacement). Please provide a valid replacement employee ID.");
			}
			employee.setEmployee_replaceby_id(employeeRepository.findByIdAndIs_active(basicInfo.getReplacedByEmpId(), 0)
					.orElseThrow(() -> new ResourceNotFoundException(
							"Inactive Replacement Employee not found with ID: " + basicInfo.getReplacedByEmpId() + 
							". Only inactive employees (is_active = 0) can be used as replacement when joinTypeId is 3 (Replacement).")));
		} else {
			// For other join types, replacedByEmpId is optional
			// Only inactive employees (is_active = 0) can be used as replacement
			if (basicInfo.getReplacedByEmpId() != null && basicInfo.getReplacedByEmpId() > 0) {
				employee.setEmployee_replaceby_id(employeeRepository.findByIdAndIs_active(basicInfo.getReplacedByEmpId(), 0)
						.orElse(null));
			} else {
				employee.setEmployee_replaceby_id(null);
			}
		}

		return employee;
	}
	
	/**
	 * Prepare EmpPfDetails entity WITHOUT saving
	 * Only stores previous UAN number and previous ESI number (not current PF/ESI/UAN)
	 */
	private EmpPfDetails prepareEmpPfDetailsEntity(BasicInfoDTO basicInfo, Employee employee) {
		if (basicInfo == null) return null;
		
		// Only check for previous UAN and previous ESI numbers
		if (basicInfo.getPreUanNum() == null && basicInfo.getPreEsiNum() == null) {
			return null;
		}
		
		EmpPfDetails empPfDetails = new EmpPfDetails();
		empPfDetails.setEmployee_id(employee);
		
		// Only store previous UAN and previous ESI numbers
		empPfDetails.setPre_uan_no(basicInfo.getPreUanNum());
		empPfDetails.setPre_esi_no(basicInfo.getPreEsiNum());
		
		// Do NOT store current PF/ESI/UAN numbers at HR level
		// empPfDetails.setPf_no(basicInfo.getPfNo());
		// empPfDetails.setPf_join_date(basicInfo.getPfJoinDate());
		// empPfDetails.setUan_no(basicInfo.getUanNo());
		// empPfDetails.setEsi_no(basicInfo.getEsiNo());
		
		empPfDetails.setIs_active(1);
		
		return empPfDetails;
	}
	
	/**
	 * Prepare EmpDetails entity WITHOUT saving
	 */
	private EmpDetails prepareEmpDetailsEntity(BasicInfoDTO basicInfo, AddressInfoDTO addressInfo, Employee employee) {
		if (basicInfo == null) {
			throw new ResourceNotFoundException("Basic Info is required");
		}
		
		EmpDetails empDetails = new EmpDetails();
		empDetails.setEmployee_id(employee);
		empDetails.setAdhaar_name(basicInfo.getAdhaarName());
		empDetails.setDate_of_birth(basicInfo.getDateOfBirth());
		empDetails.setPersonal_email(basicInfo.getEmail());
		
		// Set emergency contact phone number from basicInfo (REQUIRED NOT NULL)
		if (basicInfo.getEmergencyPhNo() == null || basicInfo.getEmergencyPhNo().trim().isEmpty()) {
			throw new ResourceNotFoundException("Emergency contact phone number (emergencyPhNo) is required (NOT NULL column)");
		}
		empDetails.setEmergency_ph_no(basicInfo.getEmergencyPhNo().trim());
		
		// Set emergency contact relation ID from basicInfo (optional)
		if (basicInfo.getEmergencyRelationId() != null && basicInfo.getEmergencyRelationId() > 0) {
			empDetails.setRelation_id(relationRepository.findById(basicInfo.getEmergencyRelationId())
					.orElseThrow(() -> new ResourceNotFoundException("Emergency Relation not found with ID: " + basicInfo.getEmergencyRelationId())));
		} else {
			empDetails.setRelation_id(null); // Optional - can be null
		}
		empDetails.setAdhaar_no(basicInfo.getAadharNum());
		empDetails.setPancard_no(basicInfo.getPancardNum());
		empDetails.setAdhaar_enrolment_no(basicInfo.getAadharEnrolmentNum());
		empDetails.setPassout_year(0);
		empDetails.setIs_active(1);
		empDetails.setStatus("ACTIVE");

		if (basicInfo.getBloodGroupId() == null) {
			throw new ResourceNotFoundException("BloodGroup ID is required (NOT NULL column)");
		}
		empDetails.setBloodGroup_id(bloodGroupRepository.findByIdAndIsActive(basicInfo.getBloodGroupId(), 1)
				.orElseThrow(() -> new ResourceNotFoundException("Active BloodGroup not found with ID: " + basicInfo.getBloodGroupId())));

		if (basicInfo.getCasteId() == null) {
			throw new ResourceNotFoundException("Caste ID is required (NOT NULL column)");
		}
		empDetails.setCaste_id(casteRepository.findById(basicInfo.getCasteId())
				.orElseThrow(() -> new ResourceNotFoundException("Caste not found with ID: " + basicInfo.getCasteId())));

		if (basicInfo.getReligionId() == null) {
			throw new ResourceNotFoundException("Religion ID is required (NOT NULL column)");
		}
		empDetails.setReligion_id(relegionRepository.findById(basicInfo.getReligionId())
				.orElseThrow(() -> new ResourceNotFoundException("Religion not found with ID: " + basicInfo.getReligionId())));

		if (basicInfo.getMaritalStatusId() == null) {
			throw new ResourceNotFoundException("MaritalStatus ID is required (NOT NULL column)");
		}
			empDetails.setMarital_status_id(maritalStatusRepository.findByIdAndIsActive(basicInfo.getMaritalStatusId(), 1)
				.orElseThrow(() -> new ResourceNotFoundException("Active MaritalStatus not found with ID: " + basicInfo.getMaritalStatusId())));
		
		return empDetails;
	}
	
	/**
	 * Prepare Address entities WITHOUT saving
	 */
	private java.util.List<EmpaddressInfo> prepareAddressEntities(AddressInfoDTO addressInfo, Employee employee) {
		java.util.List<EmpaddressInfo> addressList = new java.util.ArrayList<>();
		
		if (addressInfo == null) return addressList;
		
		if (addressInfo.getCurrentAddress() != null) {
			EmpaddressInfo currentAddr = createAddressEntity(addressInfo.getCurrentAddress(), employee, "CURR");
			addressList.add(currentAddr);
		}
		
		if (!Boolean.TRUE.equals(addressInfo.getPermanentAddressSameAsCurrent())) {
			if (addressInfo.getPermanentAddress() != null) {
				EmpaddressInfo permanentAddr = createAddressEntity(addressInfo.getPermanentAddress(), employee, "PERM");
				addressList.add(permanentAddr);
			}
		}
		
		return addressList;
	}
	
	/**
	 * Prepare Family entities WITHOUT saving
	 */
	private java.util.List<EmpFamilyDetails> prepareFamilyEntities(FamilyInfoDTO familyInfo, Employee employee) {
		java.util.List<EmpFamilyDetails> familyList = new java.util.ArrayList<>();
		
		if (familyInfo == null || familyInfo.getFamilyMembers() == null || familyInfo.getFamilyMembers().isEmpty()) {
			return familyList;
		}
		
		for (FamilyInfoDTO.FamilyMemberDTO memberDTO : familyInfo.getFamilyMembers()) {
			if (memberDTO != null) {
				EmpFamilyDetails familyMember = createFamilyMemberEntity(memberDTO, employee);
				familyList.add(familyMember);
			}
		}
		
		return familyList;
	}
	
	/**
	 * Prepare Experience entities WITHOUT saving
	 */
	private java.util.List<EmpExperienceDetails> prepareExperienceEntities(PreviousEmployerInfoDTO previousEmployerInfo, Employee employee) {
		java.util.List<EmpExperienceDetails> experienceList = new java.util.ArrayList<>();
		
		if (previousEmployerInfo == null || previousEmployerInfo.getPreviousEmployers() == null 
				|| previousEmployerInfo.getPreviousEmployers().isEmpty()) {
			return experienceList;
		}
		
		for (PreviousEmployerInfoDTO.EmployerDetailsDTO employer : previousEmployerInfo.getPreviousEmployers()) {
			if (employer != null) {
				EmpExperienceDetails experience = createExperienceEntity(employer, employee);
				experienceList.add(experience);
			}
		}
		
		return experienceList;
	}
	
	/**
	 * Helper method to create Experience entity
	 */
	private EmpExperienceDetails createExperienceEntity(PreviousEmployerInfoDTO.EmployerDetailsDTO employerDTO, Employee employee) {
		EmpExperienceDetails experience = new EmpExperienceDetails();
		experience.setEmployee_id(employee);
		
		if (employerDTO.getCompanyName() != null) {
			String companyName = employerDTO.getCompanyName().trim();
			if (companyName.length() > 50) {
				companyName = companyName.substring(0, 50);
			}
			experience.setPre_organigation_name(companyName);
		} else {
			throw new ResourceNotFoundException("Company Name is required (NOT NULL column)");
		}
		
		if (employerDTO.getFromDate() != null) {
			experience.setDate_of_join(employerDTO.getFromDate());
		} else {
			throw new ResourceNotFoundException("From Date is required (NOT NULL column)");
		}
		
		if (employerDTO.getToDate() != null) {
			experience.setDate_of_leave(employerDTO.getToDate());
		} else {
			throw new ResourceNotFoundException("To Date is required (NOT NULL column)");
		}
		
		if (employerDTO.getDesignation() != null) {
			String designation = employerDTO.getDesignation().trim();
			if (designation.length() > 50) {
				designation = designation.substring(0, 50);
			}
			experience.setDesignation(designation);
		} else {
			throw new ResourceNotFoundException("Designation is required (NOT NULL column)");
		}
		
		if (employerDTO.getLeavingReason() != null) {
			String leavingReason = employerDTO.getLeavingReason().trim();
			if (leavingReason.length() > 50) {
				leavingReason = leavingReason.substring(0, 50);
			}
			experience.setLeaving_reason(leavingReason);
		} else {
			throw new ResourceNotFoundException("Leaving Reason is required (NOT NULL column)");
		}
		
		if (employerDTO.getNatureOfDuties() != null) {
			String natureOfDuties = employerDTO.getNatureOfDuties().trim();
			if (natureOfDuties.length() > 50) {
				natureOfDuties = natureOfDuties.substring(0, 50);
			}
			experience.setNature_of_duties(natureOfDuties);
		} else {
			throw new ResourceNotFoundException("Nature of Duties is required (NOT NULL column)");
		}
		
		String companyAddress = employerDTO.getCompanyAddressLine1() != null ? employerDTO.getCompanyAddressLine1() : "";
		if (employerDTO.getCompanyAddressLine2() != null) {
			companyAddress += " " + employerDTO.getCompanyAddressLine2();
		}
		if (companyAddress.trim().isEmpty()) {
			throw new ResourceNotFoundException("Company Address is required (NOT NULL column)");
		}
		String trimmedAddress = companyAddress.trim();
		if (trimmedAddress.length() > 50) {
			trimmedAddress = trimmedAddress.substring(0, 50);
		}
		experience.setCompany_addr(trimmedAddress);
		
		experience.setGross_salary(employerDTO.getGrossSalaryPerMonth() != null ? employerDTO.getGrossSalaryPerMonth() : 0);
		experience.setIs_active(1);
		// If preChaitanyaId is null or 0, set it to null (not 0)
		Integer preChaitanyaId = employerDTO.getPreChaitanyaId();
		experience.setPre_chaitanya_id((preChaitanyaId != null && preChaitanyaId > 0) ? preChaitanyaId : null);
		
		return experience;
	}
	
	/**
	 * Prepare Qualification entities WITHOUT saving
	 */
	private java.util.List<EmpQualification> prepareQualificationEntities(QualificationDTO qualification, Employee employee) {
		java.util.List<EmpQualification> qualificationList = new java.util.ArrayList<>();
		
		if (qualification == null || qualification.getQualifications() == null || qualification.getQualifications().isEmpty()) {
			return qualificationList;
		}
		
		for (QualificationDTO.QualificationDetailsDTO qualDTO : qualification.getQualifications()) {
			if (qualDTO != null) {
				EmpQualification empQual = createQualificationEntity(qualDTO, employee);
				qualificationList.add(empQual);
			}
		}
		
		return qualificationList;
	}
	
	/**
	 * Helper method to create Qualification entity
	 */
	private EmpQualification createQualificationEntity(QualificationDTO.QualificationDetailsDTO qualDTO, Employee employee) {
		EmpQualification empQual = new EmpQualification();
		empQual.setEmp_id(employee);
		empQual.setPassedout_year(qualDTO.getPassedOutYear());
		empQual.setSpecialization(qualDTO.getSpecialization());
		empQual.setUniversity(qualDTO.getUniversity());
		empQual.setInstitute(qualDTO.getInstitute());
		
		if (qualDTO.getQualificationId() != null) {
			empQual.setQualification_id(qualificationRepository.findById(qualDTO.getQualificationId())
					.orElseThrow(() -> new ResourceNotFoundException("Qualification not found")));
		}
		
		if (qualDTO.getQualificationDegreeId() != null) {
			empQual.setQualification_degree_id(qualificationDegreeRepository.findById(qualDTO.getQualificationDegreeId())
					.orElseThrow(() -> new ResourceNotFoundException("QualificationDegree not found")));
		}
		
		empQual.setIs_active(1);
		
		return empQual;
	}
	
	/**
	 * Prepare Document entities WITHOUT saving
	 */
	private java.util.List<EmpDocuments> prepareDocumentEntities(DocumentDTO documents, Employee employee) {
		java.util.List<EmpDocuments> documentList = new java.util.ArrayList<>();
		
		if (documents == null || documents.getDocuments() == null || documents.getDocuments().isEmpty()) {
			return documentList;
		}
		
		for (DocumentDTO.DocumentDetailsDTO docDTO : documents.getDocuments()) {
			if (docDTO != null) {
				EmpDocuments doc = createDocumentEntity(docDTO, employee);
				documentList.add(doc);
			}
		}
		
		return documentList;
	}
	
	/**
	 * Helper method to create Document entity
	 */
	private EmpDocuments createDocumentEntity(DocumentDTO.DocumentDetailsDTO docDTO, Employee employee) {
		EmpDocuments doc = new EmpDocuments();
		doc.setEmp_id(employee);
		doc.setDoc_path(docDTO.getDocPath());
		
		// Set ssc_no ONLY if doc_type_id = 1 (SSC document type)
		if (docDTO.getDocTypeId() != null && docDTO.getDocTypeId() == 1) {
			doc.setSsc_no(docDTO.getSscNo());
		} else {
			doc.setSsc_no(null); // Don't save ssc_no for other document types
		}
		
		doc.setIs_verified(docDTO.getIsVerified() != null && docDTO.getIsVerified() ? 1 : 0);
		doc.setIs_active(1);
		
		if (docDTO.getDocTypeId() != null) {
			doc.setEmp_doc_type_id(empDocTypeRepository.findById(docDTO.getDocTypeId())
					.orElseThrow(() -> new ResourceNotFoundException("DocumentType not found")));
		} else {
			throw new ResourceNotFoundException("Document Type ID is required (NOT NULL column)");
		}
		
		return doc;
	}
	
	/**
	 * Helper method to create Family Group Photo document entity
	 * Family Group Photo is found by document name "Family Group Photo"
	 */
	private EmpDocuments createFamilyGroupPhotoDocument(String familyGroupPhotoPath, Employee employee) {
		EmpDocuments doc = new EmpDocuments();
		doc.setEmp_id(employee);
		doc.setDoc_path(familyGroupPhotoPath);
		doc.setSsc_no(null); // Family Group Photo doesn't have SSC number
		doc.setIs_verified(0); // Default: not verified, HR will verify later
		doc.setIs_active(1);
		
		// Set document type to Family Group Photo by name (doc_name = "Family Group Photo")
		doc.setEmp_doc_type_id(empDocTypeRepository.findByDocNameAndIsActive("Family Group Photo", 1)
				.orElseThrow(() -> new ResourceNotFoundException("Family Group Photo document type not found or inactive in document type master")));
		
		return doc;
	}
	
	/**
	 * Prepare Bank entities WITHOUT saving
	 */
	private java.util.List<BankDetails> prepareBankEntities(BankInfoDTO bankInfo, Employee employee) {
		java.util.List<BankDetails> bankList = new java.util.ArrayList<>();
		
		if (bankInfo == null) return bankList;
		
		EmpPaymentType paymentType = null;
		if (bankInfo.getPaymentTypeId() != null && bankInfo.getPaymentTypeId() > 0) {
			paymentType = empPaymentTypeRepository.findByIdAndIsActive(bankInfo.getPaymentTypeId(), 1)
					.orElseThrow(() -> new ResourceNotFoundException("Active Payment Type not found with ID: " + bankInfo.getPaymentTypeId()));
		}
		
		if (bankInfo.getPersonalAccount() != null) {
			BankDetails personalAccount = new BankDetails();
			personalAccount.setEmpId(employee);
			personalAccount.setAccType("PERSONAL");
			personalAccount.setBankName(bankInfo.getPersonalAccount().getBankName());
			// Bank branch is not required for personal accounts - leaving it null
			personalAccount.setBankBranch(null);
			personalAccount.setBankHolderName(bankInfo.getPersonalAccount().getAccountHolderName());
			personalAccount.setEmpPaymentType(paymentType);
			
			if (bankInfo.getPersonalAccount().getAccountNo() != null) {
				try {
					Long accNoLong = Long.parseLong(bankInfo.getPersonalAccount().getAccountNo());
					personalAccount.setAccNo(accNoLong);
				} catch (NumberFormatException e) {
					throw new ResourceNotFoundException("Invalid account number format. Account number must be numeric.");
				}
			} else {
				throw new ResourceNotFoundException("Account number is required (NOT NULL column)");
			}
			
			if (bankInfo.getPersonalAccount().getIfscCode() != null) {
				personalAccount.setIfscCode(bankInfo.getPersonalAccount().getIfscCode());
			} else {
				throw new ResourceNotFoundException("IFSC Code is required (NOT NULL column)");
			}
			
			personalAccount.setNetPayable(null);
			personalAccount.setIsActive(1);
			bankList.add(personalAccount);
		}
		
		if (bankInfo.getSalaryAccount() != null) {
			BankDetails salaryAccount = new BankDetails();
			salaryAccount.setEmpId(employee);
			salaryAccount.setAccType("SALARY");
			
			if (bankInfo.getBankBranchId() != null && bankInfo.getBankBranchId() > 0) {
				OrgBankBranch orgBankBranch = orgBankBranchRepository.findById(bankInfo.getBankBranchId())
						.orElseThrow(() -> new ResourceNotFoundException("Organization Bank Branch not found with ID: " + bankInfo.getBankBranchId()));
				
				if (orgBankBranch.getBranch_name() != null && !orgBankBranch.getBranch_name().trim().isEmpty()) {
					salaryAccount.setBankBranch(orgBankBranch.getBranch_name());
				}
			}
			
			salaryAccount.setEmpPaymentType(paymentType);
			
			if (bankInfo.getBankId() != null && bankInfo.getBankId() > 0) {
				OrgBank orgBank = orgBankRepository.findById(bankInfo.getBankId())
						.orElseThrow(() -> new ResourceNotFoundException("Organization Bank not found with ID: " + bankInfo.getBankId()));
				
				if (orgBank.getBank_name() != null && !orgBank.getBank_name().trim().isEmpty()) {
					salaryAccount.setBankName(orgBank.getBank_name());
				}
				
				if (bankInfo.getSalaryAccount().getIfscCode() != null && !bankInfo.getSalaryAccount().getIfscCode().trim().isEmpty()) {
					salaryAccount.setIfscCode(bankInfo.getSalaryAccount().getIfscCode());
				} else if (orgBank.getIfsc_code() != null && !orgBank.getIfsc_code().trim().isEmpty()) {
					salaryAccount.setIfscCode(orgBank.getIfsc_code());
				} else {
					throw new ResourceNotFoundException("IFSC Code is required (NOT NULL column). Please provide IFSC code either in salary account or ensure it exists in Organization Bank.");
				}
			} else {
				if (bankInfo.getSalaryAccount().getIfscCode() != null && !bankInfo.getSalaryAccount().getIfscCode().trim().isEmpty()) {
					salaryAccount.setIfscCode(bankInfo.getSalaryAccount().getIfscCode());
				} else {
					throw new ResourceNotFoundException("IFSC Code is required (NOT NULL column)");
				}
			}
			
			if (bankInfo.getSalaryAccount().getAccountHolderName() != null && !bankInfo.getSalaryAccount().getAccountHolderName().trim().isEmpty()) {
				salaryAccount.setBankHolderName(bankInfo.getSalaryAccount().getAccountHolderName());
			} else {
				String employeeName = employee.getFirst_name() + " " + employee.getLast_name();
				salaryAccount.setBankHolderName(employeeName.trim());
			}
			
			if (bankInfo.getSalaryAccount().getAccountNo() != null) {
				try {
					Long accNoLong = Long.parseLong(bankInfo.getSalaryAccount().getAccountNo());
					salaryAccount.setAccNo(accNoLong);
				} catch (NumberFormatException e) {
					throw new ResourceNotFoundException("Invalid account number format. Account number must be numeric.");
				}
			} else {
				throw new ResourceNotFoundException("Account number is required (NOT NULL column)");
			}
			
			salaryAccount.setNetPayable(null);
			salaryAccount.setIsActive(1);
			bankList.add(salaryAccount);
		}
		
		return bankList;
	}
	
	/**
	 * Validate all prepared entities before saving to catch any issues before ID consumption
	 * This is the FINAL check before employee.save() consumes a sequence number
	 */
	private void validatePreparedEntities(Employee employee, EmpDetails empDetails, EmpPfDetails empPfDetails,
			java.util.List<EmpaddressInfo> addressEntities, java.util.List<EmpFamilyDetails> familyEntities,
			java.util.List<EmpExperienceDetails> experienceEntities, java.util.List<EmpQualification> qualificationEntities,
			java.util.List<EmpDocuments> documentEntities, java.util.List<BankDetails> bankEntities) {
		
		if (employee == null) {
			throw new ResourceNotFoundException("Employee entity cannot be null");
		}
		if (employee.getFirst_name() == null || employee.getFirst_name().trim().isEmpty()) {
			throw new ResourceNotFoundException("Employee first name is required");
		}
		if (employee.getLast_name() == null || employee.getLast_name().trim().isEmpty()) {
			throw new ResourceNotFoundException("Employee last name is required");
		}
		// Email validation removed - email is not stored in Employee entity
		if (employee.getCreated_by() == null || employee.getCreated_by() <= 0) {
			throw new ResourceNotFoundException("Employee created_by must be set (NOT NULL column)");
		}
		
		if (empDetails == null) {
			throw new ResourceNotFoundException("EmpDetails entity cannot be null");
		}
		if (empDetails.getCreated_by() == null || empDetails.getCreated_by() <= 0) {
			empDetails.setCreated_by(employee.getCreated_by());
		}
		if (empDetails.getEmergency_ph_no() == null || empDetails.getEmergency_ph_no().trim().isEmpty()) {
			throw new ResourceNotFoundException("EmpDetails: Emergency Phone Number is required (NOT NULL column)");
		}
		
		if (empPfDetails != null && (empPfDetails.getCreated_by() == null || empPfDetails.getCreated_by() <= 0)) {
			empPfDetails.setCreated_by(employee.getCreated_by());
		}
		
		for (EmpaddressInfo addr : addressEntities) {
			if (addr.getEmp_id() == null) {
				throw new ResourceNotFoundException("Address entity must have emp_id reference");
			}
			if (addr.getCreated_by() == null || addr.getCreated_by() <= 0) {
				addr.setCreated_by(employee.getCreated_by());
			}
			if (addr.getCountry_id() == null) {
				throw new ResourceNotFoundException("Address: Country ID is required (NOT NULL column)");
			}
			if (addr.getState_id() == null) {
				throw new ResourceNotFoundException("Address: State ID is required (NOT NULL column)");
			}
			if (addr.getCity_id() == null) {
				throw new ResourceNotFoundException("Address: City ID is required (NOT NULL column)");
			}
		}
		
		for (EmpFamilyDetails family : familyEntities) {
			if (family.getEmp_id() == null) {
				throw new ResourceNotFoundException("Family entity must have emp_id reference");
			}
			if (family.getCreated_by() == null || family.getCreated_by() <= 0) {
				family.setCreated_by(employee.getCreated_by());
			}
			if (family.getFirst_name() == null || family.getFirst_name().trim().isEmpty()) {
				throw new ResourceNotFoundException("Family: First Name is required (NOT NULL column)");
			}
			if (family.getLast_name() == null || family.getLast_name().trim().isEmpty()) {
				throw new ResourceNotFoundException("Family: Last Name is required (NOT NULL column)");
			}
			if (family.getOccupation() == null || family.getOccupation().trim().isEmpty()) {
				throw new ResourceNotFoundException("Family: Occupation is required (NOT NULL column)");
			}
			if (family.getGender_id() == null) {
				throw new ResourceNotFoundException("Family: Gender ID is required (NOT NULL column)");
			}
			if (family.getBlood_group_id() == null) {
				throw new ResourceNotFoundException("Family: Blood Group ID is required (NOT NULL column)");
			}
			if (family.getNationality() == null || family.getNationality().trim().isEmpty()) {
				throw new ResourceNotFoundException("Family: Nationality is required (NOT NULL column)");
			}
			if (family.getRelation_id() == null) {
				throw new ResourceNotFoundException("Family: Relation ID is required (NOT NULL column)");
			}
		}
		
		for (EmpExperienceDetails exp : experienceEntities) {
			if (exp.getEmployee_id() == null) {
				throw new ResourceNotFoundException("Experience entity must have employee_id reference");
			}
			if (exp.getCreated_by() == null || exp.getCreated_by() <= 0) {
				exp.setCreated_by(employee.getCreated_by());
			}
			if (exp.getPre_organigation_name() == null || exp.getPre_organigation_name().trim().isEmpty()) {
				throw new ResourceNotFoundException("Experience: Company Name is required (NOT NULL column)");
			}
			if (exp.getPre_organigation_name() != null && exp.getPre_organigation_name().length() > 50) {
				throw new ResourceNotFoundException("Experience: Company Name cannot exceed 50 characters. Current length: " + exp.getPre_organigation_name().length());
			}
			if (exp.getDate_of_join() == null) {
				throw new ResourceNotFoundException("Experience: From Date is required (NOT NULL column)");
			}
			if (exp.getDate_of_leave() == null) {
				throw new ResourceNotFoundException("Experience: To Date is required (NOT NULL column)");
			}
			if (exp.getDesignation() == null || exp.getDesignation().trim().isEmpty()) {
				throw new ResourceNotFoundException("Experience: Designation is required (NOT NULL column)");
			}
			if (exp.getDesignation() != null && exp.getDesignation().length() > 50) {
				throw new ResourceNotFoundException("Experience: Designation cannot exceed 50 characters. Current length: " + exp.getDesignation().length());
			}
			if (exp.getLeaving_reason() == null || exp.getLeaving_reason().trim().isEmpty()) {
				throw new ResourceNotFoundException("Experience: Leaving Reason is required (NOT NULL column)");
			}
			if (exp.getLeaving_reason() != null && exp.getLeaving_reason().length() > 50) {
				throw new ResourceNotFoundException("Experience: Leaving Reason cannot exceed 50 characters. Current length: " + exp.getLeaving_reason().length());
			}
			if (exp.getNature_of_duties() == null || exp.getNature_of_duties().trim().isEmpty()) {
				throw new ResourceNotFoundException("Experience: Nature of Duties is required (NOT NULL column)");
			}
			if (exp.getNature_of_duties() != null && exp.getNature_of_duties().length() > 50) {
				throw new ResourceNotFoundException("Experience: Nature of Duties cannot exceed 50 characters. Current length: " + exp.getNature_of_duties().length());
			}
			if (exp.getCompany_addr() == null || exp.getCompany_addr().trim().isEmpty()) {
				throw new ResourceNotFoundException("Experience: Company Address is required (NOT NULL column)");
			}
			if (exp.getCompany_addr() != null && exp.getCompany_addr().length() > 50) {
				throw new ResourceNotFoundException("Experience: Company Address cannot exceed 50 characters. Current length: " + exp.getCompany_addr().length());
			}
		}
		
		for (EmpQualification qual : qualificationEntities) {
			if (qual.getEmp_id() == null) {
				throw new ResourceNotFoundException("Qualification entity must have emp_id reference");
			}
			if (qual.getCreated_by() == null || qual.getCreated_by() <= 0) {
				qual.setCreated_by(employee.getCreated_by());
			}
		}
		
		for (EmpDocuments doc : documentEntities) {
			if (doc.getEmp_id() == null) {
				throw new ResourceNotFoundException("Document entity must have emp_id reference");
			}
			if (doc.getCreated_by() == null || doc.getCreated_by() <= 0) {
				doc.setCreated_by(employee.getCreated_by());
			}
		}
		
		for (BankDetails bank : bankEntities) {
			if (bank.getEmpId() == null) {
				throw new ResourceNotFoundException("Bank entity must have emp_id reference");
			}
			if (bank.getCreatedBy() == null || bank.getCreatedBy() <= 0) {
				bank.setCreatedBy(employee.getCreated_by());
			}
			if (bank.getAccType() == null || bank.getAccType().trim().isEmpty()) {
				throw new ResourceNotFoundException("Bank: Account Type is required (NOT NULL column)");
			}
			if (bank.getBankHolderName() == null || bank.getBankHolderName().trim().isEmpty()) {
				throw new ResourceNotFoundException("Bank: Account Holder Name is required (NOT NULL column)");
			}
			if (bank.getAccNo() == null) {
				throw new ResourceNotFoundException("Bank: Account Number is required (NOT NULL column)");
			}
			if (bank.getIfscCode() == null || bank.getIfscCode().trim().isEmpty()) {
				throw new ResourceNotFoundException("Bank: IFSC Code is required (NOT NULL column)");
			}
		}
	}
	
	/**
	 * Prepare Category Info updates WITHOUT saving
	 */
	private void prepareCategoryInfoUpdates(CategoryInfoDTO categoryInfo, Employee employee) {
		if (categoryInfo == null) return;
		
		if (categoryInfo.getEmployeeTypeId() != null) {
			employee.setEmployee_type_id(employeeTypeRepository.findById(categoryInfo.getEmployeeTypeId())
					.orElseThrow(() -> new ResourceNotFoundException("Employee Type not found with ID: " + categoryInfo.getEmployeeTypeId())));
		}
		if (categoryInfo.getDepartmentId() != null) {
			employee.setDepartment(departmentRepository.findById(categoryInfo.getDepartmentId())
					.orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + categoryInfo.getDepartmentId())));
		}
		if (categoryInfo.getDesignationId() != null) {
			employee.setDesignation(designationRepository.findById(categoryInfo.getDesignationId())
					.orElseThrow(() -> new ResourceNotFoundException("Designation not found with ID: " + categoryInfo.getDesignationId())));
		}
		
	}

	private EmpaddressInfo createAddressEntity(AddressInfoDTO.AddressDTO addressDTO, Employee employee, String addressType) {
		EmpaddressInfo address = new EmpaddressInfo();
		address.setAddrs_type(addressType);
		address.setHouse_no(addressDTO.getAddressLine1());
		address.setLandmark(addressDTO.getAddressLine2() + " " + (addressDTO.getAddressLine3() != null ? addressDTO.getAddressLine3() : ""));
		address.setPostal_code(addressDTO.getPin());
		// Note: emrg_contact_no column does NOT exist in sce_emp_addrs table
		// Removed to prevent SQL errors
		// address.setEmrg_contact_no(Long.parseLong(addressDTO.getPhoneNumber() != null ? addressDTO.getPhoneNumber() : "0"));
		address.setIs_active(1);
		
		address.setEmp_id(employee);
		if (addressDTO.getCountryId() != null) {
			address.setCountry_id(countryRepository.findById(addressDTO.getCountryId())
					.orElseThrow(() -> new ResourceNotFoundException("Country not found")));
		} else {
			throw new ResourceNotFoundException("Country ID is required (NOT NULL column)");
		}
		if (addressDTO.getStateId() != null) {
			address.setState_id(stateRepository.findById(addressDTO.getStateId())
					.orElseThrow(() -> new ResourceNotFoundException("State not found")));
		} else {
			throw new ResourceNotFoundException("State ID is required (NOT NULL column)");
		}
		if (addressDTO.getCityId() != null) {
			address.setCity_id(cityRepository.findById(addressDTO.getCityId())
					.orElseThrow(() -> new ResourceNotFoundException("City not found")));
		} else {
			throw new ResourceNotFoundException("City ID is required (NOT NULL column)");
		}
		

		return address;
	}


	private EmpFamilyDetails createFamilyMemberEntity(FamilyInfoDTO.FamilyMemberDTO memberDTO, Employee employee) {
		EmpFamilyDetails familyMember = new EmpFamilyDetails();
		
		familyMember.setEmp_id(employee);
		familyMember.setFirst_name(memberDTO.getFirstName());
		familyMember.setLast_name(memberDTO.getLastName());
		familyMember.setIs_late(memberDTO.getIsLate() != null && memberDTO.getIsLate() ? "Y" : "N");
		familyMember.setOccupation(memberDTO.getOccupation());
		familyMember.setNationality(memberDTO.getNationality());
		familyMember.setIs_active(1);
		familyMember.setDate_of_birth(memberDTO.getDateOfBirth());
		
		if (memberDTO.getIsDependent() != null) {
			familyMember.setIs_dependent(memberDTO.getIsDependent() ? 1 : 0);
		} else {
			familyMember.setIs_dependent(null);
		}

		if (memberDTO.getRelationId() != null) {
			familyMember.setRelation_id(relationRepository.findById(memberDTO.getRelationId())
					.orElseThrow(() -> new ResourceNotFoundException("Relation not found")));
		} else {
			throw new ResourceNotFoundException("Relation ID is required (NOT NULL column)");
		}

		Integer genderIdToUse;
		if (memberDTO.getRelationId() != null) {
			if (memberDTO.getRelationId() == 1) {
				// Father - gender is automatically set to Male (1), ignore genderId from DTO if sent
				genderIdToUse = 1;
				if (memberDTO.getGenderId() != null) {
					logger.debug("Ignoring genderId {} from DTO for Father (relationId=1). Auto-setting to Male (1).", memberDTO.getGenderId());
				}
			} else if (memberDTO.getRelationId() == 2) {
				// Mother - gender is automatically set to Female (2), ignore genderId from DTO if sent
				genderIdToUse = 2;
				if (memberDTO.getGenderId() != null) {
					logger.debug("Ignoring genderId {} from DTO for Mother (relationId=2). Auto-setting to Female (2).", memberDTO.getGenderId());
				}
			} else {
				// For other relations, use genderId from DTO (required)
				genderIdToUse = memberDTO.getGenderId();
			}
		} else {
			genderIdToUse = memberDTO.getGenderId();
		}
		
		if (genderIdToUse != null) {
			final Integer finalGenderId = genderIdToUse;
			familyMember.setGender_id(genderRepository.findById(finalGenderId)
					.orElseThrow(() -> new ResourceNotFoundException("Gender not found with ID: " + finalGenderId)));
		} else {
			throw new ResourceNotFoundException("Gender ID is required (NOT NULL column). For Father/Mother it's auto-set, for others please provide genderId.");
		}

		if (memberDTO.getBloodGroupId() != null) {
			familyMember.setBlood_group_id(bloodGroupRepository.findByIdAndIsActive(memberDTO.getBloodGroupId(), 1)
					.orElseThrow(() -> new ResourceNotFoundException("Active BloodGroup not found with ID: " + memberDTO.getBloodGroupId())));
		} else {
			throw new ResourceNotFoundException("BloodGroup ID is required (NOT NULL column)");
		}

		// Set is_sri_chaitanya_emp (required NOT NULL, use value from DTO or default to 0 = No)
		Integer isSriChaitanyaEmpValue = 0;
		if (memberDTO.getIsSriChaitanyaEmp() != null) {
			isSriChaitanyaEmpValue = memberDTO.getIsSriChaitanyaEmp() ? 1 : 0;
		}
		familyMember.setIs_sri_chaitanya_emp(isSriChaitanyaEmpValue);
		
		// If is_sri_chaitanya_emp is true, parent_emp_id is MANDATORY
		if (isSriChaitanyaEmpValue == 1) {
			if (memberDTO.getParentEmpId() == null || memberDTO.getParentEmpId() <= 0) {
				throw new ResourceNotFoundException(
						"parentEmpId is required when isSriChaitanyaEmp is true. Please provide a valid parent employee ID.");
			}
			// Find and set parent employee
			Employee parentEmployee = employeeRepository.findById(memberDTO.getParentEmpId())
					.orElseThrow(() -> new ResourceNotFoundException(
							"Parent Employee not found with ID: " + memberDTO.getParentEmpId() + 
							". parentEmpId is required when isSriChaitanyaEmp is true."));
			familyMember.setParent_emp_id(parentEmployee);
		} else {
			// Optional - can be null if not Sri Chaitanya employee
			if (memberDTO.getParentEmpId() != null && memberDTO.getParentEmpId() > 0) {
				Employee parentEmployee = employeeRepository.findById(memberDTO.getParentEmpId())
						.orElseThrow(() -> new ResourceNotFoundException(
								"Parent Employee not found with ID: " + memberDTO.getParentEmpId()));
				familyMember.setParent_emp_id(parentEmployee);
			}
		}
		
		// Set email (optional)
		familyMember.setEmail(memberDTO.getEmail());
		
		// Set contact_no (optional) - convert String to Long
		if (memberDTO.getPhoneNumber() != null && !memberDTO.getPhoneNumber().trim().isEmpty()) {
			try {
				familyMember.setContact_no(Long.parseLong(memberDTO.getPhoneNumber()));
			} catch (NumberFormatException e) {
				throw new ResourceNotFoundException(
						"Invalid contact number format. Expected numeric value, got: " + memberDTO.getPhoneNumber());
			}
		}

		return familyMember;
	}






	/**
	 * Save Agreement Information and Cheque Details
	 * Agreement info (agreement_org_id, agreement_type, provided_cheque) is stored in Employee table
	 * Cheque details are stored in EmpChequeDetails table (only if provided_cheque = true)
	 */
	private void saveAgreementInfo(AgreementInfoDTO agreementInfo, Employee employee) {
		if (agreementInfo == null) return;
		
		// Set agreement information in Employee entity
		if (agreementInfo.getAgreementOrgId() != null) {
			employee.setAgreement_org_id(agreementInfo.getAgreementOrgId());
		}
		
		if (agreementInfo.getAgreementType() != null && !agreementInfo.getAgreementType().trim().isEmpty()) {
			employee.setAgreement_type(agreementInfo.getAgreementType());
		}
		
		// Set is_check_submit from frontend (passed directly from checkbox, not derived from providedCheque)
		// Store in Employee.is_check_submit field (OIS Check Submit)
		// Value: 1 = checked, 0 = unchecked, null = not provided (keeps existing value)
		if (agreementInfo.getIsCheckSubmit() != null) {
			employee.setIs_check_submit(agreementInfo.getIsCheckSubmit());
			logger.info("Updated is_check_submit (OIS Check Submit) for employee (emp_id: {}): {}", 
					employee.getEmp_id(), agreementInfo.getIsCheckSubmit());
			} else {
			logger.debug("is_check_submit not provided in AgreementInfoDTO, keeping existing value for employee (emp_id: {})", 
					employee.getEmp_id());
		}
		
		// Save cheque details only if provided_cheque is true
		// Supports multiple cheques - each cheque in the list creates or updates a separate EmpChequeDetails record
		if (Boolean.TRUE.equals(agreementInfo.getProvidedCheque()) 
				&& agreementInfo.getChequeDetails() != null 
				&& !agreementInfo.getChequeDetails().isEmpty()) {
			
			int empId = employee.getEmp_id();
			// Get existing active cheque details for this employee
			List<EmpChequeDetails> existingCheques = empChequeDetailsRepository.findAll().stream()
					.filter(c -> c.getEmpId() != null && c.getEmpId().getEmp_id() == empId && c.getIsActive() == 1)
					.collect(Collectors.toList());
			
			// Process new cheques - update existing or create new
			for (int i = 0; i < agreementInfo.getChequeDetails().size(); i++) {
				AgreementInfoDTO.ChequeDetailDTO chequeDTO = agreementInfo.getChequeDetails().get(i);
				if (chequeDTO == null) continue;
				
				if (chequeDTO.getChequeNo() == null) {
					throw new ResourceNotFoundException("Cheque Number is required (NOT NULL column)");
				}
				
				if (chequeDTO.getChequeBankName() == null || chequeDTO.getChequeBankName().trim().isEmpty()) {
					throw new ResourceNotFoundException("Cheque Bank Name is required (NOT NULL column)");
				}
				
				if (chequeDTO.getChequeBankIfscCode() == null || chequeDTO.getChequeBankIfscCode().trim().isEmpty()) {
					throw new ResourceNotFoundException("Cheque Bank IFSC Code is required (NOT NULL column)");
				}
				
				if (i < existingCheques.size()) {
					// Update existing record
					EmpChequeDetails existing = existingCheques.get(i);
					existing.setChequeNo(chequeDTO.getChequeNo());
					existing.setChequeBankName(chequeDTO.getChequeBankName().trim());
					existing.setChequeBankIfscCode(chequeDTO.getChequeBankIfscCode().trim());
					existing.setIsActive(1);
					existing.setUpdatedDate(new java.sql.Timestamp(System.currentTimeMillis()));
					empChequeDetailsRepository.save(existing);
					logger.debug("Updated existing EmpChequeDetails[{}] for employee (emp_id: {})", i, empId);
				} else {
					// Create new record
					EmpChequeDetails cheque = new EmpChequeDetails();
					cheque.setEmpId(employee);
					cheque.setChequeNo(chequeDTO.getChequeNo());
					cheque.setChequeBankName(chequeDTO.getChequeBankName().trim());
					cheque.setChequeBankIfscCode(chequeDTO.getChequeBankIfscCode().trim());
					cheque.setIsActive(1);
					
					// Set audit fields
					if (employee.getCreated_by() != null) {
						cheque.setCreatedBy(employee.getCreated_by());
					} else {
						cheque.setCreatedBy(1);
					}
					cheque.setCreatedDate(new java.sql.Timestamp(System.currentTimeMillis()));
					
					empChequeDetailsRepository.save(cheque);
					logger.debug("Created new EmpChequeDetails[{}] for employee (emp_id: {})", i, empId);
				}
			}
			
			// Mark any extra existing cheques as inactive
			if (existingCheques.size() > agreementInfo.getChequeDetails().size()) {
				for (int i = agreementInfo.getChequeDetails().size(); i < existingCheques.size(); i++) {
					existingCheques.get(i).setIsActive(0);
					existingCheques.get(i).setUpdatedDate(new java.sql.Timestamp(System.currentTimeMillis()));
					empChequeDetailsRepository.save(existingCheques.get(i));
					logger.debug("Marked EmpChequeDetails[{}] as inactive for employee (emp_id: {})", i, empId);
				}
			}
			
			logger.info("✅ Updated/Created {} cheque details for Employee ID: {}", 
					agreementInfo.getChequeDetails().size(), employee.getEmp_id());
		} else {
			// If provided_cheque is false or null, mark all existing cheques as inactive
			int empId = employee.getEmp_id();
			List<EmpChequeDetails> existingCheques = empChequeDetailsRepository.findAll().stream()
					.filter(c -> c.getEmpId() != null && c.getEmpId().getEmp_id() == empId && c.getIsActive() == 1)
					.collect(Collectors.toList());
			
			for (EmpChequeDetails existing : existingCheques) {
				existing.setIsActive(0);
				existing.setUpdatedDate(new java.sql.Timestamp(System.currentTimeMillis()));
				empChequeDetailsRepository.save(existing);
			}
			
			if (!existingCheques.isEmpty()) {
				logger.info("Marked {} existing cheque details as inactive (providedCheque=false) for Employee ID: {}", 
						existingCheques.size(), empId);
			}
		}
	}
	
	/**
	 * Update EmpDetails fields from source to target
	 * Helper method to copy all fields when updating existing record
	 */
	private void updateEmpDetailsFields(EmpDetails target, EmpDetails source) {
		target.setAdhaar_name(source.getAdhaar_name());
		target.setDate_of_birth(source.getDate_of_birth());
		target.setPersonal_email(source.getPersonal_email());
		target.setEmergency_ph_no(source.getEmergency_ph_no());
		target.setRelation_id(source.getRelation_id());
		target.setAdhaar_no(source.getAdhaar_no());
		target.setPancard_no(source.getPancard_no());
		target.setAdhaar_enrolment_no(source.getAdhaar_enrolment_no());
		target.setBloodGroup_id(source.getBloodGroup_id());
		target.setCaste_id(source.getCaste_id());
		target.setReligion_id(source.getReligion_id());
		target.setMarital_status_id(source.getMarital_status_id());
		target.setIs_active(source.getIs_active());
		target.setStatus(source.getStatus());
	}
	
	/**
	 * Update EmpDetails fields except personal_email (preserve existing email)
	 * Used when email already exists in database to avoid unique constraint violation
	 */
	private void updateEmpDetailsFieldsExceptEmail(EmpDetails target, EmpDetails source) {
		target.setAdhaar_name(source.getAdhaar_name());
		target.setDate_of_birth(source.getDate_of_birth());
		// DO NOT update personal_email - preserve existing email to avoid unique constraint violation
		// target.setPersonal_email(source.getPersonal_email()); // SKIPPED
		target.setEmergency_ph_no(source.getEmergency_ph_no());
		target.setRelation_id(source.getRelation_id());
		target.setAdhaar_no(source.getAdhaar_no());
		target.setPancard_no(source.getPancard_no());
		target.setAdhaar_enrolment_no(source.getAdhaar_enrolment_no());
		target.setBloodGroup_id(source.getBloodGroup_id());
		target.setCaste_id(source.getCaste_id());
		target.setReligion_id(source.getReligion_id());
		target.setMarital_status_id(source.getMarital_status_id());
		target.setIs_active(source.getIs_active());
		target.setStatus(source.getStatus());
	}
	
	/**
	 * Update or create Address entities - overwrites existing records
	 * When permanentAddressSameAsCurrent = true, only CURR address is saved and PERM is marked inactive
	 */
	private void updateOrCreateAddressEntities(List<EmpaddressInfo> newAddresses, Employee employee, AddressInfoDTO addressInfo) {
		int empId = employee.getEmp_id();
		List<EmpaddressInfo> existingAddresses = empaddressInfoRepository.findAll().stream()
				.filter(addr -> addr.getEmp_id() != null && addr.getEmp_id().getEmp_id() == empId && addr.getIs_active() == 1)
				.collect(Collectors.toList());
		
		// Process new addresses - match by address type (CURR/PERM) instead of index
		for (EmpaddressInfo newAddr : newAddresses) {
			newAddr.setEmp_id(employee);
			newAddr.setIs_active(1);
			
			// Find existing address with same type (CURR or PERM)
			Optional<EmpaddressInfo> existingByType = existingAddresses.stream()
					.filter(addr -> addr.getAddrs_type() != null && 
							addr.getAddrs_type().equals(newAddr.getAddrs_type()))
					.findFirst();
			
			if (existingByType.isPresent()) {
				// Update existing record with same type
				EmpaddressInfo existing = existingByType.get();
				updateAddressFields(existing, newAddr);
				empaddressInfoRepository.save(existing);
				existingAddresses.remove(existing); // Remove from list so it won't be marked inactive
				logger.debug("Updated existing EmpaddressInfo (type: {}) for employee (emp_id: {})", 
						newAddr.getAddrs_type(), empId);
			} else {
				// Create new record
				empaddressInfoRepository.save(newAddr);
				logger.debug("Created new EmpaddressInfo (type: {}) for employee (emp_id: {})", 
						newAddr.getAddrs_type(), empId);
			}
		}
		
		// CRITICAL: If permanentAddressSameAsCurrent = true, explicitly mark any PERM addresses as inactive
		// This ensures only CURR address is active when permanent address is same as current
		int permInactiveCount = 0;
		if (addressInfo != null && Boolean.TRUE.equals(addressInfo.getPermanentAddressSameAsCurrent())) {
			for (EmpaddressInfo existingAddr : existingAddresses) {
				if ("PERM".equals(existingAddr.getAddrs_type())) {
					existingAddr.setIs_active(0);
					empaddressInfoRepository.save(existingAddr);
					permInactiveCount++;
					logger.debug("Marked PERM EmpaddressInfo as inactive (permanentAddressSameAsCurrent=true) for employee (emp_id: {})", empId);
				}
			}
			// Remove PERM addresses from the list so they won't be processed again
			existingAddresses.removeIf(addr -> "PERM".equals(addr.getAddrs_type()));
		}
		
		// Mark any remaining existing addresses as inactive (these are addresses that weren't in new list)
		for (EmpaddressInfo remainingAddr : existingAddresses) {
			remainingAddr.setIs_active(0);
			empaddressInfoRepository.save(remainingAddr);
			logger.debug("Marked EmpaddressInfo (type: {}) as inactive for employee (emp_id: {})", 
					remainingAddr.getAddrs_type(), empId);
		}
		
		int totalInactiveCount = existingAddresses.size() + permInactiveCount;
		logger.info("Updated/Created {} address records for employee (emp_id: {}). permanentAddressSameAsCurrent={}, Marked {} as inactive ({} PERM + {} others).", 
				newAddresses.size(), empId, 
				addressInfo != null ? addressInfo.getPermanentAddressSameAsCurrent() : false, 
				totalInactiveCount, permInactiveCount, existingAddresses.size());
	}
	
	/**
	 * Update or create Family entities - overwrites existing records
	 */
	private void updateOrCreateFamilyEntities(List<EmpFamilyDetails> newFamily, Employee employee) {
		int empId = employee.getEmp_id();
		List<EmpFamilyDetails> existingFamily = empFamilyDetailsRepository.findAll().stream()
				.filter(fam -> fam.getEmp_id() != null && fam.getEmp_id().getEmp_id() == empId && fam.getIs_active() == 1)
				.collect(Collectors.toList());
		
		int maxSize = Math.max(newFamily.size(), existingFamily.size());
		for (int i = 0; i < maxSize; i++) {
			if (i < newFamily.size()) {
				EmpFamilyDetails newFam = newFamily.get(i);
				newFam.setEmp_id(employee);
				newFam.setIs_active(1);
				
				if (i < existingFamily.size()) {
					// Update existing record
					EmpFamilyDetails existing = existingFamily.get(i);
					updateFamilyFields(existing, newFam);
					empFamilyDetailsRepository.save(existing);
					logger.debug("Updated existing EmpFamilyDetails[{}] for employee (emp_id: {})", i, empId);
				} else {
					// Create new record
					empFamilyDetailsRepository.save(newFam);
					logger.debug("Created new EmpFamilyDetails[{}] for employee (emp_id: {})", i, empId);
				}
			} else if (i < existingFamily.size()) {
				// Mark extra existing records as inactive
				existingFamily.get(i).setIs_active(0);
				empFamilyDetailsRepository.save(existingFamily.get(i));
				logger.debug("Marked EmpFamilyDetails[{}] as inactive for employee (emp_id: {})", i, empId);
			}
		}
		logger.info("Updated/Created {} family records for employee (emp_id: {})", newFamily.size(), empId);
	}
	
	/**
	 * Update or create Experience entities - overwrites existing records
	 */
	private void updateOrCreateExperienceEntities(List<EmpExperienceDetails> newExperience, Employee employee) {
		int empId = employee.getEmp_id();
		List<EmpExperienceDetails> existingExperience = empExperienceDetailsRepository.findAll().stream()
				.filter(exp -> exp.getEmployee_id() != null && exp.getEmployee_id().getEmp_id() == empId && exp.getIs_active() == 1)
				.collect(Collectors.toList());
		
		int maxSize = Math.max(newExperience.size(), existingExperience.size());
		for (int i = 0; i < maxSize; i++) {
			if (i < newExperience.size()) {
				EmpExperienceDetails newExp = newExperience.get(i);
				newExp.setEmployee_id(employee);
				newExp.setIs_active(1);
				
				if (i < existingExperience.size()) {
					// Update existing record
					EmpExperienceDetails existing = existingExperience.get(i);
					updateExperienceFields(existing, newExp);
					empExperienceDetailsRepository.save(existing);
					logger.debug("Updated existing EmpExperienceDetails[{}] for employee (emp_id: {})", i, empId);
			} else {
					// Create new record
					empExperienceDetailsRepository.save(newExp);
					logger.debug("Created new EmpExperienceDetails[{}] for employee (emp_id: {})", i, empId);
				}
			} else if (i < existingExperience.size()) {
				// Mark extra existing records as inactive
				existingExperience.get(i).setIs_active(0);
				empExperienceDetailsRepository.save(existingExperience.get(i));
				logger.debug("Marked EmpExperienceDetails[{}] as inactive for employee (emp_id: {})", i, empId);
			}
		}
		logger.info("Updated/Created {} experience records for employee (emp_id: {})", newExperience.size(), empId);
	}
	
	/**
	 * Update or create Qualification entities - overwrites existing records
	 */
	private void updateOrCreateQualificationEntities(List<EmpQualification> newQualification, Employee employee) {
		int empId = employee.getEmp_id();
		List<EmpQualification> existingQualification = empQualificationRepository.findAll().stream()
				.filter(qual -> qual.getEmp_id() != null && qual.getEmp_id().getEmp_id() == empId && qual.getIs_active() == 1)
				.collect(Collectors.toList());
		
		int maxSize = Math.max(newQualification.size(), existingQualification.size());
		for (int i = 0; i < maxSize; i++) {
			if (i < newQualification.size()) {
				EmpQualification newQual = newQualification.get(i);
				newQual.setEmp_id(employee);
				newQual.setIs_active(1);
				
				if (i < existingQualification.size()) {
					// Update existing record
					EmpQualification existing = existingQualification.get(i);
					updateQualificationFields(existing, newQual);
					empQualificationRepository.save(existing);
					logger.debug("Updated existing EmpQualification[{}] for employee (emp_id: {})", i, empId);
			} else {
					// Create new record
					empQualificationRepository.save(newQual);
					logger.debug("Created new EmpQualification[{}] for employee (emp_id: {})", i, empId);
				}
			} else if (i < existingQualification.size()) {
				// Mark extra existing records as inactive
				existingQualification.get(i).setIs_active(0);
				empQualificationRepository.save(existingQualification.get(i));
				logger.debug("Marked EmpQualification[{}] as inactive for employee (emp_id: {})", i, empId);
			}
		}
		logger.info("Updated/Created {} qualification records for employee (emp_id: {})", newQualification.size(), empId);
	}
	
	/**
	 * Update or create Document entities - overwrites existing records
	 */
	private void updateOrCreateDocumentEntities(List<EmpDocuments> newDocuments, Employee employee) {
		int empId = employee.getEmp_id();
		List<EmpDocuments> existingDocuments = empDocumentsRepository.findAll().stream()
				.filter(doc -> doc.getEmp_id() != null && doc.getEmp_id().getEmp_id() == empId && doc.getIs_active() == 1)
				.collect(Collectors.toList());
		
		int maxSize = Math.max(newDocuments.size(), existingDocuments.size());
		for (int i = 0; i < maxSize; i++) {
			if (i < newDocuments.size()) {
				EmpDocuments newDoc = newDocuments.get(i);
				newDoc.setEmp_id(employee);
				newDoc.setIs_active(1);
				
				if (i < existingDocuments.size()) {
					// Update existing record
					EmpDocuments existing = existingDocuments.get(i);
					updateDocumentFields(existing, newDoc);
					empDocumentsRepository.save(existing);
					logger.debug("Updated existing EmpDocuments[{}] for employee (emp_id: {})", i, empId);
				} else {
					// Create new record
					empDocumentsRepository.save(newDoc);
					logger.debug("Created new EmpDocuments[{}] for employee (emp_id: {})", i, empId);
				}
			} else if (i < existingDocuments.size()) {
				// Mark extra existing records as inactive
				existingDocuments.get(i).setIs_active(0);
				empDocumentsRepository.save(existingDocuments.get(i));
				logger.debug("Marked EmpDocuments[{}] as inactive for employee (emp_id: {})", i, empId);
			}
		}
		logger.info("Updated/Created {} document records for employee (emp_id: {})", newDocuments.size(), empId);
	}
	
	/**
	 * Update or create Bank entities - overwrites existing records
	 */
	private void updateOrCreateBankEntities(List<BankDetails> newBanks, Employee employee) {
		int empId = employee.getEmp_id();
		List<BankDetails> existingBanks = bankDetailsRepository.findByEmpIdAndIsActive(empId, 1);
		
		int maxSize = Math.max(newBanks.size(), existingBanks.size());
		for (int i = 0; i < maxSize; i++) {
			if (i < newBanks.size()) {
				BankDetails newBank = newBanks.get(i);
				newBank.setEmpId(employee);
				newBank.setIsActive(1);
				
				if (i < existingBanks.size()) {
					// Update existing record
					BankDetails existing = existingBanks.get(i);
					updateBankFields(existing, newBank);
					bankDetailsRepository.save(existing);
					logger.debug("Updated existing BankDetails[{}] for employee (emp_id: {})", i, empId);
				} else {
					// Create new record
					bankDetailsRepository.save(newBank);
					logger.debug("Created new BankDetails[{}] for employee (emp_id: {})", i, empId);
				}
			} else if (i < existingBanks.size()) {
				// Mark extra existing records as inactive
				existingBanks.get(i).setIsActive(0);
				bankDetailsRepository.save(existingBanks.get(i));
				logger.debug("Marked BankDetails[{}] as inactive for employee (emp_id: {})", i, empId);
			}
		}
		logger.info("Updated/Created {} bank records for employee (emp_id: {})", newBanks.size(), empId);
	}
	
	/**
	 * Helper methods to update entity fields
	 */
	private void updateAddressFields(EmpaddressInfo target, EmpaddressInfo source) {
		target.setAddrs_type(source.getAddrs_type());
		target.setCountry_id(source.getCountry_id());
		target.setState_id(source.getState_id());
		target.setCity_id(source.getCity_id());
		target.setPostal_code(source.getPostal_code());
		target.setHouse_no(source.getHouse_no());
		target.setLandmark(source.getLandmark());
		target.setIs_active(source.getIs_active());
	}
	
	private void updateFamilyFields(EmpFamilyDetails target, EmpFamilyDetails source) {
		target.setFirst_name(source.getFirst_name());
		target.setLast_name(source.getLast_name());
		target.setDate_of_birth(source.getDate_of_birth());
		target.setGender_id(source.getGender_id());
		target.setRelation_id(source.getRelation_id());
		target.setBlood_group_id(source.getBlood_group_id());
		target.setNationality(source.getNationality());
		target.setOccupation(source.getOccupation());
		target.setIs_dependent(source.getIs_dependent());
		target.setIs_late(source.getIs_late());
		target.setIs_sri_chaitanya_emp(source.getIs_sri_chaitanya_emp());
		target.setParent_emp_id(source.getParent_emp_id()); // CRITICAL: Update parent_emp_id
		target.setEmail(source.getEmail());
		target.setContact_no(source.getContact_no());
		target.setIs_active(source.getIs_active());
	}
	
	private void updateExperienceFields(EmpExperienceDetails target, EmpExperienceDetails source) {
		target.setPre_organigation_name(source.getPre_organigation_name());
		target.setDate_of_join(source.getDate_of_join()); // CRITICAL: Update date_of_join
		target.setDate_of_leave(source.getDate_of_leave()); // CRITICAL: Update date_of_leave
		target.setDesignation(source.getDesignation());
		target.setLeaving_reason(source.getLeaving_reason()); // CRITICAL: Update leaving_reason
		target.setNature_of_duties(source.getNature_of_duties());
		target.setCompany_addr(source.getCompany_addr());
		target.setGross_salary(source.getGross_salary());
		target.setPre_chaitanya_id(source.getPre_chaitanya_id());
		target.setIs_active(source.getIs_active());
	}
	
	private void updateQualificationFields(EmpQualification target, EmpQualification source) {
		target.setQualification_id(source.getQualification_id());
		target.setQualification_degree_id(source.getQualification_degree_id());
		target.setUniversity(source.getUniversity());
		target.setInstitute(source.getInstitute());
		target.setPassedout_year(source.getPassedout_year());
		target.setSpecialization(source.getSpecialization());
		target.setIs_active(source.getIs_active());
	}
	
	private void updateDocumentFields(EmpDocuments target, EmpDocuments source) {
		target.setEmp_doc_type_id(source.getEmp_doc_type_id());
		target.setDoc_path(source.getDoc_path());
		target.setSsc_no(source.getSsc_no());
		target.setIs_verified(source.getIs_verified());
		target.setIs_active(source.getIs_active());
	}
	
	private void updateBankFields(BankDetails target, BankDetails source) {
		target.setEmpPaymentType(source.getEmpPaymentType());
		target.setBankHolderName(source.getBankHolderName());
		target.setAccNo(source.getAccNo());
		target.setIfscCode(source.getIfscCode());
		target.setNetPayable(source.getNetPayable());
		target.setBankName(source.getBankName());
		target.setBankBranch(source.getBankBranch());
		target.setAccType(source.getAccType());
		target.setBankStatementChequePath(source.getBankStatementChequePath()); // Update bank statement/cheque path
		target.setIsActive(source.getIsActive());
	}
	
	/**
	 * Update existing employee entity with new data from BasicInfoDTO
	 * Preserves emp_id and other fields that shouldn't be changed
	 * 
	 * @param employee Existing employee entity
	 * @param basicInfo BasicInfoDTO with updated data
	 */
	private void updateEmployeeEntity(Employee employee, BasicInfoDTO basicInfo) {
		if (basicInfo == null) {
			return;
		}
		
		logger.info("🔄 Updating employee entity (emp_id: {}) with new data", employee.getEmp_id());
		
		// Update basic fields
		if (basicInfo.getFirstName() != null && !basicInfo.getFirstName().trim().isEmpty()) {
			employee.setFirst_name(basicInfo.getFirstName());
		}
		if (basicInfo.getLastName() != null && !basicInfo.getLastName().trim().isEmpty()) {
			employee.setLast_name(basicInfo.getLastName());
		}
		if (basicInfo.getDateOfJoin() != null) {
			employee.setDate_of_join(basicInfo.getDateOfJoin());
		}
		if (basicInfo.getPrimaryMobileNo() != null && basicInfo.getPrimaryMobileNo() > 0) {
			employee.setPrimary_mobile_no(basicInfo.getPrimaryMobileNo());
		}
		// Email is not set to Employee entity - it goes to EmpDetails.personal_email only
		employee.setEmail(null);
		
		if (basicInfo.getTotalExperience() != null) {
			employee.setTotal_experience(basicInfo.getTotalExperience().doubleValue());
		}
		
		// Keep is_active and status as they are (don't reset)
		// employee.setIs_active(1); // Keep existing value
		// employee.setStatus("ACTIVE"); // Keep existing value
		
		// Update temp_payroll_id if provided (should already be set, but update anyway)
		if (basicInfo.getTempPayrollId() != null && !basicInfo.getTempPayrollId().trim().isEmpty()) {
			employee.setTemp_payroll_id(basicInfo.getTempPayrollId());
		}
		
		// Update created_by if provided, otherwise keep existing
		if (basicInfo.getCreatedBy() != null && basicInfo.getCreatedBy() > 0) {
			employee.setCreated_by(basicInfo.getCreatedBy());
		}
		
		// Update foreign key relationships
		if (basicInfo.getCampusId() != null) {
			employee.setCampus_id(campusRepository.findByCampusIdAndIsActive(basicInfo.getCampusId(), 1)
					.orElseThrow(() -> new ResourceNotFoundException("Active Campus not found with ID: " + basicInfo.getCampusId())));
		}
		
		if (basicInfo.getGenderId() != null) {
			employee.setGender(genderRepository.findByIdAndIsActive(basicInfo.getGenderId(), 1)
					.orElseThrow(() -> new ResourceNotFoundException("Active Gender not found")));
		}
		
		if (basicInfo.getDesignationId() != null) {
			employee.setDesignation(designationRepository.findByIdAndIsActive(basicInfo.getDesignationId(), 1)
					.orElseThrow(() -> new ResourceNotFoundException("Active Designation not found")));
		}
		
		if (basicInfo.getDepartmentId() != null) {
			employee.setDepartment(departmentRepository.findByIdAndIsActive(basicInfo.getDepartmentId(), 1)
					.orElseThrow(() -> new ResourceNotFoundException("Active Department not found")));
		}
		
		if (basicInfo.getCategoryId() != null) {
			employee.setCategory(categoryRepository.findByIdAndIsActive(basicInfo.getCategoryId(), 1)
					.orElseThrow(() -> new ResourceNotFoundException("Active Category not found")));
		}
		
		if (basicInfo.getEmpTypeId() != null) {
			employee.setEmployee_type_id(employeeTypeRepository.findByIdAndIsActive(basicInfo.getEmpTypeId(), 1)
					.orElseThrow(() -> new ResourceNotFoundException("Active EmployeeType not found")));
		}
		
		if (basicInfo.getEmpWorkModeId() != null) {
			employee.setWorkingMode_id(workingModeRepository.findByIdAndIsActive(basicInfo.getEmpWorkModeId(), 1)
					.orElseThrow(() -> new ResourceNotFoundException("Active WorkingMode not found")));
		}
		
		if (basicInfo.getJoinTypeId() != null) {
			employee.setJoin_type_id(joiningAsRepository.findByIdAndIsActive(basicInfo.getJoinTypeId(), 1)
					.orElseThrow(() -> new ResourceNotFoundException("Active JoiningAs not found")));
			
			// Handle replacement logic
			if (basicInfo.getJoinTypeId() == 3) {
				if (basicInfo.getReplacedByEmpId() == null || basicInfo.getReplacedByEmpId() <= 0) {
					throw new ResourceNotFoundException(
							"replacedByEmpId is required when joinTypeId is 3 (Replacement). Please provide a valid replacement employee ID.");
				}
				employee.setEmployee_replaceby_id(employeeRepository.findByIdAndIs_active(basicInfo.getReplacedByEmpId(), 0)
						.orElseThrow(() -> new ResourceNotFoundException(
								"Inactive Replacement Employee not found with ID: " + basicInfo.getReplacedByEmpId() + 
								". Only inactive employees (is_active = 0) can be used as replacement.")));
			} else if (basicInfo.getReplacedByEmpId() != null && basicInfo.getReplacedByEmpId() > 0) {
				employee.setEmployee_replaceby_id(employeeRepository.findByIdAndIs_active(basicInfo.getReplacedByEmpId(), 0)
						.orElse(null));
			} else {
				employee.setEmployee_replaceby_id(null);
			}
			
			// Handle contract dates
			if (basicInfo.getJoinTypeId() == 4) {
				if (basicInfo.getContractStartDate() != null) {
					employee.setContract_start_date(basicInfo.getContractStartDate());
				} else if (basicInfo.getDateOfJoin() != null) {
					employee.setContract_start_date(basicInfo.getDateOfJoin());
				}
				
				if (basicInfo.getContractEndDate() != null) {
					employee.setContract_end_date(basicInfo.getContractEndDate());
				} else {
					java.sql.Date startDate = basicInfo.getContractStartDate() != null ? 
							basicInfo.getContractStartDate() : basicInfo.getDateOfJoin();
					if (startDate != null) {
						long oneYearInMillis = 365L * 24 * 60 * 60 * 1000;
						java.util.Date endDateUtil = new java.util.Date(startDate.getTime() + oneYearInMillis);
						employee.setContract_end_date(new java.sql.Date(endDateUtil.getTime()));
					}
				}
			}
		}
		
		if (basicInfo.getModeOfHiringId() != null) {
			employee.setModeOfHiring_id(modeOfHiringRepository.findByIdAndIsActive(basicInfo.getModeOfHiringId(), 1)
					.orElseThrow(() -> new ResourceNotFoundException("Active ModeOfHiring not found")));
		}
		
		// Update reference employees
		if (basicInfo.getReferenceEmpId() != null && basicInfo.getReferenceEmpId() > 0) {
			employee.setEmployee_reference(employeeRepository.findByIdAndIs_active(basicInfo.getReferenceEmpId(), 1)
					.orElseThrow(() -> new ResourceNotFoundException("Active Reference Employee not found with ID: " + basicInfo.getReferenceEmpId())));
		}
		
		if (basicInfo.getHiredByEmpId() != null && basicInfo.getHiredByEmpId() > 0) {
			employee.setEmployee_hired(employeeRepository.findByIdAndIs_active(basicInfo.getHiredByEmpId(), 1)
					.orElseThrow(() -> new ResourceNotFoundException("Active Hired By Employee not found with ID: " + basicInfo.getHiredByEmpId())));
		}
		
		if (basicInfo.getManagerId() != null && basicInfo.getManagerId() > 0) {
			employee.setEmployee_manager_id(employeeRepository.findByIdAndIs_active(basicInfo.getManagerId(), 1)
					.orElseThrow(() -> new ResourceNotFoundException("Active Manager not found with ID: " + basicInfo.getManagerId())));
		}
		
		if (basicInfo.getReportingManagerId() != null && basicInfo.getReportingManagerId() > 0) {
			employee.setEmployee_reporting_id(employeeRepository.findByIdAndIs_active(basicInfo.getReportingManagerId(), 1)
					.orElseThrow(() -> new ResourceNotFoundException("Active Reporting Manager not found with ID: " + basicInfo.getReportingManagerId())));
		}
		
		// Note: qualification_id will be set later from qualification tab's highest qualification
		// Status update is handled in onboardEmployee method (Back to Campus -> Pending at DO)
		
		logger.info("✅ Completed updating employee entity (emp_id: {})", employee.getEmp_id());
	}
}