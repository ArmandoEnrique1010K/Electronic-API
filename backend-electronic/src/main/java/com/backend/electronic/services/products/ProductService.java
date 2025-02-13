package com.backend.electronic.services.products;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.query.Param;
import org.springframework.web.multipart.MultipartFile;

import com.backend.electronic.models.dto.ProductDetailDto;
import com.backend.electronic.models.dto.ProductsListDto;
import com.backend.electronic.models.entities.Product;
import com.backend.electronic.models.entities.ProductFeature;
import com.backend.electronic.models.requests.ProductRequest;

public interface ProductService {

    // Se utiliza la lista de productos
    public List<ProductsListDto> findAll();

    public List<ProductsListDto> findAllByName(String name);

    public List<ProductsListDto> findAllByOffer();

    public List<ProductsListDto> findAllByCategoryId(Long id);

    public List<ProductsListDto> findAllByFilters(String name, Long idCategory, List<Long> idsBrands, Boolean offer);

    public List<ProductsListDto> findAllByFeatureValues(List<Long> featureValues);

    public List<ProductsListDto> findAllBySevenFilters(String name, Long idCategory, List<Long> idsBrands,
            Boolean offer, Double minPrice, Double maxPrice, List<Long> featureValues);

    public Optional<ProductDetailDto> findById(Long id);

    public ProductDetailDto save(Product product, MultipartFile file);

    // METODO PARA GUARDAR UN PRODUCTO INCLUYENDO SU FICHA TECNICA
    public ProductDetailDto saveWithTechSheet(Product product, MultipartFile file);

    public Optional<ProductDetailDto> update(ProductRequest product, MultipartFile file, Long id);

    public void disable(Long id);

}
