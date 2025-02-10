package com.backend.electronic.services.products;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.backend.electronic.models.dto.ProductDetailDto;
import com.backend.electronic.models.dto.ProductsListDto;
import com.backend.electronic.models.dto.TechSheetDto;
import com.backend.electronic.models.dto.mapper.ProductDetailTechSheetDtoMapper;
import com.backend.electronic.models.dto.mapper.ProductDtoMapper;
import com.backend.electronic.models.entities.Brand;
import com.backend.electronic.models.entities.Category;
import com.backend.electronic.models.entities.Feature;
import com.backend.electronic.models.entities.FeatureValue;
import com.backend.electronic.models.entities.Image;
import com.backend.electronic.models.entities.Product;
import com.backend.electronic.models.entities.ProductFeature;
import com.backend.electronic.models.requests.ProductRequest;
import com.backend.electronic.repositories.BrandRepository;
import com.backend.electronic.repositories.CategoryRepository;
import com.backend.electronic.repositories.FeatureRepository;
import com.backend.electronic.repositories.FeatureValueRepository;
import com.backend.electronic.repositories.ImageRepository;
import com.backend.electronic.repositories.ProductFeatureRepository;
import com.backend.electronic.repositories.ProductRepository;
import com.backend.electronic.services.images.ImageService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private FeatureRepository featureRepository;

    @Autowired
    private FeatureValueRepository featureValueRepository;
    @Autowired
    private ProductFeatureRepository productFeatureRepository;

    @Autowired
    private ProductDtoMapper productDtoMapper;

    @Autowired
    private ImageService imageService;

    // @Autowired
    // private ProductDetailTechSheetDtoMapper productDetailTechSheetDtoMapper;

    // TODO: ¿QUE ES ESO?
    @PersistenceContext
    private EntityManager entityManager;

    // TODO: SIMPLIFICAR EL CODIGO DE ESTE ARCHIVO
    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    @Transactional(readOnly = true)
    @Override
    public List<ProductsListDto> findAll() {
        List<Product> products = productRepository.findAllProducts();
        return products.stream().map(
                productDtoMapper::toListDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<ProductsListDto> findAllByName(String name) {
        List<Product> products = productRepository.findAllProductsByName(name);
        return products.stream().map(
                productDtoMapper::toListDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<ProductsListDto> findAllByOffer() {
        List<Product> products = productRepository.findAllProductsByOffer();
        return products.stream().map(
                productDtoMapper::toListDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<ProductsListDto> findAllByCategoryId(Long id) {
        List<Product> products = productRepository.findAllProductsByCategoryId(id);
        return products.stream().map(
                productDtoMapper::toListDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<ProductsListDto> findAllByFilters(String name, Long idCategory, List<Long> idsBrands, Boolean offer) {
        List<Product> products = productRepository.findAllFilteredProducts(name, idCategory, idsBrands, offer);
        return products.stream().map(
                productDtoMapper::toListDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<ProductsListDto> findAllByFeatureValues(List<Long> featureValues) {
        List<Product> products = productRepository.findByFeatureValues(featureValues);
        return products.stream().map(
                productDtoMapper::toListDto)
                .collect(Collectors.toList());

    }

    // NO SE RECOMIENDA USAR LO SIGUIENTE EN FINDBYID

    // @Transactional(readOnly = true)
    // @Override
    // public Optional<ProductDetailDto> findById(Long id) {
    // return
    // Optional.ofNullable(productRepository.findById(id).map(productDtoMapper::toDetailDto).orElseThrow());
    // }

    @Transactional(readOnly = true)
    @Override
    public Optional<ProductDetailDto> findById(Long id) {
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isPresent() && optionalProduct.get().getStatus() == true) {
            // Nota, solamente retornara el producto si el status esta en true
            // TODO: APLICAR ESTA TECNICA EN LAS DEMAS ENTITADES
            return optionalProduct.map(productDtoMapper::toDetailDto);
        } else {
            return Optional.empty();
        }
    }

    @Transactional
    @Override
    public ProductDetailDto save(Product product, MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("La imagen del producto no puede estar vacía");
        }

        Brand brand = brandRepository.findById(product.getBrand().getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "La marca con ID " + product.getBrand().getId() + " no existe"));
        product.setBrand(brand);

        Category category = categoryRepository.findById(product.getCategory().getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "La categoría con ID " + product.getCategory().getId() + " no existe"));
        product.setCategory(category);

        // Asignar valores directos al producto (solamente los campos que no seran
        // llenados por el usuario)
        product.setStatus(true);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        // 🔹 Primero guardamos el producto sin la imagen
        Product savedProduct = productRepository.save(product);

        // 🔹 Ahora guardamos la imagen, porque ya sabemos que el producto se guardó
        // bien
        String nameImage = imageService.storeImage(file);
        Image image = new Image();
        image.setName(nameImage);
        image = imageRepository.save(image);

        // 🔹 Ahora asignamos la imagen al producto y guardamos de nuevo
        savedProduct.setImage(image);
        productRepository.save(savedProduct); // Segunda actualización con imagen

        return productDtoMapper.toDetailDto(savedProduct);
    }

    @Transactional
    @Override
    public ProductDetailDto saveWithTechSheet(Product product, MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("La imagen del producto no puede estar vacía");
        }

        Brand brand = brandRepository.findById(product.getBrand().getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "La marca con ID " + product.getBrand().getId() + " no existe"));
        product.setBrand(brand);

        Category category = categoryRepository.findById(product.getCategory().getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "La categoría con ID " + product.getCategory().getId() + " no existe"));
        product.setCategory(category);

        // Asignar valores directos al producto (solamente los campos que no seran
        // llenados por el usuario)
        product.setStatus(true);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        // 🔹 Ahora guardamos la imagen, porque ya sabemos que el producto se guardó
        // bien
        String nameImage = imageService.storeImage(file);
        Image image = new Image();
        image.setName(nameImage);
        image = imageRepository.save(image);

        // 🔹 Ahora asignamos la imagen al producto y guardamos de nuevo
        product.setImage(image);

        // Guardar el producto
        Product savedProduct = productRepository.save(product);
        Long idSavedProduct = savedProduct.getId();
        System.out.println("EL PRODUCTO VA A TENER EL ID: " + idSavedProduct);

        // ✅ Validar que hay características antes de procesarlas
        if (product.getProductFeature() == null || product.getProductFeature().isEmpty()) {
            throw new IllegalArgumentException("No se recibieron características para el producto.");
        }

        // ✅ Procesamos cada característica del producto
        for (ProductFeature eachProductFeature : product.getProductFeature()) {
            if (eachProductFeature.getFeature() == null || eachProductFeature.getFeatureValue() == null) {
                throw new IllegalArgumentException("Cada ProductFeature debe contener una Feature y un FeatureValue.");
            }

            String featureName = eachProductFeature.getFeature().getName();
            String featureValueName = eachProductFeature.getFeatureValue().getValue();
            System.out.println(featureName);
            System.out.println(featureValueName);

            // ✅ Buscar o crear Feature
            // 1° DEBE BUSCAR SI ESA CARACTERISTICA YA EXISTE
            Feature feature = featureRepository.findByName(featureName)
                    // .orElseGet(() -> featureRepository.save(new Feature(null, featureName, true,
                    // null)));
                    .orElseGet(() -> {
                        Feature newFeature = new Feature();
                        newFeature.setName(featureName);
                        newFeature.setStatus(true);

                        Feature savedFeature = featureRepository.save(newFeature); // 🔹 Guardar antes de usarlo
                        System.out.println("Feature guardado con ID: " + savedFeature.getId()); // ✅ Verificar ID

                        return featureRepository.save(newFeature);
                    });

            if (feature.getId() == null) {
                throw new IllegalStateException("El Feature no tiene ID después de ser guardado");
            }

            System.out.println(feature.getId());
            System.out.println("Caracteristica: " + feature);

            Optional<FeatureValue> existingFeatureValue = featureValueRepository.findByFeatureAndValue(feature,
                    featureValueName);
            FeatureValue featureValue = existingFeatureValue.orElseGet(() -> {
                FeatureValue newFeatureValue = new FeatureValue();
                newFeatureValue.setFeature(feature); // 🔹 Ya tiene ID porque se guardó antes
                newFeatureValue.setValue(featureValueName);
                return featureValueRepository.save(newFeatureValue);
            });

            // ✅ Buscar o crear FeatureValue (asegurando que corresponda con Feature
            // FeatureValue featureValue =
            // featureValueRepository.findByFeatureAndValue(feature, featureValueName)
            // .orElseGet(() -> {
            // FeatureValue newFeatureValue = new FeatureValue();
            // newFeatureValue.setFeature(feature);
            // newFeatureValue.setValue(featureValueName);
            // return featureValueRepository.save(newFeatureValue);
            // // featureValueRepository.save(new FeatureValue(null, feature,
            // featureValueName,
            // // null))
            // });

            // ✅ Evitar insertar duplicados en ProductFeature
            if (!productFeatureRepository.existsByProductAndFeatureValue(savedProduct, featureValue)) {
                ProductFeature productFeature = new ProductFeature();
                productFeature.setProduct(savedProduct);
                productFeature.setFeature(feature);
                productFeature.setFeatureValue(featureValue);
                productFeatureRepository.save(productFeature);
            }

            // TODO: ASIGNA feature_id EN LA TABLA feature_value, PERO INSERTA 2 VECES EL
            // MISMO REGISTRO
            // TODO: SI VERIFICA LOS VALORES DE feature Y value SI YA EXISTEN EN LA BASE DE
            // DATOS
            // TODO: GUARDA 2 VECES EL MISMO REGISTRO EN product_feature

            // ESTE ERROR SE MUESTRA... PORQUE HAY
            // Error al guardar el producto: Query did not return a unique result: 2 results
            // were returned

            // 1. Guardar Feature si no existe
            // 2. Buscar o guardar FeatureValue (DEBE INCLUIR `Feature` en la consulta)
            // 3. Verificar que no exista ya la relación en `product_feature`

            // Feature feature = featureRepository.findByName(featureName)
            // .orElseGet(() -> {
            // Feature newFeature = new Feature();
            // newFeature.setName(featureName);
            // newFeature.setStatus(true);
            // return featureRepository.save(newFeature);
            // });

            // FeatureValue featureValue =
            // featureValueRepository.findByFeatureAndValue(feature, featureValueName)
            // .orElseGet(() -> {
            // FeatureValue newFeatureValue = new FeatureValue();
            // newFeatureValue.setFeature(feature);
            // newFeatureValue.setValue(featureValueName);
            // return featureValueRepository.save(newFeatureValue);
            // });

            // if (!productFeatureRepository.existsByProductAndFeatureValue(savedProduct,
            // featureValue)) {
            // ProductFeature productFeature = new ProductFeature();
            // productFeature.setProduct(savedProduct);
            // productFeature.setFeature(feature);
            // productFeature.setFeatureValue(featureValue);
            // productFeatureRepository.save(productFeature);

            // }
        }

        System.out.println("Ficha técnica guardada correctamente.");

        // return productDetailTechSheetDtoMapper.toDto(savedProduct);
        return productDtoMapper.toDetailDto(savedProduct);

    }

    @Transactional
    @Override
    public Optional<ProductDetailDto> update(ProductRequest product, MultipartFile file, Long id) {

        Optional<Product> optional = productRepository.findById(id);

        if (optional.isEmpty()) {
            return Optional.empty(); // Si el producto no existe, no hacemos nada
        }

        Product productDb = optional.get();

        // Validamos que la marca y la categoría existan antes de asignarlas
        Brand brand = brandRepository.findById(product.getBrand().getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "La marca con ID " + product.getBrand().getId() + " no existe"));
        productDb.setBrand(brand);

        Category category = categoryRepository.findById(product.getCategory().getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "La categoría con ID " + product.getCategory().getId() + " no existe"));
        productDb.setCategory(category);

        // Actualizamos los datos del producto
        productDb.setName(product.getName());
        productDb.setCode(product.getCode());
        productDb.setInOffer(product.getInOffer());
        productDb.setPrice(product.getPrice());

        if (productDb.getInOffer()) {
            productDb.setOfferPrice(product.getOfferPrice());
        } else {
            productDb.setOfferPrice(null);
        }

        productDb.setDescription(product.getDescription());
        productDb.setStatus(true);
        productDb.setUpdatedAt(LocalDateTime.now());

        // Primero guardamos el producto sin la imagen
        Product updatedProduct = productRepository.save(productDb);

        // Si se sube una imagen nueva, la reemplazamos
        if (file != null && !file.isEmpty()) {
            System.out.println("SUBIENDO UNA NUEVA IMAGEN...");
            String nameImage = imageService.storeImage(file);

            Image image = new Image();
            image.setName(nameImage);
            image = imageRepository.save(image);

            updatedProduct.setImage(image);
            productRepository.save(updatedProduct); // Segunda actualización con imagen
        } else {
            System.out.println("NO SE SUBIÓ UNA NUEVA IMAGEN, SE MANTIENE LA ACTUAL.");
        }

        return Optional.of(productDtoMapper.toDetailDto(updatedProduct));
    }

    @Override
    @Transactional
    public void disable(Long id) {
        Optional<Product> optional = productRepository.findById(id);

        if (optional.isPresent()) {
            Product productDb = optional.orElseThrow();
            productDb.setStatus(false);
            productRepository.save(productDb);
        }
    }

}

// @Cascade
// https://rajendraprasadpadma.medium.com/object-references-an-unsaved-transient-instance-save-the-transient-instance-before-flushing-1bede249108
