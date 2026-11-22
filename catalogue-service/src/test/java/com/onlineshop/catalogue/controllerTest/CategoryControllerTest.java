package com.onlineshop.catalogue.controllerTest;

import com.onlineshop.catalogue.controller.CategoryController;
import com.onlineshop.catalogue.dto.CategoryDto;
import com.onlineshop.catalogue.service.CategoryService;
import com.onlineshop.catalogue.exception.GlobalExceptionHandler;
import com.onlineshop.catalogue.exception.CategoryAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.List;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class CategoryControllerTest {

    private static final Long CATEGORY_ID = 1L;
    private static final String CATEGORY_NAME = "Computing";
    private static final String CATEGORY_DESCRIPTION = "Equipment";
    private static final String CATEGORYS_URL = "/categories";
    private static final String CATEGORY_URL = CATEGORYS_URL + "/{id}";
    private static final String INVALID_JSON = "{";

    private static final String REQUEST_JSON = """
        {
            "id": 1,
            "name": "Computing",
            "description": "Equipment",
            "productList": []
        }
        """;

    @Mock
    private CategoryService categoryService;
    private MockMvc mockMvc;
    private CategoryDto categoryDto;

    @BeforeEach
    public void setUp() {
        // Exercise HTTP handling with a mocked categoryService.
        mockMvc = MockMvcBuilders.standaloneSetup(new CategoryController(categoryService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        categoryDto = new CategoryDto(CATEGORY_ID, CATEGORY_NAME, CATEGORY_DESCRIPTION, List.of());
    }

    @Test
    public void shouldReturnCategoryWhenIdExists() throws Exception {
        // Arrange
        when(categoryService.getCategoryById(CATEGORY_ID)).thenReturn(Optional.of(categoryDto));

        // Act and assert
        mockMvc.perform(get(CATEGORY_URL, CATEGORY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(CATEGORY_ID.intValue()))
                .andExpect(jsonPath("$.name").value(CATEGORY_NAME));

        verify(categoryService).getCategoryById(CATEGORY_ID);
    }

    @Test
    public void shouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
        // Arrange
        when(categoryService.getCategoryById(CATEGORY_ID)).thenReturn(Optional.empty());

        // Act and assert
        mockMvc.perform(get(CATEGORY_URL, CATEGORY_ID)).andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnAllCategorys() throws Exception {
        // Arrange
        when(categoryService.getAllCategories()).thenReturn(List.of(categoryDto));

        // Act and assert
        mockMvc.perform(get(CATEGORYS_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value(CATEGORY_NAME));
    }

    @Test
    public void shouldReturnEmptyListWhenNoCategorysExist() throws Exception {
        // Arrange
        when(categoryService.getAllCategories()).thenReturn(List.of());

        // Act and assert
        mockMvc.perform(get(CATEGORYS_URL))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    public void shouldCreateCategory() throws Exception {
        // Arrange
        when(categoryService.createCategory(categoryDto)).thenReturn(categoryDto);

        // Act and assert
        mockMvc.perform(post(CATEGORYS_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(CATEGORY_NAME));

        verify(categoryService).createCategory(categoryDto);
    }

    @Test
    public void shouldRejectMalformedJson() throws Exception {
        // Act and assert
        mockMvc.perform(post(CATEGORYS_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(INVALID_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(categoryService);
    }

    @Test
    public void shouldUpdateCategory() throws Exception {
        // Arrange
        when(categoryService.updateCategory(CATEGORY_ID, categoryDto)).thenReturn(categoryDto);

        // Act and assert
        mockMvc.perform(put(CATEGORY_URL, CATEGORY_ID).contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(CATEGORY_ID.intValue()));

        verify(categoryService).updateCategory(CATEGORY_ID, categoryDto);
    }

    @Test
    public void shouldDeleteCategory() throws Exception {
        // Act and assert
        mockMvc.perform(delete(CATEGORY_URL, CATEGORY_ID))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(categoryService).deleteCategory(CATEGORY_ID);
    }

    @Test
    public void shouldReturnConflictForDuplicateCategory() throws Exception {
        // Arrange
        when(categoryService.createCategory(categoryDto))
                .thenThrow(new CategoryAlreadyExistsException(CATEGORY_NAME));
        // Act and assert
        mockMvc.perform(post(CATEGORYS_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST_JSON))
                .andExpect(status().isConflict());
    }
}
