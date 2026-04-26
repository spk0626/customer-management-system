package com.customermgmt.repository.projection;

import java.time.LocalDate;

public interface CustomerListProjection {

    Long getId();

    String getName();

    LocalDate getDateOfBirth();

    String getNicNumber();

    boolean isActive();

    Long getMobileCount();

    Long getAddressCount();

    Long getFamilyMemberCount();
}
