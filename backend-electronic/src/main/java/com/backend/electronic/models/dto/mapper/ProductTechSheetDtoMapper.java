package com.backend.electronic.models.dto.mapper;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import com.backend.electronic.models.dto.ProductTechSheetDto;
import com.backend.electronic.models.entities.Product;
import com.backend.electronic.models.entities.ProductFeature;

// public class ProductTechSheetDto {
//     private Long id;
//     private Map<String, String> techSheet;
// }

@Component
@Mapper(componentModel = "spring")
public interface ProductTechSheetDtoMapper {

    // UTILIZAR LA ENTIDAD PRODUCT EN LUGAR DE PRODUCTFEATURE
    // Campos: id del producto y una lista con los campos feature y featureValue
    // para la ficha tecnica
    @Mapping(target = "id", source = "id")

    // CASO CUANDO SE QUIERE HACER MAPEAR UNA LISTA
    @Mapping(target = "techSheet", source = "productFeatures", qualifiedByName = "mapFeatureValuesToTechSheetDto")
    ProductTechSheetDto toDto(Product product);

    @Named("mapFeatureValuesToTechSheetDto")
    default Map<String, String> mapFeatureValuesToTechSheetDto(List<ProductFeature> productFeatures) {
        if (productFeatures == null) {
            return Collections.emptyMap();
        }

        return productFeatures.stream()
                .collect(Collectors.toMap(
                        pf -> pf.getFeature().getName(), // Key: Feature name
                        pf -> pf.getFeatureValue().getValue(), // Value: Feature value
                        (existing, replacement) -> existing // If duplicates, keep the first
                ));

        // .collect(Collectors.toMap(
        // pf -> pf.getFeature().getName(), // Clave: Nombre de la característica
        // pf -> pf.getValue(), // Valor: Valor de la característica
        // (existing, replacement) -> existing // Si hay duplicados, mantener el primero
        // ));
    }
}