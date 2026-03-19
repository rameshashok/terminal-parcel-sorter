package com.nps.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "sorting_rules")
public class SortingRule extends PanacheEntity {

    public String ruleCode;
    public String description;
    public String postalCodePattern;
    public String assignedBelt;
    public int priority;
    public boolean active;

    public static SortingRule findByPostalCode(String postalCode) {
        return find("postalCodePattern = ?1 AND active = true ORDER BY priority DESC", postalCode).firstResult();
    }
}
