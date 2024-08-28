package kr.giljabi.api.entity;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import kr.giljabi.api.repository.GiljabiGpsDataRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class UniqueUuidValidator implements ConstraintValidator<UniqueUuid, String> {

    @Autowired
    private GiljabiGpsDataRepository gpsdataRepository;

    @Override
    public void initialize(UniqueUuid constraintAnnotation) {
    }

    @Override
    public boolean isValid(String uuid, ConstraintValidatorContext context) {
        return !gpsdataRepository.existsByUuid(uuid);
    }
}