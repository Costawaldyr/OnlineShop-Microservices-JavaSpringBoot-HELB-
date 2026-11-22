package com.onlineshop.catalogue.controllerTest;

import com.onlineshop.catalogue.controller.ProductController;
import com.onlineshop.catalogue.dto.ProductDto;
import com.onlineshop.catalogue.service.ProductService;
import com.onlineshop.catalogue.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class ProductControllerTest {

    private static final Long PRODUCT_ID = 1L;
    private static final String PRODUCT_NAME = "Keyboard";
    private static final String PRODUCT_DESCRIPTION = "USB keyboard";
    private static final String PRODUCTS_URL = "/products";
    private static final String PRODUCT_URL = PRODUCTS_URL + "/{id}";
    private static final String INVALID_JSON = "{";
    private static final Long CATEGORY_ID = 2L;
    private static final BigDecimal PRICE = new BigDecimal("29.99");
    private static final int STOCK = 10;
    private static final int QUANTITY = 2;

    private static final String REQUEST_JSON = """
        {
            "id": 1,
            "name": "Keyboard",
            "description": "USB keyboard",
            "price": 29.99,
            "stock": 10,
            "categoryId": 2
        }
        """;

    @Mock
    private ProductService productService;

    private MockMvc mockMvc;
    private ProductDto productDto;

    @BeforeEach
    public void setUp() {
        // Exercise HTTP handling with a mocked productService.
        mockMvc = MockMvcBuilders.standaloneSetup(new ProductController(productService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        productDto = new ProductDto(PRODUCT_ID, PRODUCT_NAME, PRODUCT_DESCRIPTION, PRICE, STOCK, CATEGORY_ID);
    }

    @Test
    public void shouldReturnProductWhenIdExists() throws Exception {

        when(productService.getProductById(PRODUCT_ID)).thenReturn(Optional.of(productDto));

        mockMvc.perform(get(PRODUCT_URL, PRODUCT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(PRODUCT_ID.intValue()))
                .andExpect(jsonPath("$.name").value(PRODUCT_NAME));

        verify(productService).getProductById(PRODUCT_ID);
    }

    @Test
    public void shouldReturnNotFoundWhenIdDoesNotExist() throws Exception {

        when(productService.getProductById(PRODUCT_ID)).thenReturn(Optional.empty());
        mockMvc.perform(get(PRODUCT_URL, PRODUCT_ID)).andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnAllProducts() throws Exception {

        when(productService.getAllProducts()).thenReturn(List.of(productDto));
        mockMvc.perform(get(PRODUCTS_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value(PRODUCT_NAME));
    }

    @Test
    public void shouldReturnEmptyListWhenNoProductsExist() throws Exception {

        when(productService.getAllProducts()).thenReturn(List.of());
        mockMvc.perform(get(PRODUCTS_URL))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    public void shouldCreateProduct() throws Exception {

        when(productService.createProduct(productDto)).thenReturn(productDto);
        mockMvc.perform(post(PRODUCTS_URL).contentType(MediaType.APPLICATION_JSON).content(REQUEST_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(PRODUCT_NAME));

        verify(productService).createProduct(productDto);
    }

    @Test
    public void shouldRejectMalformedJson() throws Exception {

        mockMvc.perform(post(PRODUCTS_URL).contentType(MediaType.APPLICATION_JSON).content(INVALID_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productService);
    }

    @Test
    public void shouldUpdateProduct() throws Exception {

        when(productService.updateProduct(PRODUCT_ID, productDto)).thenReturn(productDto);

        mockMvc.perform(put(PRODUCT_URL, PRODUCT_ID).contentType(MediaType.APPLICATION_JSON).content(REQUEST_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(PRODUCT_ID.intValue()));

        verify(productService).updateProduct(PRODUCT_ID, productDto);
    }

    @Test
    public void shouldDeleteProduct() throws Exception {
        mockMvc.perform(delete(PRODUCT_URL, PRODUCT_ID))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(productService).deleteProduct(PRODUCT_ID);
    }

    @Test
    public void shouldReturnProductsForCategory() throws Exception {
        when(productService.getProductsByCategory(CATEGORY_ID)).thenReturn(List.of(productDto));
        mockMvc.perform(get(PRODUCTS_URL + "/by-category/{categoryId}", CATEGORY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoryId").value(CATEGORY_ID.intValue()));
    }

    @Test
    public void shouldDecreaseStock() throws Exception {
        mockMvc.perform(post(PRODUCT_URL + "/stock/decrease", PRODUCT_ID)
                        .param("quantity", Integer.toString(QUANTITY)))
                .andExpect(status().isNoContent());

        verify(productService).decreaseStock(PRODUCT_ID, QUANTITY);
    }

    @Test
    public void shouldRejectMissingQuantity() throws Exception {
        mockMvc.perform(post(PRODUCT_URL + "/stock/decrease", PRODUCT_ID))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productService);
    }
}
