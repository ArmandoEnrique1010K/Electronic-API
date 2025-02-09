package com.backend.electronic.models.entities;

import java.util.List;

import org.hibernate.annotations.Cascade;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// TODO: MODIFICAR ESTO
@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "feature_value")
public class FeatureValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @Cascade(org.hibernate.annotations.CascadeType.PERSIST) // TODO: INVESTIGAR @CASCADE
    @JoinColumn(name = "feature_id")
    private Feature feature;

    @NotBlank
    private String value;

    @OneToMany(mappedBy = "featureValue", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductFeature> productFeatures;

}
